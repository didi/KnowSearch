/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.transform.integration;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.xpack.core.transform.transforms.TransformIndexerStats;
import org.elasticsearch.xpack.core.transform.transforms.TransformStoredDoc;
import org.elasticsearch.xpack.core.transform.transforms.persistence.TransformInternalIndexConstants;
import org.junit.Before;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.xpack.core.transform.TransformField.INDEX_DOC_TYPE;
import static org.elasticsearch.xpack.transform.TransformFeatureSet.PROVIDED_STATS;

public class TransformUsageIT extends TransformRestTestCase {

    @Before
    public void createIndexes() throws IOException {
        createReviewsIndex();
    }

    public void testUsage() throws Exception {
        Response usageResponse = client().performRequest(new Request("GET", "_xpack/usage"));

        Map<?, ?> usageAsMap = entityAsMap(usageResponse);
        assertTrue((boolean) XContentMapValues.extractValue("transform.available", usageAsMap));
        assertTrue((boolean) XContentMapValues.extractValue("transform.enabled", usageAsMap));
        // no transforms, no stats
        assertEquals(null, XContentMapValues.extractValue("transform.transforms", usageAsMap));
        assertEquals(null, XContentMapValues.extractValue("transform.stats", usageAsMap));

        // create transforms
        createPivotReviewsTransform("test_usage", "pivot_reviews", null);
        createPivotReviewsTransform("test_usage_no_stats", "pivot_reviews_no_stats", null);
        createContinuousPivotReviewsTransform("test_usage_continuous", "pivot_reviews_continuous", null);
        usageResponse = client().performRequest(new Request("GET", "_xpack/usage"));
        usageAsMap = entityAsMap(usageResponse);
        assertEquals(3, XContentMapValues.extractValue("transform.transforms._all", usageAsMap));
        assertEquals(3, XContentMapValues.extractValue("transform.transforms.stopped", usageAsMap));

        startAndWaitForTransform("test_usage", "pivot_reviews");
        stopTransform("test_usage", false);

        Request statsExistsRequest = new Request(
            "GET",
            TransformInternalIndexConstants.LATEST_INDEX_NAME
                + "/_search?q="
                + INDEX_DOC_TYPE.getPreferredName()
                + ":"
                + TransformStoredDoc.NAME
        );
        // Verify that we have one stat document
        assertBusy(() -> {
            Map<String, Object> hasStatsMap = entityAsMap(client().performRequest(statsExistsRequest));
            assertEquals(1, XContentMapValues.extractValue("hits.total", hasStatsMap));
        });

        startAndWaitForContinuousTransform("test_usage_continuous", "pivot_reviews_continuous", null);

        Request getRequest = new Request("GET", getTransformEndpoint() + "test_usage/_stats");
        Map<String, Object> stats = entityAsMap(client().performRequest(getRequest));
        Map<String, Integer> expectedStats = new HashMap<>();
        for (String statName : PROVIDED_STATS) {
            @SuppressWarnings("unchecked")
            List<Integer> specificStatistic = ((List<Integer>) XContentMapValues.extractValue("transforms.stats." + statName, stats));
            assertNotNull(specificStatistic);
            Integer statistic = (specificStatistic).get(0);
            expectedStats.put(statName, statistic);
        }

        // Simply because we wait for continuous to reach checkpoint 1, does not mean that the statistics are written yet.
        // Since we search against the indices for the statistics, we need to ensure they are written, so we will wait for that
        // to be the case.
        assertBusy(() -> {
            Response response = client().performRequest(new Request("GET", "_xpack/usage"));
            Map<String, Object> statsMap = entityAsMap(response);
            // we should see some stats
            assertEquals(3, XContentMapValues.extractValue("transform.transforms._all", statsMap));
            assertEquals(2, XContentMapValues.extractValue("transform.transforms.stopped", statsMap));
            assertEquals(1, XContentMapValues.extractValue("transform.transforms.started", statsMap));
            for (String statName : PROVIDED_STATS) {
                if (statName.equals(TransformIndexerStats.INDEX_TIME_IN_MS.getPreferredName())
                    || statName.equals(TransformIndexerStats.SEARCH_TIME_IN_MS.getPreferredName())) {
                    continue;
                }
                assertEquals(
                    "Incorrect stat " + statName,
                    expectedStats.get(statName) * 2,
                    XContentMapValues.extractValue("transform.stats." + statName, statsMap)
                );
            }
            // Refresh the index so that statistics are searchable
            refreshIndex(TransformInternalIndexConstants.LATEST_INDEX_VERSIONED_NAME);
        }, 60, TimeUnit.SECONDS);

        stopTransform("test_usage_continuous", false);

        usageResponse = client().performRequest(new Request("GET", "_xpack/usage"));
        usageAsMap = entityAsMap(usageResponse);

        assertEquals(3, XContentMapValues.extractValue("transform.transforms._all", usageAsMap));
        assertEquals(3, XContentMapValues.extractValue("transform.transforms.stopped", usageAsMap));
    }
}
