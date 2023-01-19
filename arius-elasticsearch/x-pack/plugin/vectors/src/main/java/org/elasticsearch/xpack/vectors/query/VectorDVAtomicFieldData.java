/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */


package org.elasticsearch.xpack.vectors.query;

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.index.fielddata.AtomicFieldData;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.index.fielddata.SortedBinaryDocValues;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

final class VectorDVAtomicFieldData implements AtomicFieldData {

    private final LeafReader reader;
    private final String field;
    private final boolean isDense;

    VectorDVAtomicFieldData(LeafReader reader, String field, boolean isDense) {
        this.reader = reader;
        this.field = field;
        this.isDense = isDense;
    }

    @Override
    public long ramBytesUsed() {
        return 0; // not exposed by Lucene
    }

    @Override
    public Collection<Accountable> getChildResources() {
        return Collections.emptyList();
    }

    @Override
    public SortedBinaryDocValues getBytesValues() {
        throw new UnsupportedOperationException("String representation of doc values for vector fields is not supported");
    }

    @Override
    public ScriptDocValues<BytesRef> getScriptValues() {
        try {
            final BinaryDocValues values = DocValues.getBinary(reader, field);
            if (isDense) {
                return new VectorScriptDocValues.DenseVectorScriptDocValues(values);
            } else {
                return new VectorScriptDocValues.SparseVectorScriptDocValues(values);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load doc values for vector field!", e);
        }
    }

    @Override
    public void close() {
        // no-op
    }
}
