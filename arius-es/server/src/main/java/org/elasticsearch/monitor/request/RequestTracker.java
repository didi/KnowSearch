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

package org.elasticsearch.monitor.request;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.TransportBulkAction;
import org.elasticsearch.action.search.TransportSearchAction;
import org.elasticsearch.common.metrics.MeanMetric;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.common.util.concurrent.ConcurrentMapLong;
import org.elasticsearch.search.internal.SearchContext;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.Transport;
import org.elasticsearch.transport.TransportResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * author weizijun
 * date：2019-07-19
 */
public class RequestTracker {
    private static final Logger logger = LogManager.getLogger(RequestTracker.class);

    public final static String REQUEST_ID = "requestId";
    private static final long IO_SLOW_LOG_THRESHOLD = 1000;

    public static final long TRACE_THRESHOLD = 10;
    public static final long INDEX_SEARCH_SLOWLOG_THRESHOLD_DEBUG = 200;
    public static final long INDEX_SEARCH_SLOWLOG_THRESHOLD_INFO = 500;
    public static final long INDEX_SEARCH_SLOWLOG_THRESHOLD_WARN = 1000;

    public static final long INDEX_BULK_SLOWLOG_THRESHOLD_DEBUG = 500;
    public static final long INDEX_BULK_SLOWLOG_THRESHOLD_INFO = 2000;
    public static final long INDEX_BULK_SLOWLOG_THRESHOLD_WARN = 5000;

    public static final long TRANSPORT_HANDLES_TIME_THRESHOLD = 30000;
    public static final long TRANSPORT_HANDLES_COUNT_THRESHOLD = 5;

    private final static MeanMetric ioTcpCostMetric = new MeanMetric();
    private final static MeanMetric ioHttpCostMetric = new MeanMetric();
    private final static MeanMetric searchCostMetric = new MeanMetric();
    private final static MeanMetric searchShardsCountMetric = new MeanMetric();
    private final static MeanMetric bulkCostMetric = new MeanMetric();
    private final static MeanMetric bulkShardsCountMetric = new MeanMetric();
    private static Map<String, Map<String, String>> indexErrorMap = new ConcurrentHashMap<>();
    private static Object indexErrorLock = new Object();

    private final static ConcurrentMapLong<Transport.ResponseContext<? extends TransportResponse>> transportHandlers = ConcurrentCollections
        .newConcurrentMapLongWithAggressiveConcurrency();

    public static final Setting<Long> IO_SLOW_LOG_THRESHOLD_SETTINGS = Setting.longSetting("request.tracker.io.threshold", IO_SLOW_LOG_THRESHOLD, 0L, Setting.Property.Dynamic, Setting.Property.NodeScope);

