package com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/28 上午10:23
 * @modified By D10865
 *
 * 查询语句文件名常量
 *
 * 命名规则 类名/方法名
 *
 * 在dslFiles目录下新建以类名为名称的文件夹，以方法名为名称的文件名
 *
 */
public class DslsConstant {

    private DslsConstant() {
    }



    /**
     * 获得所有aggs请求的indices字段值
     */
    public static final String GET_INDICES_FOR_AGGS                                          = "IndexHit/getIndicesForAggs";
    /**
     * 获得普通请求的的index字段值
     */
    public static final String GET_INDEX_FOR_NORMALS                                         = "IndexHit/getIndexForNormal";

    /************************************************************** IndexTemplateLabelESDao **************************************************************/
    /**
     * 根据索引模板ID获取标签
     */
    public static final String V2_SCROLL_LABEL_BY_TEMPLATE_ID                                = "IndexTemplateLabelESDao/getLabelByLogicTemplateId";
    /**
     * 根据标签ID获取标签
     */
    public static final String V2_SCROLL_LABEL_BY_LABEL_ID                                   = "IndexTemplateLabelESDao/getLabelByLabelId";


    /************************************************************** AriusStatsClusterInfoEsDao **************************************************************/

    /**
     * 查询一段时间内物理集群分位使用信息
     */
    public static final String GET_CLUSTER_PHY_AGG_PERCENTILES_METRICS_BY_AGG_PARAM          = "AriusStatsClusterPhyInfoEsDao/getAggPercentilesMetricsByRange";

    /**
     * 查询一段时间内物理集群其他使用信息
     */
    public static final String GET_CLUSTER_METRICS_BY_RANGE_AND_INTERVAL                     = "AriusStatsClusterPhyInfoEsDao/getClusterMetricsByRangeAndInterval";

    /**
     * 获取网关数量
     */
    public static final String GET_GATEWAY_COUNT                                             = "AriusStatsClusterPhyInfoEsDao/getGatewayCount";


    /************************************************************** AriusStatsIndexInfoEsDao **************************************************************/
    /**
     * 查询索引模板一段时间访问内的最大容量
     */
    public static final String GET_TEMPLATE_TOTAL_SIZE_BY_TIME_RANGE                         = "AriusStatsIndexInfoEsDao/getTemplateTotalSizeByTimeRange";
    /**
     * 查询索引模板一段时间tps的最大容量
     */
    public static final String GET_TEMPLATE_TOTAL_MAX_TPS_BY_TIME_RANGE                      = "AriusStatsIndexInfoEsDao/getTemplateTotalMaxTpsByTimeRange";
    /**
     * 查询索引模板一段时间tps的最大容量
     */
    public static final String GET_TEMPLATE_TOTAL_MAX_TPS_BY_TIME_RANGE_NO_PERCENT           = "AriusStatsIndexInfoEsDao/getTemplateTotalMaxTpsByTimeRangeNoPercent";
    /**
     * 根据物理索引模板获取模板总的大小
     */
    public static final String GET_TEMPLATE_TOTAL_SIZE_BY_TEMPLATE_ID                        = "AriusStatsIndexInfoEsDao/getTemplateTotalSizeByTemplateId";
    /**
     * 根据逻辑索引模板获取模板总的大小
     */
    public static final String GET_LOGIC_TEMPLATE_TOTAL_SIZE_BY_TEMPLATE_ID                  = "AriusStatsIndexInfoEsDao/getTemplateTotalSizeByLogicTemplateId";
    /**
     * 根据索引模板一段时间获取模板总的文档大小
     */
    public static final String GET_TEMPLATE_TOTAL_DOC_BY_TIME_RANGE                          = "AriusStatsIndexInfoEsDao/getTemplateTotalDocNuByTimeRange";
    /**
     * 根据索引模板ID一段时间获取模板总的文档大小
     */
    public static final String GET_TEMPLATE_TOTAL_DOC_BY_LOGIC_TEMPLATE_ID_AND_TIME_RANGE    = "AriusStatsIndexInfoEsDao/getTemplateTotalDocNuByLogicTemplateIdAndTimeRange";
    /**
     * 获取索引模板最大索引的容量
     */
    public static final String GET_TEMPLATE_MAX_INDEX_SIZE                                   = "AriusStatsIndexInfoEsDao/getTemplateMaxIndexSize";
    /**
     * 获取索引模板最大索引的文档条数
     */
    public static final String GET_TEMPLATE_MAX_INDEX_DOC                                    = "AriusStatsIndexInfoEsDao/getTemplateMaxIndexDoc";
    /**
     * 根据索引模板和集群获取相关统计信息
     */
    public static final String GET_TEMPLATE_REAL_STATIS_INFO_BY_TEMPLATE_AND_CLUSTER         = "AriusStatsIndexInfoEsDao/getIndexRealTimeStatisInfoByTemplateAndCluster";
    /**
     * 根据索引模板和集群获取相关统计信息
     */
    public static final String GET_TEMPLATE_REAL_INFO_BY_TEMPLATE_AND_CLUSTER                = "AriusStatsIndexInfoEsDao/getIndexRealTimeInfoByTemplateAndCluster";
    /**
     * 查询一段时间内的索引的最大值值信息
     */
    public static final String GET_MAXINFO_BY_TIME_RANGE_AND_TEMPLATE                        = "AriusStatsIndexInfoEsDao/getMaxInfoByRangeTimeAndTemplate";
    /**
     * 根据模板Id查询一段时间内的索引节点统计信息
     */
    public static final String GET_INDEX_STATS_BY_TIME_RANGE_AND_TEMPLATEID                  = "AriusStatsIndexInfoEsDao/getIndexStatsByTempalteId";
    /**
     * 获取集群实时总的tps/qps信息
     */
    public static final String GET_CLUSTER_REAL_TIME_TPS_QPS_INFO                            = "AriusStatsIndexInfoEsDao/getClusterTpsQpsInfo";
    /**
     * 获取某个索引模板最近一段时间tps总和
     */
    public static final String GET_AVG_TPS_BY_LOGIC_ID_AND_TIME_RANGE                        = "AriusStatsIndexInfoEsDao/getAvgTpsByLogicIdAndTimeRange";
    /**
     * 获取某个逻辑模板历史一段时间tps每小时最大值
     */
    public static final String GET_HISTORY_MAX_TPS_BY_LOGIC_ID_AND_TIME_RANGE                = "AriusStatsIndexInfoEsDao/getHistoryMaxTpsByLogicIdAndTimeRange";
    /**
     * 查询一段时间内的索引查询率
     */
    public static final String GET_QUERY_RATE_BY_INDEX_DATE_RANGE                            = "AriusStatsIndexInfoEsDao/getQueryRateByIndexDateRange";

