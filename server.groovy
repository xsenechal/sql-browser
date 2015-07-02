import groovy.sql.Sql

import javax.ws.rs.*
import javax.ws.rs.core.*
import com.sun.jersey.api.core.*
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory
import org.glassfish.grizzly.http.server.HttpServer

/**
 * https://jersey.java.net/documentation/latest/user-guide.html
 * https://theholyjava.wordpress.com/2012/04/04/exposing-functionality-over-http-with-groovy-and-ultra-lightweight-http-servers/
 */
@GrabConfig(systemClassLoader = true)
@GrabResolver(name = 'gretty', root = 'http://groovypp.artifactoryonline.com/groovypp/libs-releases-local')
@Grapes([
        @Grab('com.sun.jersey:jersey-server:1.12'),
        @Grab('com.sun.jersey:jersey-core:1.12'),
        @Grab(group='com.sun.jersey', module='jersey-grizzly2', version='1.12'),
        @Grab(group='javax.ws.rs', module='jsr311-api', version='1.1.1'),
        @Grab('mysql:mysql-connector-java:5.1.35')])

@Path("/")
class Main {

    final def db = [url: 'jdbc:mysql://localhost:3306/user', user: 'root', psw: '' ]

    @Path("/code/{code}") @GET @Produces("text/plain")
    public String getUserByCode(@PathParam('code') String code) {
        return getUser(code)
    }

    def getUser(def code) {
        println "Connecting to the DB to check '$code'..."
        def sql = Sql.newInstance( db.url, db.user, db.psw)
        return sql.firstRow("select * from users where code = $code") ?: "No such code found"
    }

    public static startServer() {
        ResourceConfig resources = new ClassNamesResourceConfig(Main)
        def uri = UriBuilder.fromUri("http://localhost/").port(6789).build();
        HttpServer httpServer = GrizzlyServerFactory.createHttpServer(uri, resources);
        println("Jersey app started with WADL available at ${uri}application.wadl")
        System.in.read();
        httpServer.stop();
    }
}

Main.startServer()