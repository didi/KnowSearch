/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.integration;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.xpack.core.ml.action.GetDataFrameAnalyticsStatsAction;
import org.elasticsearch.xpack.core.ml.dataframe.DataFrameAnalyticsConfig;
import org.elasticsearch.xpack.core.ml.dataframe.DataFrameAnalyticsDest;
import org.elasticsearch.xpack.core.ml.dataframe.DataFrameAnalyticsSource;
import org.elasticsearch.xpack.core.ml.dataframe.DataFrameAnalyticsState;
import org.elasticsearch.xpack.core.ml.dataframe.analyses.OutlierDetection;
import org.junit.After;

import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

public class RunDataFrameAnalyticsIT extends MlNativeDataFrameAnalyticsIntegTestCase {

    @After
    public void cleanup() {
        cleanUp();
    }

    public void testOutlierDetectionWithFewDocuments() throws Exception {
        String sourceIndex = "test-outlier-detection-with-few-docs";

        client().admin().indices().prepareCreate(sourceIndex)
            .addMapping("_doc", "numeric_1", "type=double", "numeric_2", "type=float", "categorical_1", "type=keyword")
            .get();

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk();
        bulkRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        for (int i = 0; i < 5; i++) {
            IndexRequest indexRequest = new IndexRequest(sourceIndex);

            // We insert one odd value out of 5 for one feature
            String docId = i == 0 ? "outlier" : "normal" + i;
            indexRequest.id(docId);
            indexRequest.source("numeric_1", i == 0 ? 100.0 : 1.0, "numeric_2", 1.0, "categorical_1", "foo_" + i);
            bulkRequestBuilder.add(indexRequest);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            fail("Failed to index data: " + bulkResponse.buildFailureMessage());
        }

        String id = "test_outlier_detection_with_few_docs";
        DataFrameAnalyticsConfig config = buildAnalytics(id, sourceIndex, sourceIndex + "-results", null,
            new OutlierDetection.Builder().build());
        registerAnalytics(config);
        putAnalytics(config);

        assertIsStopped(id);
        assertProgress(id, 0, 0, 0, 0);

        startAnalytics(id);
        waitUntilAnalyticsIsStopped(id);

        SearchResponse sourceData = client().prepareSearch(sourceIndex).get();
        double scoreOfOutlier = 0.0;
        double scoreOfNonOutlier = -1.0;
        for (SearchHit hit : sourceData.getHits()) {
            GetResponse destDocGetResponse = client().prepareGet().setIndex(config.getDest().getIndex()).setId(hit.getId()).get();
            assertThat(destDocGetResponse.isExists(), is(true));
            Map<String, Object> sourceDoc = hit.getSourceAsMap();
            Map<String, Object> destDoc = destDocGetResponse.getSource();
            for (String field : sourceDoc.keySet()) {
                assertThat(destDoc.containsKey(field), is(true));
                assertThat(destDoc.get(field), equalTo(sourceDoc.get(field)));
            }
            assertThat(destDoc.containsKey("ml"), is(true));

            @SuppressWarnings("unchecked")
            Map<String, Object> resultsObject = (Map<String, Object>) destDoc.get("ml");

            assertThat(resultsObject.containsKey("outlier_score"), is(true));
            double outlierScore = (double) resultsObject.get("outlier_score");
            assertThat(outlierScore, allOf(greaterThanOrEqualTo(0.0), lessThanOrEqualTo(1.0)));
            if (hit.getId().equals("outlier")) {
                scoreOfOutlier = outlierScore;
            } else {
                if (scoreOfNonOutlier < 0) {
                    scoreOfNonOutlier = outlierScore;
                } else {
                    assertThat(outlierScore, equalTo(scoreOfNonOutlier));
                }
            }
        }
        assertThat(scoreOfOutlier, is(greaterThan(scoreOfNonOutlier)));

        assertProgress(id, 100, 100, 100, 100);
        assertThat(searchStoredProgress(id).getHits().getTotalHits().value, equalTo(1L));
        assertThatAuditMessagesMatch(id,
            "Created analytics with analysis type [outlier_detection]",
            "Estimated memory usage for this analytics to be",
            "Starting analytics on node",
            "Started analytics",
            "Creating destination index [test-outlier-detection-with-few-docs-results]",
            "Finished reindexing to destination index [test-outlier-detection-with-few-docs-results]",
            "Finished analysis");
    }

