/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.execution.search;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.xpack.sql.execution.search.extractor.BucketExtractor;
import org.elasticsearch.xpack.sql.session.RowSet;
import org.elasticsearch.xpack.sql.session.SchemaRowSet;
import org.elasticsearch.xpack.sql.type.Schema;

import java.util.BitSet;
import java.util.List;

/**
 * Extension of the {@link RowSet} over a composite agg, extending it to provide its schema.
 * Used for the initial response.
 */
class SchemaCompositeAggRowSet extends CompositeAggRowSet implements SchemaRowSet {

    private final Schema schema;

    SchemaCompositeAggRowSet(Schema schema, List<BucketExtractor> exts, BitSet mask, SearchResponse r, int limitAggs) {
        super(exts, mask, r, limitAggs);
        this.schema = schema;
    }

    @Override
    public Schema schema() {
        return schema;
    }
}