    /**
     * 查询单个索引聚合信息
     */
    public static final String GET_AGG_SINGLE_INDEX_METRICS                                  = "AriusStatsIndexInfoEsDao/getAggSingleIndexMetrics";

    /**
     * 查询topN索引聚合信息
     */
    public static final String GET_TOPN_INDEX_AGG_METRICS                                    = "AriusStatsIndexInfoEsDao/getTopNIndicesAggMetrics";

    /**
     * 获取指定集群有索引信息的时间戳
     */
    public static final String GET_HAS_INDEX_METRICS_DATA_TIME                               = "AriusStatsIndexInfoEsDao/getHasIndexMetricsDataTime";

    /**
     * 查询第一个时间分片中指标数值的索引指标信息
     */
    public static final String GET_MULTIPLE_INDEX_FIRST_INTERVAL_AGG_METRICS                 = "AriusStatsIndexInfoEsDao/getAggMultipleIndicesMetrics";

    /**
     * 查询第一个时间分片中指标数值的索引指标信息
     */
    public static final String GET_MULTIPLE_INDEX_FIRST_INTERVAL_AGG_METRICS_WITH_STEP       = "AriusStatsIndexInfoEsDao/getAggMultipleIndicesMetricsWithStep";
    public static final String GET_AGG_MULTIPLE_INDICES_METRICS_WITH_STEP_AND_INDEX_LIST       = "AriusStatsIndexInfoEsDao/getAggMultipleIndicesMetricsWithStepAndIndexList";


    /**
     * 查询第一个时间分片中指标数值的逻辑模板指标信息
     */
    public static final String GET_MULTIPLE_TEMPLATE_FIRST_INTERVAL_AGG_METRICS_WITH_STEP    = "AriusStatsIndexInfoEsDao/getAggMultipleTemplateMetricsWithStep";

    /**
     * 查询topN模板聚合信息
     */
    public static final String GET_TOPN_TEMPLATE_AGG_METRICS                                 = "AriusStatsIndexInfoEsDao/getTopNTemplateAggMetrics";

    /**
     * 查询单个模板聚合信息
     */
    public static final String GET_AGG_SINGLE_TEMPLATE_METRICS                               = "AriusStatsIndexInfoEsDao/getAggSingleTemplateMetrics";
    public static final String GET_AGG_MULTIPLE_TEMPLATE_METRICS_WITH_STEP_AND_LOGIC_IDS                               = "AriusStatsIndexInfoEsDao/getAggMultipleTemplateMetricsWithStepAndLogicIds";

    /**
     * 获取单个指标项
     */
    public static final String GET_SINGLE_METRICS                                            = "AriusStatsIndexInfoEsDao/getSingleMetrics";

    /************************************************************** AriusStatsIndexNodeInfoEsDao **************************************************************/
    /**
     * 查询一段时间内的索引节点统计信息
     */
    public static final String GET_INDEX_NODE_STATS_BY_TIME_RANGE                            = "AriusStatsIndexNodeInfoEsDao/getIndexToNodeStats";
    /**
     * 查询一段时间内的索引节点统计信息
     */
    public static final String GET_INDEX_NODE_STATS_BY_TIME_RANGE_AND_TEMPALTEID             = "AriusStatsIndexNodeInfoEsDao/getIndexToNodeStatsByTemplateId";

    /************************************************************** AriusStatsIngestInfoEsDao **************************************************************/
    /**
     * 获取某个索引模板最近一段时间限流导致的失败数量
     */
    public static final String GET_INGEST_FAILED_COUNT_BY_LOGIC_ID_AND_TIME_RANGE            = "AriusStatsIngestInfoEsDao/getIngestFailCountByLogicIdAndTimeRange";

    /************************************************************** AriusStatsNodeInfoEsDao   **************************************************************/

    /**
     * 获取所有集群节点的物理存储空间大小
     */
    public static final String GET_ALL_CLUSTER_NODE_PHY_STORE_SIZE                           = "AriusStatsNodeInfoEsDao/getAllClusterNodePhyStoreSize";
    /**
     * 获取集群实时总的发送流量和接收流量信息
     */
    public static final String GET_CLUSTER_REAL_TIME_RX_TX_INFO                              = "AriusStatsNodeInfoEsDao/getClusterRxTxInfo";
    /**
     * 获取集群实时cpu平均使用率
     */
    public static final String GET_CLUSTER_REAL_TIME_CPU_AVG_INFO                            = "AriusStatsNodeInfoEsDao/getClusterCpuAvgInfo";

    /**
     * 获取集群实时分位值和平均使用率
     */
    public static final String AGG_CLUSTER_REAL_TIME_AVG_AND_PERCENT                         = "AriusStatsNodeInfoEsDao/aggClusterAvgAndPercentiles";

    /**
     * 获取集群查询/写入总耗时
     */
    public static final String AGG_CLUSTER_INDEXING_SEARCH_TIME_SUM                         = "AriusStatsNodeInfoEsDao/aggClusterIndexingOrSearchTimeSum";

    /**
     * 获取集群磁盘空闲率实时分位值和平均使用率
     */
    public static final String AGG_CLUSTER_AVG_AND_PERCENT_FOR_DISK_FREE_USAGE_PERCENT       = "AriusStatsNodeInfoEsDao/aggClusterAvgAndPercentilesForDiskFreeUsagePercent";

    /**
     * 获取获取集群写入耗时最大值
     */
    public static final String GET_CLUSTER_INDEXING_LATENCY_MAX                              = "AriusStatsNodeInfoEsDao/getClusterIndexingLatency";
    /**
     * 获取获取集群查询耗时最大值
     */
    public static final String GET_CLUSTER_SEARCH_LATENCY_MAX                                = "AriusStatsNodeInfoEsDao/getClusterSearchLatency";

