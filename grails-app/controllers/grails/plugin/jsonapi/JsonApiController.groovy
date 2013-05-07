package grails.plugin.jsonapi

import grails.plugin.gson.converters.GSON
import org.springframework.dao.DataIntegrityViolationException
import static javax.servlet.http.HttpServletResponse.*
import static org.codehaus.groovy.grails.web.servlet.HttpHeaders.*
import static grails.plugin.gson.http.HttpConstants.*
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.springframework.beans.factory.InitializingBean

class JsonApiController implements InitializingBean {

    def gsonBuilder
    def messageSource
    def grailsApplication
    def pluginConfiguration

    def beforeInterceptor = [action: this.&verifyActionAllowed]

    private verifyActionAllowed() {
        if (!actionIsAllowed(actionName, params.apiVersion, params.entity)) {
            respondMethodNotAllowed()
            return false
        }
    }

    public void afterPropertiesSet() {
        pluginConfiguration = grailsApplication.config?.grails?.plugins?.jsonapi ?: [:]
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def entity = loadEntityClass(params.apiVersion, params.entity)
        if (!entity) {
            respondEntityNotFound params.entity
            return
        }
        def instances = invoke(entity, 'list', params)
        def count = instances.size()
        response.addIntHeader X_PAGINATION_TOTAL, count
        render actionResult(instances)
    }

    def show() {
        def domainInstance = retrieveRecord params.apiVersion, params.entity, params.id
        if (domainInstance) {
            respondFound domainInstance
        } else {
            respondNotFound params.entity, params.id
        }
    }
  
    def save() {
        if (!requestIsJson()) {
            respondNotAcceptable()
            return
        }
        def entity = loadEntityClass(params.apiVersion, params.entity)
        def domainInstance = InvokerHelper.invokeConstructorOf(entity.clazz, request.GSON) 
        if (domainInstance.save(flush: true)) {
            respondCreated params.apiVersion, params.entity, domainInstance
        } else {
            respondUnprocessableEntity domainInstance
        }
    }
  
    def update() {
        if (!requestIsJson()) {
            respondNotAcceptable()
            return
        }
        def domainInstance = retrieveRecord params.apiVersion, params.entity, params.id
        if (!domainInstance) {
            respondNotFound params.entity, params.id
            return
        }
        if (params.version != null) {
            if (domainInstance.version > params.long('version')) {
                respondConflict(params.entity, domainInstance)
                return
            }
        }
        domainInstance.properties = request.GSON
        if (domainInstance.save(flush: true)) {
            respondUpdated domainInstance
        } else {
            respondUnprocessableEntity domainInstance
        }
    }

    def delete() {
        def domainInstance = retrieveRecord params.apiVersion, params.entity, params.id
        if (!domainInstance) {
            respondNotFound params.entity, params.id
            return
        }
        try {
            domainInstance.delete(flush: true)
            respondDeleted params.entity, params.id
        } catch (DataIntegrityViolationException e) {
            respondNotDeleted params.entity, params.id
        }
    }

    private boolean requestIsJson() {
        GSON.isJson(request)
    }

    private actionResult(subject, jsonp=false) {
        def gson = gsonBuilder.create()
        def responseText = gson.toJson(subject)
        if (params.callback) {
            responseText = "${params.callback}(${responseText})"
        } 
        return [contentType: 'application/json', text: responseText]
    }

    /*
     * Returns "false" only if the entity configuration has the "allowed" field specified but this doesn't contain the requested action.
     * If entity is not configured or it hasn't the "allowed" field, it returns "true".
     */
    private boolean actionIsAllowed(String actionName, String apiVersion, String entityName) {
        def entityConfig = getEntityConfiguration(apiVersion, entityName)
        if (entityConfig == null) {
            return true
        }
        if (entityConfig.allowed) {
            return entityConfig.allowed.contains(actionName)
        }
        return true
    }

    private loadEntityClass(String apiVersion, String simpleClassName) {
        def entityConfig = getEntityConfiguration(apiVersion, simpleClassName)
        if (entityConfig == null) {
            return null
        }
        def entity = null
        if (entityConfig.domain) {
            def domainClassName = entityConfig.domain
            entity = grailsApplication.getDomainClass(domainClassName)
        } else {
            entity = grailsApplication.getArtefactByLogicalPropertyName(DomainClassArtefactHandler.TYPE, simpleClassName)
        }
        return entity
    }

