package grails.plugin.jsonapi

import grails.persistence.Entity

@Entity
class Book {
    String title
    Author author
}
