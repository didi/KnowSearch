package com.didi.arius.gateway.common.metrics;

import com.didi.arius.gateway.common.utils.MetricUtil;
import com.didi.arius.gateway.metrics.MetricsBuilder;
import com.didi.arius.gateway.metrics.MetricsSource;
import com.didi.arius.gateway.metrics.lib.MetricMutablePeriodGaugeLong;
import com.didi.arius.gateway.metrics.lib.MetricMutableStat;
import com.didi.arius.gateway.metrics.lib.MetricsRegistry;

public class QueryMetrics implements MetricsSource {
	private MetricsRegistry metricsRegistry;
	
	private static final String APPID_COUNT_NAME = "query.count";	
	private static final String APPID_COST_NAME = "query.cost";	
	
	private static final String APPID_SLOWLOG_COUNT_NAME = "slowlog.count";
	private static final String APPID_SLOWLOG_COST_NAME = "slowlog.cost";
	
	private static final String APPID_QUERY_REQUEST_LENGTH_NAME = "query.request.length";
	private static final String APPID_QUERY_RESPONSE_LENGTH_NAME = "query.response.length";
	private static final String APPID_QUERY_TOOK_NAME = "query.tookInMillis";
	private static final String APPID_QUERY_HITS_NAME = "query.totalHits";
	private static final String APPID_QUERY_TOTAL_SHARDS_NAME = "query.totalShards";
	private static final String APPID_QUERY_FAILED_SHARDS_NAME = "query.failedShards";
	
	private static final String APPID_AGGS_COUNT_NAME = "query.aggs.count";
	
	
	private MetricMutablePeriodGaugeLong countMetric;
	
	private MetricMutableStat costMetric;
	
	private MetricMutablePeriodGaugeLong slowlogCountMetric;
	
	private MetricMutableStat slowlogCostMetric;
	
	private MetricMutableStat requestLengthMetric;
	private MetricMutableStat responseLengthMetric;
	private MetricMutableStat tookInMillisMetric;
	private MetricMutableStat totalHitsMetric;
	private MetricMutableStat totalShardsMetric;
	private MetricMutableStat failedShardsMetric;
	
	private MetricMutablePeriodGaugeLong aggsCountMetric;

	public QueryMetrics(int appid) {
		super();
		metricsRegistry = new MetricsRegistry("query");
		metricsRegistry.tag("appid", "", String.valueOf(appid));
		
		countMetric = metricsRegistry.newPeriodGauge(APPID_COUNT_NAME, "appid ["
				+ appid + "] query count", 0L);
		
		costMetric = metricsRegistry.newStat(APPID_COST_NAME,
				"appid ["
				+ appid + "] query cost", "ops", "time", true);		
		
		slowlogCountMetric = metricsRegistry.newPeriodGauge(APPID_SLOWLOG_COUNT_NAME, "appid ["
				+ appid + "] slowlog count", 0L);
		
		slowlogCostMetric = metricsRegistry.newStat(APPID_SLOWLOG_COST_NAME,
				"appid ["
				+ appid + "] slowlog cost", "ops", "time", true);
		
		requestLengthMetric = metricsRegistry.newStat(APPID_QUERY_REQUEST_LENGTH_NAME,
				"appid [" + appid + "] request length", "ops", "length");
		
		responseLengthMetric = metricsRegistry.newStat(APPID_QUERY_RESPONSE_LENGTH_NAME,
				"appid [" + appid + "] response length", "ops", "length");
		
		tookInMillisMetric = metricsRegistry.newStat(APPID_QUERY_TOOK_NAME,
				"appid [" + appid + "] tookInMillis", "ops", "millis");
		
		totalHitsMetric = metricsRegistry.newStat(APPID_QUERY_HITS_NAME,
				"appid [" + appid + "] totalHits", "ops", "count");
		
		totalShardsMetric = metricsRegistry.newStat(APPID_QUERY_TOTAL_SHARDS_NAME,
				"appid [" + appid + "] totalShards", "ops", "count");
		
		failedShardsMetric = metricsRegistry.newStat(APPID_QUERY_FAILED_SHARDS_NAME,
				"appid [" + appid + "] failedShards", "ops", "count");
		
		aggsCountMetric = metricsRegistry.newPeriodGauge(APPID_AGGS_COUNT_NAME, "appid ["
				+ appid + "] query count", 0L);
		
		MetricUtil.register("gateway_"+appid, "arius-gateway metrics", this);
	}
	
	@Override
	public void getMetrics(MetricsBuilder builder, boolean all) {
		metricsRegistry.snapshot(builder.addRecord(metricsRegistry.name()),
				true);
	}
	
	public void incrCost(long cost) {
		countMetric.incr();
		costMetric.add(cost);
	}
	
	public void incrSlowlogCost(long cost) {
		slowlogCountMetric.incr();
		slowlogCostMetric.add(cost);
	}
	
	public void incrReqeustLength(long length) {
		requestLengthMetric.add(length);
	}
	
	public void incrResponseLength(long length) {
		responseLengthMetric.add(length);
	}
	
	public void incrSearchResponseMetrics(long tookInMillis, long totalHits, int totalShards, int failedShards) {
		tookInMillisMetric.add(tookInMillis);
		totalHitsMetric.add(totalHits);
		totalShardsMetric.add(totalShards);
		failedShardsMetric.add(failedShards);
	}
	
	public void incrAggs() {
		aggsCountMetric.incr();
	}

}
