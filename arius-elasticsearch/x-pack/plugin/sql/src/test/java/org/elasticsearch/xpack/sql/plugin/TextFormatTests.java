/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.plugin;


import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.rest.FakeRestRequest;
import org.elasticsearch.xpack.sql.action.SqlQueryResponse;
import org.elasticsearch.xpack.sql.proto.ColumnInfo;
import org.elasticsearch.xpack.sql.proto.Mode;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.elasticsearch.xpack.sql.plugin.TextFormat.CSV;
import static org.elasticsearch.xpack.sql.plugin.TextFormat.TSV;
import static org.hamcrest.CoreMatchers.is;

public class TextFormatTests extends ESTestCase {

    public void testPlainTextDetection() {
        TextFormat text = TextFormat.fromMediaTypeOrFormat("text/plain");
        assertThat(text, is(TextFormat.PLAIN_TEXT));
    }

    public void testCsvDetection() {
        TextFormat text = TextFormat.fromMediaTypeOrFormat("text/csv");
        assertThat(text, is(CSV));
    }

    public void testTsvDetection() {
        TextFormat text = TextFormat.fromMediaTypeOrFormat("text/tab-separated-values");
        assertThat(text, is(TSV));
    }

    public void testInvalidFormat() {
        Exception e = expectThrows(IllegalArgumentException.class, () -> TextFormat.fromMediaTypeOrFormat("text/garbage"));
        assertEquals("invalid format [text/garbage]", e.getMessage());
    }

    public void testCsvContentType() {
        assertEquals("text/csv; charset=utf-8; header=present", CSV.contentType(req()));
    }

    public void testCsvContentTypeWithoutHeader() {
        assertEquals("text/csv; charset=utf-8; header=absent", CSV.contentType(reqNoHeader()));
    }

    public void testTsvContentType() {
        assertEquals("text/tab-separated-values; charset=utf-8", TSV.contentType(req()));
    }

    public void testCsvEscaping() {
        assertEquals("string", CSV.maybeEscape("string"));
        assertEquals("", CSV.maybeEscape(""));
        assertEquals("\"\"\"\"", CSV.maybeEscape("\""));
        assertEquals("\"\"\",\"\"\"", CSV.maybeEscape("\",\""));
        assertEquals("\"\"\"quo\"\"ted\"\"\"", CSV.maybeEscape("\"quo\"ted\""));
    }

    public void testTsvEscaping() {
        assertEquals("string", TSV.maybeEscape("string"));
        assertEquals("", TSV.maybeEscape(""));
        assertEquals("\"", TSV.maybeEscape("\""));
        assertEquals("\\t", TSV.maybeEscape("\t"));
        assertEquals("\\n\"\\t", TSV.maybeEscape("\n\"\t"));
    }

    public void testCsvFormatWithEmptyData() {
        String text = CSV.format(req(), emptyData());
        assertEquals("name\r\n", text);
    }

    public void testTsvFormatWithEmptyData() {
        String text = TSV.format(req(), emptyData());
        assertEquals("name\n", text);
    }

    public void testCsvFormatWithRegularData() {
        String text = CSV.format(req(), regularData());
        assertEquals("string,number\r\n" +
                "Along The River Bank,708\r\n" +
                "Mind Train,280\r\n",
                text);
    }

    public void testTsvFormatWithRegularData() {
        String text = TSV.format(req(), regularData());
        assertEquals("string\tnumber\n" +
                "Along The River Bank\t708\n" +
                "Mind Train\t280\n",
                text);
    }

    public void testCsvFormatWithEscapedData() {
        String text = CSV.format(req(), escapedData());
        assertEquals("first,\"\"\"special\"\"\"\r\n" +
                "normal,\"\"\"quo\"\"ted\"\",\n\"\r\n" +
                "commas,\"a,b,c,\n,d,e,\t\n\"\r\n"
                , text);
    }

    public void testTsvFormatWithEscapedData() {
        String text = TSV.format(req(), escapedData());
        assertEquals("first\t\"special\"\n" +
                "normal\t\"quo\"ted\",\\n\n" +
                "commas\ta,b,c,\\n,d,e,\\t\\n\n"
                , text);
    }

    private static SqlQueryResponse emptyData() {
        return new SqlQueryResponse(null, Mode.JDBC, false, singletonList(new ColumnInfo("index", "name", "keyword")), emptyList());
    }

    private static SqlQueryResponse regularData() {
        // headers
        List<ColumnInfo> headers = new ArrayList<>();
        headers.add(new ColumnInfo("index", "string", "keyword"));
        headers.add(new ColumnInfo("index", "number", "integer"));

        // values
        List<List<Object>> values = new ArrayList<>();
        values.add(asList("Along The River Bank", 11 * 60 + 48));
        values.add(asList("Mind Train", 4 * 60 + 40));

        return new SqlQueryResponse(null, Mode.JDBC, false, headers, values);
    }

    private static SqlQueryResponse escapedData() {
        // headers
        List<ColumnInfo> headers = new ArrayList<>();
        headers.add(new ColumnInfo("index", "first", "keyword"));
        headers.add(new ColumnInfo("index", "\"special\"", "keyword"));

        // values
        List<List<Object>> values = new ArrayList<>();
        values.add(asList("normal", "\"quo\"ted\",\n"));
        values.add(asList("commas", "a,b,c,\n,d,e,\t\n"));

        return new SqlQueryResponse(null, Mode.JDBC, false, headers, values);
    }

    private static RestRequest req() {
        return new FakeRestRequest();
    }

    private static RestRequest reqNoHeader() {
        return new FakeRestRequest.Builder(NamedXContentRegistry.EMPTY).withParams(singletonMap("header", "absent")).build();
    }
}
