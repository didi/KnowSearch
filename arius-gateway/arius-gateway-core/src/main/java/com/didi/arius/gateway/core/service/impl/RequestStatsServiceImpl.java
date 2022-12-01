package com.didi.arius.gateway.core.service.impl;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.event.ActionPostResponseEvent;
import com.didi.arius.gateway.common.event.PostResponseEvent;
import com.didi.arius.gateway.common.event.QueryPostResponseEvent;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metrics.ActionMetric;
import com.didi.arius.gateway.common.metrics.AppMetric;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.RequestStatsService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import lombok.NoArgsConstructor;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@NoArgsConstructor
public class RequestStatsServiceImpl implements RequestStatsService, ApplicationListener<PostResponseEvent> {
    protected static final ILog bootLogger = LogFactory.getLog(QueryConsts.BOOT_LOGGER);
    private ConcurrentMap<String, ActionMetric> actionMetricMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, AppMetric> appMetricMap = new ConcurrentHashMap<>();

    @Autowired
    private QueryConfig queryConfig;

    @Autowired
    private ThreadPool threadPool;

    @Value("${arius.gateway.adminSchedulePeriod}")
    private long schedulePeriod;

    private Cache queryContextCache;
    private Cache actionContextCache;

    @PostConstruct
    public void init(){
        threadPool.submitScheduleAtFixTask(this::dealRequest, schedulePeriod, schedulePeriod);

        CacheManager manager = CacheManager.create(RequestStatsServiceImpl.class.getResourceAsStream("/ehcache.xml"));
        queryContextCache = manager.getCache("queryContextCache");
        actionContextCache = manager.getCache("actionContextCache");
    }

    @Override
    public void dealRequest(){
        try {
            dealHttpRequest();
            dealTcpRequest();
        } catch (Exception e) {
            bootLogger.error("longTimeContextSchedule exception", e);
        }
    }

    @Override
    public void putQueryContext(String key, QueryContext value) {
        Element element = new Element(key, value);
        queryContextCache.put(element);
    }

    @Override
    public QueryContext getQueryContext(String key) {
        Element element = queryContextCache.get(key);
        if (element != null) {
            return (QueryContext) element.getObjectValue();
        } else {
            return null;
        }
    }

    @Override
    public List<String> getQueryKeys() {
        return queryContextCache.getKeys();
    }

    @Override
    public void removeQueryContext(String key) {
        queryContextCache.remove(key);
    }

    @Override
    public void putActionContext(String key, ActionContext value) {
        Element element = new Element(key, value);
        actionContextCache.put(element);
    }

    @Override
    public ActionContext getActionContext(String key) {
        Element element = actionContextCache.get(key);
        if (element != null) {
            return (ActionContext) element.getObjectValue();
        } else {
            return null;
        }
    }

    @Override
    public void removeActionContext(String key) {
        actionContextCache.remove(key);
    }

    @Override
    public List<String> getActionKeys() {
        return actionContextCache.getKeys();
    }

    @Override
    public void statsAdd(String actionName, int appid, String searchId, long cost, RestStatus restStatus) {
        if (searchId == null) {
            searchId = QueryConsts.TOTAL_SEARCH_ID;
        }

        appIncr(actionName, appid, searchId, cost, restStatus);
        actionIncr(actionName, appid, searchId, cost, restStatus);
    }

    @Override
    public ConcurrentMap<String, ActionMetric> getActionMetricMap() {
        return actionMetricMap;
    }

    @Override
    public ConcurrentMap<Integer, AppMetric> getAppMetricMap() {
        return appMetricMap;
    }

    @Override
    public void onApplicationEvent(PostResponseEvent postResponseEvent) {
        if(postResponseEvent instanceof ActionPostResponseEvent){
            ActionPostResponseEvent actionPostResponseEvent = (ActionPostResponseEvent)postResponseEvent;
            ActionContext actionContext = actionPostResponseEvent.getActionContext();

            if (null != actionContext) {
                removeActionContext(actionContext.getRequestId());
		    }
        }

        if(postResponseEvent instanceof QueryPostResponseEvent){
            QueryPostResponseEvent queryPostResponseEvent = (QueryPostResponseEvent)postResponseEvent;
            QueryContext queryContext = queryPostResponseEvent.getQueryContext();

            if(null != queryContext){
                removeQueryContext(queryContext.getRequestId());
            }
        }
    }

    /************************************************************** private method **************************************************************/
    private void dealHttpRequest() {
        List<String> queryKeys = getQueryKeys();
        int currentCount = 0;
        int slowCount = 0;
        long maxCost = 0;
        for (String key : queryKeys) {
            QueryContext queryContext = getQueryContext(key);
            if (queryContext != null) {
                currentCount++;

                long cost = System.currentTimeMillis() - queryContext.getRequestTime();
                if (cost > QueryConsts.SLOW_REQUEST_COST) {
                    slowCount++;

                    bootLogger.warn("slow request||appid={}||requestId={}||dslTemplateKey={}||cost={}",
                            queryContext.getAppid(), queryContext.getRequestId(), queryContext.getDslTemplateKey(), cost);
                }

                if (cost > maxCost) {
                    maxCost = cost;
                }
            }
        }

        bootLogger.info("current http request stats, currentCount={}, slowCount={}, maxCost={}, semaphore={}", currentCount, slowCount, maxCost, queryConfig.getHttpSemaphore().availablePermits());
    }

    private void dealTcpRequest() {
        List<String> queryKeys = getActionKeys();
        int currentCount = 0;
        int slowCount = 0;
        long maxCost = 0;
        for (String key : queryKeys) {
            ActionContext actionContext = getActionContext(key);
            if (actionContext != null) {
                currentCount++;

                long cost = System.currentTimeMillis() - actionContext.getRequestTime();
                if (cost > QueryConsts.SLOW_REQUEST_COST) {
                    slowCount++;

                    bootLogger.warn("slow request||appid={}||requestId={}||dslTemplateKey={}||cost={}",
                            actionContext.getAppid(), actionContext.getRequestId(), actionContext.getDslTemplateKey(), cost);
                }

                if (cost > maxCost) {
                    maxCost = cost;
                }
            }
        }

        bootLogger.info("current tcp request stats, currentCount={}, slowCount={}, maxCost={}, semaphore={}", currentCount, slowCount, maxCost, queryConfig.getTcpSemaphore().availablePermits());
    }

    private void appIncr(String actionName, int appid, String searchId, long cost, RestStatus restStatus) {
        if (appid != QueryConsts.TOTAL_APPID_ID) {
            appIncr(actionName, QueryConsts.TOTAL_APPID_ID, searchId, cost, restStatus);
        }

        AppMetric appMetric = appMetricMap.get(appid);
        if (appMetric == null) {
            synchronized (appMetricMap) {
                appMetric = new AppMetric(appid);
                appMetricMap.putIfAbsent(appid, appMetric);
            }
        }

        appMetric.incr(searchId, actionName, restStatus, cost);
    }

    private void actionIncr(String actionName, int appid, String searchId, long cost, RestStatus restStatus) {
        if (appid != QueryConsts.TOTAL_APPID_ID) {
            actionIncr(actionName, QueryConsts.TOTAL_APPID_ID, searchId, cost, restStatus);
        }

        ActionMetric actionMetric = actionMetricMap.get(actionName);
        if (actionMetric == null) {
            synchronized (actionMetricMap) {
                actionMetric = new ActionMetric(actionName);
                actionMetricMap.putIfAbsent(actionName, actionMetric);
            }
        }

        actionMetric.incr(appid, restStatus, cost);
    }
}
