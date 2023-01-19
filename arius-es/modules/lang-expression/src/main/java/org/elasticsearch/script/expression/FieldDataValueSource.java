/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.script.expression;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;
import org.elasticsearch.index.fielddata.AtomicNumericFieldData;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.NumericDoubleValues;
import org.elasticsearch.search.MultiValueMode;

/**
 * A {@link ValueSource} wrapper for field data.
 */
class FieldDataValueSource extends ValueSource {

    final IndexFieldData<?> fieldData;
    final MultiValueMode multiValueMode;

    protected FieldDataValueSource(IndexFieldData<?> fieldData, MultiValueMode multiValueMode) {
        this.fieldData = Objects.requireNonNull(fieldData);
        this.multiValueMode = Objects.requireNonNull(multiValueMode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldDataValueSource that = (FieldDataValueSource) o;

        if (!fieldData.equals(that.fieldData)) return false;
        return multiValueMode == that.multiValueMode;

    }

    @Override
    public int hashCode() {
        int result = fieldData.hashCode();
        result = 31 * result + multiValueMode.hashCode();
        return result;
    }

    @Override
    @SuppressWarnings("rawtypes") // ValueSource uses a rawtype
    public FunctionValues getValues(Map context, LeafReaderContext leaf) throws IOException {
        AtomicNumericFieldData leafData = (AtomicNumericFieldData) fieldData.load(leaf);
        NumericDoubleValues docValues = multiValueMode.select(leafData.getDoubleValues());
        return new DoubleDocValues(this) {
          @Override
          public double doubleVal(int doc) throws IOException {
            if (docValues.advanceExact(doc)) {
                return docValues.doubleValue();
            } else {
                return 0;
            }
          }
        };
    }

    @Override
    public String description() {
        return "field(" + fieldData.getFieldName() + ")";
    }
}
