/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.action;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.test.AbstractSerializingTestCase;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.sql.proto.ColumnInfo;
import org.elasticsearch.xpack.sql.proto.Mode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.elasticsearch.common.xcontent.ToXContent.EMPTY_PARAMS;
import static org.elasticsearch.xpack.sql.action.AbstractSqlQueryRequest.CURSOR;
import static org.hamcrest.Matchers.hasSize;

public class SqlQueryResponseTests extends AbstractSerializingTestCase<SqlQueryResponse> {

    static String randomStringCursor() {
        return randomBoolean() ? "" : randomAlphaOfLength(10);
    }

    @Override
    protected SqlQueryResponse createTestInstance() {
        return createRandomInstance(randomStringCursor(), randomFrom(Mode.values()), randomBoolean());
    }

    @Override
    protected Writeable.Reader<SqlQueryResponse> instanceReader() {
        return SqlQueryResponse::new;
    }

    public static SqlQueryResponse createRandomInstance(String cursor, Mode mode, boolean columnar) {
        int columnCount = between(1, 10);

        List<ColumnInfo> columns = null;
        if (randomBoolean()) {
            columns = new ArrayList<>(columnCount);
            for (int i = 0; i < columnCount; i++) {
                columns.add(new ColumnInfo(randomAlphaOfLength(10), randomAlphaOfLength(10), randomAlphaOfLength(10), randomInt(25)));
            }
        }

        List<List<Object>> rows;
        if (randomBoolean()) {
            rows = Collections.emptyList();
        } else {
            int rowCount = between(1, 10);
            if (columnar && columns != null) {
                int temp = rowCount;
                rowCount = columnCount;
                columnCount = temp;
            }
            
            rows = new ArrayList<>(rowCount);
            for (int r = 0; r < rowCount; r++) {
                List<Object> row = new ArrayList<>(rowCount);
                for (int c = 0; c < columnCount; c++) {
                    Supplier<Object> value = randomFrom(Arrays.asList(
                            () -> randomAlphaOfLength(10),
                            ESTestCase::randomLong,
                            ESTestCase::randomDouble,
                            () -> null));
                    row.add(value.get());
                }
                rows.add(row);
            }
        }
        return new SqlQueryResponse(cursor, mode, false, columns, rows);
    }

    public void testToXContent() throws IOException {
        SqlQueryResponse testInstance = createTestInstance();

        XContentBuilder builder = testInstance.toXContent(XContentFactory.jsonBuilder(), EMPTY_PARAMS);
        Map<String, Object> rootMap = XContentHelper.convertToMap(BytesReference.bytes(builder), false, builder.contentType()).v2();

        logger.info(Strings.toString(builder));

        if (testInstance.columns() != null) {
            List<?> columns = (List<?>) rootMap.get("columns");
            assertThat(columns, hasSize(testInstance.columns().size()));
            for (int i = 0; i < columns.size(); i++) {
                Map<?, ?> columnMap = (Map<?, ?>) columns.get(i);
                ColumnInfo columnInfo = testInstance.columns().get(i);
                assertEquals(columnInfo.name(), columnMap.get("name"));
                assertEquals(columnInfo.esType(), columnMap.get("type"));
                assertEquals(columnInfo.displaySize(), columnMap.get("display_size"));
            }
        } else {
            assertNull(rootMap.get("columns"));
        }

        List<?> rows;
        if (testInstance.columnar()) {
            rows = ((List<?>) rootMap.get("values"));
        } else {
            rows = ((List<?>) rootMap.get("rows"));
        }
        assertNotNull(rows);
        assertThat(rows, hasSize(testInstance.rows().size()));
        for (int i = 0; i < rows.size(); i++) {
            List<?> row = (List<?>) rows.get(i);
            assertEquals(row, testInstance.rows().get(i));
        }

        if (testInstance.cursor().equals("") == false) {
            assertEquals(rootMap.get(CURSOR.getPreferredName()), testInstance.cursor());
        }
    }

    @Override
    protected SqlQueryResponse doParseInstance(XContentParser parser) {
        org.elasticsearch.xpack.sql.proto.SqlQueryResponse response =
            org.elasticsearch.xpack.sql.proto.SqlQueryResponse.fromXContent(parser);
        return new SqlQueryResponse(response.cursor(), Mode.JDBC, false, response.columns(), response.rows());
    }
}
