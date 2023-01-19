/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.proto;

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Objects;

/**
 * Request to clean all SQL resources associated with the cursor for JDBC/CLI client
 */
public class SqlClearCursorRequest extends AbstractSqlRequest {

    private final String cursor;

    public SqlClearCursorRequest(String cursor, RequestInfo requestInfo) {
        super(requestInfo);
        this.cursor = cursor;
    }

    public String getCursor() {
        return cursor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SqlClearCursorRequest that = (SqlClearCursorRequest) o;
        return Objects.equals(cursor, that.cursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cursor);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field("cursor", cursor);
        builder.field("mode", mode().toString());
        if (clientId() != null) {
            builder.field("client_id", clientId());
        }
        return builder;
    }
}
