package com.didi.arius.gateway.common.metrics;

import org.elasticsearch.rest.RestStatus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
* @author weizijun
* @date：2016年8月28日
* 
*/
public class SearchMetric {
	private String searchId;
	
	public SearchMetric(String searchId) {
		this.searchId = searchId;
	}
	
	private ConcurrentMap<String, StatusMetric> actionMetricMap = new ConcurrentHashMap<>();
	
	public void incr(String actionName, RestStatus restStatus, long cost) {
		StatusMetric statusMetric = actionMetricMap.get(actionName);
		if (statusMetric == null) {
			synchronized (actionMetricMap) {
				statusMetric = new StatusMetric();
				actionMetricMap.putIfAbsent(actionName, statusMetric);
			}
		}
		
		statusMetric.incr(restStatus, cost);
	}

	public String getSearchId() {
		return searchId;
	}

	public ConcurrentMap<String, StatusMetric> getActionMetricMap() {
		return actionMetricMap;
	}
}