    /**
     * 获取topN节点维度统计信息
     */
    public static final String GET_TOPN_NODE_AGG_METRICS_INFO                                = "AriusStatsNodeInfoEsDao/getTopNNodeAggMetricsInfo";

    /**
     * 获取集群多个节点维度统计信息
     */
    public static final String GET_AGG_CLUSTER_PHY_NODES_INFO                                = "AriusStatsNodeInfoEsDao/getAggClusterPhyNodesInfo";

    /**
     * 获取集群单个节点维度统计信息
     */
    public static final String GET_AGG_CLUSTER_PHY_SINGLE_NODE_NODE                          = "AriusStatsNodeInfoEsDao/getAggClusterPhySingleNodeInfo";

    /**
     * 获取指定集群节点有节点信息的时间戳
     */
    public static final String GET_HAS_NODE_METRICS_DATA_TIME                                = "AriusStatsNodeInfoEsDao/getHasNodeMetricInfoTime";
    /**
     * 获取某个区间范围内的某个sum聚合值
     */
    public static final String GET_FIELD_SUM_AND_RANGE_FIELD_TOTAL                           = "AriusStatsNodeInfoEsDao/getFieldSumAndRangeFieldTotal";

    /**
     * 获取某个区间范围内节点的某个sum聚合值
     */
    public static final String GET_NODE_FIELD_SUM_AND_RANGE_FIELD_TOTAL                      = "AriusStatsNodeInfoEsDao/getNodeFieldSumAndRangeFieldTotal";

    /**
     * 获取时间段内指标的最大值或平均值
     */
    public static final String GET_AGG_CLUSTER_NODE_INFO_WITH_STEP                           = "AriusStatsNodeInfoEsDao/getAggClusterNodeInfoWithStep";




    /************************************************************** TemplateAccessCountEsDao **************************************************************/
    /**
     * 根据时间范围获取索引模板查询统计次数
     */
    public static final String SCROLL_GET_TEMPLATE_ACCESS_BY_RANGE                           = "TemplateAccessCountEsDao/getAllTemplateAccessByDateRange";
    /**
     * 根据时间范围和集群获取索引模板查询统计次数
     */
    public static final String SCROLL_GET_TEMPLATE_ACCESS_BY_RANGE_AND_CLUSTER               = "TemplateAccessCountEsDao/getAllTemplateAccessByDateRangeAndCluster";
    /**
     * 根据索引模板名称获取历史查询次数统计
     */
    public static final String SCROLL_GET_TEMPLATE_HISTORY_BY_TEMPLATE                       = "TemplateAccessCountEsDao/getAllTemplateAccessHistoryByTemplate";
    /**
     * 根据索引模板ID获取历史查询次数统计
     */
    public static final String SCROLL_GET_TEMPLATE_HISTORY_BY_ID                             = "TemplateAccessCountEsDao/getAllTemplateAccessHistoryByTemplateId";
    /**
     * 根据时间范围获取指定索引模板查询统计次数
     */
    public static final String SCROLL_GET_TEMPLATE_BY_TEMPLATE_RANGE                         = "TemplateAccessCountEsDao/getAllTemplateAccessByTemplateDateRange";
    /**
     * 根据索引模板Id获取近7天访问记录
     */
    public static final String GET_LAST_7_DAY_ACCESS_BY_TEMPLATE_ID                          = "TemplateAccessCountEsDao/getTemplateAccessLast7DayByTemplateId";
    /**
     * 根据索引模板Id获取近7天访问记录
     */
    public static final String GET_LAST_N_DAY_ACCESS_BY_LOGIC_TEMPLATE_ID                    = "TemplateAccessCountEsDao/getTemplateAccessLastNDayByLogicTemplateId";

    /************************************************************** GatewayJoinEsDao **************************************************************/
    /**
     * 分页获取dsl异常查询列表
     */
    public static final String GET_GATEWAY_ERROR_BY_CONDITION                           = "GatewayJoinEsDao/getGatewayErrorByCondition";
    /**
     * 分页获取dsl慢查询列表
     */
    public static final String GET_GATEWAY_SLOW_BY_CONDITION                            = "GatewayJoinEsDao/getGatewaySlowByCondition";
    /**
     * 根据index获得对应的templateMD5
     */
    public static final String GET_DSLS_BY_INDEX_NAME                                        = "GatewayJoinEsDao/getTemplateMD5ByIndexName";
    public static final String GET_ONE_DSL_BY_PROJECT_ID_AND_INDEX_NAME = "GatewayJoinEsDao/getOneDSLByProjectIdAndIndexName";

    /**
     * 根据index获得对应的查询请求
     */
    public static final String GET_SEARCH_REQUEST_BY_INDEX_NAME = "GatewayJoinEsDao/getSearchRequestByIndexName";
    /**
     * 根据projectId获得对应的查询99分位
     */
    public static final String GET_QUERY_RT_BY_PROJECT_ID           = "GatewayJoinEsDao/getQueryRtByProjectId";
    /**
     * 根据projectId获得查询topNum信息
     */
    public static final String GET_TOP_NUM_QUERY_INFO_BY_PROJECT_ID = "GatewayJoinEsDao/getQueryTopNumInfoByProjectId";
    /**
     * 获取某个查询模板一周内查询次数
     */
    public static final String GET_WEEK_SEARCH_COUNT_BY_MD5         = "GatewayJoinEsDao/getWeekSearchCountByMd5";
 
