/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.querydsl.agg;

import org.elasticsearch.search.aggregations.AggregationBuilder;

import java.util.List;

import static org.elasticsearch.search.aggregations.MatrixStatsAggregationBuilders.matrixStats;

public class MatrixStatsAgg extends LeafAgg {

    private final List<String> fields;

    public MatrixStatsAgg(String id, List<String> fields) {
        super(id, "<multi-field>");
        this.fields = fields;
    }

    @Override
    AggregationBuilder toBuilder() {
        return matrixStats(id()).fields(fields);
    }
}
