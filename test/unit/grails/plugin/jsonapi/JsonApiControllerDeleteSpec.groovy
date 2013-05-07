package grails.plugin.jsonapi

import grails.persistence.Entity
import grails.test.mixin.*
import com.google.gson.GsonBuilder
import grails.converters.*


@TestFor(JsonApiController)
@Mock([Book, Author])
class JsonApiControllerDeleteSpec extends spock.lang.Specification {

    def setup() {
        controller.gsonBuilder = new GsonBuilder()  // [ create:{ [ toJson:{ 'json' }] } ]
        controller.pluginConfiguration = [root:'/api', v1:[endpoints:[book:[:], author:[:]]]]
    }

    def "delete action should remove entity"() {
        given:
            def iain = new Author(name: 'Iain Banks').save(failOnError: true)
            def book1 = new Book(title: 'Excession', author: iain).save(failOnError: true)
        when:
            request.method = "DELETE"
            params.apiVersion = 'v1'
            params.entity = 'book'
            params.id = book1.id
            controller.delete()
        then:
            def json = JSON.parse(response.text)
            Book.count() == 0
            json.message == 'default.deleted.message'
    }

    def "delete action should give 404 if entity not found"() {
        given:
            def iain = new Author(name: 'Iain Banks').save(failOnError: true)
            def book1 = new Book(title: 'Excession', author: iain).save(failOnError: true)
        when:
            request.method = "DELETE"
            params.apiVersion = 'v1'
            params.entity = 'book'
            params.id = '123'
            controller.delete()
        then:
            def json = JSON.parse(response.text)
            Book.count() == 1
            json.message == 'default.not.found.message'
            response.status == 404
    }
}
