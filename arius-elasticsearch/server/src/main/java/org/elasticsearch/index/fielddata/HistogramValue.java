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

package org.elasticsearch.index.fielddata;

import java.io.IOException;

/**
 * Per-document histogram value. Every value of the histogram consist on
 * a value and a count.
 */
public abstract class HistogramValue {

    /**
     * Advance this instance to the next value of the histogram
     * @return true if there is a next value
     */
    public abstract boolean next() throws IOException;

    /**
     * the current value of the histogram
     * @return the current value of the histogram
     */
    public abstract double value();

    /**
     * The current count of the histogram
     * @return the current count of the histogram
     */
    public abstract int count();

}
