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

package org.elasticsearch.join.aggregations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.Writeable.Reader;
import org.elasticsearch.common.xcontent.NamedXContentRegistry.Entry;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.InternalSingleBucketAggregationTestCase;
import org.elasticsearch.search.aggregations.bucket.ParsedSingleBucketAggregation;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;

public class InternalParentTests extends InternalSingleBucketAggregationTestCase<InternalParent> {

    @Override
    protected List<Entry> getNamedXContents() {
        List<Entry> extendedNamedXContents = new ArrayList<>(super.getNamedXContents());
        extendedNamedXContents.add(new Entry(Aggregation.class, new ParseField(ParentAggregationBuilder.NAME),
                (p, c) -> ParsedParent.fromXContent(p, (String) c)));
        return extendedNamedXContents ;
    }

    @Override
    protected InternalParent createTestInstance(String name, long docCount, InternalAggregations aggregations,
            List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData) {
        return new InternalParent(name, docCount, aggregations, pipelineAggregators, metaData);
    }

    @Override
    protected void extraAssertReduced(InternalParent reduced, List<InternalParent> inputs) {
        // Nothing extra to assert
    }

    @Override
    protected Reader<InternalParent> instanceReader() {
        return InternalParent::new;
    }

    @Override
    protected Class<? extends ParsedSingleBucketAggregation> implementationClass() {
        return ParsedParent.class;
    }
}
