package com.didi.arius.gateway.core.service;

import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metrics.ActionMetric;
import com.didi.arius.gateway.common.metrics.AppMetric;
import org.elasticsearch.rest.RestStatus;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public interface RequestStatsService {

    /**
     * 处理统计 HttpRequest和TcpRequest
     */
    void dealRequest();

    /**
     * 缓存 QueryContext
     * @param key
     * @param value
     */
    void putQueryContext(String key, QueryContext value);

    /**
     * 从缓存中获取 QueryContext
     * @param key
     * @return
     */
    QueryContext getQueryContext(String key);

    /**
     * 获取QueryContext缓存中的所有key
     * @return
     */
    List<String> getQueryKeys();

    /**
     * 删除缓存中为key的QueryContext
     * @param key
     */
    void removeQueryContext(String key);

    /**
     * 缓存ActionContext
     * @param key
     * @param value
     */
    void putActionContext(String key, ActionContext value);

    /**
     * 获取缓存中key的ActionContext
     * @param key
     * @return
     */
    ActionContext getActionContext(String key);

    /**
     * 删除缓存中key的ActionContext
     * @param key
     */
    void removeActionContext(String key);

    /**
     * 获取ActionContext缓存中的所有key
     * @return
     */
    List<String> getActionKeys();

    /**
     * 统计app, action的访问耗时
     * @param actionName
     * @param appid
     * @param searchId
     * @param cost
     * @param restStatus
     */
    void statsAdd(String actionName, int appid, String searchId, long cost, RestStatus restStatus);

    /**
     * 获取actionMetricMap
     * @return
     */
    ConcurrentMap<String, ActionMetric> getActionMetricMap();

    /**
     * 获取appMetricMap
     * @return
     */
    ConcurrentMap<Integer, AppMetric> getAppMetricMap();
}
