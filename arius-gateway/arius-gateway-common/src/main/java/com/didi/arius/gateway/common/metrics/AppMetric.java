package com.didi.arius.gateway.common.metrics;

import com.didi.arius.gateway.common.consts.QueryConsts;
import org.elasticsearch.rest.RestStatus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
* @author weizijun
* @date：2016年8月28日
* 
*/
public class AppMetric {
	private int appid;
	
	public AppMetric(int appid) {
		this.appid = appid;
	}
	
	private ConcurrentMap<String, SearchMetric> searchsMetricMap = new ConcurrentHashMap<>();
	
	public void incr(String searchId, String actionName, RestStatus restStatus, long cost) {
		if (!searchId.equals(QueryConsts.TOTAL_SEARCH_ID)) {
			incr(QueryConsts.TOTAL_SEARCH_ID, actionName, restStatus, cost);
		}
		
		SearchMetric searchMetric = searchsMetricMap.get(searchId);
		if (searchMetric == null) {
			synchronized (searchsMetricMap) {
				searchMetric = new SearchMetric(searchId);
				searchsMetricMap.putIfAbsent(searchId, searchMetric);
			}
		}
		
		searchMetric.incr(actionName, restStatus, cost);
	}

	public int getAppid() {
		return appid;
	}

	public ConcurrentMap<String, SearchMetric> getSearchsMetricMap() {
		return searchsMetricMap;
	}
}
