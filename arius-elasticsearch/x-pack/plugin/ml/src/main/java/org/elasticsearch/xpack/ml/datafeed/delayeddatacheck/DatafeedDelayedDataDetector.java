/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.datafeed.delayeddatacheck;

import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xpack.core.action.util.PageParams;
import org.elasticsearch.xpack.core.ml.action.GetBucketsAction;
import org.elasticsearch.xpack.core.ml.datafeed.extractor.ExtractorUtils;
import org.elasticsearch.xpack.core.ml.job.results.Bucket;
import org.elasticsearch.xpack.core.ml.utils.Intervals;
import org.elasticsearch.xpack.ml.datafeed.delayeddatacheck.DelayedDataDetectorFactory.BucketWithMissingData;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.xpack.core.ClientHelper.ML_ORIGIN;


/**
 * This class will search the buckets and indices over a given window to determine if any data is missing
 */
public class DatafeedDelayedDataDetector implements DelayedDataDetector {

    private static final String DATE_BUCKETS = "date_buckets";

    private final long bucketSpan;
    private final long window;
    private final Client client;
    private final String timeField;
    private final String jobId;
    private final QueryBuilder datafeedQuery;
    private final String[] datafeedIndices;

    DatafeedDelayedDataDetector(long bucketSpan, long window, String jobId, String timeField, QueryBuilder datafeedQuery,
                                String[] datafeedIndices, Client client) {
        this.bucketSpan = bucketSpan;
        this.window = window;
        this.jobId = jobId;
        this.timeField = timeField;
        this.datafeedQuery = datafeedQuery;
        this.datafeedIndices = datafeedIndices;
        this.client = client;
    }

    /**
     * This method looks at the {@link DatafeedDelayedDataDetector#datafeedIndices}
     * from {@code latestFinalizedBucket - window} to {@code latestFinalizedBucket} and compares the document counts with the
     * {@link DatafeedDelayedDataDetector#jobId}'s finalized buckets' event counts.
     *
     * It is done synchronously, and can block for a considerable amount of time, it should only be executed within the appropriate
     * thread pool.
     *
     * @param latestFinalizedBucketMs The latest finalized bucket timestamp in milliseconds, signifies the end of the time window check
     * @return A List of {@link BucketWithMissingData} objects that contain each bucket with the current number of missing docs
     */
    @Override
    public List<BucketWithMissingData> detectMissingData(long latestFinalizedBucketMs) {
        final long end = Intervals.alignToFloor(latestFinalizedBucketMs, bucketSpan);
        final long start = Intervals.alignToFloor(latestFinalizedBucketMs - window, bucketSpan);

        if (end <= start) {
            return Collections.emptyList();
        }

        List<Bucket> finalizedBuckets = checkBucketEvents(start, end);
        Map<Long, Long> indexedData = checkCurrentBucketEventCount(start, end);
        return finalizedBuckets.stream()
            // We only care about the situation when data is added to the indices
            // Older data could have been removed from the indices, and should not be considered "missing data"
            .filter(bucket -> calculateMissing(indexedData, bucket) > 0)
            .map(bucket -> BucketWithMissingData.fromMissingAndBucket(calculateMissing(indexedData, bucket), bucket))
            .collect(Collectors.toList());
    }

    @Override
    public long getWindow() {
        return window;
    }

    private List<Bucket> checkBucketEvents(long start, long end) {
        GetBucketsAction.Request request = new GetBucketsAction.Request(jobId);
        request.setStart(Long.toString(start));
        request.setEnd(Long.toString(end));
        request.setSort("timestamp");
        request.setDescending(false);
        request.setExcludeInterim(true);
        request.setPageParams(new PageParams(0, (int)((end - start)/bucketSpan)));

        try (ThreadContext.StoredContext ignore = client.threadPool().getThreadContext().stashWithOrigin(ML_ORIGIN)) {
            GetBucketsAction.Response response = client.execute(GetBucketsAction.INSTANCE, request).actionGet();
            return response.getBuckets().results();
        }
    }

    private Map<Long, Long> checkCurrentBucketEventCount(long start, long end) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(0)
            .aggregation(new DateHistogramAggregationBuilder(DATE_BUCKETS)
                .fixedInterval(new DateHistogramInterval(bucketSpan + "ms")).field(timeField))
            .query(ExtractorUtils.wrapInTimeRangeQuery(datafeedQuery, timeField, start, end));

        SearchRequest searchRequest = new SearchRequest(datafeedIndices).source(searchSourceBuilder);
        try (ThreadContext.StoredContext ignore = client.threadPool().getThreadContext().stashWithOrigin(ML_ORIGIN)) {
            SearchResponse response = client.execute(SearchAction.INSTANCE, searchRequest).actionGet();
            List<? extends Histogram.Bucket> buckets = ((Histogram)response.getAggregations().get(DATE_BUCKETS)).getBuckets();
            Map<Long, Long> hashMap = new HashMap<>(buckets.size());
            for (Histogram.Bucket bucket : buckets) {
                long bucketTime = toHistogramKeyToEpoch(bucket.getKey());
                if (bucketTime < 0) {
                    throw new IllegalStateException("Histogram key [" + bucket.getKey() + "] cannot be converted to a timestamp");
                }
                hashMap.put(bucketTime, bucket.getDocCount());
            }
            return hashMap;
        }
    }

    private static long toHistogramKeyToEpoch(Object key) {
        if (key instanceof ZonedDateTime) {
            return ((ZonedDateTime)key).toInstant().toEpochMilli();
        } else if (key instanceof Double) {
            return ((Double)key).longValue();
        } else if (key instanceof Long){
            return (Long)key;
        } else {
            return -1L;
        }
    }

    private static long calculateMissing(Map<Long, Long> indexedData, Bucket bucket) {
        return indexedData.getOrDefault(bucket.getEpoch() * 1000, 0L) - bucket.getEventCount();
    }
}
