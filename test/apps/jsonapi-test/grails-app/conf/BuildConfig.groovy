grails.servlet.version = "2.5"
grails.project.work.dir = 'target'
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.dependency.resolution = {
    inherits "global"
    log "error"
    checksums true
    legacyResolve false

    repositories {
        inherits true
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        test 'org.spockframework:spock-grails-support:0.7-groovy-2.0'
        test 'org.codehaus.groovy.modules.http-builder:http-builder:0.6'
    }

    plugins {

        build ":tomcat:$grailsVersion"

        runtime ":hibernate:$grailsVersion"
        runtime ":jquery:1.8.3"
        runtime ":resources:1.1.6"
        runtime ":database-migration:1.3.2"

        compile ':cache:1.0.1'
        compile ':gson:1.1.4'

        test ':fixtures:1.2'
        test(':spock:0.7') {
            exclude 'spock-grails-support'
        }
    }
}

grails.plugin.location.'json-api' = '../../..'
