/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.action;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Objects;

/**
 * Response for the sql action for translating SQL queries into ES requests
 */
public class SqlTranslateResponse extends ActionResponse implements ToXContentObject {
    private SearchSourceBuilder source;
    private String index;

    public SqlTranslateResponse(StreamInput in) throws IOException {
        super(in);
        index = in.readOptionalString();
        source = new SearchSourceBuilder(in);
    }

    public SqlTranslateResponse(String index, SearchSourceBuilder source) {
        this.index = index;
        this.source = source;
    }

    public SearchSourceBuilder source() {
        return source;
    }

    public String index() {
        return index;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeOptionalString(index);
        source.writeTo(out);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        SqlTranslateResponse other = (SqlTranslateResponse) obj;
        return Objects.equals(source, other.source) &&
            Objects.equals(index, other.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, index);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        return source.toXContent(builder, params);
    }
}
