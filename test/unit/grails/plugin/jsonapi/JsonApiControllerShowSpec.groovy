package grails.plugin.jsonapi

import grails.persistence.Entity
import grails.test.mixin.*
import com.google.gson.GsonBuilder
import grails.converters.*


@TestFor(JsonApiController)
@Mock([Book, Author])
class JsonApiControllerShowSpec extends spock.lang.Specification {

    def setup() {
        controller.gsonBuilder = new GsonBuilder()  // [ create:{ [ toJson:{ 'json' }] } ]
        controller.pluginConfiguration = [root:'/api', v1:[endpoints:[book:[:], author:[:]]]]
    }

    def "show action should show the requested entity"() {
        given:
            def iain = new Author(name: 'Iain Banks').save(failOnError: true)
            def bret = new Author(name: 'Bret Easton Ellis').save(failOnError: true)
            def book1 = new Book(title: 'Excession', author: iain).save(failOnError: true)
            def book2 = new Book(title: 'American psycho', author: bret).save(failOnError: true)
        when:
            request.method = "GET"
            params.apiVersion = 'v1'
            params.entity = 'book'
            params.id = book1.id
            controller.show()
        then:
            def json = JSON.parse(response.text)
            json.id == book1.id
            json.title == 'Excession'
            json.author.name == 'Iain Banks'
    }

    def "show action should support jsonp"() {
        given:
            def iain = new Author(name: 'Iain Banks').save(failOnError: true)
            def bret = new Author(name: 'Bret Easton Ellis').save(failOnError: true)
            def book1 = new Book(title: 'Excession', author: iain).save(failOnError: true)
            def book2 = new Book(title: 'American psycho', author: bret).save(failOnError: true)
        when:
            request.method = "GET"
            params.apiVersion = 'v1'
            params.entity = 'book'
            params.id = book1.id
            params.callback = 'testcallback'
            controller.show()
        then:
            response.text == 'testcallback({"title":"Excession","author":{"name":"Iain Banks","id":1},"id":1})'
            def actualJson = (response.text - 'testcallback(')[0..-2] 
            def json = JSON.parse(actualJson)
            json.id == book1.id
            json.title == 'Excession'
            json.author.name == 'Iain Banks'
    }

}