    /**
     * 根据索引名称和MD5获取一条查询明细中使用的字段信息
     */
    public static final String GET_GATEWAY_JOIN_BY_INDICES_MD5                               = "GatewayJoinEsDao/getPoByIndicesAndMd5";
    /**
     * 聚合查询gateway join日志索引，获取到某个索引查询使用的MD5列表
     */
    public static final String AGG_INDICES_MD5                                               = "GatewayJoinEsDao/aggIndicesDslMd5ByRange";
    /**
     * 获取查询被限流的MD5
     */
    public static final String GET_QUERY_LIMIT_ERROR_MD5                                     = "GatewayJoinEsDao/getQueryLimitErrorMd5";
    /**
     * 根据project id和MD5获取最新的一次查询记录
     */
    public static final String GET_ONE_GATEWAY_JOIN_BY_KEY                                   = "GatewayJoinEsDao/getFirstByProjectIdAndTemplateMd5";
    /**
     * 获取匹配的索引
     */
    public static final String MATCH_GATEWAY_INDICES                                         = "GatewayJoinEsDao/matchIndices";
    /**
     * 按时间聚合获取查询模板
     */
    public static final String AGG_MD5_BY_TIMESTAMP   = "GatewayJoinEsDao/getInfoByIds";
    /**
     * 按PROJECT id和查询模板进行聚合
     */
    public static final String AGG_PROJECT_ID_MD5     = "GatewayJoinEsDao/getIds";
    /**
     * 根据索引和MD5获取查询语句
     */
    public static final String GET_DSL_BY_MD5_INDICES = "GatewayJoinEsDao/getDslByIndexAndTemplateMD5";

    /**
     * 获取某个PROJECT id某一天的查询gateway 分布情况
     */
    public static final String GET_ACCESS_GATEWAY_INFO_BY_PROJECT_ID = "GatewayJoinEsDao/getAccessGatewayInfoByProjectIdDate";
    /**
     * 根据查询模板获取错误详情
     */
    public static final String GET_EXCEPTION_BY_MD5                  = "GatewayJoinEsDao/queryErrorDslDetailByProjectIdTemplateAndDate";
    /**
     * 根据异常名称获取查询模板MD5
     */
    public static final String AGG_MD5_BY_EXCEPTION_NAME                                     = "GatewayJoinEsDao/queryErrorDslByProjectIdExceptionAndDate";
    /**
     * 获取一小时内查询模板提取失败的次数
     */
    public static final String GET_FAILED_DSL_TEMPLATE                                       = "GatewayJoinEsDao/getFailedDslTemplateCount";
    /**
     * 获取查询模板提取失败的索引信息
     */
    public static final String GET_FAILED_DSL_TEMPLATE_INDICES = "GatewayJoinEsDao/getFailedDslTemplateSearchIndices";
    /**
     * 查询某个PROJECT id 一天查询总量
     */
    public static final String GET_SEARCH_COUNT_BY_PROJECT_ID = "GatewayJoinEsDao/getTotalSearchCountByProjectIdAndDate";
    /**
     * 获取qps信息(最大qps，平均qps，最小qps)
     */
    public static final String GET_QPS_INFO_BY_PROJECT_ID     = "GatewayJoinEsDao/getQpsInfoByProjectIdAndDate";
    /**
     * 获取查询耗时分位图
     */
    public static final String GET_COST_INFO_BY_PROJECT_ID = "GatewayJoinEsDao/getCostInfoByProjectIdAndDate";
    /**
     * 获取慢查语句dslTemplateMd5和次数
     */
    public static final String GET_SLOW_DSL_BY_PROJECT_ID  = "GatewayJoinEsDao/querySlowDslByProjectIdAndDate";
    /**
     * 根据projectId和dsl查询模板MD5获取具体慢查语句
     */
    public static final String GET_SLOW_DSL_BY_KEY         = "GatewayJoinEsDao/querySlowDslCountAndDetailByByProjectIdAndDslTemplate";
    /**
     * 根据projectId和查询模板获取某一天查询最大值
     */
    public static final String GET_GATEWAY_JOIN_MAX_QPS_BY_KEY  = "GatewayJoinEsDao/queryMaxSearchQpsByProjectIdAndDslTemplate";
    /**
     * 获取某个projectId某一天的查询错误数和错误信息
     */
    public static final String AGG_EXCEPTION_NAME_BY_PROJECT_ID = "GatewayJoinEsDao/getErrorSearchCountAndErrorDetailByProjectIdDate";
    /**
     * 获取指定projectId和时间范围下查询方式统计次数
     */
    public static final String GET_REQUEST_TYPE_BY_PROJECT_ID            = "GatewayJoinEsDao/getRequestTypeByProjectId";
    /**
     * GET_ACCESS_INDEX_NAME_BY_PROJECT_ID
     */
    public static final String GET_ACCESS_INDEX_NAME_BY_PROJECT_ID            = "GatewayJoinEsDao/getAccessIndexNameByProjectId";
    /**
     * 获取指定projectId一段时间内的错误查询记录
     */
    public static final String GET_GATEWAY_ERROR_LIST_BY_PROJECT_ID_AND_RANGE = "GatewayJoinEsDao/getGatewayErrorListByProjectId";
    /**
     * 获取指定一段时间内的错误查询记录
     */
    public static final String GET_GATEWAY_ERROR_LIST_BY_RANGE                = "GatewayJoinEsDao/getGatewayErrorList";
    /**
     * 获取指定索引的查询错误个数
     */
    public static final String GET_ERROR_CNT_BY_TEMPLATE_NAME                                = "GatewayJoinEsDao/getErrorCntByTemplateName";
    /**
     * 获取指定索引的查询错误个数
     */
    public static final String GET_SLOW_CNT_BY_TEMPLATE_NAME                 = "GatewayJoinEsDao/getSlowCntByTemplateName";
    /**
     * 获取指定projectId一段时间内的慢查询记录
     */
    public static final String GET_GATEWAY_SLOW_LIST_BY_PROJECT_ID_AND_RANGE = "GatewayJoinEsDao/getGatewaySlowListByProjectId";
    /**
     * 获取指定projectId一段时间内的慢查询记录
     */
    public static final String GET_GATEWAY_SLOW_LIST_BY_RANGE       = "GatewayJoinEsDao/getGatewaySlowList";
    /**
     * 根据索引模板逻辑id，获取查询语句中使用的type名称
     */
    public static final String GET_SEARCH_TYPES_BY_LOGIC_ID               = "GatewayJoinEsDao/getSearchTypesByLogicId";
    /**
     * 根据时间范围和projectId获取查询量
     */
    public static final String GET_SEARCH_COUNT_BY_PROJECT_ID_TIME_RANGE   = "GatewayJoinEsDao/getSearchCountByProjectId";
    /**
     * 获取多type查询映射后的索引信息
     */
    public static final String SCROLL_MULITY_TYPE_GATEWAY_JOIN_BY_SHARD_NO = "GatewayJoinEsDao/scrollMulityTypeByShardNo";
    /**
     * 获取请求日志
     */
    public static final String SCROLL_REQUEST_LOG_GATEWAY_JOIN_BY_SHARD_NO = "GatewayJoinEsDao/scrollRequestLogByShardNo";

