/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.transform.integration;

import org.elasticsearch.Version;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transform.transforms.TransformConfig;
import org.elasticsearch.client.transform.transforms.TransformConfigUpdate;
import org.elasticsearch.client.transform.transforms.TransformStats;
import org.elasticsearch.client.transform.transforms.DestConfig;
import org.elasticsearch.client.transform.transforms.TimeSyncConfig;
import org.elasticsearch.client.transform.transforms.pivot.SingleGroupSource;
import org.elasticsearch.client.transform.transforms.pivot.TermsGroupSource;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.After;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.oneOf;

public class TransformIT extends TransformIntegTestCase {

    @After
    public void cleanTransforms() throws IOException {
        cleanUp();
    }

    public void testTransformCrud() throws Exception {
        String indexName = "basic-crud-reviews";
        createReviewsIndex(indexName, 100);

        Map<String, SingleGroupSource> groups = new HashMap<>();
        groups.put("by-day", createDateHistogramGroupSourceWithCalendarInterval("timestamp", DateHistogramInterval.DAY, null));
        groups.put("by-user", TermsGroupSource.builder().setField("user_id").build());
        groups.put("by-business", TermsGroupSource.builder().setField("business_id").build());

        AggregatorFactories.Builder aggs = AggregatorFactories.builder()
            .addAggregator(AggregationBuilders.avg("review_score").field("stars"))
            .addAggregator(AggregationBuilders.max("timestamp").field("timestamp"));

        TransformConfig config = createTransformConfig("transform-crud",
            groups,
            aggs,
            "reviews-by-user-business-day",
            indexName);

        assertTrue(putTransform(config, RequestOptions.DEFAULT).isAcknowledged());
        assertTrue(startTransform(config.getId(), RequestOptions.DEFAULT).isAcknowledged());

        waitUntilCheckpoint(config.getId(), 1L);

        stopTransform(config.getId());

        TransformConfig storedConfig = getTransform(config.getId()).getTransformConfigurations().get(0);
        assertThat(storedConfig.getVersion(), equalTo(Version.CURRENT));
        Instant now = Instant.now();
        assertTrue("[create_time] is not before current time", storedConfig.getCreateTime().isBefore(now));
        deleteTransform(config.getId());
    }

    public void testContinuousTransformCrud() throws Exception {
        String indexName = "continuous-crud-reviews";
        createReviewsIndex(indexName, 100);

        Map<String, SingleGroupSource> groups = new HashMap<>();
        groups.put("by-day", createDateHistogramGroupSourceWithCalendarInterval("timestamp", DateHistogramInterval.DAY, null));
        groups.put("by-user", TermsGroupSource.builder().setField("user_id").build());
        groups.put("by-business", TermsGroupSource.builder().setField("business_id").build());

        AggregatorFactories.Builder aggs = AggregatorFactories.builder()
            .addAggregator(AggregationBuilders.avg("review_score").field("stars"))
            .addAggregator(AggregationBuilders.max("timestamp").field("timestamp"));

        TransformConfig config = createTransformConfigBuilder("transform-crud",
            groups,
            aggs,
            "reviews-by-user-business-day",
            QueryBuilders.matchAllQuery(),
            indexName)
            .setSyncConfig(new TimeSyncConfig("timestamp", TimeValue.timeValueSeconds(1)))
            .build();

        assertTrue(putTransform(config, RequestOptions.DEFAULT).isAcknowledged());
        assertTrue(startTransform(config.getId(), RequestOptions.DEFAULT).isAcknowledged());

        waitUntilCheckpoint(config.getId(), 1L);
        assertThat(getTransformStats(config.getId()).getTransformsStats().get(0).getState(),
                equalTo(TransformStats.State.STARTED));

        long docsIndexed = getTransformStats(config.getId())
            .getTransformsStats()
            .get(0)
            .getIndexerStats()
            .getNumDocuments();

        TransformConfig storedConfig = getTransform(config.getId()).getTransformConfigurations().get(0);
        assertThat(storedConfig.getVersion(), equalTo(Version.CURRENT));
        Instant now = Instant.now();
        assertTrue("[create_time] is not before current time", storedConfig.getCreateTime().isBefore(now));

        // index some more docs
        long timeStamp = Instant.now().toEpochMilli() - 1_000;
        long user = 42;
        indexMoreDocs(timeStamp, user, indexName);
        waitUntilCheckpoint(config.getId(), 2L);

        // Assert that we wrote the new docs
        assertThat(getTransformStats(config.getId())
            .getTransformsStats()
            .get(0)
            .getIndexerStats()
            .getNumDocuments(), greaterThan(docsIndexed));

        stopTransform(config.getId());
        deleteTransform(config.getId());
    }