    private retrieveRecord (apiVersion, entityName, entityId) {
        def entity = loadEntityClass(apiVersion, entityName)
        def obj = null
        if (entity) {
            obj = invoke(entity, 'get', entityId)
        }
        return obj
    }

    private invoke(domainClass, methodname, params=null) {
        InvokerHelper.invokeStaticMethod(domainClass.clazz, methodname, params)
    }

    private void respondFound(domainInstance) {
        response.status = SC_OK
        render actionResult(domainInstance)
    }

    private void respondCreated(apiVersion, entity, instance) {
        response.status = SC_CREATED
        def pluginRoot = getPluginRoot()
        response.addHeader LOCATION, "${pluginRoot}/${apiVersion}/${entity}/${instance.id}"
        render actionResult(instance)
    }

    private void respondUpdated(domainInstance) {
        response.status = SC_OK
        render actionResult(domainInstance)
    }

    private void respondUnprocessableEntity(domainInstance) {
        def responseBody = [:]
        responseBody.errors = domainInstance.errors.allErrors.collect {
          message(error: it)
        }
        response.status = SC_UNPROCESSABLE_ENTITY
        render actionResult(responseBody)
    }

    private void respondMethodNotAllowed() {
        def responseBody = [:]
        responseBody.message = message(code: 'jsonapi.error.methodNotAllowed', default:"Method not allowed")
        response.status = SC_METHOD_NOT_ALLOWED
        render actionResult(responseBody)
    }

    private void respondEntityNotFound(String entity) {
        def responseBody = [:]
        responseBody.message = message(code: 'jsonapi.error.entityNotFound', default:"Entity ${entity} not found")
        response.status = SC_NOT_FOUND
        render actionResult(responseBody)
    }

    private void respondNotFound(entity, id) {
        def responseBody = [:]
        responseBody.message = message(code: 'default.not.found.message', args: [message(code: "${entity}.label", default: entity.capitalize()), id])
        response.status = SC_NOT_FOUND
        render actionResult(responseBody)
    }

    private void respondConflict(entity, domainInstance) {
        domainInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
          [message(code: "${entity}.label", default: entity.capitalize())] as Object[],
          "Another user has updated this ${entity} while you were editing")
        def responseBody = [:]
        responseBody.errors = domainInstance.errors.allErrors.collect {
            message(error: it)
        }
        response.status = SC_CONFLICT
        render actionResult(responseBody)
    }

    private void respondDeleted(entity, id) {
        def responseBody = [:]
        responseBody.message = message(code: 'default.deleted.message', args: [message(code: "${entity}.label", default: entity.capitalize()), id])
        response.status = SC_OK
        render actionResult(responseBody)
    }

    private void respondNotDeleted(entity, id) {
        def responseBody = [:]
        responseBody.message = message(code: 'default.not.deleted.message', args: [message(code: "${entity}.label", default: entity.capitalize()), id])
        response.status = SC_INTERNAL_SERVER_ERROR
        render actionResult(responseBody)
    }

    private void respondNotAcceptable() {
        response.status = SC_NOT_ACCEPTABLE
        response.contentLength = 0
        response.outputStream.flush()
        response.outputStream.close()
    }

    private getEntityConfiguration(apiVersion, entityName) {
        def versionConfig = pluginConfiguration[apiVersion]
        def endpointsConfig = versionConfig?.endpoints
        if (!endpointsConfig || !endpointsConfig.containsKey(entityName)) {
            return null
        }
        return endpointsConfig[entityName]
    }

    private getPluginConfigurationForApiVersion(apiVersion) {
        return getPluginConfiguration()[apiVersion] ? getPluginConfiguration()[apiVersion] : [:]
    }

    private getPluginConfiguration() {
        if (!pluginConfiguration) {
            pluginConfiguration = grailsApplication.config?.grails?.plugins?.jsonapi
        }
        return pluginConfiguration
    }

    private getPluginRoot() {
        return getPluginConfiguration().root ? getPluginConfiguration().root : '/api'
    }

}
