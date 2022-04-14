package com.didi.arius.gateway.common.metrics;

import org.elasticsearch.common.metrics.MeanMetric;
import org.elasticsearch.rest.RestStatus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
* @author weizijun
* @date：2016年8月28日
* 
*/
public class StatusMetric {
	private ConcurrentMap<RestStatus, MeanMetric> statusMetricMap = new ConcurrentHashMap<>();

	public void incr(RestStatus restStatus, long cost) {
		MeanMetric meanMetric = statusMetricMap.get(restStatus);
		if (meanMetric == null) {
			synchronized (statusMetricMap) {
				meanMetric = new MeanMetric();
				statusMetricMap.putIfAbsent(restStatus, meanMetric);
			}
		}
		
		meanMetric.inc(cost);
	}

	public ConcurrentMap<RestStatus, MeanMetric> getStatusMetricMap() {
		return statusMetricMap;
	}
}
