/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql.execution.search;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.xpack.sql.execution.search.extractor.BucketExtractor;
import org.elasticsearch.xpack.sql.type.Schema;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PivotCursor extends CompositeAggCursor {

    public static final String NAME = "p";

    private final Map<String, Object> previousKey;

    PivotCursor(Map<String, Object> previousKey, byte[] next, List<BucketExtractor> exts, BitSet mask, int remainingLimit,
            boolean includeFrozen,
            String... indices) {
        super(next, exts, mask, remainingLimit, includeFrozen, indices);
        this.previousKey = previousKey;
    }

    public PivotCursor(StreamInput in) throws IOException {
        super(in);
        previousKey = in.readBoolean() == true ? in.readMap() : null;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        if (previousKey != null) {
            out.writeBoolean(true);
            out.writeMap(previousKey);
        } else {
            out.writeBoolean(false);
        }
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    protected Supplier<CompositeAggRowSet> makeRowSet(SearchResponse response) {
        return () -> new PivotRowSet(Schema.EMPTY, extractors(), mask(), response, limit(), previousKey);
    }

    @Override
    protected BiFunction<byte[], CompositeAggRowSet, CompositeAggCursor> makeCursor() {
        return (q, r) -> {
            Map<String, Object> lastAfterKey = r instanceof PivotRowSet ? ((PivotRowSet) r).lastAfterKey() : null;
            return new PivotCursor(lastAfterKey, q, r.extractors(), r.mask(), r.remainingData(), includeFrozen(), indices());
        };
    }

    @Override
    public String toString() {
        return "pivot for index [" + Arrays.toString(indices()) + "]";
    }
}