    /************************************************************** GatewayOverviewMetricsDao **************************************************************/
    /**
     * 获取总览公共指标。totalHits，totalCost，totalShards，failedShards
     */
    public static final String GET_GATEWAY_COMMON_METRICS                                    = "GatewayOverviewMetricsDao/getCommonMetrics";

    /**
     * 获取查询量
     */
    public static final String GET_GATEWAY_READ_COUNT                                        = "GatewayOverviewMetricsDao/getReadCount";
    /**
     * 获取dsl/sql分布。searchType
     */
    public static final String GET_GATEWAY_SEARCH_TYPE                                       = "GatewayOverviewMetricsDao/getSearchType";
    /**
     * 获取写入指标。写入量，写入平均耗时，写入平均响应长度
     */
    public static final String GET_GATEWAY_WRITE_METRICS                                     = "GatewayOverviewMetricsDao/getWriteMetrics";

    /************************************************************** GatewayIndexMetricsDao **************************************************************/
    /**
     * 获取索引模版写入量，写入耗时 topN
     */
    public static final String GET_GATEWAY_INDEX_WRITE                                       = "GatewayIndexMetricsDao/getAggWrite";

    /**
     * 获取索引模版写入量，写入耗时 by templateName
     */
    public static final String GET_GATEWAY_INDEX_WRITE_BY_TEMPLATE_NAME                      = "GatewayIndexMetricsDao/getAggWriteByTemplateName";

    /**
     * 获取索引模版查询量，查询耗时 topN
     */
    public static final String GET_GATEWAY_INDEX_SEARCH                                      = "GatewayIndexMetricsDao/getAggSearch";

    /**
     * 获取索引模版查询量，查询耗时 by templateName
     */
    public static final String GET_GATEWAY_INDEX_SEARCH_BY_TEMPLATE_NAME                     = "GatewayIndexMetricsDao/getAggSearchByTemplateName";

    /************************************************************** GatewayNodeMetricsDao **************************************************************/

    /**
     * 获取某 gatewayNode 读分布
     */
    public static final String GET_GATEWAY_NODE_FIELD_BY_IP                                  = "GatewayNodeMetricsDao/getAggFieldByIp";

    /**
     * 获取 topN gatewayNode 读分布
     */
    public static final String GET_GATEWAY_NODE_FIELD                                        = "GatewayNodeMetricsDao/getAggField";

    /**
     * 获取 topN gatewayNode 写分布
     */
    public static final String GET_GATEWAY_NODE_WRITE                                        = "GatewayNodeMetricsDao/getAggWriteAndGatewayNode";

    /**
     * 获取某 gatewayNode 写分布
     */
    public static final String GET_GATEWAY_NODE_WRITE_BY_IP                                  = "GatewayNodeMetricsDao/getAggWriteAndGatewayNodeByIp";

    /**
     * 获取某 clientNode 读分布
     */
    public static final String GET_CLIENT_NODE_FIELD_BY_IP                                   = "GatewayNodeMetricsDao/getClientNodeAggFieldByIp";

    /**
     * 获取 topN clientNode 读分布
     */
    public static final String GET_CLIENT_NODE_FIELD                                         = "GatewayNodeMetricsDao/getClientNodeAggField";

    /**
     * 获取 topN clientNode 写分布
     */
    public static final String GET_CLIENT_NODE_WRITE                                         = "GatewayNodeMetricsDao/getClientNodeAggWrite";

    /**
     * 获取某 clientNode 写分布
     */
    public static final String GET_CLIENT_NODE_WRITE_BY_IP                                   = "GatewayNodeMetricsDao/getClientNodeAggWriteByIp";

    /**
     * 获取 gatewayNode 相关的 clientNode ip 信息
     */
    public static final String GET_CLIENT_NODE_BY_GATEWAY_NODE                               = "GatewayNodeMetricsDao/getClientNodeByGatewayNode";

    /************************************************************** GatewayAppMetricsDao **************************************************************/
    /**
     * 获取某个字段分布.(searchCost，totalCost)
     */
    public static final String GET_GATEWAY_APP_FIELD                                         = "GatewayAppMetricsDao/getAggField";

    /**
     * 获取某个字段分布.(searchCost，totalCost) by projectId
     */
    public static final String GET_GATEWAY_APP_FIELD_BY_PROJECT_ID = "GatewayAppMetricsDao/getAggFieldByProjectId";
    /**
     * 获取各App查询量
     */
    public static final String GET_GATEWAY_APP_FIELD_COUNT         = "GatewayAppMetricsDao/getAggFieldCount";

    /**
     * 获取各App查询量 by projectId
     */
    public static final String GET_GATEWAY_APP_FIELD_COUNT_BY_PROJECT_ID = "GatewayAppMetricsDao/getAggFieldCountByProjectId";

    /************************************************************** GatewayDslMetricsDao **************************************************************/
    public static final String GET_TOTAL_COST_BY_FIELD = "GatewayDslMetricsDao/getTotalCostByField";
    public static final String GET_DSL_COUNT_BY_FILELD = "GatewayDslMetricsDao/getDslCountByFileld";
    public static final String GET_DSL_COUNT_EXTENDED_BOUNDS_BY_FILELD = "GatewayDslMetricsDao/getDslCountExtendedBoundsByFileld";
    /**
     * 获取某个指定字段下第一个有时间的数据
     */
    public static final String GET_HAS_METRIC_INFO_TIME_BY_FIELD = "GatewayDslMetricsDao/getHasMetricInfoTimeByField";
    /**
     * 保持和之前的结果相同
     */
    public static final String GET_TOTAL_COST_EXTENDED_BOUNDS_BY_FIELD = "GatewayDslMetricsDao/getTotalCostExtendedBoundsByField";
    /**
     * 获取各个查询模版访问量 topNu
     */
    public static final String GET_GATEWAY_DSL_COUNT                                         = "GatewayDslMetricsDao/getDslCount";