    public void testOutlierDetectionWithEnoughDocumentsToScroll() throws Exception {
        String sourceIndex = "test-outlier-detection-with-enough-docs-to-scroll";

        client().admin().indices().prepareCreate(sourceIndex)
            .addMapping("_doc", "numeric_1", "type=double", "numeric_2", "type=float", "categorical_1", "type=keyword")
            .get();

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk();
        bulkRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        int docCount = randomIntBetween(1024, 2048);
        for (int i = 0; i < docCount; i++) {
            IndexRequest indexRequest = new IndexRequest(sourceIndex);
            indexRequest.source("numeric_1", randomDouble(), "numeric_2", randomFloat(), "categorical_1", randomAlphaOfLength(10));
            bulkRequestBuilder.add(indexRequest);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            fail("Failed to index data: " + bulkResponse.buildFailureMessage());
        }

        String id = "test_outlier_detection_with_enough_docs_to_scroll";
        DataFrameAnalyticsConfig config = buildAnalytics(id, sourceIndex, sourceIndex + "-results", "custom_ml",
            new OutlierDetection.Builder().build());
        registerAnalytics(config);
        putAnalytics(config);

        assertIsStopped(id);
        assertProgress(id, 0, 0, 0, 0);

        startAnalytics(id);
        waitUntilAnalyticsIsStopped(id);

        // Check we've got all docs
        SearchResponse searchResponse = client().prepareSearch(config.getDest().getIndex()).setTrackTotalHits(true).get();
        assertThat(searchResponse.getHits().getTotalHits().value, equalTo((long) docCount));

        // Check they all have an outlier_score
        searchResponse = client().prepareSearch(config.getDest().getIndex())
            .setTrackTotalHits(true)
            .setQuery(QueryBuilders.existsQuery("custom_ml.outlier_score")).get();
        assertThat(searchResponse.getHits().getTotalHits().value, equalTo((long) docCount));

        assertProgress(id, 100, 100, 100, 100);
        assertThat(searchStoredProgress(id).getHits().getTotalHits().value, equalTo(1L));
        assertThatAuditMessagesMatch(id,
            "Created analytics with analysis type [outlier_detection]",
            "Estimated memory usage for this analytics to be",
            "Starting analytics on node",
            "Started analytics",
            "Creating destination index [test-outlier-detection-with-enough-docs-to-scroll-results]",
            "Finished reindexing to destination index [test-outlier-detection-with-enough-docs-to-scroll-results]",
            "Finished analysis");
    }

