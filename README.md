Json Api Grails plugin
======================

Easy creation of Rest Json-based api for Gorm classes.

Features and differences form Json Rest API plugin:

- serialization and deserialization using Gson (via Gson Grails plugin)

- api versioning

- configuration outside domain classes

- configurable endpoints and allowed actions


Configuration
-------------

A sample Config.groovy snippet:

```groovy
grails {
  plugins {
    jsonapi {
      // the base endpoint (default: '/api')
      root = '/jsonapiroot'
      // version v1:
      v1 {
         // supported endpoints for v1
         endpoints {
            // /jsonapiroot/v1/person
            person {
                // no configuration is needed, apart for the endpoint declaration
                // in this case the class is resolved looking for the domain Person
            }
            book {
              // you can specify class
              domain = 'mypkg.Book'
            }
            // you can use the endpoints you want
            people { 
              domain = 'mypkg.Person'
              allowed = ['show', 'list']
            }
         }
      }
    }
  }
}
```

i18n
----

Plugin configurable messages:

- `jsonapi.error.methodNotAllowed` default: "Method not allowed"

- `jsonapi.error.entityNotFound` default: "Entity ${entity} not found"


Test
----

Directory `test/apps` contains two applications using the plugin:

- gson-test is a port of the application used for functional tests from Gson plugin ( https://github.com/robfletcher/grails-gson/tree/master/test/apps/gson-test)

- jsonapi-test is the actual app used to test configurations etc.

To run the full test suite:

    grails test-app -unit
    cd test/apps/gson-test && grails refresh-dependencies && test-app -functional
    cd test/apps/jsonapi-test && grails refresh-dependencies && test-app -functional