    /**
     * 获取某个查询模版访问量 by dslTemplateMd5
     */
    public static final String GET_GATEWAY_DSL_COUNT_BY_MD5                                  = "GatewayDslMetricsDao/getDslCountByMd5";
    /**
     * 获取各个查询模版访问耗时 topNu
     */
    public static final String GET_GATEWAY_DSL_TOTAL_COST                                    = "GatewayDslMetricsDao/getDslTotalCost";

    /**
     * 获取某个查询模版访问耗时 by dslTemplateMd5
     */
    public static final String GET_GATEWAY_DSL_TOTAL_COST_BY_MD5 = "GatewayDslMetricsDao/getDslTotalCostByMd5";
    /**
     * 获取某个projectId下的 dslTemplateMd5
     */
    public static final String GET_GATEWAY_DSL_MD5_BY_PROJECT_ID = "GatewayDslMetricsDao/getDslMd5ByProjectId";

    /************************************************************** DslAnalyzeResultTypeEsDao **************************************************************/
    /**
     * 根据projectId查询分析结果
     */
    public static final String GET_DSL_ANALYZE_RESULT_BY_PROJECT_ID           = "DslAnalyzeResultTypeEsDao/getDslAnalyzeResultByProjectId";
    /**
     * 根据projectId查询[startDate, endDate]分析结果
     */
    public static final String GET_DSL_ANALYZE_RESULT_BY_PROJECT_ID_AND_RANGE = "DslAnalyzeResultTypeEsDao/getDslAnalyzeResultByProjectIdAndRange";

    /**************************************************************      DslTemplateEsDao     **************************************************************/
    /**
     * 根据指定条件分页获取dsl查询模版数据
     */
    public static final String GET_DSL_TEMPLATE_BY_CONDITION                                 = "DslTemplateEsDao/getDslTemplateByCondition";
    /**
     * 根据指定条件分页获取没有元数据集群的dsl查询模版数据
     */
    public static final String GET_DSL_TEMPLATE_BY_CONDITION_WITHOUT_METADATA_CLUSTER        = "DslTemplateEsDao/getDslTemplateByConditionWithoutMetadataCluster";
    /**
     * 获取所有查询模板最近修改时间在(now-1d,now)范围内，并且不启用的查询模板，然后删除过期的查询模板数据
     */
    public static final String GET_EXPIRED_DSL_TEMPLATE       = "DslTemplateEsDao/deleteExpiredDslTemplate";
    /**
     * 获取某个projectId的所有查询模板数据,已排除老查询模板
     */
    public static final String GET_DSL_TEMPLATE_BY_PROJECT_ID = "DslTemplateEsDao/getAllDslTemplatePoByProjectId";
    /**
     * 获取查询模板创建时间大于指定天偏移的查询模板数据，用于设置慢查耗时阈值
     */
    public static final String GET_DSL_TEMPLATES_BY_RANGE     = "DslTemplateEsDao/getDslTemplatesByDateRange";
    /**
     * 滚动获取查询模板
     */
    public static final String SCROLL_DSL_TEMPLATES                                          = "DslTemplateEsDao/handleScrollDslTemplates";
    /**
     * 根据索引名称获取查询的查询模板
     */
    public static final String GET_DSL_TEMPLATES_BY_INDEXNAME                                = "DslTemplateEsDao/getTemplateMD5ByIndexName";
    /**
     * 根据索引名称获取查询的查询模板
     */
    public static final String GET_DSL_TEMPLATES_BY_INDEXNAME_WITH_DAY_RANGE       = "DslTemplateEsDao/getTemplateMD5ByIndexNameWithDayRange";
    /**
     * 根据索引名称获取查询的查询模板
     */
    public static final String GET_DSL_TEMPLATES_BY_INDEXNAME_PROJECT_ID                = "DslTemplateEsDao/getTemplateMD5ByIndexNameAndProjectId";
    /**
     * 根据索引名称获取查询的查询模板
     */
    public static final String GET_DSL_TEMPLATES_BY_INDEXNAME_PROJECT_ID_WITH_DAY_RANGE = "DslTemplateEsDao/getTemplateMD5ByIndexNameAndProjectIdWithDayRange";
    /**
     * 获取早期查询模板，不包括手动修改限流值
     */
    public static final String GET_EARLIEST_DSL_TEMPLATES                               = "DslTemplateEsDao/getEarliestDslTemplate";
    /**
     * 获取一段时间不使用的查询模板，不包括黑名单和手动修改限流值
     */
    public static final String GET_LONG_TIME_NOT_USE_DSL_TEMPLATES      = "DslTemplateEsDao/getLongTimeNotUseDslTemplate";
    /**
     * 获取projectId一段时间内的dslMetrics
     */
    public static final String GET_DSL_TEMPLATE_BY_PROJECT_ID_AND_RANGE = "DslTemplateEsDao/getDslTemplateByProjectIdAndRange";
    /**
     * 获取过期的查询模板信息
     */
    public static final String GET_EXPIRED_DELETED_DSL_TEMPLATE        = "DslTemplateEsDao/getExpiredAndWillDeleteDslTemplate";
    /**
     * 获取某个projectId的新增查询模板个数,已排除老查询模板
     */
    public static final String GET_INCREASE_DSL_TEMPLATE_BY_PROJECT_ID = "DslTemplateEsDao/getIncreaseTemplateCountByProjectId";
    /**
     * 获取某个projectId的查询模板个数,已排除老版本查询模板
     */
    public static final String GET_DSL_TEMPLATE_COUNT_BY_PROJECT_ID    = "DslTemplateEsDao/getTemplateCountByProjectId";
    /**
     * 获取最近查询模板，不包括手动修改限流值和黑名单
     */
    public static final String GET_NEAREST_DSL_TEMPLATES               = "DslTemplateEsDao/getNearestDslTemplate";
    /**
     * 获取最近没有设置黑白名单的查询模板,已排除老查询模板
     */
    public static final String GET_NEAREST_DSL_TEMPLATE_ACCESSABLE                           = "DslTemplateEsDao/getNearestDslTemplateAccessable";
    /**
     * 获取到缺少ariusCreateTime字段的文档
     */
    public static final String GET_MISSING_ARIUS_CREATE_TIME                                 = "DslTemplateEsDao/getMissingAriusCreateTime";

