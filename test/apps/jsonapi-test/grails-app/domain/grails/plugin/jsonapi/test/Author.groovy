package grails.plugin.jsonapi.test

class Author {
    static hasMany = [books: Book]
    String name
    static constraints = {
    }
}