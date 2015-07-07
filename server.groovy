import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
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

    @Path("/request") @POST @Produces("application/json")
    public String exeRequest(String clientJson) {
        def client = new JsonSlurper().parseText(clientJson)
        groovy.sql.Sql sql = Sql.newInstance(client.con.url, client.con.user, client.con.psw)
        def result = sql.rows(client.request)
        return new JsonBuilder(result).toString()
    }
    @Path("/tables") @POST @Produces("application/json")
    public String getTables(String clientJson) {
        def client = new JsonSlurper().parseText(clientJson)
        groovy.sql.Sql sql = Sql.newInstance(client.con.url, client.con.user, client.con.psw)
        def resultSet = sql.connection.metaData.getTables(null, null, null, null)
        def tables = getMap(resultSet)
        return new JsonBuilder(tables).toString()
    }

    @Path("/metadata/{table}") @POST @Produces("application/json")
    public String getColumns(@PathParam("table") String table,  String clientJson) {
        def client = new JsonSlurper().parseText(clientJson)
        groovy.sql.Sql sql = Sql.newInstance(client.con.url, client.con.user, client.con.psw)

        def columns      = getMap(sql.connection.metaData.getColumns(null, null, table, null))
        def exportedKeys = getMap(sql.connection.metaData.getExportedKeys(null, null, table))
        def importedKeys = getMap(sql.connection.metaData.getImportedKeys(null, null, table))
        return new JsonBuilder([columns:columns, exportedKeys:exportedKeys, importedKeys: importedKeys]).toString()
    }

    @Path("/static/{path:.+}") @GET @Produces("text/plain")
    public InputStream getStatic(@PathParam('path') String path) {
        return new FileInputStream('static/' + path)
    }

    @Path("/") @GET @Produces("text/html")
    public InputStream getHome() {
        return getStatic('index.html')
    }

    public static startServer() {
        ResourceConfig resources = new ClassNamesResourceConfig(Main)
        def uri = UriBuilder.fromUri("http://localhost/").port(6789).build();
        HttpServer httpServer = GrizzlyServerFactory.createHttpServer(uri, resources);
        println("Jersey app started at ${uri}")
        System.in.read();
        httpServer.stop();
    }

    private List getMap(resultSet) {
        def list = []
        while(resultSet.next()) {
            list << resultSet.toRowResult()
        }
        return list
    }
}

Main.startServer()