    public void testContinuousTransformUpdate() throws Exception {
        String indexName = "continuous-reviews-update";
        createReviewsIndex(indexName, 10);

        Map<String, SingleGroupSource> groups = new HashMap<>();
        groups.put("by-user", TermsGroupSource.builder().setField("user_id").build());

        AggregatorFactories.Builder aggs = AggregatorFactories.builder()
            .addAggregator(AggregationBuilders.avg("review_score").field("stars"))
            .addAggregator(AggregationBuilders.max("timestamp").field("timestamp"));

        String id = "transform-to-update";
        String dest = "reviews-by-user-business-day-to-update";
        TransformConfig config = createTransformConfigBuilder(id,
            groups,
            aggs,
            dest,
            QueryBuilders.matchAllQuery(),
            indexName)
            .setSyncConfig(new TimeSyncConfig("timestamp", TimeValue.timeValueSeconds(1)))
            .build();

        assertTrue(putTransform(config, RequestOptions.DEFAULT).isAcknowledged());
        assertTrue(startTransform(config.getId(), RequestOptions.DEFAULT).isAcknowledged());

        waitUntilCheckpoint(config.getId(), 1L);
        assertThat(getTransformStats(config.getId()).getTransformsStats().get(0).getState(),
            oneOf(TransformStats.State.STARTED, TransformStats.State.INDEXING));

        long docsIndexed = getTransformStats(config.getId())
            .getTransformsStats()
            .get(0)
            .getIndexerStats()
            .getNumDocuments();

        TransformConfig storedConfig = getTransform(config.getId()).getTransformConfigurations().get(0);
        assertThat(storedConfig.getVersion(), equalTo(Version.CURRENT));
        Instant now = Instant.now();
        assertTrue("[create_time] is not before current time", storedConfig.getCreateTime().isBefore(now));

        String pipelineId = "add_forty_two";
        TransformConfigUpdate update = TransformConfigUpdate.builder()
            .setDescription("updated config")
            .setDest(DestConfig.builder().setIndex(dest).setPipeline(pipelineId).build())
            .build();

        RestHighLevelClient hlrc = new TestRestHighLevelClient();
        final XContentBuilder pipelineBuilder = jsonBuilder()
            .startObject()
            .startArray("processors")
            .startObject()
            .startObject("set")
            .field("field", "static_forty_two")
            .field("value", 42)
            .endObject()
            .endObject()
            .endArray()
            .endObject();
        hlrc.ingest().putPipeline(new PutPipelineRequest(pipelineId, BytesReference.bytes(pipelineBuilder), XContentType.JSON),
            RequestOptions.DEFAULT);

        updateConfig(id, update);

        // index some more docs
        long timeStamp = Instant.now().toEpochMilli() - 1_000;
        long user = 42;
        indexMoreDocs(timeStamp, user, indexName);

        // Since updates are loaded on checkpoint start, we should see the updated config on this next run
        waitUntilCheckpoint(config.getId(), 2L);
        long numDocsAfterCp2 = getTransformStats(config.getId())
            .getTransformsStats()
            .get(0)
            .getIndexerStats()
            .getNumDocuments();
        assertThat(numDocsAfterCp2, greaterThan(docsIndexed));

        final SearchRequest searchRequest = new SearchRequest(dest)
                .source(new SearchSourceBuilder()
                    .trackTotalHits(true)
                    .query(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.termQuery("static_forty_two", 42))));
        // assert that we have the new field and its value is 42 in at least some docs
        assertBusy(() -> {
            final SearchResponse searchResponse = hlrc.search(searchRequest, RequestOptions.DEFAULT);
            assertThat(searchResponse.getHits().getTotalHits().value, greaterThan(0L));
            hlrc.indices().refresh(new RefreshRequest(dest), RequestOptions.DEFAULT);
        }, 30, TimeUnit.SECONDS);

        stopTransform(config.getId());
        deleteTransform(config.getId());
    }

    public void testStopWaitForCheckpoint() throws Exception {
        String indexName = "wait-for-checkpoint-reviews";
        String transformId = "data-frame-transform-wait-for-checkpoint";
        createReviewsIndex(indexName, 1000);

        Map<String, SingleGroupSource> groups = new HashMap<>();
        groups.put("by-day", createDateHistogramGroupSourceWithCalendarInterval("timestamp", DateHistogramInterval.DAY, null));
        groups.put("by-user", TermsGroupSource.builder().setField("user_id").build());
        groups.put("by-business", TermsGroupSource.builder().setField("business_id").build());

        AggregatorFactories.Builder aggs = AggregatorFactories.builder()
            .addAggregator(AggregationBuilders.avg("review_score").field("stars"))
            .addAggregator(AggregationBuilders.max("timestamp").field("timestamp"));

        TransformConfig config = createTransformConfigBuilder(transformId,
            groups,
            aggs,
            "reviews-by-user-business-day",
            QueryBuilders.matchAllQuery(),
            indexName)
            .setSyncConfig(new TimeSyncConfig("timestamp", TimeValue.timeValueSeconds(1)))
            .build();

        assertTrue(putTransform(config, RequestOptions.DEFAULT).isAcknowledged());
        assertTrue(startTransform(config.getId(), RequestOptions.DEFAULT).isAcknowledged());

        // waitForCheckpoint: true should make the transform continue until we hit the first checkpoint, then it will stop
        stopTransform(transformId, false, null, true);

        // Wait until the first checkpoint
        waitUntilCheckpoint(config.getId(), 1L);

        // Even though we are continuous, we should be stopped now as we needed to stop at the first checkpoint
        assertBusy(() -> {
            TransformStats stateAndStats = getTransformStats(config.getId()).getTransformsStats().get(0);
            assertThat(stateAndStats.getState(), equalTo(TransformStats.State.STOPPED));
            assertThat(stateAndStats.getIndexerStats().getNumDocuments(), equalTo(1000L));
        });

        stopTransform(config.getId());
        deleteTransform(config.getId());
    }

    private void indexMoreDocs(long timestamp, long userId, String index) throws Exception {
        BulkRequest bulk = new BulkRequest(index);
        for (int i = 0; i < 25; i++) {
            int stars = (i + 20) % 5;
            long business = (i + 100) % 50;

            StringBuilder sourceBuilder = new StringBuilder();
            sourceBuilder.append("{\"user_id\":\"")
                .append("user_")
                .append(userId)
                .append("\",\"count\":")
                .append(i)
                .append(",\"business_id\":\"")
                .append("business_")
                .append(business)
                .append("\",\"stars\":")
                .append(stars)
                .append(",\"timestamp\":")
                .append(timestamp)
                .append("}");
            bulk.add(new IndexRequest().source(sourceBuilder.toString(), XContentType.JSON));
        }
        bulk.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        bulkIndexDocs(bulk);
    }
}
