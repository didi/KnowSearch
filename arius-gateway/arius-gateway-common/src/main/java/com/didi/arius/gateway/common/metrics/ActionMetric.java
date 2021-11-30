package com.didi.arius.gateway.common.metrics;

import org.elasticsearch.rest.RestStatus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
* @author weizijun
* @date：2016年8月28日
* 
*/
public class ActionMetric {
	private String actionName;

	public ActionMetric(String actionName) {
		this.actionName = actionName;
	}
	
	private ConcurrentMap<Integer, StatusMetric> appMetrisMap = new ConcurrentHashMap<>();
	
	public void incr(int appid, RestStatus restStatus, long cost) {
		StatusMetric statusMetric = appMetrisMap.get(appid);
		if (statusMetric == null) {
			synchronized (appMetrisMap) {
				statusMetric = new StatusMetric();
				appMetrisMap.putIfAbsent(appid, statusMetric);
			}
		}
		
		statusMetric.incr(restStatus, cost);
	}

	public String getActionName() {
		return actionName;
	}

	public ConcurrentMap<Integer, StatusMetric> getAppMetrisMap() {
		return appMetrisMap;
	}
}
