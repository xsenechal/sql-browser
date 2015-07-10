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
@Grapes([
        @Grab('com.sun.jersey:jersey-server:1.12'),
        @Grab('com.sun.jersey:jersey-core:1.12'),
        @Grab('com.sun.jersey:jersey-grizzly2:1.12'),
        @Grab('javax.ws.rs:jsr311-api:1.1.1'),
])

@Path("/")
class Main {

    static final JDBC_JAR_FOLDER = "./lib"
    static final APP_URI = "http://localhost/"
    static final APP_PORT = 6789


    @Path("/request") @POST @Produces("application/json")
    public String exeRequest(String clientJson) {
        def client = new JsonSlurper().parseText(clientJson)
        groovy.sql.Sql sql = Sql.newInstance(client.con.url, client.con.user, client.con.password, client.con.driverClass)
        def result = sql.rows(client.request)
        return new JsonBuilder(result).toString()
    }
    @Path("/tables") @POST @Produces("application/json")
    public String getTables(String clientJson) {
        def client = new JsonSlurper().parseText(clientJson)
        groovy.sql.Sql sql = Sql.newInstance(client.con.url, client.con.user, client.con.password, client.con.driverClass)
        String[] tableType =  ["TABLE"].toArray(String);
        def resultSet = sql.connection.metaData.getTables(null, client.con.schema, client.con.tableFilter, tableType )
        def tables = getMap(resultSet)
        return new JsonBuilder(tables).toString()
    }

    @Path("/metadata/{table}") @POST @Produces("application/json")
    public String getColumns(@PathParam("table") String table,  String clientJson) {
        def client = new JsonSlurper().parseText(clientJson)
        groovy.sql.Sql sql = Sql.newInstance(client.con.url, client.con.user, client.con.password, client.con.driverClass)

        def columns      = getMap(sql.connection.metaData.getColumns(null, client.con.schema, table, null))
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

    public startServer() {
        new File(JDBC_JAR_FOLDER).eachFile { println "adding ${it.name} to classpath"; this.class.classLoader.rootLoader.addURL(it.toURL()) }

        ResourceConfig resources = new ClassNamesResourceConfig(Main)
        def uri = UriBuilder.fromUri(APP_URI).port(APP_PORT).build();
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

new Main().startServer()