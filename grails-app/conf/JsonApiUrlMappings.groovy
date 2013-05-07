
import grails.util.Holders

class JsonApiUrlMappings {

	static mappings = {

        def config = Holders.getGrailsApplication().config?.grails?.plugins?.jsonapi
        def pluginRoot = config.root ? config.root : '/api'

        "${pluginRoot}/${apiVersion}/${entity}" (controller: 'jsonApi') {
            action = [ GET: 'list', POST: 'save' ]
        }

        "${pluginRoot}/${apiVersion}/${entity}/${id}" (controller: 'jsonApi') {
            action = [ GET: 'show', PUT: 'update', DELETE: 'delete' ]
        }
	}
}
