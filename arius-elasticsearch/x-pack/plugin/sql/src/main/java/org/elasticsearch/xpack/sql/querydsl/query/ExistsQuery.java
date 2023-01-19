/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.querydsl.query;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.xpack.sql.tree.Source;

import static org.elasticsearch.index.query.QueryBuilders.existsQuery;

public class ExistsQuery extends LeafQuery {

    private final String name;

    public ExistsQuery(Source source, String name) {
        super(source);
        this.name = name;
    }

    @Override
    public QueryBuilder asBuilder() {
        return existsQuery(name);
    }

    @Override
    protected String innerToString() {
        return name;
    }
}
