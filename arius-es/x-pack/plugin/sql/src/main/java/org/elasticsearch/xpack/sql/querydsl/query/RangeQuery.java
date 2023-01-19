/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.querydsl.query;

import java.util.Objects;

import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.xpack.sql.tree.Source;

import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

public class RangeQuery extends LeafQuery {

    private final String field;
    private final Object lower, upper;
    private final boolean includeLower, includeUpper;
    private final String format;

    public RangeQuery(Source source, String field, Object lower, boolean includeLower, Object upper, boolean includeUpper) {
        this(source, field, lower, includeLower, upper, includeUpper, null);
    }

    public RangeQuery(Source source, String field, Object lower, boolean includeLower, Object upper,
            boolean includeUpper, String format) {
        super(source);
        this.field = field;
        this.lower = lower;
        this.upper = upper;
        this.includeLower = includeLower;
        this.includeUpper = includeUpper;
        this.format = format;
    }

    public String field() {
        return field;
    }

    public Object lower() {
        return lower;
    }

    public Object upper() {
        return upper;
    }

    public boolean includeLower() {
        return includeLower;
    }

    public boolean includeUpper() {
        return includeUpper;
    }

    public String format() {
        return format;
    }

    @Override
    public QueryBuilder asBuilder() {
        RangeQueryBuilder queryBuilder = rangeQuery(field).from(lower, includeLower).to(upper, includeUpper);
        if (Strings.hasText(format)) {
            queryBuilder.format(format);
        }

        return queryBuilder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, lower, upper, includeLower, includeUpper, format);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        RangeQuery other = (RangeQuery) obj;
        return Objects.equals(field, other.field) &&
                Objects.equals(includeLower, other.includeLower) &&
                Objects.equals(includeUpper, other.includeUpper) &&
                Objects.equals(lower, other.lower) &&
                Objects.equals(upper, other.upper) &&
                Objects.equals(format, other.format);
    }

    @Override
    protected String innerToString() {
        return field + ":"
            + (includeLower ? "[" : "(") + lower + ", "
            + upper + (includeUpper ? "]" : ")");
    }
}