    public static final Setting<Long> INDEX_SEARCH_SLOWLOG_THRESHOLD_DEBUG_SETTINGS = Setting.longSetting("request.tracker.search.threshold.debug", INDEX_SEARCH_SLOWLOG_THRESHOLD_DEBUG, 0L, Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<Long> INDEX_SEARCH_SLOWLOG_THRESHOLD_INFO_SETTINGS = Setting.longSetting("request.tracker.search.threshold.info", INDEX_SEARCH_SLOWLOG_THRESHOLD_INFO, 0L, Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<Long> INDEX_SEARCH_SLOWLOG_THRESHOLD_WARN_SETTINGS = Setting.longSetting("request.tracker.search.threshold.warn", INDEX_SEARCH_SLOWLOG_THRESHOLD_WARN, 0L, Setting.Property.Dynamic, Setting.Property.NodeScope);

    public static final Setting<Long> INDEX_BULK_SLOWLOG_THRESHOLD_DEBUG_SETTINGS = Setting.longSetting("request.tracker.bulk.threshold.debug", INDEX_BULK_SLOWLOG_THRESHOLD_DEBUG, 0L, Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<Long> INDEX_BULK_SLOWLOG_THRESHOLD_INFO_SETTINGS = Setting.longSetting("request.tracker.bulk.threshold.info", INDEX_BULK_SLOWLOG_THRESHOLD_INFO, 0L, Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<Long> INDEX_BULK_SLOWLOG_THRESHOLD_WARN_SETTINGS = Setting.longSetting("request.tracker.bulk.threshold.warn", INDEX_BULK_SLOWLOG_THRESHOLD_WARN, 0L, Setting.Property.Dynamic, Setting.Property.NodeScope);

    public static final Setting<Long> TRANSPORT_HANDLES_TIME_THRESHOLD_SETTINGS = Setting.longSetting("request.tracker.transport.handles.threshold", TRANSPORT_HANDLES_TIME_THRESHOLD, 0L, Setting.Property.Dynamic, Setting.Property.NodeScope);

    private long ioSlowlogThreshold = IO_SLOW_LOG_THRESHOLD;

    private long searchWarnThreshold = INDEX_SEARCH_SLOWLOG_THRESHOLD_WARN;
    private long searchInfoThreshold = INDEX_SEARCH_SLOWLOG_THRESHOLD_INFO;
    private long searchDebugThreshold = INDEX_SEARCH_SLOWLOG_THRESHOLD_DEBUG;

    private long indexWarnThreshold = INDEX_BULK_SLOWLOG_THRESHOLD_WARN;
    private long indexInfoThreshold = INDEX_BULK_SLOWLOG_THRESHOLD_INFO;
    private long indexDebugThreshold = INDEX_BULK_SLOWLOG_THRESHOLD_DEBUG;

    private long transportHandlesTimeThreshold = TRANSPORT_HANDLES_TIME_THRESHOLD;

    private RequestStats requestStats;

    private static RequestTracker instance = new RequestTracker();

    public static RequestTracker getInstance() {
        return instance;
    }

    private RequestTracker (){
        requestStats = new RequestStats();
    }

    public void init(Settings settings, ClusterSettings clusterSettings , ThreadPool threadPool) {
        this.ioSlowlogThreshold = IO_SLOW_LOG_THRESHOLD_SETTINGS.get(settings);

        this.searchDebugThreshold = INDEX_SEARCH_SLOWLOG_THRESHOLD_DEBUG_SETTINGS.get(settings);
        this.searchInfoThreshold = INDEX_SEARCH_SLOWLOG_THRESHOLD_INFO_SETTINGS.get(settings);
        this.searchWarnThreshold = INDEX_SEARCH_SLOWLOG_THRESHOLD_WARN_SETTINGS.get(settings);

        this.indexDebugThreshold = INDEX_BULK_SLOWLOG_THRESHOLD_DEBUG_SETTINGS.get(settings);
        this.indexInfoThreshold = INDEX_BULK_SLOWLOG_THRESHOLD_INFO_SETTINGS.get(settings);
        this.indexWarnThreshold = INDEX_BULK_SLOWLOG_THRESHOLD_WARN_SETTINGS.get(settings);

        transportHandlesTimeThreshold = TRANSPORT_HANDLES_TIME_THRESHOLD_SETTINGS.get(settings);

        clusterSettings.addSettingsUpdateConsumer(IO_SLOW_LOG_THRESHOLD_SETTINGS, (threshold) -> this.ioSlowlogThreshold = threshold);
        clusterSettings.addSettingsUpdateConsumer(INDEX_SEARCH_SLOWLOG_THRESHOLD_DEBUG_SETTINGS, (threshold) -> {
            this.searchDebugThreshold = threshold;
        });
        clusterSettings.addSettingsUpdateConsumer(INDEX_SEARCH_SLOWLOG_THRESHOLD_INFO_SETTINGS, (threshold) -> {
            this.searchInfoThreshold = threshold;
        });
        clusterSettings.addSettingsUpdateConsumer(INDEX_SEARCH_SLOWLOG_THRESHOLD_WARN_SETTINGS, (threshold) -> {
            this.searchWarnThreshold = threshold;
        });
        clusterSettings.addSettingsUpdateConsumer(INDEX_BULK_SLOWLOG_THRESHOLD_DEBUG_SETTINGS, (threshold) -> {
            this.indexDebugThreshold = threshold;
        });
        clusterSettings.addSettingsUpdateConsumer(INDEX_BULK_SLOWLOG_THRESHOLD_INFO_SETTINGS, (threshold) -> {
            this.indexInfoThreshold = threshold;
        });
        clusterSettings.addSettingsUpdateConsumer(INDEX_BULK_SLOWLOG_THRESHOLD_WARN_SETTINGS, (threshold) -> {
            this.indexWarnThreshold = threshold;
        });
        clusterSettings.addSettingsUpdateConsumer(TRANSPORT_HANDLES_TIME_THRESHOLD_SETTINGS, (threshold) -> {
            this.transportHandlesTimeThreshold = threshold;
        });

        threadPool.scheduleWithFixedDelay(() -> {
            requestStats.stats(ioTcpCostMetric, ioHttpCostMetric, searchCostMetric, searchShardsCountMetric, bulkCostMetric, bulkShardsCountMetric);
            logIndexError();
            logSlowHandler();
        }, TimeValue.timeValueMinutes(1), ThreadPool.Names.GENERIC);
    }


    public void addIndexErrorLog(String pipelineId, BulkItemResponse[] bulkItemResponses) {
        if (pipelineId == null) {
            return;
        }

        for (BulkItemResponse bulkItemResponse : bulkItemResponses) {
            if (bulkItemResponse != null && bulkItemResponse.isFailed()) {
                Exception exception = bulkItemResponse.getFailure().getCause();
                String excepName;
                if (exception != null) {
                    excepName = exception.getClass().getName();
                } else {
                    excepName = "null";
                }
                addIndexErrorLog(pipelineId, excepName, buildErrMsg(pipelineId, excepName, bulkItemResponse.getFailureMessage()));
            }
        }
    }

    public void addIndexErrorLog(String pipeLineId, String exception, String msg) {
        synchronized (indexErrorLock) {
            if (!indexErrorMap.containsKey(pipeLineId)) {
                indexErrorMap.put(pipeLineId, new ConcurrentHashMap<>());
            }

            Map<String, String> ppiMap = indexErrorMap.get(pipeLineId);
            if (ppiMap.containsKey(exception)) {
                return;
            }

            ppiMap.put(exception, msg);
        }
    }

    public void logIndexError() {
        Map<String, Map<String, String>> errMap = null;

        synchronized (indexErrorLock) {
            errMap = indexErrorMap;
            indexErrorMap = new ConcurrentHashMap<>();
        }

        for(String ppi : errMap.keySet()) {
            for(String exeption : errMap.get(ppi).keySet()) {
                logger.info(errMap.get(ppi).get(exeption));
            }
        }
    }

    public void logTcpRequest(String outRequestId, long requestId, String action, boolean isRequest, String peer, long start) {
        long cost = System.currentTimeMillis() - start;
        ioTcpCostMetric.inc(cost);
        if (logger.isWarnEnabled()) {
            if (cost > ioSlowlogThreshold) {
                logger.warn("tcp cost too long||outRequestId={}||requestId={}||action={}||isRequest={}||peer={}||cost={}",
                    outRequestId, requestId, action, isRequest, peer, cost);
            }
        }
    }

    public void logHttpRequest(String requestId, String uri, String peer, long start) {
        long cost = System.currentTimeMillis() - start;
        ioHttpCostMetric.inc(cost);
        if (logger.isWarnEnabled()) {
            if (cost > ioSlowlogThreshold) {
                logger.warn("http cost too long||cost={}||requestId={}||uri={}||peer={}", cost, requestId, uri, peer);
            }
        }
    }

    public void logSearchRequest(final ActionRequest request, final TransportSearchAction.SearchTimeProvider timeProvider, long taskId, long cost) {
        searchCostMetric.inc(cost);
        searchShardsCountMetric.inc(timeProvider.getShardsSize());
        long current = System.currentTimeMillis();
        String requsetId = request.getHeader(REQUEST_ID);
        if (logger.isTraceEnabled() && cost > TRACE_THRESHOLD) {
            logger.trace(timeProvider.recordCost(requsetId, taskId, current, cost));
        } else if (logger.isDebugEnabled() && cost > searchDebugThreshold) {
            logger.debug(timeProvider.recordCost(requsetId, taskId, current, cost));
        } else if (logger.isInfoEnabled() && cost > searchInfoThreshold) {
            logger.info(timeProvider.recordCost(requsetId, taskId, current, cost));
        } else if (logger.isWarnEnabled() && cost > searchWarnThreshold) {
            logger.warn(timeProvider.recordCost(requsetId, taskId, current, cost));
        }
    }

    public void logIndexRequest(final BulkRequest request, final TransportBulkAction.IndexTimeProvider timeProvider) {
        bulkCostMetric.inc(timeProvider.getTotalCost());
        bulkShardsCountMetric.inc(timeProvider.getShardBulkCost().size());
        String requsetId = request.getHeader(REQUEST_ID);
        if (logger.isTraceEnabled() && timeProvider.getTotalCost() > TRACE_THRESHOLD) {
            logger.trace(timeProvider.recordCost(request, requsetId));
        } else if (logger.isDebugEnabled() && timeProvider.getTotalCost() > indexDebugThreshold) {
            logger.debug(timeProvider.recordCost(request, requsetId));
        } else if (logger.isInfoEnabled() && timeProvider.getTotalCost() > indexInfoThreshold) {
            logger.info(timeProvider.recordCost(request, requsetId));
        } else if (logger.isWarnEnabled() && timeProvider.getTotalCost() > indexWarnThreshold) {
            logger.warn(timeProvider.recordCost(request, requsetId));
        }
    }

    public void logActionRequest(String action, long requestId, long taskId, long start, long executeStart, long end) {
        if (!action.startsWith("indices:data/read") && !action.startsWith("indices:data/write")) {
            return;
        }

        long waitTime = executeStart - start;
        long totalTime = end - start;
        if (logger.isTraceEnabled() && totalTime > TRACE_THRESHOLD) {
            logger.trace("actionStats||action={}||requestId={}||taskId={}||waitTime={}||totalTime={}", action, requestId, taskId, waitTime, totalTime);
        } else if (logger.isDebugEnabled() && totalTime > indexDebugThreshold) {
            logger.debug("actionStats||action={}||requestId={}||taskId={}||waitTime={}||totalTime={}", action, requestId, taskId, waitTime, totalTime);
        } else if (logger.isInfoEnabled() && totalTime > indexInfoThreshold) {
            logger.info("actionStats||action={}||requestId={}||taskId={}||waitTime={}||totalTime={}", action, requestId, taskId, waitTime, totalTime);
        } else if (logger.isWarnEnabled() && totalTime > indexWarnThreshold) {
            logger.warn("actionStats||action={}||requestId={}||taskId={}||waitTime={}||totalTime={}", action, requestId, taskId, waitTime, totalTime);
        }
    }

    public void addTransportHandler(long requestId, Transport.ResponseContext context) {
        transportHandlers.put(requestId, context);
    }

    public void removeTransportHandler(long requestId) {
        transportHandlers.remove(requestId);
    }

    public void logSlowHandler() {
        PriorityQueue<Transport.ResponseContext> priorityQueue = getSlowQueue();
        logger.info("slowTransportHandler||count={}", priorityQueue.size());

        int topCount = 0;
        long now = System.currentTimeMillis();
        Transport.ResponseContext context = priorityQueue.poll();
        while (context != null && topCount < TRANSPORT_HANDLES_COUNT_THRESHOLD) {
            logger.info("topSlowTransportHandler||top={}||action={}||cost={}", topCount, context.action(), now - context.getCreateTime());
            context = priorityQueue.poll();
            topCount++;
        }
    }

    public PriorityQueue<Transport.ResponseContext> getSlowQueue() {
        PriorityQueue<Transport.ResponseContext> priorityQueue = new PriorityQueue<>((o1, o2) -> (int) (o1.getCreateTime() - o2.getCreateTime()));
        long now = System.currentTimeMillis();
        transportHandlers.forEach((requestId, context) -> {
            if ((now - context.getCreateTime()) > transportHandlesTimeThreshold) {
                priorityQueue.add(context);
            }
        });

        return priorityQueue;
    }

    public static void registerHeader(Map<String, List<String>> headers, ActionRequest actionRequest) {
        if (headers.get(REQUEST_ID) != null) {
            actionRequest.putHeader(REQUEST_ID, header(headers.get(REQUEST_ID)));
        }
    }

    public static String header(List<String> values) {
        if (values != null && values.isEmpty() == false) {
            return values.get(0);
        }
        return null;
    }

    public static long getParentTaskId(SearchContext searchContext) {
        if (searchContext.getTask() == null) {
            return 0;
        }

        if (searchContext.getTask().getParentTaskId() == null) {
            return 0;
        }

        return searchContext.getTask().getParentTaskId().getId();
    }

    private String buildErrMsg(String pipelineId, String exception, String errMsg) {
        return String.format(Locale.ROOT, "indexError||pipelineId=%s||exception=%s||errMsg=%s", pipelineId, exception, errMsg);
    }

}
