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
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.aggregations.Aggregator.SubAggCollectionMode;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregatorFactory.ExecutionMode;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.test.ESIntegTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.search.aggregations.AggregationBuilders.sum;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertSearchResponse;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.IsNull.notNullValue;

@ESIntegTestCase.SuiteScopeTestCase
public class TermsDocCountErrorIT extends ESIntegTestCase {

    private static final String STRING_FIELD_NAME = "s_value";
    private static final String LONG_FIELD_NAME = "l_value";
    private static final String DOUBLE_FIELD_NAME = "d_value";


    public static String randomExecutionHint() {
        return randomBoolean() ? null : randomFrom(ExecutionMode.values()).toString();
    }

    private static int numRoutingValues;

    @Override
    public void setupSuiteScopeCluster() throws Exception {
        assertAcked(client().admin().indices().prepareCreate("idx")
                .addMapping("type", STRING_FIELD_NAME, "type=keyword").get());
        List<IndexRequestBuilder> builders = new ArrayList<>();
        int numDocs = between(10, 200);
        int numUniqueTerms = between(2,numDocs/2);
        for (int i = 0; i < numDocs; i++) {
            builders.add(client().prepareIndex("idx", "type", ""+i).setSource(jsonBuilder()
                    .startObject()
                    .field(STRING_FIELD_NAME, "val" + randomInt(numUniqueTerms))
                    .field(LONG_FIELD_NAME, randomInt(numUniqueTerms))
                    .field(DOUBLE_FIELD_NAME, 1.0 * randomInt(numUniqueTerms))
                    .endObject()));
        }
        assertAcked(prepareCreate("idx_single_shard")
                .addMapping("type", STRING_FIELD_NAME, "type=keyword")
                .setSettings(Settings.builder().put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)));
        for (int i = 0; i < numDocs; i++) {
            builders.add(client().prepareIndex("idx_single_shard", "type", ""+i).setSource(jsonBuilder()
                    .startObject()
                    .field(STRING_FIELD_NAME, "val" + randomInt(numUniqueTerms))
                    .field(LONG_FIELD_NAME, randomInt(numUniqueTerms))
                    .field(DOUBLE_FIELD_NAME, 1.0 * randomInt(numUniqueTerms))
                    .endObject()));
        }
        numRoutingValues = between(1,40);
        assertAcked(prepareCreate("idx_with_routing")
            .addMapping("type", "{ \"type\" : { \"_routing\" : { \"required\" : true } } }", XContentType.JSON));
        for (int i = 0; i < numDocs; i++) {
            builders.add(client().prepareIndex("idx_single_shard", "type", "" + i)
                .setRouting(String.valueOf(randomInt(numRoutingValues)))
                .setSource(jsonBuilder()
                    .startObject()
                    .field(STRING_FIELD_NAME, "val" + randomInt(numUniqueTerms))
                    .field(LONG_FIELD_NAME, randomInt(numUniqueTerms))
                    .field(DOUBLE_FIELD_NAME, 1.0 * randomInt(numUniqueTerms))
                    .endObject()));
        }
        assertAcked(prepareCreate("idx_fixed_docs_0").addMapping("type", STRING_FIELD_NAME, "type=keyword")
                .setSettings(Settings.builder().put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)));
        Map<String, Integer> shard0DocsPerTerm = new HashMap<>();
        shard0DocsPerTerm.put("A", 25);
        shard0DocsPerTerm.put("B", 18);
        shard0DocsPerTerm.put("C", 6);
        shard0DocsPerTerm.put("D", 3);
        shard0DocsPerTerm.put("E", 2);
        shard0DocsPerTerm.put("F", 2);
        shard0DocsPerTerm.put("G", 2);
        shard0DocsPerTerm.put("H", 2);
        shard0DocsPerTerm.put("I", 1);
        shard0DocsPerTerm.put("J", 1);
        for (Map.Entry<String, Integer> entry : shard0DocsPerTerm.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                String term = entry.getKey();
                builders.add(client().prepareIndex("idx_fixed_docs_0", "type", term + "-" + i)
                        .setSource(jsonBuilder().startObject().field(STRING_FIELD_NAME, term).endObject()));
            }
        }

        assertAcked(prepareCreate("idx_fixed_docs_1").addMapping("type", STRING_FIELD_NAME, "type=keyword")
                .setSettings(Settings.builder().put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)));
        Map<String, Integer> shard1DocsPerTerm = new HashMap<>();
        shard1DocsPerTerm.put("A", 30);
        shard1DocsPerTerm.put("B", 25);
        shard1DocsPerTerm.put("F", 17);
        shard1DocsPerTerm.put("Z", 16);
        shard1DocsPerTerm.put("G", 15);
        shard1DocsPerTerm.put("H", 14);
        shard1DocsPerTerm.put("I", 10);
        shard1DocsPerTerm.put("Q", 6);
        shard1DocsPerTerm.put("J", 8);
        shard1DocsPerTerm.put("C", 4);
        for (Map.Entry<String, Integer> entry : shard1DocsPerTerm.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                String term = entry.getKey();
                builders.add(client().prepareIndex("idx_fixed_docs_1", "type", term + "-" + i)
                        .setSource(jsonBuilder().startObject().field(STRING_FIELD_NAME, term).field("shard", 1).endObject()));
            }
        }

        assertAcked(prepareCreate("idx_fixed_docs_2")
                .addMapping("type", STRING_FIELD_NAME, "type=keyword")
                .setSettings(Settings.builder().put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)));
        Map<String, Integer> shard2DocsPerTerm = new HashMap<>();
        shard2DocsPerTerm.put("A", 45);
        shard2DocsPerTerm.put("C", 44);
        shard2DocsPerTerm.put("Z", 36);
        shard2DocsPerTerm.put("G", 30);
        shard2DocsPerTerm.put("E", 29);
        shard2DocsPerTerm.put("H", 28);
        shard2DocsPerTerm.put("Q", 2);
        shard2DocsPerTerm.put("D", 1);
        for (Map.Entry<String, Integer> entry : shard2DocsPerTerm.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                String term = entry.getKey();
                builders.add(client().prepareIndex("idx_fixed_docs_2", "type", term + "-" + i)
                        .setSource(jsonBuilder().startObject().field(STRING_FIELD_NAME, term).field("shard", 2).endObject()));
            }
        }

        indexRandom(true, builders);
        ensureSearchable();
    }

    private void assertDocCountErrorWithinBounds(int size, SearchResponse accurateResponse, SearchResponse testResponse) {
        Terms accurateTerms = accurateResponse.getAggregations().get("terms");
        assertThat(accurateTerms, notNullValue());
        assertThat(accurateTerms.getName(), equalTo("terms"));
        assertThat(accurateTerms.getDocCountError(), equalTo(0L));

        Terms testTerms = testResponse.getAggregations().get("terms");
        assertThat(testTerms, notNullValue());
        assertThat(testTerms.getName(), equalTo("terms"));
        assertThat(testTerms.getDocCountError(), greaterThanOrEqualTo(0L));
        List<? extends Bucket> testBuckets = testTerms.getBuckets();
        assertThat(testBuckets.size(), lessThanOrEqualTo(size));
        assertThat(accurateTerms.getBuckets().size(), greaterThanOrEqualTo(testBuckets.size()));

        for (Terms.Bucket testBucket : testBuckets) {
            assertThat(testBucket, notNullValue());
            Terms.Bucket accurateBucket = accurateTerms.getBucketByKey(testBucket.getKeyAsString());
            assertThat(accurateBucket, notNullValue());
            assertThat(accurateBucket.getDocCountError(), equalTo(0L));
            assertThat(testBucket.getDocCountError(), lessThanOrEqualTo(testTerms.getDocCountError()));
            assertThat(testBucket.getDocCount() + testBucket.getDocCountError(), greaterThanOrEqualTo(accurateBucket.getDocCount()));
            assertThat(testBucket.getDocCount() - testBucket.getDocCountError(), lessThanOrEqualTo(accurateBucket.getDocCount()));
        }

        for (Terms.Bucket accurateBucket: accurateTerms.getBuckets()) {
            assertThat(accurateBucket, notNullValue());
            Terms.Bucket testBucket = accurateTerms.getBucketByKey(accurateBucket.getKeyAsString());
            if (testBucket == null) {
                assertThat(accurateBucket.getDocCount(), lessThanOrEqualTo(testTerms.getDocCountError()));
            }

        }
    }

    private void assertNoDocCountError(int size, SearchResponse accurateResponse, SearchResponse testResponse) {
        Terms accurateTerms = accurateResponse.getAggregations().get("terms");
        assertThat(accurateTerms, notNullValue());
        assertThat(accurateTerms.getName(), equalTo("terms"));
        assertThat(accurateTerms.getDocCountError(), equalTo(0L));

        Terms testTerms = testResponse.getAggregations().get("terms");
        assertThat(testTerms, notNullValue());
        assertThat(testTerms.getName(), equalTo("terms"));
        assertThat(testTerms.getDocCountError(), equalTo(0L));
        List<? extends Bucket> testBuckets = testTerms.getBuckets();
        assertThat(testBuckets.size(), lessThanOrEqualTo(size));
        assertThat(accurateTerms.getBuckets().size(), greaterThanOrEqualTo(testBuckets.size()));

        for (Terms.Bucket testBucket : testBuckets) {
            assertThat(testBucket, notNullValue());
            Terms.Bucket accurateBucket = accurateTerms.getBucketByKey(testBucket.getKeyAsString());
            assertThat(accurateBucket, notNullValue());
            assertThat(accurateBucket.getDocCountError(), equalTo(0L));
            assertThat(testBucket.getDocCountError(), equalTo(0L));
        }
    }

    private void assertNoDocCountErrorSingleResponse(int size, SearchResponse testResponse) {
        Terms testTerms = testResponse.getAggregations().get("terms");
        assertThat(testTerms, notNullValue());
        assertThat(testTerms.getName(), equalTo("terms"));
        assertThat(testTerms.getDocCountError(), equalTo(0L));
        List<? extends Bucket> testBuckets = testTerms.getBuckets();
        assertThat(testBuckets.size(), lessThanOrEqualTo(size));

        for (Terms.Bucket testBucket : testBuckets) {
            assertThat(testBucket, notNullValue());
            assertThat(testBucket.getDocCountError(), equalTo(0L));
        }
    }

    private void assertUnboundedDocCountError(int size, SearchResponse accurateResponse, SearchResponse testResponse) {
        Terms accurateTerms = accurateResponse.getAggregations().get("terms");
        assertThat(accurateTerms, notNullValue());
        assertThat(accurateTerms.getName(), equalTo("terms"));
        assertThat(accurateTerms.getDocCountError(), equalTo(0L));

        Terms testTerms = testResponse.getAggregations().get("terms");
        assertThat(testTerms, notNullValue());
        assertThat(testTerms.getName(), equalTo("terms"));
        assertThat(testTerms.getDocCountError(),anyOf(equalTo(-1L), equalTo(0L)));
        List<? extends Bucket> testBuckets = testTerms.getBuckets();
        assertThat(testBuckets.size(), lessThanOrEqualTo(size));
        assertThat(accurateTerms.getBuckets().size(), greaterThanOrEqualTo(testBuckets.size()));

        for (Terms.Bucket testBucket : testBuckets) {
            assertThat(testBucket, notNullValue());
            Terms.Bucket accurateBucket = accurateTerms.getBucketByKey(testBucket.getKeyAsString());
            assertThat(accurateBucket, notNullValue());
            assertThat(accurateBucket.getDocCountError(), equalTo(0L));
            assertThat(testBucket.getDocCountError(), anyOf(equalTo(-1L), equalTo(0L)));
        }
    }

    public void testStringValueField() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertDocCountErrorWithinBounds(size, accurateResponse, testResponse);
    }

    public void testStringValueFieldSingleShard() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountError(size, accurateResponse, testResponse);
    }

    public void testStringValueFieldWithRouting() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);

        SearchResponse testResponse = client().prepareSearch("idx_with_routing").setTypes("type")
                .setRouting(String.valueOf(between(1, numRoutingValues)))
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountErrorSingleResponse(size, testResponse);
    }

    public void testStringValueFieldDocCountAsc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.count(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.count(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertUnboundedDocCountError(size, accurateResponse, testResponse);
    }

    public void testStringValueFieldTermSortAsc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.key(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.key(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountError(size, accurateResponse, testResponse);
    }

    public void testStringValueFieldTermSortDesc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.key(false))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.key(false))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountError(size, accurateResponse, testResponse);
    }

    public void testStringValueFieldSubAggAsc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.aggregation("sortAgg", true))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(LONG_FIELD_NAME)))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.aggregation("sortAgg", true))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(LONG_FIELD_NAME)))
                .get();

        assertSearchResponse(testResponse);

        assertUnboundedDocCountError(size, accurateResponse, testResponse);
    }

    public void testStringValueFieldSubAggDesc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.aggregation("sortAgg", false))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(LONG_FIELD_NAME)))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.aggregation("sortAgg", false))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(LONG_FIELD_NAME)))
                .get();

        assertSearchResponse(testResponse);

        assertUnboundedDocCountError(size, accurateResponse, testResponse);
    }

    public void testLongValueField() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertDocCountErrorWithinBounds(size, accurateResponse, testResponse);
    }

    public void testLongValueFieldSingleShard() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountError(size, accurateResponse, testResponse);
    }

    public void testLongValueFieldWithRouting() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);

        SearchResponse testResponse = client().prepareSearch("idx_with_routing").setTypes("type")
                .setRouting(String.valueOf(between(1, numRoutingValues)))
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountErrorSingleResponse(size, testResponse);
    }

    public void testLongValueFieldDocCountAsc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.count(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.count(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertUnboundedDocCountError(size, accurateResponse, testResponse);
    }

    public void testLongValueFieldTermSortAsc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.key(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.key(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountError(size, accurateResponse, testResponse);
    }

    public void testLongValueFieldTermSortDesc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.key(false))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.key(false))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountError(size, accurateResponse, testResponse);
    }

    public void testLongValueFieldSubAggAsc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.aggregation("sortAgg", true))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(LONG_FIELD_NAME)))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.aggregation("sortAgg", true))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(LONG_FIELD_NAME)))
                .get();

        assertSearchResponse(testResponse);

        assertUnboundedDocCountError(size, accurateResponse, testResponse);
    }

    public void testLongValueFieldSubAggDesc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.aggregation("sortAgg", false))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(DOUBLE_FIELD_NAME)))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(LONG_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.aggregation("sortAgg", false))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(DOUBLE_FIELD_NAME)))
                .get();

        assertSearchResponse(testResponse);

        assertUnboundedDocCountError(size, accurateResponse, testResponse);
    }

    public void testDoubleValueField() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertDocCountErrorWithinBounds(size, accurateResponse, testResponse);
    }

    public void testDoubleValueFieldSingleShard() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountError(size, accurateResponse, testResponse);
    }

    public void testDoubleValueFieldWithRouting() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);

        SearchResponse testResponse = client().prepareSearch("idx_with_routing").setTypes("type")
                .setRouting(String.valueOf(between(1, numRoutingValues)))
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountErrorSingleResponse(size, testResponse);
    }

    public void testDoubleValueFieldDocCountAsc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.count(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.count(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertUnboundedDocCountError(size, accurateResponse, testResponse);
    }

    public void testDoubleValueFieldTermSortAsc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.key(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.key(true))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountError(size, accurateResponse, testResponse);
    }

    public void testDoubleValueFieldTermSortDesc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.key(false))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.key(false))
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();

        assertSearchResponse(testResponse);

        assertNoDocCountError(size, accurateResponse, testResponse);
    }

    public void testDoubleValueFieldSubAggAsc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.aggregation("sortAgg", true))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(LONG_FIELD_NAME)))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.aggregation("sortAgg", true))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(LONG_FIELD_NAME)))
                .get();

        assertSearchResponse(testResponse);

        assertUnboundedDocCountError(size, accurateResponse, testResponse);
    }

    public void testDoubleValueFieldSubAggDesc() throws Exception {
        int size = randomIntBetween(1, 20);
        int shardSize = randomIntBetween(size, size * 2);
        SearchResponse accurateResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(10000).shardSize(10000)
                        .order(BucketOrder.aggregation("sortAgg", false))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(LONG_FIELD_NAME)))
                .get();

        assertSearchResponse(accurateResponse);

        SearchResponse testResponse = client().prepareSearch("idx_single_shard").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(DOUBLE_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(size)
                        .shardSize(shardSize)
                        .order(BucketOrder.aggregation("sortAgg", false))
                        .collectMode(randomFrom(SubAggCollectionMode.values()))
                        .subAggregation(sum("sortAgg").field(LONG_FIELD_NAME)))
                .get();

        assertSearchResponse(testResponse);

        assertUnboundedDocCountError(size, accurateResponse, testResponse);
    }

    /**
     * Test a case where we know exactly how many of each term is on each shard
     * so we know the exact error value for each term. To do this we search over
     * 3 one-shard indices.
     */
    public void testFixedDocs() throws Exception {
        SearchResponse response = client().prepareSearch("idx_fixed_docs_0", "idx_fixed_docs_1", "idx_fixed_docs_2").setTypes("type")
                .addAggregation(terms("terms")
                        .executionHint(randomExecutionHint())
                        .field(STRING_FIELD_NAME)
                        .showTermDocCountError(true)
                        .size(5).shardSize(5)
                        .collectMode(randomFrom(SubAggCollectionMode.values())))
                .get();
        assertSearchResponse(response);

        Terms terms = response.getAggregations().get("terms");
        assertThat(terms, notNullValue());
        assertThat(terms.getDocCountError(), equalTo(46L));
        List<? extends Bucket> buckets = terms.getBuckets();
        assertThat(buckets, notNullValue());
        assertThat(buckets.size(), equalTo(5));

        Bucket bucket = buckets.get(0);
        assertThat(bucket, notNullValue());
        assertThat(bucket.getKey(), equalTo("A"));
        assertThat(bucket.getDocCount(), equalTo(100L));
        assertThat(bucket.getDocCountError(), equalTo(0L));

        bucket = buckets.get(1);
        assertThat(bucket, notNullValue());
        assertThat(bucket.getKey(), equalTo("Z"));
        assertThat(bucket.getDocCount(), equalTo(52L));
        assertThat(bucket.getDocCountError(), equalTo(2L));

        bucket = buckets.get(2);
        assertThat(bucket, notNullValue());
        assertThat(bucket.getKey(), equalTo("C"));
        assertThat(bucket.getDocCount(), equalTo(50L));
        assertThat(bucket.getDocCountError(), equalTo(15L));


        bucket = buckets.get(3);
        assertThat(bucket, notNullValue());
        assertThat(bucket.getKey(), equalTo("G"));
        assertThat(bucket.getDocCount(), equalTo(45L));
        assertThat(bucket.getDocCountError(), equalTo(2L));

        bucket = buckets.get(4);
        assertThat(bucket, notNullValue());
        assertThat(bucket.getKey(), equalTo("B"));
        assertThat(bucket.getDocCount(), equalTo(43L));
        assertThat(bucket.getDocCountError(), equalTo(29L));
    }

}
