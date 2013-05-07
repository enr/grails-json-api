
grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits("global") { }
    log "warn"
    legacyResolve false

    repositories {
        grailsCentral()
        mavenCentral()
    }

    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
    }

    plugins {
        build(":release:2.2.1", ":rest-client-builder:1.0.3") {
            export = false
        }
        compile ':gson:1.1.4'
        test(":spock:0.7") {
            export = false
            exclude "spock-grails-support"
        }
    }
}
