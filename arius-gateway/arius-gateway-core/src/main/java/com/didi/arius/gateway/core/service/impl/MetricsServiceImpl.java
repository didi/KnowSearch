package com.didi.arius.gateway.core.service.impl;

import com.didi.arius.gateway.common.event.ActionPostResponseEvent;
import com.didi.arius.gateway.common.event.PostResponseEvent;
import com.didi.arius.gateway.common.event.QueryPostResponseEvent;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metrics.IndexMetrics;
import com.didi.arius.gateway.common.metrics.QueryMetrics;
import com.didi.arius.gateway.core.service.MetricsService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author fitz
 * @date 2021/5/31 12:08 下午
 */
@Service
public class MetricsServiceImpl implements MetricsService, ApplicationListener<PostResponseEvent> {
    private ConcurrentMap<Integer, QueryMetrics> appidMetricsMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, IndexMetrics> indexMetricsMap = new ConcurrentHashMap<>();

    @Override
    public void addQueryCost(int appid, long cost) {
        QueryMetrics queryMetrics = getQueryMetrics(appid);
        queryMetrics.incrCost(cost);
    }

    @Override
    public void addSlowlogCost(int appid, long cost) {
        QueryMetrics queryMetrics = getQueryMetrics(appid);
        queryMetrics.incrSlowlogCost(cost);
    }

    @Override
    public void addSearchResponseMetrics(int appid, long tookInMillis, long totalHits, int totalShards, int failedShards) {
        QueryMetrics queryMetrics = getQueryMetrics(appid);
        queryMetrics.incrSearchResponseMetrics(tookInMillis, totalHits, totalShards, failedShards);
    }

    @Override
    public void addReqeustLength(int appid, long length) {
        QueryMetrics queryMetrics = getQueryMetrics(appid);
        queryMetrics.incrReqeustLength(length);
    }

    @Override
    public void addResponseLength(int appid, long length) {
        QueryMetrics queryMetrics = getQueryMetrics(appid);
        queryMetrics.incrResponseLength(length);
    }

    @Override
    public void addQueryMetrics(int appid, long cost, long requestLength, long responseLength) {
        QueryMetrics queryMetrics = getQueryMetrics(appid);
        queryMetrics.incrCost(cost);
        queryMetrics.incrReqeustLength(requestLength);
        queryMetrics.incrResponseLength(responseLength);
    }

    @Override
    public void addIndexMetrics(String index, String operation, long cost, long requestLength, long responseLength) {
        IndexMetrics indexMetrics = getIndexMetrics(index, operation);
        indexMetrics.incrCost(cost);
        indexMetrics.incrReqeustLength(requestLength);
        indexMetrics.incrResponseLength(responseLength);
    }

    @Override
    public void incrQueryAggs(int appid) {
        QueryMetrics queryMetrics = getQueryMetrics(appid);
        queryMetrics.incrAggs();
    }

    @Override
    public void onApplicationEvent(PostResponseEvent postResponseEvent) {
        if(postResponseEvent instanceof ActionPostResponseEvent){
            ActionPostResponseEvent actionPostResponseEvent = (ActionPostResponseEvent)postResponseEvent;
            ActionContext actionContext = actionPostResponseEvent.getActionContext();

            if (null != actionContext) {
                addQueryMetrics(actionContext.getAppid(), actionContext.getCostTime(), actionContext.getRequestLength(), actionContext.getResponseLength());
            }
        }

        if(postResponseEvent instanceof QueryPostResponseEvent){
            QueryPostResponseEvent queryPostResponseEvent = (QueryPostResponseEvent)postResponseEvent;
            QueryContext queryContext = queryPostResponseEvent.getQueryContext();

            if(null != queryContext){
                dealResponse(queryContext);
            }
        }

    }

    private void dealResponse(QueryContext queryContext) {
        int responseBodyLen = queryContext.getResponse() != null ? queryContext.getResponse().content().length() : 0;
        if (queryContext.isDetailLog()) {
            if (queryContext.getCostTime() > queryContext.getRequestSlowlogThresholdMills()) {
                addSlowlogCost(queryContext.getAppid(), queryContext.getCostTime());
            }
            addQueryMetrics(queryContext.getAppid(), queryContext.getCostTime(), queryContext.getPostBody().length(), responseBodyLen);
        }
    }

    /************************************************************** private method **************************************************************/
    private QueryMetrics getQueryMetrics(int appid) {
        QueryMetrics queryMetrics = appidMetricsMap.get(appid);
        if (queryMetrics == null) {
            synchronized (appidMetricsMap) {
                queryMetrics = appidMetricsMap.get(appid);
                if (queryMetrics == null) {
                    queryMetrics = new QueryMetrics(appid);
                    appidMetricsMap.putIfAbsent(appid, queryMetrics);
                }
            }
        }

        return queryMetrics;
    }

    private IndexMetrics getIndexMetrics(String index, String operation) {
        IndexMetrics indexMetrics = indexMetricsMap.get(index + operation);
        if (indexMetrics == null) {
            synchronized (indexMetricsMap) {
                indexMetrics = indexMetricsMap.get(index + operation);
                if (indexMetrics == null) {
                    indexMetrics = new IndexMetrics(index, operation);
                    indexMetricsMap.putIfAbsent(index+operation, indexMetrics);
                }
            }
        }

        return indexMetrics;
    }
}
