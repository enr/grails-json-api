package grails.plugin.jsonapi.test

import groovyx.net.http.*
import spock.lang.*
import static grails.plugin.gson.http.HttpConstants.SC_UNPROCESSABLE_ENTITY
import static groovyx.net.http.ContentType.JSON
import static javax.servlet.http.HttpServletResponse.*
import static org.apache.http.entity.ContentType.APPLICATION_JSON

@Unroll
class InvalidRequestSpec extends RestEndpointSpec {

	void '#action returns a 404 if entity is not allowed for version'() {
		when:
		//HttpResponseDecorator response = 
		http."$method"(path: "v2/book/${id}")

		then:
		//response.status == SC_NOT_FOUND
		def e = thrown(HttpResponseException)
		e.response.status == SC_NOT_FOUND

		where:
		action   | method	| id
		'list'   | 'get'	| ''
		'show'   | 'get'	| '1'
	}

	void '#action returns a 405 if it is not allowed for the given endpoint'() {
		when:
		http."$method"(path: "v4/author/${id}")

		then:
		def e = thrown(HttpResponseException)
		e.response.status == SC_METHOD_NOT_ALLOWED

		where:
		action     | method	  | id
		'list'     | 'get'	  | ''
		'show'     | 'get'	  | '1'
		'save'     | 'post'	  | ''
		'update'   | 'put'	  | '1'
		'delete'   | 'delete' | '1'
	}
}
