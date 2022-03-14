package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by fitz on 2021-08-12
 */
public enum GatewayMetricsTypeEnum {
	/**
	 * overview metrics
	 */
	QUERY_TOTAL_HITS_AVG_COUNT(MetricsConstant.OVERVIEW,"queryTotalHitsAvgCount", "total_hits_avg", "平均查询命中数量"),
	QUERY_COST_AVG(MetricsConstant.OVERVIEW, "queryCostAvg", MetricsConstant.TOTAL_COST_AVG, "查询平均响应时间"),
	QUERY_TOTAL_SHARDS_AVG(MetricsConstant.OVERVIEW, "queryTotalShardsAvg", "total_shards_avg", "查询平均shard数量"),
	QUERY_FAILED_SHARDS_AVG(MetricsConstant.OVERVIEW, "queryFailedShardsAvg", "failed_shards_avg", "查询失败平均shard数量"),
	READ_DOC_COUNT(MetricsConstant.OVERVIEW, "readDocCount", MetricsConstant.DOC_COUNT, "查询量"),
	QUERY_SEARCH_TYPE(MetricsConstant.OVERVIEW, "querySearchType", MetricsConstant.DOC_COUNT, "dsl/sql分布"),
	WRITE_DOC_COUNT(MetricsConstant.OVERVIEW, "writeDocCount", MetricsConstant.DOC_COUNT, "写入量"),
	WRITE_TOTAL_COST(MetricsConstant.OVERVIEW, "writeTotalCost", MetricsConstant.TOTAL_COST_AVG, "写入平均耗时"),
	WRITE_RESPONSE_LEN(MetricsConstant.OVERVIEW, "writeResponseLen", "response_len_avg", "写入请求响应长度"),

	/**
	 * index metrics
	 */
	WRITE_INDEX_COUNT("index", "writeIndexCount", MetricsConstant.DOC_COUNT, "索引写入量分布"),
	WRITE_INDEX_TOTAL_COST("index", "writeIndexTotalCost", MetricsConstant.TOTAL_COST_AVG, "索引平均写入耗时"),
	SEARCH_INDEX_COUNT("index", "searchIndexCount", MetricsConstant.DOC_COUNT, "索引查询量分布"),
	SEARCH_INDEX_TOTAL_COST("index", "searchIndexTotalCost", MetricsConstant.TOTAL_COST_AVG, "索引平均查询耗时"),


	/**
	 * gateway node metrics
	 */
	WRITE_GATEWAY_NODE("node", "writeGatewayNode", MetricsConstant.DOC_COUNT, "gateway节点写入量分布"),
	QUERY_GATEWAY_NODE("node", "queryGatewayNode", MetricsConstant.DOC_COUNT, "gateway节点查询分布"),

	/**
	 * es client node metrics
	 */
	WRITE_CLIENT_NODE("clientNode", "writeClientNode", MetricsConstant.DOC_COUNT, "clientNode节点写入量分布"),
	QUERY_CLIENT_NODE("clientNode", "queryClientNode", MetricsConstant.DOC_COUNT, "clientNode节点查询分布"),

	/**
	 * 项目 metrics
	 */
	QUERY_APP_SEARCH_COST("app", "queryAppSearchCost", "search_cost_avg", "appid查询时间分布"),
	QUERY_APP_COUNT("app", "queryAppCount", MetricsConstant.DOC_COUNT, "appid查询量"),
	QUERY_APP_TOTAL_COST("app", "queryAppTotalCost", MetricsConstant.TOTAL_COST_AVG, "appid查询平均响应时间"),


	/**
	 * 查询模版 metrics
	 */
	QUERY_DSL_COUNT("dsl", "queryDslCount", MetricsConstant.DOC_COUNT, "查询模板访问量"),
	QUERY_DSL_TOTAL_COST("dsl", "queryDslTotalCost", MetricsConstant.TOTAL_COST_AVG, "询模板访问耗时");


	GatewayMetricsTypeEnum(String group, String type, String aggKey, String desc) {
		this.group = group;
		this.type = type;
		this.aggKey = aggKey;
		this.desc = desc;
	}

	public static final List<String> commonOverviewMetrics = Lists.newArrayList(
			QUERY_TOTAL_HITS_AVG_COUNT.getType(),
			QUERY_COST_AVG.getType(),
			QUERY_TOTAL_SHARDS_AVG.getType(),
			QUERY_FAILED_SHARDS_AVG.getType()
	);

	public static final List<String> writeOverviewMetrics = Lists.newArrayList(
			WRITE_DOC_COUNT.getType(),
			WRITE_TOTAL_COST.getType(),
			WRITE_RESPONSE_LEN.getType()
	);

	public static final List<String> writeIndexMetrics = Lists.newArrayList(
			WRITE_INDEX_COUNT.getType(),
			WRITE_INDEX_TOTAL_COST.getType()
	);
	public static final List<String> searchIndexMetrics = Lists.newArrayList(
			SEARCH_INDEX_COUNT.getType(),
			SEARCH_INDEX_TOTAL_COST.getType()
	);

	public static final List<String> commonAppMetrics = Lists.newArrayList(
			QUERY_APP_SEARCH_COST.getType(),
			QUERY_APP_TOTAL_COST.getType()
	);

	/**
	 * 分组名称
	 */
	private String group;

	/**
	 * 指标名
	 */
	private String type;

	/**
	 * dsl聚合自定义key
	 */
	private String aggKey;

	/**
	 * 描述信息
	 */
	private String desc;


	public String getType() {
		return type;
	}

	public String getGroup() {
		return group;
	}

	public String getDesc() {
		return desc;
	}

	public String getAggKey() {
		return aggKey;
	}

	public static List<String> getMetricsByGroup(String group) {
		return Arrays.stream(GatewayMetricsTypeEnum.values()).filter(x -> x.getGroup().equals(group)).map(GatewayMetricsTypeEnum::getType).collect(Collectors.toList());

	}

	public static String type2AggKey(String type) {
		for (GatewayMetricsTypeEnum gatewayMetricsTypeEnum : GatewayMetricsTypeEnum.values()) {
			if (gatewayMetricsTypeEnum.getType().equals(type)) {
				return gatewayMetricsTypeEnum.getAggKey();
			}
		}
		return "";
	}

	public static GatewayMetricsTypeEnum type2Value(String type) {
		for (GatewayMetricsTypeEnum gatewayMetricsTypeEnum : GatewayMetricsTypeEnum.values()) {
			if (gatewayMetricsTypeEnum.getType().equals(type)) {
				return gatewayMetricsTypeEnum;
			}
		}
		return null;
	}
}
