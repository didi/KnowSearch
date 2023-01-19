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

package org.elasticsearch.search.aggregations.bucket.adjacency;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.internal.SearchContext;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.TestSearchContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdjacencyMatrixAggregationBuilderTests extends ESTestCase {

    public void testFilterSizeLimitation() throws Exception {
        // filter size grater than max size should thrown a exception
        QueryShardContext queryShardContext = mock(QueryShardContext.class);
        IndexShard indexShard = mock(IndexShard.class);
        Settings settings = Settings.builder()
            .put("index.max_adjacency_matrix_filters", 2)
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
            .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 1)
            .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 2)
            .build();
        IndexMetaData indexMetaData = IndexMetaData.builder("index").settings(settings).build();
        IndexSettings indexSettings = new IndexSettings(indexMetaData, Settings.EMPTY);
        when(indexShard.indexSettings()).thenReturn(indexSettings);
        when(queryShardContext.getIndexSettings()).thenReturn(indexSettings);
        SearchContext context = new TestSearchContext(queryShardContext, indexShard);

        Map<String, QueryBuilder> filters = new HashMap<>(3);
        for (int i = 0; i < 3; i++) {
            QueryBuilder queryBuilder = mock(QueryBuilder.class);
            // return builder itself to skip rewrite
            when(queryBuilder.rewrite(queryShardContext)).thenReturn(queryBuilder);
            filters.put("filter" + i, queryBuilder);
        }
        AdjacencyMatrixAggregationBuilder builder = new AdjacencyMatrixAggregationBuilder("dummy", filters);
        IllegalArgumentException ex = expectThrows(IllegalArgumentException.class,
            () -> builder.doBuild(context.getQueryShardContext(), null, new AggregatorFactories.Builder()));
        assertThat(ex.getMessage(), equalTo("Number of filters is too large, must be less than or equal to: [2] but was [3]."
            + "This limit can be set by changing the [" + IndexSettings.MAX_ADJACENCY_MATRIX_FILTERS_SETTING.getKey()
            + "] index level setting."));

        // filter size not grater than max size should return an instance of AdjacencyMatrixAggregatorFactory
        Map<String, QueryBuilder> emptyFilters = Collections.emptyMap();

        AdjacencyMatrixAggregationBuilder aggregationBuilder = new AdjacencyMatrixAggregationBuilder("dummy", emptyFilters);
        AggregatorFactory factory = aggregationBuilder.doBuild(context.getQueryShardContext(), null, new AggregatorFactories.Builder());
        assertThat(factory instanceof AdjacencyMatrixAggregatorFactory, is(true));
        assertThat(factory.name(), equalTo("dummy"));
        assertWarnings("[index.max_adjacency_matrix_filters] setting was deprecated in Elasticsearch and will be "
                + "removed in a future release! See the breaking changes documentation for the next major version.");
    }
}