    /**************************************************************      IndexSizeEsDao     **************************************************************/
    /**
     * 根据索引模板名称获取索引大小信息
     */
    public static final String GET_INDEX_SIZE_BY_TEMPLATE                                    = "IndexSizeEsDao/getIndexSizeByTemplateName";
    /**
     * 获取昨天索引大小
     */
    public static final String GET_YESTERDAY_INDEX_SIZE                                      = "IndexSizeEsDao/getYesterDayIndexSize";

    /************************************************************      IndexCatESDAO     **************************************************************/
    /**
     * 条件查询索引cat/index信息
     */
    public static final String GET_CAT_INDEX_INFO_BY_CONDITION                               = "IndexCatESDAO/getCatIndexInfoByCondition";

    /**
     * 条件查询索引cat/index信息
     */
    public static final String GET_ALL_CAT_INDEX_INFO_BY_TERMS                               = "IndexCatESDAO/getAllCatIndexInfoByTerms";

    public static final String GET_PLATFORM_CREATE_CAT_INDEX  = "IndexCatESDAO/getPlatformCreateCatIndex";
    public static final String GET_ALL_CAT_INDEX_NAME_BY_CLUSTERS  = "IndexCatESDAO/getAllCatIndexNameByClusters";
    public static final String GET_ALL_CAT_INDEX_NAME_BY_RESOURCE_ID  = "IndexCatESDAO/getAllCatIndexNameByResourceId";
    public static final String GET_PLATFORM_CREATE_CAT_INDEX_BY_INDEX_PROJECT  = "IndexCatESDAO/getPlatformCreateCatIndexByIndexProject";
    public static final String GET_PLATFORM_CREATE_CAT_INDEX_BY_INDEX_PROJECT_AND_FUZZY_INDEX_AND_CLUSTER_LOGIC  = "IndexCatESDAO/getPlatformCreateCatIndexByIndexProjectAndFuzzyIndex";
    public static final String GET_PLATFORM_CREATE_CAT_INDEX_BY_INDEX_PROJECT_AND_FUZZY_INDEX_AND_CLUSTER_PHY  =
            "IndexCatESDAO/getPlatformCreateCatIndexByIndexProjectAndFuzzyIndexAndClusterPhy";
    public static final String GET_PLATFORM_CREATE_CAT_INDEX_GROUP_BY_CLUSTER  = "IndexCatESDAO/getPlatformCreateCatIndexGroupByCluster";
    
    public static final String GET_PLATFORM_CREATE_CAT_INDEX_BY_ID  = "IndexCatESDAO/getPlatformCreateCatIndexById";

    /************************************************************      ShardCatESDAO     **************************************************************/
    /**
     * 条件查询索引cat/shard信息
     */
    public static final String GET_CAT_SHARD_INFO_BY_CONDITION                               = "ShardCatESDAO/getCatShardInfoByCondition";

    /**
     * 构造多字段模糊查询
     */
    public static final String SHOULD_TERM_CELL                                              = "ShardCatESDAO/shouldTermCell";




    /**************************************************************  IndexNameAccessCountEsDao **************************************************************/
    /**
     * 根据索引模板名称获取查询的具体索引统计次数
     */
    public static final String SCROLL_GET_TEMPLATE_DETAIL_BY_TEMPLATE                        = "IndexNameAccessCountEsDao/getIndexNameAccessByTemplate";
    /**
     * 根据索引模板ID获取查询的具体索引统计次数
     */
    public static final String SCROLL_GET_TEMPLATE_DETAIL_BY_ID                              = "IndexNameAccessCountEsDao/getIndexNameAccessByTemplateId";

    /************************************************************** projecTTemplateAccessCountEsDao **************************************************************/
    /**
     * 根据索引模板获取访问projectId列表
     */
    public static final String GET_ACCESS_PROJECT_ID_BY_TEMPLATE_NAME                     = "ProjectIdTemplateAccessCountEsDao/getAccessProjectIdByTemplateName";
    /**
     * 根据索引模板Id获取最近days访问projectId详细信息
     */
    public static final String GET_ACCESS_PROJECT_ID_INFO_BY_LOGIC_EMPLATE_ID                 = "ProjectIdTemplateAccessCountEsDao/getAccessProjectIdInfoByLogicTemplateId";
    /**
     * 根据索引模板Id获取【statdDate,endDate】访问projectId详细信息
     */
    public static final String GET_ACCESS_PROJECT_ID_INFO_BY_LOGIC_TEMPLATE_ID_AND_DATE_RANGE = "ProjectIdTemplateAccessCountEsDao/getAccessProjectIdInfoByLogicTemplateIdAndDateRange";

    /************************************************************** projectIdTemplateAccessCountEsDao **************************************************************/
    /**
     * 获取一个查询模板的数据
     */
    public static final String GET_DSL_METRICS_BY_KEY              = "DslMetricsEsDao/getNeariestDslMetricsByProjectIdTemplateMd5";
    /**
     * 查询某个projectId一天查询次数
     */
    public static final String GET_TOTAL_SEARCHCOUNT_BY_PROJECT_ID = "DslMetricsEsDao/getTotalSearchByProjectIdDate";
    /**
     * 获取最大一分钟查询量
     */
    public static final String GET_MAX_QPS_BY_KEY                                     = "DslMetricsEsDao/getMaxProjectIdTemplateQpsInfoByProjectIdTemplateMd5";
    /**
     * 根据projectId和dslMd5获取一段时间内的详细metrics
     */
    public static final String GET_DSL_DETAIL_METRICS_BY_PROJECT_ID_AND_MD5_AND_RANGE = "DslMetricsEsDao/getDslDetailMetricByProjectIdAndDslTemplateMd5";
    /**
     * 根据projectId和dslMd5获取一段时间内的详细metrics
     */
    public static final String GET_PROJECT_ID_TEMPLATE_MD5_INFO = "DslMetricsEsDao/getProjectIdTemplateMd5Info";
    /**
     * 根据时间范围查询某个projectId的记录数
     */
    public static final String GET_TOTAL_HITS_BY_PROJECT_ID     = "DslMetricsEsDao/queryTotalHitsByProjectIdDate";

