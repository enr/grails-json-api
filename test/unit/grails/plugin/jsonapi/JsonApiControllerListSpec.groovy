package grails.plugin.jsonapi

import grails.persistence.Entity
import grails.test.mixin.*
import com.google.gson.GsonBuilder
import grails.converters.*


@TestFor(JsonApiController)
@Mock([Book, Author])
class JsonApiControllerListSpec extends spock.lang.Specification {

    def setup() {
        controller.gsonBuilder = new GsonBuilder()  // [ create:{ [ toJson:{ 'json' }] } ]
        controller.pluginConfiguration = [root:'/api', v1:[endpoints:[book:[:], author:[:]]]]
    }

    def "list action should show all entities"() {
        given:
            def iain = new Author(name: 'Iain Banks').save(failOnError: true)
            def bret = new Author(name: 'Bret Easton Ellis').save(failOnError: true)
            def book1 = new Book(title: 'Excession', author: iain).save(failOnError: true)
            def book2 = new Book(title: 'American psycho', author: bret).save(failOnError: true)
        when:
            request.method = "GET"
            params.apiVersion = 'v1'
            params.entity = 'book'
            controller.list()
        then:
            def json = JSON.parse(response.text)
            json.size() == 2
            def excession = json[0]
            excession.title == 'Excession'
            excession.author.name == 'Iain Banks'
            def ap = json[1]
            ap.author.name == 'Bret Easton Ellis'
            ap.title == 'American psycho'
    }

}
