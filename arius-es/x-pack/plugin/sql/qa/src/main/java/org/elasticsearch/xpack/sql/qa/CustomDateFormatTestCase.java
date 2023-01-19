/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql.qa;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.time.DateUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.xpack.sql.qa.jdbc.JdbcIntegrationTestCase;
import org.elasticsearch.xpack.sql.qa.rest.BaseRestSqlTestCase;
import org.elasticsearch.xpack.sql.qa.rest.RestSqlTestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/*
 * Test class that covers the NOW()/CURRENT_DATE()/CURRENT_TIME() family of functions in a comparison condition
 * with different timezones and custom date formats for the date fields in ES.
 */
public abstract class CustomDateFormatTestCase extends BaseRestSqlTestCase {
    
    private static String[] customFormats = new String[] {"HH:mm yyyy-MM-dd", "HH:mm:ss yyyy-dd-MM", "HH:mm:ss VV", "HH:mm:ss VV z",
            "yyyy-MM-dd'T'HH:mm:ss'T'VV'T'z"};
    private static String[] nowFunctions = new String[] {"NOW()", "CURRENT_DATE()", "CURRENT_TIME()", "CURRENT_TIMESTAMP()"};
    private static String[] operators = new String[] {" < ", " > ", " <= ", " >= ", " = ", " != "};
    
    public void testCustomDateFormatsWithNowFunctions() throws IOException {
        createIndex();
        String[] docs = new String[customFormats.length];
        String zID = JdbcIntegrationTestCase.randomKnownTimeZone();
        StringBuilder datesConditions = new StringBuilder();
        
        for (int i = 0; i < customFormats.length; i++) {
            String field = "date_" + i;
            docs[i] = "{\"" + field + "\":\"" +
                    DateTimeFormatter.ofPattern(customFormats[i], Locale.ROOT).format(DateUtils.nowWithMillisResolution()) + "\"}";
            datesConditions.append(i > 0 ? " OR " : "").append(field + randomFrom(operators) + randomFrom(nowFunctions));
        }
        
        index(docs);
        
        Request request = new Request("POST", RestSqlTestCase.SQL_QUERY_REST_ENDPOINT);
        request.setEntity(new StringEntity("{\"query\":\"SELECT COUNT(*) AS c FROM test WHERE "
                + datesConditions.toString() + "\""
                + mode("plain")
                + ",\"time_zone\":\"" + zID + "\"" + "}", ContentType.APPLICATION_JSON));
        
        Response response = client().performRequest(request);
        String expectedJsonSnippet = "{\"columns\":[{\"name\":\"c\",\"type\":\"long\"}],\"rows\":[[";
        try (InputStream content = response.getEntity().getContent()) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = content.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String actualJson = result.toString("UTF-8");
            // we just need to get a response that's not a date parsing error
            assertTrue(actualJson.startsWith(expectedJsonSnippet));
        }
    }
    
    private void createIndex() throws IOException {
        Request request = new Request("PUT", "/test");      
        XContentBuilder index = JsonXContent.contentBuilder().prettyPrint().startObject();

        index.startObject("mappings"); {
            index.startObject("properties"); {
                for (int i = 0; i < customFormats.length; i++) {
                    String fieldName = "date_" + i;
                    index.startObject(fieldName); {
                        index.field("type", "date");
                        index.field("format", customFormats[i]);
                    }
                    index.endObject();
                }
            index.endObject();
            }
        }
        index.endObject();
        index.endObject();

        request.setJsonEntity(Strings.toString(index));
        client().performRequest(request);
    }
}
