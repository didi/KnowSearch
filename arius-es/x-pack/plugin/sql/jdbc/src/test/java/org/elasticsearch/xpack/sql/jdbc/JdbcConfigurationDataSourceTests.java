/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql.jdbc;

import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.http.MockResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class JdbcConfigurationDataSourceTests extends WebServerTestCase {
    
    public void testDataSourceConfigurationWithSSLInURL() throws SQLException, URISyntaxException, IOException {
        webServer().enqueue(new MockResponse().setResponseCode(200).addHeader("Content-Type", "application/json").setBody(
                XContentHelper.toXContent(createCurrentVersionMainResponse(), XContentType.JSON, false).utf8ToString()));
        
        Map<String, String> urlPropMap = JdbcConfigurationTests.sslProperties();
        Properties allProps = new Properties();
        allProps.putAll(urlPropMap);
        String sslUrlProps = urlPropMap.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
        
        EsDataSource dataSource = new EsDataSource();
        String address = "jdbc:es://" + webServerAddress() + "/?" + sslUrlProps;
        dataSource.setUrl(address);
        JdbcConnection connection = null;
        
        try {
            connection = (JdbcConnection) dataSource.getConnection();
        } catch (SQLException sqle) {
            fail("Connection creation should have been successful. Error: " + sqle);
        }
        
        assertEquals(address, connection.getURL());
        JdbcConfigurationTests.assertSslConfig(allProps, connection.cfg.sslConfig());
    }
}
