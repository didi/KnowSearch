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
package org.elasticsearch.search.aggregations.bucket;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.test.ESIntegTestCase;
import org.hamcrest.Matchers;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.avg;
import static org.elasticsearch.search.aggregations.AggregationBuilders.histogram;
import static org.elasticsearch.search.aggregations.AggregationBuilders.missing;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertSearchResponse;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

@ESIntegTestCase.SuiteScopeTestCase
public class MissingIT extends ESIntegTestCase {

    static int numDocs, numDocsMissing, numDocsUnmapped;

    @Override
    public void setupSuiteScopeCluster() throws Exception {
        assertAcked(client().admin().indices().prepareCreate("idx")
                .addMapping("type", "tag", "type=keyword").get());
        List<IndexRequestBuilder> builders = new ArrayList<>();
        numDocs = randomIntBetween(5, 20);
        numDocsMissing = randomIntBetween(1, numDocs - 1);
        for (int i = 0; i < numDocsMissing; i++) {
            builders.add(client().prepareIndex("idx", "type", ""+i).setSource(jsonBuilder()
                    .startObject()
                    .field("value", i)
                    .endObject()));
        }
        for (int i = numDocsMissing; i < numDocs; i++) {
            builders.add(client().prepareIndex("idx", "type", ""+i).setSource(jsonBuilder()
                    .startObject()
                    .field("tag", "tag1")
                    .endObject()));
        }

        createIndex("unmapped_idx");
        numDocsUnmapped = randomIntBetween(2, 5);
        for (int i = 0; i < numDocsUnmapped; i++) {
            builders.add(client().prepareIndex("unmapped_idx", "type", ""+i).setSource(jsonBuilder()
                    .startObject()
                    .field("value", i)
                    .endObject()));
        }

        prepareCreate("empty_bucket_idx").addMapping("type", "value", "type=integer").get();
        for (int i = 0; i < 2; i++) {
            builders.add(client().prepareIndex("empty_bucket_idx", "type", ""+i).setSource(jsonBuilder()
                    .startObject()
                    .field("value", i*2)
                    .endObject()));
        }

        indexRandom(true, builders);
        ensureSearchable();
    }

    public void testUnmapped() throws Exception {
        SearchResponse response = client().prepareSearch("unmapped_idx")
                .addAggregation(missing("missing_tag").field("tag"))
                .get();

        assertSearchResponse(response);


        Missing missing = response.getAggregations().get("missing_tag");
        assertThat(missing, notNullValue());
        assertThat(missing.getName(), equalTo("missing_tag"));
        assertThat(missing.getDocCount(), equalTo((long) numDocsUnmapped));
    }

    public void testPartiallyUnmapped() throws Exception {
        SearchResponse response = client().prepareSearch("idx", "unmapped_idx")
                .addAggregation(missing("missing_tag").field("tag"))
                .get();

        assertSearchResponse(response);


        Missing missing = response.getAggregations().get("missing_tag");
        assertThat(missing, notNullValue());
        assertThat(missing.getName(), equalTo("missing_tag"));
        assertThat(missing.getDocCount(), equalTo((long) numDocsMissing + numDocsUnmapped));
    }

    public void testSimple() throws Exception {
        SearchResponse response = client().prepareSearch("idx")
                .addAggregation(missing("missing_tag").field("tag"))
                .get();

        assertSearchResponse(response);


        Missing missing = response.getAggregations().get("missing_tag");
        assertThat(missing, notNullValue());
        assertThat(missing.getName(), equalTo("missing_tag"));
        assertThat(missing.getDocCount(), equalTo((long) numDocsMissing));
    }

    public void testWithSubAggregation() throws Exception {
        SearchResponse response = client().prepareSearch("idx", "unmapped_idx")
                .addAggregation(missing("missing_tag").field("tag")
                        .subAggregation(avg("avg_value").field("value")))
                .get();

        assertSearchResponse(response);

        assertThat("Not all shards are initialized", response.getSuccessfulShards(), equalTo(response.getTotalShards()));

        Missing missing = response.getAggregations().get("missing_tag");
        assertThat(missing, notNullValue());
        assertThat(missing.getName(), equalTo("missing_tag"));
        assertThat(missing.getDocCount(), equalTo((long) numDocsMissing + numDocsUnmapped));
        assertThat((long) ((InternalAggregation)missing).getProperty("_count"), equalTo((long) numDocsMissing + numDocsUnmapped));
        assertThat(missing.getAggregations().asList().isEmpty(), is(false));

        long sum = 0;
        for (int i = 0; i < numDocsMissing; ++i) {
            sum += i;
        }
        for (int i = 0; i < numDocsUnmapped; ++i) {
            sum += i;
        }
        Avg avgValue = missing.getAggregations().get("avg_value");
        assertThat(avgValue, notNullValue());
        assertThat(avgValue.getName(), equalTo("avg_value"));
        assertThat(avgValue.getValue(), equalTo((double) sum / (numDocsMissing + numDocsUnmapped)));
        assertThat((double) ((InternalAggregation)missing).getProperty("avg_value.value"),
                equalTo((double) sum / (numDocsMissing + numDocsUnmapped)));
    }

    public void testEmptyAggregation() throws Exception {
        SearchResponse searchResponse = client().prepareSearch("empty_bucket_idx")
                .setQuery(matchAllQuery())
                .addAggregation(histogram("histo").field("value").interval(1L).minDocCount(0)
                        .subAggregation(missing("missing").field("value")))
                .get();

        assertThat(searchResponse.getHits().getTotalHits().value, equalTo(2L));
        Histogram histo = searchResponse.getAggregations().get("histo");
        assertThat(histo, Matchers.notNullValue());
        Histogram.Bucket bucket = histo.getBuckets().get(1);
        assertThat(bucket, Matchers.notNullValue());

        Missing missing = bucket.getAggregations().get("missing");
        assertThat(missing, Matchers.notNullValue());
        assertThat(missing.getName(), equalTo("missing"));
        assertThat(missing.getDocCount(), is(0L));
    }


}
