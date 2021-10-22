package com.didi.arius.gateway.common.consts;

public class QueryConsts {
	public static final String HEAD_AUTHORIZATION = "Authorization";
	public static final String HEAD_SEARCH_ID = "SEARCH_ID";
	public static final String HEAD_USER = "username";
	public static final String HEAD_CLUSTER_ID = "CLUSTER-ID";
	public static final String HEAD_USERNAME = "x-username";
	public static final String HEAD_SSO_USERNAME = "X-SSO-USER";
	public static final String HEAD_CLIENT_VERSION = "ES-Client-Version";
	public static final String HEAD_KIBANA_VERSION = "kbn-version";
	public static final String NEW_KIBANA_VERSION_START = "7";
	public static final String COOKIE_USERNAME = "_sso_username";
	public static final String GET_CLUSTER_ID = "cluster_id";

	public static final String TRACE_ID = "traceid";
	
	public static final String SPAN_ID = "cspanid";
	
	public static final String TOTAL_SEARCH_ID = "all";
	public static final int TOTAL_APPId_ID = -1;
	
	public static final String HTTP_ATTR_QUERY_CONTEXT = "queryContext";
	
	public static final String DLFLAG_PREFIX = "_arius_";
	public static final String STAT_LOGGER = "queryStats";
	public static final String TRACE_LOGGER = "traceLog";
	public static final String AUDIT_LOGGER = "auditLog";
	public static final String BOOT_LOGGER = "bootLog";
	
	public static final int RETRY_COUNT = 3;
	public static final int RETRY_SLEEP_MILLIS = 100;
	
	public static final String DEFAULT_TRIB_CLUSTER = "elasticsearch";
	
	public static final long DEFAULT_SCROLL_MILLIS = 600000;
	public static final long DEFAULT_SCROLL_TIMEOUT = 5000;
	
	public static final int DEFAULT_HTTP_TIMEOUT = 30000;
	
	public static final String MESSAGE_FIELD = "message";
	public static final String TOPIC_FIELD = "kTopic";
	public static final String PARTITION_FIELD = "kPartition";
	public static final String OFFSET_FIELD = "kOffset";
	public static final String UNIQUE_KEY_FIELD = "uniqueKey";
	
	public static final String CHECK_MODE_BLACK = "black";
	public static final int QUERY_DSL_LIMIT_WAIT = 50;
	public static final int AGGS_BUCKET_MEM_UNIT = 0;
	public static final int AGGS_DEFAULT_SHARD_NUMBER = 50;
	public static final int QUERY_DSL_MODIFY_TIME_EARLY = 600000;
	
	public static final int DATE_HISTOGRAM_DEFAUL_RANGE = 3600000;
	
	public static final int MIN_APPID_NUMBER = 5;
	public static final int MIN_TEMPLATE_NUMBER = 5;

	public static final int SLOW_REQUEST_COST = 30000;

	public static final String DEFAULT_ES_VERSION = "2.3.3";
	public static final String ES_VERSION_2_PREFIX = "2";
	public static final String ES_VERSION_6_PREFIX = "6";
	public static final String ES_VERSION_7_PREFIX = "7";

	public static final String DETAIL_LOG_FLAG = "detail.log.flag";
	// 动态配置项，可以跳过多type索引启用映射查询的appid列表
	public static final String MAPPING_INDEXNAME_WHITE_APPIDS = "mapping.indexname.white.appids";

	public static final long DAY_MILLIS = 24 * 60 * 60 * 1000;
	public static final long DEFALUT_INDEX_DAY = 93;
	public static final long MAX_INDEX_COUNT = 100;

	public static final int MAX_SOCKET_TIMEOUT = 120000;

	public static final String GATEWAY_GET_APP_TICKET      = "xTc59aY72";
	public static final String GATEWAY_GET_APP_TICKET_NAME = "X-ARIUS-GATEWAY-TICKET";

	public static final String GATEWAY_WRITE_MODE = "write";

	public static final String SEARCH_IGNORE_THROTTLED = "ignore_throttled";

	public static final String GATEWAY_GROUP = "Normal";
}