    public void testOutlierDetectionWithMoreFieldsThanDocValueFieldLimit() throws Exception {
        String sourceIndex = "test-outlier-detection-with-more-fields-than-docvalue-limit";

        client().admin().indices().prepareCreate(sourceIndex).get();

        GetSettingsRequest getSettingsRequest = new GetSettingsRequest();
        getSettingsRequest.indices(sourceIndex);
        getSettingsRequest.names(IndexSettings.MAX_DOCVALUE_FIELDS_SEARCH_SETTING.getKey());
        getSettingsRequest.includeDefaults(true);

        GetSettingsResponse docValueLimitSetting = client().admin().indices().getSettings(getSettingsRequest).actionGet();
        int docValueLimit = IndexSettings.MAX_DOCVALUE_FIELDS_SEARCH_SETTING.get(
            docValueLimitSetting.getIndexToSettings().values().iterator().next().value);

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk();
        bulkRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        for (int i = 0; i < 100; i++) {

            StringBuilder source = new StringBuilder("{");
            for (int fieldCount = 0; fieldCount < docValueLimit + 1; fieldCount++) {
                source.append("\"field_").append(fieldCount).append("\":").append(randomDouble());
                if (fieldCount < docValueLimit) {
                    source.append(",");
                }
            }
            source.append("}");

            IndexRequest indexRequest = new IndexRequest(sourceIndex);
            indexRequest.source(source.toString(), XContentType.JSON);
            bulkRequestBuilder.add(indexRequest);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            fail("Failed to index data: " + bulkResponse.buildFailureMessage());
        }

        String id = "test_outlier_detection_with_more_fields_than_docvalue_limit";
        DataFrameAnalyticsConfig config = buildAnalytics(id, sourceIndex, sourceIndex + "-results", null,
            new OutlierDetection.Builder().build());
        registerAnalytics(config);
        putAnalytics(config);

        assertIsStopped(id);
        assertProgress(id, 0, 0, 0, 0);

        startAnalytics(id);
        waitUntilAnalyticsIsStopped(id);

        SearchResponse sourceData = client().prepareSearch(sourceIndex).get();
        for (SearchHit hit : sourceData.getHits()) {
            GetResponse destDocGetResponse = client().prepareGet().setIndex(config.getDest().getIndex()).setId(hit.getId()).get();
            assertThat(destDocGetResponse.isExists(), is(true));
            Map<String, Object> sourceDoc = hit.getSourceAsMap();
            Map<String, Object> destDoc = destDocGetResponse.getSource();
            for (String field : sourceDoc.keySet()) {
                assertThat(destDoc.containsKey(field), is(true));
                assertThat(destDoc.get(field), equalTo(sourceDoc.get(field)));
            }
            assertThat(destDoc.containsKey("ml"), is(true));

            @SuppressWarnings("unchecked")
            Map<String, Object> resultsObject = (Map<String, Object>) destDoc.get("ml");

            assertThat(resultsObject.containsKey("outlier_score"), is(true));
            double outlierScore = (double) resultsObject.get("outlier_score");
            assertThat(outlierScore, allOf(greaterThanOrEqualTo(0.0), lessThanOrEqualTo(1.0)));
        }

        assertProgress(id, 100, 100, 100, 100);
        assertThat(searchStoredProgress(id).getHits().getTotalHits().value, equalTo(1L));
        assertThatAuditMessagesMatch(id,
            "Created analytics with analysis type [outlier_detection]",
            "Estimated memory usage for this analytics to be",
            "Starting analytics on node",
            "Started analytics",
            "Creating destination index [test-outlier-detection-with-more-fields-than-docvalue-limit-results]",
            "Finished reindexing to destination index [test-outlier-detection-with-more-fields-than-docvalue-limit-results]",
            "Finished analysis");
    }

    public void testStopOutlierDetectionWithEnoughDocumentsToScroll() throws Exception {
        String sourceIndex = "test-stop-outlier-detection-with-enough-docs-to-scroll";

        client().admin().indices().prepareCreate(sourceIndex)
            .addMapping("_doc", "numeric_1", "type=double", "numeric_2", "type=float", "categorical_1", "type=keyword")
            .get();

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk();
        bulkRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        int docCount = randomIntBetween(1024, 2048);
        for (int i = 0; i < docCount; i++) {
            IndexRequest indexRequest = new IndexRequest(sourceIndex);
            indexRequest.source("numeric_1", randomDouble(), "numeric_2", randomFloat(), "categorical_1", randomAlphaOfLength(10));
            bulkRequestBuilder.add(indexRequest);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            fail("Failed to index data: " + bulkResponse.buildFailureMessage());
        }

        String id = "test_stop_outlier_detection_with_enough_docs_to_scroll";
        DataFrameAnalyticsConfig config = buildAnalytics(id, sourceIndex, sourceIndex + "-results", "custom_ml",
            new OutlierDetection.Builder().build());
        registerAnalytics(config);
        putAnalytics(config);

        assertIsStopped(id);
        startAnalytics(id);
        // State here could be any of STARTED, REINDEXING or ANALYZING

        assertThat(stopAnalytics(id).isStopped(), is(true));
        assertIsStopped(id);
        if (indexExists(config.getDest().getIndex()) == false) {
            // We stopped before we even created the destination index
            return;
        }

        SearchResponse searchResponse = client().prepareSearch(config.getDest().getIndex()).setTrackTotalHits(true).get();
        if (searchResponse.getHits().getTotalHits().value == docCount) {
            searchResponse = client().prepareSearch(config.getDest().getIndex())
                .setTrackTotalHits(true)
                .setQuery(QueryBuilders.existsQuery("custom_ml.outlier_score")).get();
            logger.debug("We stopped during analysis: [{}] < [{}]", searchResponse.getHits().getTotalHits().value, docCount);
            assertThat(searchResponse.getHits().getTotalHits().value, lessThan((long) docCount));
        } else {
            logger.debug("We stopped during reindexing: [{}] < [{}]", searchResponse.getHits().getTotalHits().value, docCount);
        }

        assertThatAuditMessagesMatch(id,
            "Created analytics with analysis type [outlier_detection]",
            "Estimated memory usage for this analytics to be",
            "Starting analytics on node",
            "Started analytics",
            "Creating destination index [test-stop-outlier-detection-with-enough-docs-to-scroll-results]",
            "Stopped analytics");
    }

