/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.qa.jdbc;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.common.CheckedConsumer;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.test.rest.ESRestTestCase;
import org.elasticsearch.xpack.sql.jdbc.EsDataSource;
import org.junit.After;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.elasticsearch.xpack.sql.qa.jdbc.JdbcTestUtils.JDBC_TIMEZONE;
import static org.elasticsearch.xpack.sql.qa.rest.RestSqlTestCase.assertNoSearchContexts;

public abstract class JdbcIntegrationTestCase extends ESRestTestCase {

    @After
    public void checkSearchContent() throws Exception {
        // Some context might linger due to fire and forget nature of scroll cleanup
        assertNoSearchContexts();
    }

    /**
     * Read an address for Elasticsearch suitable for the JDBC driver from the system properties.
     */
    public static String elasticsearchAddress() {
        String cluster = System.getProperty("tests.rest.cluster");
        // JDBC only supports a single node at a time so we just give it one.
        return cluster.split(",")[0];
        /* This doesn't include "jdbc:es://" because we want the example in
         * esJdbc to be obvious and because we want to use getProtocol to add
         * https if we are running against https. */
    }

    public Connection esJdbc() throws SQLException {
        return esJdbc(connectionProperties());
    }

    public Connection esJdbc(Properties props) throws SQLException {
        return createConnection(props);
    }

    protected Connection createConnection(Properties connectionProperties) throws SQLException {
        String elasticsearchAddress = getProtocol() + "://" + elasticsearchAddress();
        String address = "jdbc:es://" + elasticsearchAddress;
        Connection connection = null;
        if (randomBoolean()) {
            connection = DriverManager.getConnection(address, connectionProperties);
        } else {
            EsDataSource dataSource = new EsDataSource();
            dataSource.setUrl(address);
            dataSource.setProperties(connectionProperties);
            connection = dataSource.getConnection();
        }

        assertNotNull("The timezone should be specified", connectionProperties.getProperty("timezone"));
        return connection;
    }

    //
    // methods below are used inside the documentation only
    //
    protected Connection useDriverManager() throws SQLException {
        String elasticsearchAddress = getProtocol() + "://" + elasticsearchAddress();
        // tag::connect-dm
        String address = "jdbc:es://" + elasticsearchAddress;     // <1>
        Properties connectionProperties = connectionProperties(); // <2>
        Connection connection =
            DriverManager.getConnection(address, connectionProperties);
        // end::connect-dm
        assertNotNull("The timezone should be specified", connectionProperties.getProperty("timezone"));
        return connection;
    }

    protected Connection useDataSource() throws SQLException {
        String elasticsearchAddress = getProtocol() + "://" + elasticsearchAddress();
        // tag::connect-ds
        EsDataSource dataSource = new EsDataSource();
        String address = "jdbc:es://" + elasticsearchAddress;     // <1>
        dataSource.setUrl(address);
        Properties connectionProperties = connectionProperties(); // <2>
        dataSource.setProperties(connectionProperties);
        Connection connection = dataSource.getConnection();
        // end::connect-ds
        assertNotNull("The timezone should be specified", connectionProperties.getProperty("timezone"));
        return connection;
    }

    public static void index(String index, CheckedConsumer<XContentBuilder, IOException> body) throws IOException {
        index(index, "1", body);
    }

    public static void index(String index, String documentId, CheckedConsumer<XContentBuilder, IOException> body) throws IOException {
        Request request = new Request("PUT", "/" + index + "/_doc/" + documentId);
        request.addParameter("refresh", "true");
        XContentBuilder builder = JsonXContent.contentBuilder().startObject();
        body.accept(builder);
        builder.endObject();
        request.setJsonEntity(Strings.toString(builder));
        client().performRequest(request);
    }

    public static void delete(String index, String documentId) throws IOException {
        Request request = new Request("DELETE", "/" + index + "/_doc/" + documentId);
        request.addParameter("refresh", "true");
        client().performRequest(request);
    }

    protected String clusterName() {
        try {
            String response = EntityUtils.toString(client().performRequest(new Request("GET", "/")).getEntity());
            return XContentHelper.convertToMap(JsonXContent.jsonXContent, response, false).get("cluster_name").toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The properties used to build the connection.
     */
    protected Properties connectionProperties() {
        Properties connectionProperties = new Properties();
        connectionProperties.put(JDBC_TIMEZONE, randomKnownTimeZone());
        // in the tests, don't be lenient towards multi values
        connectionProperties.put("field.multi.value.leniency", "false");
        return connectionProperties;
    }

    public static String randomKnownTimeZone() {
        // We use system default timezone for the connection that is selected randomly by TestRuleSetupAndRestoreClassEnv
        // from all available JDK timezones. While Joda and JDK are generally in sync, some timezones might not be known
        // to the current version of Joda and in this case the test might fail. To avoid that, we specify a timezone
        // known for both Joda and JDK
        // Set<String> timeZones = new HashSet<>(JODA_TIMEZONE_IDS);
        // timeZones.retainAll(JAVA_TIMEZONE_IDS);
        List<String> ids = new ArrayList<>();
        ids.add("America/Chicago");
        ids.add("Asia/Tokyo");
        ids.add("Asia/Shanghai");
        Collections.sort(ids);
        return randomFrom(ids);
    }
}