    /************************************************************** DslAnalyzeResultQpsEsDao **************************************************************/
    /**
     * 获取查询模板级别最大qps
     */
    public static final String GET_MAX_QPS_BY_PROJECT_ID_DSLTEMPLATE = "DslAnalyzeResultQpsEsDao/getMaxProjectIdTemplateQpsInfoByProjectIdTemplateMd5";

    

    /**************************************************************   IndexTemplateValueEsDao   **************************************************************/
    /**
     * 获取全部模板的价值
     */
    public static final String SCROLL_VALUE_LIST_ALL                                         = "IndexTemplateValueEsDao/listAll";



    /**************************************************************   DslFieldUseEsDao   **************************************************************/
    /**
     * 根据模板名称获取字段使用信息,最多获取30天
     */
    public static final String GET_FIELD_USE_BY_TEMPLATE                                     = "DslFieldUseEsDao/getFieldUseListByTemplateName";
    /**
     * 根据模板名称获取字段使用信息
     */
    public static final String GET_ONE_FIELD_USE_BY_TEMPLATE                                 = "DslFieldUseEsDao/getFieldUseInfoByTemplateName";


    /**
     * 获取集群task耗时分位图
     */
    public static final String AGG_CLUSTER_TASK_REAL_TIME_AVG_AND_PERCENT                    = "AriusStatsClusterTaskInfoEsDao/aggClusterTaskCostAvgAndPercentiles";

    /**
     * 获取集群task数量
     */
    public static final String AGG_CLUSTER_TASK_COUNT                                        = "AriusStatsClusterTaskInfoEsDao/aggClusterTaskCount";

    /**
     * 获取集群各个节点上的任务总耗时
     */
    public static final String AGG_CLUSTER_NODES_TASK_COST                                   = "AriusStatsClusterTaskInfoEsDao/aggClusterNodesTaskCost";

    /**
     * 获取集群节点task统计信息
     */
    public static final String GET_AGG_CLUSTER_NODE_TASK_INFO                                = "AriusStatsClusterTaskInfoEsDao/getAggClusterPhyNodesTaskInfo";

    /**
     * 获取集群节点top的task统计信息
     */
    public static final String GET_AGG_CLUSTER_TOP_NODE_TASK_INFO                            = "AriusStatsClusterTaskInfoEsDao/getTopNNodeTaskAggMetricsInfo";

    /**
     * 获取指定集群节点的task统计信息
     */
    public static final String GET_AGG_CLUSTER_SINGLE_NODE_TASK_INFO                         = "AriusStatsClusterTaskInfoEsDao/getAggClusterPhySingleNodeTaskInfo";

    /**
     * 获取指定集群节点的task详细信息
     */
    public static final String GET_CLUSTER_SINGLE_NODE_TASK_DETAIL_INFO                      = "AriusStatsClusterTaskInfoEsDao/getClusterPhySingleNodeTaskDetailInfo";

    /**
     * 获取指定集群节点有task信息的时间戳
     */
    public static final String GET_HAS_CLUSTER_NODE_TASK_DATA_TIME                           = "AriusStatsClusterTaskInfoEsDao/getHasNodeTaskInfoTime";

    /**
     * 获取指定集群采集最近的时间点
     * getTimeDifferenceBetweenNearestPointAndNow
     */
    public static final String GET_TIME_DIFFERENCE_BETWEEN_NEAREST_POINT_AND_NOW             = "AriusStatsClusterPhyInfoEsDao/getTimeDifferenceBetweenNearestPointAndNow";

    /***********************************************   AriusStatsDashBoardInfoESDAO   **************************************************************/
    public static final String FETCH_CLUSTER_HEALTH_INFO                                     = "AriusStatsDashBoardInfoESDAO/fetchClusterHealthInfo";

    public static final String GET_HAS_DASHBOARD_METRICS_DATA_TIME                           = "AriusStatsDashBoardInfoESDAO/getHasDashboardMetricInfoTime";

    /**
     * 根据一级指标类型构建最终查询dsl（针对cluster\clusterThreadPoolQueue维度指标）
     */
    public static final String GET_AGG_DASHBOARD_CLUSTER_TOP_NAME_INFO                       = "AriusStatsDashBoardInfoESDAO/getAggDashboardClusterTopNameInfo";
    /**
     * 根据一级指标类型构建最终查询dsl（针对非cluster维度指标）
     */
    public static final String GET_AGG_DASHBOARD_NO_CLUSTER_TOP_NAME_INFO                    = "AriusStatsDashBoardInfoESDAO/getAggDashboardNoClusterTopNameInfo";

    /**
     * 获取filter agg 的过滤片段
     */
    public static final String GET_AGG_FILTER_FRAGMENT                                       = "AriusStatsDashBoardInfoESDAO/getAggFilterFragment";



    /**
     * 获取top Dashboard维度统计信息(针对cluster维度、clusterThreadPoolQueue维度指标)
     */
    public static final String GET_TOP_DASHBOARD_CLUSTER_AGG_METRICS_INFO                    = "AriusStatsDashBoardInfoESDAO/getTopDashboardClusterAggMetricsInfo";

    /**
     * 获取top Dashboard维度统计信息(针对非cluster维度指标)
     */
    public static final String GET_TOP_DASHBOARD_NO_CLUSTER_AGG_METRICS_INFO                    = "AriusStatsDashBoardInfoESDAO/getTopDashboardNoClusterAggMetricsInfo";

    /**
     * 获取List类型 Dashboard维度统计信息(带标识的类型)
     */
    public static final String FETCH_LIST_FLAG_METRIC                                        =
            "AriusStatsDashBoardInfoESDAO/fetchListFlagMetric";
    
        /**
     * 获取List类型 Dashboard维度统计信息(阈值处理)
     */
    public static final String FETCH_LIST_THRESHOLDS_METRIC                                        =
            "AriusStatsDashBoardInfoESDAO/fetchListThresholdsMetric";

    /**
     * 获取List类型 Dashboard维度统计信息(带数据值的类型)
     */
    public static final String FETCH_LIST_VALUE_METRIC                                       = "AriusStatsDashBoardInfoESDAO/fetchListValueMetrics";
}