    public void testOutlierDetectionWithMultipleSourceIndices() throws Exception {
        String sourceIndex1 = "test-outlier-detection-with-multiple-source-indices-1";
        String sourceIndex2 = "test-outlier-detection-with-multiple-source-indices-2";
        String destIndex = "test-outlier-detection-with-multiple-source-indices-results";
        String[] sourceIndex = new String[] { sourceIndex1, sourceIndex2 };

        client().admin().indices().prepareCreate(sourceIndex1)
            .addMapping("_doc", "numeric_1", "type=double", "numeric_2", "type=float", "categorical_1", "type=keyword")
            .get();

        client().admin().indices().prepareCreate(sourceIndex2)
            .addMapping("_doc", "numeric_1", "type=double", "numeric_2", "type=float", "categorical_1", "type=keyword")
            .get();

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk();
        bulkRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        for (String index : sourceIndex) {
            for (int i = 0; i < 5; i++) {
                IndexRequest indexRequest = new IndexRequest(index);
                indexRequest.source("numeric_1", randomDouble(), "numeric_2", randomFloat(), "categorical_1", "foo_" + i);
                bulkRequestBuilder.add(indexRequest);
            }
        }
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            fail("Failed to index data: " + bulkResponse.buildFailureMessage());
        }

        String id = "test_outlier_detection_with_multiple_source_indices";
        DataFrameAnalyticsConfig config = new DataFrameAnalyticsConfig.Builder()
            .setId(id)
            .setSource(new DataFrameAnalyticsSource(sourceIndex, null, null))
            .setDest(new DataFrameAnalyticsDest(destIndex, null))
            .setAnalysis(new OutlierDetection.Builder().build())
            .build();
        registerAnalytics(config);
        putAnalytics(config);

        assertIsStopped(id);
        assertProgress(id, 0, 0, 0, 0);

        startAnalytics(id);
        waitUntilAnalyticsIsStopped(id);

        // Check we've got all docs
        SearchResponse searchResponse = client().prepareSearch(config.getDest().getIndex()).setTrackTotalHits(true).get();
        assertThat(searchResponse.getHits().getTotalHits().value, equalTo((long) bulkRequestBuilder.numberOfActions()));

        // Check they all have an outlier_score
        searchResponse = client().prepareSearch(config.getDest().getIndex())
            .setTrackTotalHits(true)
            .setQuery(QueryBuilders.existsQuery("ml.outlier_score")).get();
        assertThat(searchResponse.getHits().getTotalHits().value, equalTo((long) bulkRequestBuilder.numberOfActions()));

