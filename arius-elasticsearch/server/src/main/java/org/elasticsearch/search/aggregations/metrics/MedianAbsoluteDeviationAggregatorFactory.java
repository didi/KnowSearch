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

package org.elasticsearch.search.aggregations.metrics;

import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MedianAbsoluteDeviationAggregatorFactory extends ValuesSourceAggregatorFactory<ValuesSource.Numeric> {

    private final double compression;

    MedianAbsoluteDeviationAggregatorFactory(String name,
                                                    ValuesSourceConfig<ValuesSource.Numeric> config,
                                                    QueryShardContext queryShardContext,
                                                    AggregatorFactory parent,
                                                    AggregatorFactories.Builder subFactoriesBuilder,
                                                    Map<String, Object> metaData,
                                                    double compression) throws IOException {

        super(name, config, queryShardContext, parent, subFactoriesBuilder, metaData);
        this.compression = compression;
    }

    @Override
    protected Aggregator createUnmapped(SearchContext searchContext,
                                            Aggregator parent,
                                            List<PipelineAggregator> pipelineAggregators,
                                            Map<String, Object> metaData) throws IOException {

        return new MedianAbsoluteDeviationAggregator(
            name,
            searchContext,
            parent,
            pipelineAggregators,
            metaData,
            null,
            config.format(),
            compression
        );
    }

    @Override
    protected Aggregator doCreateInternal(ValuesSource.Numeric valuesSource,
                                            SearchContext searchContext,
                                            Aggregator parent,
                                            boolean collectsFromSingleBucket,
                                            List<PipelineAggregator> pipelineAggregators,
                                            Map<String, Object> metaData) throws IOException {

        return new MedianAbsoluteDeviationAggregator(
            name,
            searchContext,
            parent,
            pipelineAggregators,
            metaData,
            valuesSource,
            config.format(),
            compression
        );
    }
}
