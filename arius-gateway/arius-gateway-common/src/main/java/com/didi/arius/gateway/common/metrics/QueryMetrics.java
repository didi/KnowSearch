package com.didi.arius.gateway.common.metrics;

import com.didi.arius.gateway.common.utils.MetricUtil;
import com.didiglobal.logi.metrics.MetricsBuilder;
import com.didiglobal.logi.metrics.MetricsSource;
import com.didiglobal.logi.metrics.lib.MetricMutablePeriodGaugeLong;
import com.didiglobal.logi.metrics.lib.MetricMutableStat;
import com.didiglobal.logi.metrics.lib.MetricsRegistry;

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
	private static final String APPID_NAME = "appid [";
	private static final String COUNT_NAME = "count";
	
	
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
		
		countMetric = metricsRegistry.newPeriodGauge(APPID_COUNT_NAME, APPID_NAME
				+ appid + "] query count", 0L);
		
		costMetric = metricsRegistry.newStat(APPID_COST_NAME,
				APPID_NAME
				+ appid + "] query cost", "ops", "time", true);		
		
		slowlogCountMetric = metricsRegistry.newPeriodGauge(APPID_SLOWLOG_COUNT_NAME, APPID_NAME
				+ appid + "] slowlog count", 0L);
		
		slowlogCostMetric = metricsRegistry.newStat(APPID_SLOWLOG_COST_NAME,
				APPID_NAME
				+ appid + "] slowlog cost", "ops", "time", true);
		
		requestLengthMetric = metricsRegistry.newStat(APPID_QUERY_REQUEST_LENGTH_NAME,
				APPID_NAME + appid + "] request length", "ops", "length");
		
		responseLengthMetric = metricsRegistry.newStat(APPID_QUERY_RESPONSE_LENGTH_NAME,
				APPID_NAME + appid + "] response length", "ops", "length");
		
		tookInMillisMetric = metricsRegistry.newStat(APPID_QUERY_TOOK_NAME,
				APPID_NAME + appid + "] tookInMillis", "ops", "millis");
		
		totalHitsMetric = metricsRegistry.newStat(APPID_QUERY_HITS_NAME,
				APPID_NAME + appid + "] totalHits", "ops", COUNT_NAME);
		
		totalShardsMetric = metricsRegistry.newStat(APPID_QUERY_TOTAL_SHARDS_NAME,
				APPID_NAME + appid + "] totalShards", "ops", COUNT_NAME);
		
		failedShardsMetric = metricsRegistry.newStat(APPID_QUERY_FAILED_SHARDS_NAME,
				APPID_NAME + appid + "] failedShards", "ops", COUNT_NAME);
		
		aggsCountMetric = metricsRegistry.newPeriodGauge(APPID_AGGS_COUNT_NAME, APPID_NAME
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