        assertProgress(id, 100, 100, 100, 100);
        assertThat(searchStoredProgress(id).getHits().getTotalHits().value, equalTo(1L));
        assertThatAuditMessagesMatch(id,
            "Created analytics with analysis type [outlier_detection]",
            "Estimated memory usage for this analytics to be",
            "Starting analytics on node",
            "Started analytics",
            "Creating destination index [test-outlier-detection-with-multiple-source-indices-results]",
            "Finished reindexing to destination index [test-outlier-detection-with-multiple-source-indices-results]",
            "Finished analysis");
    }

    public void testOutlierDetectionWithPreExistingDestIndex() throws Exception {
        String sourceIndex = "test-outlier-detection-with-pre-existing-dest-index";
        String destIndex = "test-outlier-detection-with-pre-existing-dest-index-results";

        client().admin().indices().prepareCreate(sourceIndex)
            .addMapping("_doc", "numeric_1", "type=double", "numeric_2", "type=float", "categorical_1", "type=keyword")
            .get();

        client().admin().indices().prepareCreate(destIndex)
            .addMapping("_doc", "numeric_1", "type=double", "numeric_2", "type=float", "categorical_1", "type=keyword")
            .get();

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk();
        bulkRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        for (int i = 0; i < 5; i++) {
            IndexRequest indexRequest = new IndexRequest(sourceIndex);
            indexRequest.source("numeric_1", randomDouble(), "numeric_2", randomFloat(), "categorical_1", "foo_" + i);
            bulkRequestBuilder.add(indexRequest);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            fail("Failed to index data: " + bulkResponse.buildFailureMessage());
        }

        String id = "test_outlier_detection_with_pre_existing_dest_index";
        DataFrameAnalyticsConfig config = buildAnalytics(id, sourceIndex, destIndex, null, new OutlierDetection.Builder().build());
        registerAnalytics(config);
        putAnalytics(config);

        assertIsStopped(id);
        assertProgress(id, 0, 0, 0, 0);

        startAnalytics(id);
        waitUntilAnalyticsIsStopped(id);

        // Check we've got all docs
        SearchResponse searchResponse = client().prepareSearch(config.getDest().getIndex()).setTrackTotalHits(true).get();
        assertThat(searchResponse.getHits().getTotalHits().value, equalTo((long) bulkRequestBuilder.numberOfActions()));

        // Check they all have an outlier_score
        searchResponse = client().prepareSearch(config.getDest().getIndex())
            .setTrackTotalHits(true)
            .setQuery(QueryBuilders.existsQuery("ml.outlier_score")).get();
        assertThat(searchResponse.getHits().getTotalHits().value, equalTo((long) bulkRequestBuilder.numberOfActions()));

        assertProgress(id, 100, 100, 100, 100);
        assertThat(searchStoredProgress(id).getHits().getTotalHits().value, equalTo(1L));
        assertThatAuditMessagesMatch(id,
            "Created analytics with analysis type [outlier_detection]",
            "Estimated memory usage for this analytics to be",
            "Starting analytics on node",
            "Started analytics",
            "Using existing destination index [test-outlier-detection-with-pre-existing-dest-index-results]",
            "Finished reindexing to destination index [test-outlier-detection-with-pre-existing-dest-index-results]",
            "Finished analysis");
    }

    public void testModelMemoryLimitLowerThanEstimatedMemoryUsage() throws Exception {
        String sourceIndex = "test-model-memory-limit";

        client().admin().indices().prepareCreate(sourceIndex)
            .addMapping("_doc", "col_1", "type=double", "col_2", "type=float", "col_3", "type=keyword")
            .get();

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk().setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        for (int i = 0; i < 10000; i++) {  // This number of rows should make memory usage estimate greater than 1MB
            IndexRequest indexRequest = new IndexRequest(sourceIndex)
                .id("doc_" + i)
                .source("col_1", 1.0, "col_2", 1.0, "col_3", "str");
            bulkRequestBuilder.add(indexRequest);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            fail("Failed to index data: " + bulkResponse.buildFailureMessage());
        }

        String id = "test_model_memory_limit_lower_than_estimated_memory_usage";
        ByteSizeValue modelMemoryLimit = new ByteSizeValue(1, ByteSizeUnit.MB);
        DataFrameAnalyticsConfig config = new DataFrameAnalyticsConfig.Builder()
            .setId(id)
            .setSource(new DataFrameAnalyticsSource(new String[] { sourceIndex }, null, null))
            .setDest(new DataFrameAnalyticsDest(sourceIndex + "-results", null))
            .setAnalysis(new OutlierDetection.Builder().build())
            .setModelMemoryLimit(modelMemoryLimit)
            .build();

        registerAnalytics(config);
        putAnalytics(config);
        assertIsStopped(id);

        ElasticsearchStatusException exception = expectThrows(ElasticsearchStatusException.class, () -> startAnalytics(id));
        assertThat(exception.status(), equalTo(RestStatus.BAD_REQUEST));
        assertThat(
            exception.getMessage(),
            startsWith("Cannot start because the configured model memory limit [" + modelMemoryLimit +
                "] is lower than the expected memory usage"));

        assertThatAuditMessagesMatch(id,
            "Created analytics with analysis type [outlier_detection]",
            "Estimated memory usage for this analytics to be");
    }

    public void testLazyAssignmentWithModelMemoryLimitTooHighForAssignment() throws Exception {
        String sourceIndex = "test-lazy-assign-model-memory-limit-too-high";

        client().admin().indices().prepareCreate(sourceIndex)
            .addMapping("_doc", "col_1", "type=double", "col_2", "type=float", "col_3", "type=keyword")
            .get();

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk().setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        IndexRequest indexRequest = new IndexRequest(sourceIndex)
            .id("doc_1")
            .source("col_1", 1.0, "col_2", 1.0, "col_3", "str");
        bulkRequestBuilder.add(indexRequest);
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            fail("Failed to index data: " + bulkResponse.buildFailureMessage());
        }

        String id = "test_lazy_assign_model_memory_limit_too_high";
        // Assuming a 1TB job will never fit on the test machine - increase this when machines get really big!
        ByteSizeValue modelMemoryLimit = new ByteSizeValue(1, ByteSizeUnit.TB);
        DataFrameAnalyticsConfig config = new DataFrameAnalyticsConfig.Builder()
            .setId(id)
            .setSource(new DataFrameAnalyticsSource(new String[] { sourceIndex }, null, null))
            .setDest(new DataFrameAnalyticsDest(sourceIndex + "-results", null))
            .setAnalysis(new OutlierDetection.Builder().build())
            .setModelMemoryLimit(modelMemoryLimit)
            .setAllowLazyStart(true)
            .build();

        registerAnalytics(config);
        putAnalytics(config);
        assertIsStopped(id);

        // Due to lazy start being allowed, this should succeed even though no node currently in the cluster is big enough
        startAnalytics(id);

        // Wait until state is STARTING, there is no node but there is an assignment explanation.
        assertBusy(() -> {
            GetDataFrameAnalyticsStatsAction.Response.Stats stats = getAnalyticsStats(id);
            assertThat(stats.getState(), equalTo(DataFrameAnalyticsState.STARTING));
            assertThat(stats.getNode(), is(nullValue()));
            assertThat(stats.getAssignmentExplanation(), containsString("persistent task is awaiting node assignment"));
        });
        stopAnalytics(id);
        waitUntilAnalyticsIsStopped(id);

        assertThatAuditMessagesMatch(id,
            "Created analytics with analysis type [outlier_detection]",
            "Estimated memory usage for this analytics to be",
            "No node found to start analytics. Reasons [persistent task is awaiting node assignment.]",
            "Started analytics",
            "Stopped analytics");
    }

    public void testOutlierDetectionStopAndRestart() throws Exception {
        String sourceIndex = "test-outlier-detection-stop-and-restart";

        client().admin().indices().prepareCreate(sourceIndex)
            .addMapping("_doc", "numeric_1", "type=double", "numeric_2", "type=float", "categorical_1", "type=keyword")
            .get();

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk();
        bulkRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        int docCount = randomIntBetween(1024, 2048);
        for (int i = 0; i < docCount; i++) {
            IndexRequest indexRequest = new IndexRequest(sourceIndex);
            indexRequest.source("numeric_1", randomDouble(), "numeric_2", randomFloat(), "categorical_1", randomAlphaOfLength(10));
            bulkRequestBuilder.add(indexRequest);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            fail("Failed to index data: " + bulkResponse.buildFailureMessage());
        }

        String id = "test_outlier_detection_stop_and_restart";
        DataFrameAnalyticsConfig config = buildAnalytics(id, sourceIndex, sourceIndex + "-results", "custom_ml",
            new OutlierDetection.Builder().build());
        registerAnalytics(config);
        putAnalytics(config);

        assertIsStopped(id);
        startAnalytics(id);

        // Wait until state is one of REINDEXING or ANALYZING, or until it is STOPPED.
        assertBusy(() -> {
            DataFrameAnalyticsState state = getAnalyticsStats(id).getState();
            assertThat(state, is(anyOf(equalTo(DataFrameAnalyticsState.REINDEXING), equalTo(DataFrameAnalyticsState.ANALYZING),
                equalTo(DataFrameAnalyticsState.STOPPED))));
        });
        stopAnalytics(id);
        waitUntilAnalyticsIsStopped(id);

        // Now let's start it again
        try {
            startAnalytics(id);
        } catch (Exception e) {
            if (e.getMessage().equals("Cannot start because the job has already finished")) {
                // That means the job had managed to complete
            } else {
                throw e;
            }
        }

        waitUntilAnalyticsIsStopped(id);

        // Check we've got all docs
        SearchResponse searchResponse = client().prepareSearch(config.getDest().getIndex()).setTrackTotalHits(true).get();
        assertThat(searchResponse.getHits().getTotalHits().value, equalTo((long) docCount));

        // Check they all have an outlier_score
        searchResponse = client().prepareSearch(config.getDest().getIndex())
            .setTrackTotalHits(true)
            .setQuery(QueryBuilders.existsQuery("custom_ml.outlier_score")).get();
        assertThat(searchResponse.getHits().getTotalHits().value, equalTo((long) docCount));

        assertProgress(id, 100, 100, 100, 100);
        assertThat(searchStoredProgress(id).getHits().getTotalHits().value, equalTo(1L));
    }

    public void testOutlierDetectionWithCustomParams() throws Exception {
        String sourceIndex = "test-outlier-detection-with-custom-params";

        client().admin().indices().prepareCreate(sourceIndex)
            .addMapping("_doc", "numeric_1", "type=double", "numeric_2", "type=float", "categorical_1", "type=keyword")
            .get();

        BulkRequestBuilder bulkRequestBuilder = client().prepareBulk();
        bulkRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        for (int i = 0; i < 5; i++) {
            IndexRequest indexRequest = new IndexRequest(sourceIndex);

            // We insert one odd value out of 5 for one feature
            String docId = i == 0 ? "outlier" : "normal" + i;
            indexRequest.id(docId);
            indexRequest.source("numeric_1", i == 0 ? 100.0 : 1.0, "numeric_2", 1.0, "categorical_1", "foo_" + i);
            bulkRequestBuilder.add(indexRequest);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            fail("Failed to index data: " + bulkResponse.buildFailureMessage());
        }

        String id = "test_outlier_detection_with_custom_params";
        DataFrameAnalyticsConfig config = buildAnalytics(id, sourceIndex, sourceIndex + "-results", null,
            new OutlierDetection.Builder()
                .setNNeighbors(3)
                .setMethod(OutlierDetection.Method.DISTANCE_KNN)
                .setFeatureInfluenceThreshold(0.01)
                .setComputeFeatureInfluence(false)
                .setOutlierFraction(0.04)
                .setStandardizationEnabled(true)
                .build());
        registerAnalytics(config);
        putAnalytics(config);

        assertIsStopped(id);
        assertProgress(id, 0, 0, 0, 0);

        startAnalytics(id);
        waitUntilAnalyticsIsStopped(id);

        SearchResponse sourceData = client().prepareSearch(sourceIndex).get();
        double scoreOfOutlier = 0.0;
        double scoreOfNonOutlier = -1.0;
        for (SearchHit hit : sourceData.getHits()) {
            GetResponse destDocGetResponse = client().prepareGet().setIndex(config.getDest().getIndex()).setId(hit.getId()).get();
            assertThat(destDocGetResponse.isExists(), is(true));
            Map<String, Object> sourceDoc = hit.getSourceAsMap();
            Map<String, Object> destDoc = destDocGetResponse.getSource();
            for (String field : sourceDoc.keySet()) {
                assertThat(destDoc.containsKey(field), is(true));
                assertThat(destDoc.get(field), equalTo(sourceDoc.get(field)));
            }
            assertThat(destDoc.containsKey("ml"), is(true));

            @SuppressWarnings("unchecked")
            Map<String, Object> resultsObject = (Map<String, Object>) destDoc.get("ml");

            assertThat(resultsObject.containsKey("outlier_score"), is(true));
            assertThat(resultsObject.containsKey("feature_influence"), is(false));

            double outlierScore = (double) resultsObject.get("outlier_score");
            assertThat(outlierScore, allOf(greaterThanOrEqualTo(0.0), lessThanOrEqualTo(1.0)));
            if (hit.getId().equals("outlier")) {
                scoreOfOutlier = outlierScore;
            } else {
                if (scoreOfNonOutlier < 0) {
                    scoreOfNonOutlier = outlierScore;
                } else {
                    assertThat(outlierScore, equalTo(scoreOfNonOutlier));
                }
            }
        }
        assertThat(scoreOfOutlier, is(greaterThan(scoreOfNonOutlier)));

        assertProgress(id, 100, 100, 100, 100);
        assertThat(searchStoredProgress(id).getHits().getTotalHits().value, equalTo(1L));
        assertThatAuditMessagesMatch(id,
            "Created analytics with analysis type [outlier_detection]",
            "Estimated memory usage for this analytics to be",
            "Starting analytics on node",
            "Started analytics",
            "Creating destination index [test-outlier-detection-with-custom-params-results]",
            "Finished reindexing to destination index [test-outlier-detection-with-custom-params-results]",
            "Finished analysis");
    }
}
