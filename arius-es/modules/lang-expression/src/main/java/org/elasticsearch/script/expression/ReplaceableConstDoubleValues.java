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

import org.apache.lucene.search.DoubleValues;

import java.io.IOException;

/**
 * A support class for an executable expression script that allows the double returned
 * by a {@link DoubleValues} to be modified.
 */
final class ReplaceableConstDoubleValues extends DoubleValues {
    private double value = 0;

    void setValue(double value) {
        this.value = value;
    }

    @Override
    public double doubleValue() throws IOException {
        return value;
    }

    @Override
    public boolean advanceExact(int doc) throws IOException {
        return true;
    }
}
