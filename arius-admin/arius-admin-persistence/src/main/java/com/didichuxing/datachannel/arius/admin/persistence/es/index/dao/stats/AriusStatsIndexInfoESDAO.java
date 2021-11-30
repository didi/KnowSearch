package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexRealTimeInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.TopMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.IndexNameQueryAvgRatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.TemplateTpsMetricPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.common.util.MetricsUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum.INDEXING_TOTAL_RATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyIndicesMetricsEnum.QUERY_RATE;

@Component
public class AriusStatsIndexInfoESDAO extends BaseAriusStatsESDAO {

    private static final String     SIZE_NUM             = "sizeSum";
    private static final String     VALUE                = "value";
    private static final String     VALUES               = "values";
    private static final String     MAX_TPS              = "max_tps";
    private static final String     GROUP_BY_TEMPLATE_ID = "groupByTemplateId";
    private static final FutureUtil<Void> futureUtil     = FutureUtil.initBySystemAvailableProcessors("AriusStatsIndexInfoESDAO",  100);

    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsIndexInfo();

        BaseAriusStatsESDAO.register(AriusStatsEnum.INDEX_INFO, this);
    }

    /**
     * 根据集群名称，获取集群[now-2m, now-1m]总的qps 在index维度汇总
     * @param cluster
     * @return
     */
    public long getClusterQps(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_REAL_TIME_TPS_QPS_INFO, cluster,
            "now-2m", "now-1m", QUERY_RATE.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "sum").longValue(),
            3);
    }

    /**
     * 根据集群名称，获取集群[now-2m, now-1m]总的tps
     * @param cluster
     * @return
     */
    public long getClusterTps(String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_REAL_TIME_TPS_QPS_INFO, cluster,
            "now-2m", "now-1m", INDEXING_TOTAL_RATE.getType());
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "sum").longValue(),
            3);
    }

    /**
     * 根据模板名称和集群名称，获取模板的总大小(单位：byte)
     * @param template
     * @param cluster
     * @return
     */
    public double getTemplateTotalSize(String template, String cluster) {
        Long now = System.currentTimeMillis();
        return getTemplateTotalSizeByTimeRange(template, cluster, now - 15 * 60 * 1000, now);
    }

    /**
     * 查询模板一段时间访问内的最大容量
     * @param template
     * @param cluster
     * @param startTime
     * @param endTime
     * @return
     */
    public double getTemplateTotalSizeByTimeRange(String template, String cluster, Long startTime, Long endTime) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_TOTAL_SIZE_BY_TIME_RANGE, template,
            cluster, startTime, endTime);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, SIZE_NUM), 3);
    }

    /**
     * 根据物理模板ID获取模板的总大小
     * @param templateId
     * @return
     */
    public double getTemplateTotalSize(Long templateId) {
        Long now = System.currentTimeMillis();
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_TOTAL_SIZE_BY_TEMPLATE_ID,
            templateId, now - 15 * 60 * 1000, now);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, SIZE_NUM), 3);
    }

    /**
     * 根据逻辑模板ID，获取模板的总大小(单位：byte)，主备情况下，主备索引总量都会统计
     * @param logicTemplateId
     * @return
     */
    public double getLogicTemplateTotalSize(Long logicTemplateId) {
        Long now = System.currentTimeMillis();
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_LOGIC_TEMPLATE_TOTAL_SIZE_BY_TEMPLATE_ID,
            logicTemplateId, now - 15 * 60 * 1000, now);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, SIZE_NUM), 3);
    }

    /**
     * 获取索引模板一段时间的最大索引容量
     * @param template
     * @param cluster
     * @param startTime
     * @param endTime
     * @return
     */
    public long getTemplateMaxIndexSize(String template, String cluster, Long startTime, Long endTime) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_MAX_INDEX_SIZE, template, cluster,
            startTime, endTime);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
            s -> getSumFromESQueryResponse(s, "sizeMax").longValue(), 3);
    }

    /**
     * 根据模板名称，获取模板的总文档数
     * @param template
     * @param cluster
     * @return
     */
    public long getTemplateTotalDocNu(String template, String cluster) {
        Long now = System.currentTimeMillis();
        return getTemplateTotalDocNuByTimeRange(template, cluster, now - 15 * 60 * 1000, now);
    }

    /**
     * 查询模板一段时间访问内的最大模板总文档数
     * @param template
     * @param cluster
     * @param startTime
     * @param endTime
     * @return
     */
    public long getTemplateTotalDocNuByTimeRange(String template, String cluster, Long startTime, Long endTime) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_TOTAL_DOC_BY_TIME_RANGE, template,
            cluster, startTime, endTime);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
            s -> getSumFromESQueryResponse(s, "docSum").longValue(), 3);
    }

    /**
     * 查询模板一段时间访问内的最大模板总文档数
     * @param logicTemplateId
     * @param startTime
     * @param endTime
     * @return
     */
    public long getTemplateTotalDocNuByTimeRange(Long logicTemplateId, Long startTime, Long endTime) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(
            DslsConstant.GET_TEMPLATE_TOTAL_DOC_BY_LOGIC_TEMPLATE_ID_AND_TIME_RANGE, logicTemplateId, startTime,
            endTime);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
            s -> getSumFromESQueryResponse(s, "docSum").longValue(), 3);
    }

    /**
     * 获取索引模板一段时间的最大文档条数
     * @param template
     * @param cluster
     * @param startTime
     * @param endTime
     * @return
     */
    public long getTemplateMaxIndexDoc(String template, String cluster, Long startTime, Long endTime) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_MAX_INDEX_DOC, template, cluster,
            startTime, endTime);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
            s -> getSumFromESQueryResponse(s, "docMax").longValue(), 3);
    }

    /**
     * 查询模板一段时间访问内的总的tps
     * @param logicTemplateId
     * @param startTime
     * @param endTime
     * @return
     */
    public double getTemplateMaxTpsByTimeRange(Long logicTemplateId, Long startTime, Long endTime) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_TOTAL_MAX_TPS_BY_TIME_RANGE,
            logicTemplateId, startTime, endTime);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "tpsMax"), 3);
    }

    public double getTemplateMaxTpsByTimeRangeNoPercent(Long logicTemplateId, Long startTime, Long endTime) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(
            DslsConstant.GET_TEMPLATE_TOTAL_MAX_TPS_BY_TIME_RANGE_NO_PERCENT, logicTemplateId, startTime, endTime);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> getSumFromESQueryResponse(s, "tpsMax"), 3);
    }

    /**
     * 根据模板和集群，获取一段时间内的模板分钟级别平均实时数据
     * @param startDate
     * @param endDate
     * @param template
     * @param cluster
     * @return
     */
    public IndexRealTimeInfo getIndexRealTimeInfoByTemplateAndCluster(int offset, long startDate, long endDate,
                                                                      String template, String cluster) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_REAL_INFO_BY_TEMPLATE_AND_CLUSTER,
            startDate, endDate, cluster, template);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, offset);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, response -> {

            if (response == null) {
                return null;
            }

            IndexRealTimeInfo indexRealTimeInfo = new IndexRealTimeInfo();
            indexRealTimeInfo.setTemplate(template);
            indexRealTimeInfo.setCluster(cluster);
            indexRealTimeInfo.setStart(new Date(startDate));
            indexRealTimeInfo.setEnd(new Date(endDate));

            try {
                Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();

                ESAggr avgQueryTotalRate = esAggrMap.get("avg_search-query_total_rate");
                ESAggr avgIndexTotalRate = esAggrMap.get("avg_indexing-index_total_rate");
                ESAggr avgQueryAvgRate = esAggrMap.get("avg_indices-search-query_avg_time");

                if (null != avgQueryTotalRate.getUnusedMap() && null != avgQueryTotalRate.getUnusedMap().get(VALUE)) {
                    indexRealTimeInfo.setAvgSearchQueryTotalRate(
                        Double.valueOf(avgQueryTotalRate.getUnusedMap().get(VALUE).toString()));
                }

                if (null != avgIndexTotalRate.getUnusedMap() && null != avgIndexTotalRate.getUnusedMap().get(VALUE)) {
                    indexRealTimeInfo.setAvgIndexingIndexTotalRate(
                        Double.valueOf(avgIndexTotalRate.getUnusedMap().get(VALUE).toString()));
                }

                if (null != avgQueryAvgRate.getUnusedMap() && null != avgQueryAvgRate.getUnusedMap().get(VALUE)) {
                    indexRealTimeInfo.setAvgIndicesSearchQueryTime(
                        Double.valueOf(avgQueryAvgRate.getUnusedMap().get(VALUE).toString()));
                }
            } catch (Exception e) {
                LOGGER.error(
                    "class=AriusStatsIndexInfoEsDao||method=getIndexRealTimeInfoByTemplateAndCluster||errMsg=exception! response:{}",
                    response.toString(), e);
            }

            return indexRealTimeInfo;
        }, 3);
    }

    /**
     * 根据集群名称和模板名称获取模板的最大统计信息
     * @param tempalte
     * @param cluster
     * @return
     */
    public Map<String, String> getTemplateMaxInfo(String tempalte, String cluster, Long startTime, Long endTime) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String maxPercentRate = "95.0";
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_MAXINFO_BY_TIME_RANGE_AND_TEMPLATE,
            startTime, endTime, tempalte, cluster, maxPercentRate, maxPercentRate, maxPercentRate);

        return gatewayClient.performRequest(realIndexName, TYPE, dsl, response -> {
            Map<String, String> ret = new HashMap<>();

            if (response == null) {
                return ret;
            }

            try {
                Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();

                ESAggr maxTpsESAggr = esAggrMap.get(MAX_TPS);
                ESAggr maxQueryTimeESAggr = esAggrMap.get("max_query_time");
                ESAggr maxScrollTimeESAggr = esAggrMap.get("max_scroll_time");

                handleMaxTpsESAggr(maxPercentRate, ret, maxTpsESAggr);

                handleMaxQueryTimeESAggr(maxPercentRate, ret, maxQueryTimeESAggr);

                handleMaxScrollTimeESAggr(maxPercentRate, ret, maxScrollTimeESAggr);
            } catch (Exception e) {
                LOGGER.error("class=AriusStatsIndexInfoEsDao||method=getTemplateMaxInfo||errMsg=exception! response:{}",
                    response.toString(), e);
            }

            return ret;

        }, 3);
    }

    /**
     * 获取指定时间范围的指定索引模板写入最大值和最近15分钟均值
     * @param logicId
     * @param startTime 毫秒
     * @param endTime   毫秒
     * @return
     */

    /**
     * 解析结果
            {
                "groupByTemplateId" : {
                  "doc_count_error_upper_bound" : 0,
                  "sum_other_doc_count" : 0,
                  "buckets" : [
                    {
                      "key" : 14679,
                      "doc_count" : 47520,
                      "hour_buckets" : {
                        "buckets" : []
                      },
                      "max_tps" : {
                        "value" : 445788.5691677049,
                        "keys" : ["1566432000000"]
                      }
                    },
                    {
                      "key" : 15737,
                      "doc_count" : 47520,
                      "hour_buckets" : {
                        "buckets" : []
                      },
                      "max_tps" : {
                        "value" : 445098.5182184981,
                        "keys" : ["1566432000000"]
                      }
                    }
                  ]
                },
                "max_tps_templateId" : {
                  "value" : 445788.5691677049,
                  "keys" : ["14679"]
                }
         }
     */

    /**
     * 解析结果
            {
                "groupByTemplateId" : {
                "doc_count_error_upper_bound" : 0,
                    "sum_other_doc_count" : 0,
                    "buckets" : [
                    {
                      "key" : 14505,
                      "doc_count" : 180,
                      "minute_buckets" : {
                        "buckets" : [
                          {
                            "key_as_string" : "1573084800000",
                            "key" : 1573084800000,
                            "doc_count" : 60,
                            "sum_tps" : {
                              "value" : 5272.678903963821
                            }
                          }
                          .....
                        ]
                      },
                      "avg_tps" : {
                        "value" : 6332.870640468049
                      }
                    }
                  ]
                }
            }
            */
    public TemplateTpsMetricPO getTemplateTpsMetricInfo(Integer logicId, Long startTime, Long endTime,
                                                        Long currentStartDate, Long currentEndDate) {

        TemplateTpsMetricPO tpsMetricPO = new TemplateTpsMetricPO();
        Map<Long/*templateId*/, Double> currentTpsMap = Maps.newHashMap();

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        // 获取指定时间范围的指定索引模板写入每小时的最大值
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_HISTORY_MAX_TPS_BY_LOGIC_ID_AND_TIME_RANGE,
            startTime, endTime, logicId);

        ESQueryResponse esQueryResponse = gatewayClient.performRequest(realIndexName, TYPE, dsl);
        if (esQueryResponse != null && esQueryResponse.getAggs() != null) {
            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();
            setMaxTpsTimestamp(tpsMetricPO, esAggrMap, getMaxTpsValue(tpsMetricPO, esAggrMap));
        }

        // 获取指定索引模板写入最近一段时间的总和
        String indexNames = IndexNameUtils.genDailyIndexName(indexName, currentStartDate, currentEndDate);
        dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AVG_TPS_BY_LOGIC_ID_AND_TIME_RANGE,
            currentStartDate, currentEndDate, logicId);
        esQueryResponse = gatewayClient.performRequest(indexNames, TYPE, dsl);
        handleESQueryResponse(currentTpsMap, esQueryResponse);

        tpsMetricPO.setCurrentTpsMap(currentTpsMap);

        return tpsMetricPO;
    }

    /**
     * 获取指定索引的查询率
     *
     * @param indexNames
     * @param startDate
     * @param endDate
     * @return
     */
    /**
     * 解析结果
        {
            "groupByIndex" : {
              "doc_count_error_upper_bound" : 0,
              "sum_other_doc_count" : 0,
              "buckets" : [
                {
                  "key" : "router_access_20191227",
                  "doc_count" : 4892,
                  "date_bucket" : {
                    "buckets" : [
                      {
                        "key_as_string" : "2019-12-26",
                        "key" : 1577318400000,
                        "doc_count" : 958,
                        "query_rate_avg" : {
                          "value" : 56.1850716890672
                        }
                      }
                    ]
                  }
                },
                {
                  "key" : "router_access_20191228",
                  "doc_count" : 4804,
                  "date_bucket" : {
                    "buckets" : [
                      {
                        "key_as_string" : "2019-12-27",
                        "key" : 1577404800000,
                        "doc_count" : 959,
                        "query_rate_avg" : {
                          "value" : 51.70561018127233
                        }
                      }
                    ]
                  }
                }
              ]
            }
          }
     */
    public List<IndexNameQueryAvgRatePO> getIndexNameQueryAvgRate(String indexNames, Long startDate, Long endDate) {

        List<IndexNameQueryAvgRatePO> indexNameQueryAvgRatePoList = Lists.newLinkedList();

        String[] indexNameArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(indexNames, ",");
        String indexNameFormat = CommonUtils.strConcat(Lists.newArrayList(indexNameArr));

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startDate, endDate);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_QUERY_RATE_BY_INDEX_DATE_RANGE,
            indexNameFormat);

        ESQueryResponse esQueryResponse = gatewayClient.performRequest(realIndexName, TYPE, dsl);
        if (esQueryResponse != null && esQueryResponse.getAggs() != null) {

            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();
            if (esAggrMap != null && esAggrMap.containsKey("groupByIndex")) {
                ESAggr groupByIndexESAggr = esAggrMap.get("groupByIndex");
                if (groupByIndexESAggr != null && CollectionUtils.isNotEmpty(groupByIndexESAggr.getBucketList())) {
                    handleBucketList(indexNameQueryAvgRatePoList, groupByIndexESAggr);
                }
            }
        }

        return indexNameQueryAvgRatePoList;
    }

    /**
     * 根据索引逻辑模板ID获取指定范围内索引统计信息
     *
     * @param logicTemplateId
     * @param startDate
     * @param endDate
     * @return
     */
    public List<ESIndexStats> getTemplateRealStatis(Long logicTemplateId, Long startDate, Long endDate) {
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startDate, endDate);
        String dsl = dslLoaderUtil.getFormatDslByFileName(
            DslsConstant.GET_TEMPLATE_REAL_STATIS_INFO_BY_TEMPLATE_AND_CLUSTER, logicTemplateId, startDate, endDate);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> {
            List<ESIndexStats> esIndexStatsList = Lists.newLinkedList();

            if (null == s) {
                return esIndexStatsList;
            }

            List<ESBucket> esBuckets = s.getAggs().getEsAggrMap().get("groupByTimeStamp").getBucketList();

            if (CollectionUtils.isNotEmpty(esBuckets)) {
                esBuckets.forEach(esBucket -> {
                    try {
                        Map<String, Object> unUsedMap = esBucket.getUnusedMap();
                        Map<String, ESAggr> aggrMap = esBucket.getAggrMap();
                        if (null != unUsedMap && null != aggrMap) {
                            ESIndexStats esIndexStats = new ESIndexStats();

                            esIndexStats.putMetrics("store-size_in_bytes-total",
                                aggrMap.get("store-size_in_bytes-total").getUnusedMap().get(VALUE).toString());
                            esIndexStats.putMetrics("indexing-index_total_rate",
                                aggrMap.get("indexing-index_total_rate").getUnusedMap().get(VALUE).toString());
                            esIndexStats.putMetrics("indexing-index_time_in_millis_rate", aggrMap
                                .get("indexing-index_time_in_millis_rate").getUnusedMap().get(VALUE).toString());
                            esIndexStats.putMetrics("indexing-index_failed_rate",
                                aggrMap.get("indexing-index_failed_rate").getUnusedMap().get(VALUE).toString());
                            esIndexStats.putMetrics("search-scroll_time_in_millis_rate", aggrMap
                                .get("search-scroll_time_in_millis_rate").getUnusedMap().get(VALUE).toString());
                            esIndexStats.putMetrics("search-query_time_in_millis_rate",
                                aggrMap.get("search-query_time_in_millis_rate").getUnusedMap().get(VALUE).toString());
                            esIndexStats.putMetrics("search-query_total_rate",
                                aggrMap.get("search-query_total_rate").getUnusedMap().get(VALUE).toString());
                            esIndexStats.putMetrics("docs-count-total",
                                aggrMap.get("docs-count-total").getUnusedMap().get(VALUE).toString());
                            esIndexStats.setTimestamp(Long.valueOf(unUsedMap.get("key").toString()));
                            esIndexStats.setLogicTemplateId(logicTemplateId);

                            esIndexStatsList.add(esIndexStats);
                        }
                    } catch (Exception e) {
                        LOGGER.error(
                            "class=AriusStatsIndexInfoEsDao||method=getTemplateRealStatis||errMsg=get logic id {} error, response:{}",
                            logicTemplateId, s.toString(), e);
                    }
                });
            }

            return esIndexStatsList;

        }, 3);
    }

    /**
     * 获取指定时间范围的指定索引模板写入均值
     *
     * @param templateId
     * @param startDate 毫秒
     * @param endDate   毫秒
     * @return
     */
    public Double getTemplateTpsAvgInfo(Long templateId, Long startDate, Long endDate) {
        List<ESIndexStats> esIndexStats = getIndexStats(templateId, startDate, endDate);
        if (CollectionUtils.isEmpty(esIndexStats)) {
            return null;
        }

        final Double[] totalIndexing = { 0.0d };

        esIndexStats
            .forEach(esIndexStats1 -> totalIndexing[0] += Double.valueOf(esIndexStats1.getMetrics().get(INDEXING_TOTAL_RATE.getType())));

        // TPS_METRICS已经是毫秒级别的统计数据，monitor每分钟统计一次
        return totalIndexing[0] * 1000 * 60 / (endDate - startDate);
    }

    /**
     * 获取指定时间范围的指定索引模板均值查询均值
     *
     * @param templateId
     * @param startDate 毫秒
     * @param endDate   毫秒
     * @return
     */
    public Double getTemplateQpsAvgInfo(Long templateId, Long startDate, Long endDate) {
        List<ESIndexStats> esIndexStats = getIndexStats(templateId, startDate, endDate);
        if (CollectionUtils.isEmpty(esIndexStats)) {
            return null;
        }

        final Double[] totalQps = { 0.0d };

        esIndexStats.forEach(s -> totalQps[0] += Double.valueOf(s.getMetrics().get(QUERY_RATE.getType())));

        // TPS_METRICS已经是毫秒级别的统计数据，monitor每分钟统计一次
        return totalQps[0] * 1000 * 60 / (endDate - startDate);
    }

    /**
     * 获取一段时间内索引的index_node统计信息
     * @param startDate
     * @param endDate
     * @return
     */
    public List<ESIndexStats> getIndexStats(Long templateId, Long startDate, Long endDate) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startDate, endDate);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_INDEX_STATS_BY_TIME_RANGE_AND_TEMPLATEID,
            SCROLL_SIZE, startDate, endDate, templateId);

        List<ESIndexStats> esIndexStats = Lists.newLinkedList();
        gatewayClient.queryWithScroll(realIndexName, TYPE, dsl, SCROLL_SIZE, null, ESIndexStats.class, resultList -> {
            if (resultList != null) {
                esIndexStats.addAll(resultList);
            }
        });

        return esIndexStats;
    }

    /**
     * 获取最新时间分片中指标数值前TopN的索引名称
     * 如果延迟后的最新时间分片的指标值为null，最新时间迭代 - 1, 直到不为空, 迭代上限为3次。
     *
     * @param clusterPhyName          集群名称
     * @param metricsTypes            指标类型
     * @param topNu                   topN
     * @param aggType                 聚合类型
     * @param indicesBucketsMaxNum    DSL第一层bucket大小 , 聚合索引数量最大值（agg bucket number）
     * @param startTime               开始时间
     * @param endTime                 结束时间
     * @return
     */
    public List<TopMetrics> buildTopNIndexMetricsInfo(String clusterPhyName, List<String> metricsTypes, Integer topNu,
                                                      String aggType, int indicesBucketsMaxNum, Long startTime, Long endTime) {
        int retryTime = 0;
        List<VariousLineChartMetrics> variousLineChartMetrics;
        do {
            Tuple<Long, Long> firstInterval = MetricsUtils.getSortInterval(startTime, endTime, (retryTime + 3) * 60 * 1000L);
            long startTimeForOneInterval    = firstInterval.getV1();
            long endTimeForOneInterval      = firstInterval.getV2();

            String interval = MetricsUtils.getInterval(endTime - startTime);

            List<String> metricsKeys = getAggMetricsStr(metricsTypes, aggType);

            String dsl = dslLoaderUtil.getFormatDslByFileNameAndOtherParam(
                    DslsConstant.GET_MULTIPLE_INDEX_FIRST_INTERVAL_AGG_METRICS, interval, buildAggsDSL(metricsKeys, aggType),
                    clusterPhyName, startTimeForOneInterval, endTimeForOneInterval, indicesBucketsMaxNum);

            String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTimeForOneInterval,
                    endTimeForOneInterval);

            variousLineChartMetrics = gatewayClient.performRequest(metadataClusterName,
                    realIndexName, TYPE, dsl, s -> fetchMultipleAggMetrics(s, metricsKeys, topNu, true), 3);
        } while (retryTime++ > 3 && CollectionUtils.isEmpty(variousLineChartMetrics));

        return variousLineChartMetrics.stream().map(this::buildTopMetrics).collect(Collectors.toList());
    }

    /**
     * 获取topN指标信息
     * @param clusterPhyName
     * @param metricsTypes
     * @param topNu
     * @param aggType
     * @param indicesBucketsMaxNum
     * @param startTime
     * @param endTime
     * @return
     */
    public List<VariousLineChartMetrics> getTopNIndicesAggMetrics(String clusterPhyName, List<String> metricsTypes,
                                                                  Integer topNu, String aggType,
                                                                  int indicesBucketsMaxNum, Long startTime,
                                                                  Long endTime) {
        List<VariousLineChartMetrics> buildMetrics = Lists.newCopyOnWriteArrayList();
        List<TopMetrics> topNIndexMetricsList = buildTopNIndexMetricsInfo(clusterPhyName, metricsTypes, topNu, aggType,
            indicesBucketsMaxNum, startTime, endTime);

        for (TopMetrics topMetrics : topNIndexMetricsList) {
            futureUtil.runnableTask(() -> buildTopNSingleMetricsForIndex(buildMetrics, clusterPhyName, aggType,
                indicesBucketsMaxNum, startTime, endTime, topMetrics));
        }
        futureUtil.waitExecute();

        return buildMetrics;
    }

    private void buildTopNSingleMetricsForIndex(List<VariousLineChartMetrics> buildMetrics, String clusterPhyName,
                                                String aggType, int indicesBucketsMaxNum, Long startTime, Long endTime,
                                                TopMetrics topMetrics) {
        String topNameStr = null;
        if (CollectionUtils.isNotEmpty(topMetrics.getTopName())) {
            topNameStr = buildTopNameStr(topMetrics.getTopName());
        }

        if (StringUtils.isBlank(topNameStr)) {
            return;
        }

        String interval = MetricsUtils.getInterval(endTime - startTime);
        List<String> metricsKeys = getAggMetricsStr(Lists.newArrayList(topMetrics.getType()), aggType);

        String dsl = dslLoaderUtil.getDslByTopNNameInfo(DslsConstant.GET_TOPN_INDEX_AGG_METRICS, interval, topNameStr,
            buildAggsDSL(metricsKeys, aggType), clusterPhyName, startTime, endTime, indicesBucketsMaxNum);

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        List<VariousLineChartMetrics> variousLineChartMetrics = gatewayClient.performRequest(metadataClusterName,
            realIndexName, TYPE, dsl, s -> fetchMultipleAggMetrics(s, metricsKeys, null, false), 3);
        buildMetrics.addAll(variousLineChartMetrics);
    }

    /**
     * 获取单个索引指标信息
     *
     * @param clusterPhyName    集群名称
     * @param metrics           指标类型
     * @param searchIndexName   索引名称
     * @param aggType           聚合类型
     * @param startTime         开始时间
     * @param endTime           结束时间
     * @return  List<VariousLineChartMetrics>
     */
    public List<VariousLineChartMetrics> getAggSingleIndexMetrics(String clusterPhyName, List<String> metrics,
                                                                  String searchIndexName, String aggType, Long startTime,
                                                                  Long endTime) {
        String interval = MetricsUtils.getInterval(endTime - startTime);

        List<String> metricsKeys = getAggMetricsStr(metrics, aggType);

        String dsl = dslLoaderUtil.getFormatDslByFileNameAndOtherParam(DslsConstant.GET_AGG_SINGLE_INDEX_METRICS,
            interval, buildAggsDSL(metricsKeys, aggType), clusterPhyName, searchIndexName, startTime, endTime);

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(metadataClusterName, realIndexName, TYPE, dsl,
            s -> fetchSingleAggMetrics(s, metricsKeys, searchIndexName), 3);
    }

    /********************************************* private methods *********************************************/
    private void handleMaxScrollTimeESAggr(String maxPercentRate, Map<String, String> ret, ESAggr maxScrollTimeESAggr) {
        if (null != maxScrollTimeESAggr.getUnusedMap()
                && null != maxScrollTimeESAggr.getUnusedMap().get(VALUES)) {
            JSONObject values = (JSONObject) maxScrollTimeESAggr.getUnusedMap().get(VALUES);
            if (Objects.nonNull(values) && values.containsKey(maxPercentRate)
                    && Objects.nonNull(values.get(maxPercentRate))) {
                ret.put("max_scroll_time", values.get(maxPercentRate).toString());
            }
        }
    }

    private void handleMaxQueryTimeESAggr(String maxPercentRate, Map<String, String> ret, ESAggr maxQueryTimeESAggr) {
        if (null != maxQueryTimeESAggr.getUnusedMap()
                && null != maxQueryTimeESAggr.getUnusedMap().get(VALUES)) {
            JSONObject values = (JSONObject) maxQueryTimeESAggr.getUnusedMap().get(VALUES);
            if (Objects.nonNull(values) && values.containsKey(maxPercentRate)
                    && Objects.nonNull(values.get(maxPercentRate))) {
                ret.put("max_query_time", values.get(maxPercentRate).toString());
            }
        }
    }

    private void handleMaxTpsESAggr(String maxPercentRate, Map<String, String> ret, ESAggr maxTpsESAggr) {
        if (null != maxTpsESAggr.getUnusedMap() && null != maxTpsESAggr.getUnusedMap().get(VALUES)) {
            JSONObject values = (JSONObject) maxTpsESAggr.getUnusedMap().get(VALUES);
            if (Objects.nonNull(values) && values.containsKey(maxPercentRate)
                    && Objects.nonNull(values.get(maxPercentRate))) {
                ret.put(MAX_TPS, values.get(maxPercentRate).toString());
            }
        }
    }

    private void handleBucketList(List<IndexNameQueryAvgRatePO> indexNameQueryAvgRatePoList, ESAggr groupByIndexESAggr) {
        for (ESBucket esBucket : groupByIndexESAggr.getBucketList()) {
            String indexName = esBucket.getUnusedMap().get("key").toString();
            ESAggr dateBucketESAggr = esBucket.getAggrMap().get("date_bucket");

            if (dateBucketESAggr != null && CollectionUtils.isNotEmpty(dateBucketESAggr.getBucketList())) {
                for (ESBucket subBucket : dateBucketESAggr.getBucketList()) {
                    String date = subBucket.getUnusedMap().get("key_as_string").toString();

                    ESAggr queryRateAvgESAggr = subBucket.getAggrMap().get("query_rate_avg");
                    if (queryRateAvgESAggr != null
                            && queryRateAvgESAggr.getUnusedMap().get(VALUE) != null) {
                        IndexNameQueryAvgRatePO indexNameQueryAvgRatePo = new IndexNameQueryAvgRatePO();
                        indexNameQueryAvgRatePo.setDate(date);
                        indexNameQueryAvgRatePo.setIndexName(indexName);
                        indexNameQueryAvgRatePo.setQueryTotalRate(
                                Double.valueOf(queryRateAvgESAggr.getUnusedMap().get(VALUE).toString()));

                        indexNameQueryAvgRatePoList.add(indexNameQueryAvgRatePo);
                    }
                }
            }
        }
    }

    private void handleESQueryResponse(Map<Long, Double> currentTpsMap, ESQueryResponse esQueryResponse) {
        if (esQueryResponse != null && esQueryResponse.getAggs() != null) {
            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();

            if (esAggrMap != null && esAggrMap.containsKey(GROUP_BY_TEMPLATE_ID)) {
                ESAggr groupByTemplateIdESAggr = esAggrMap.get(GROUP_BY_TEMPLATE_ID);
                if (groupByTemplateIdESAggr != null
                        && CollectionUtils.isNotEmpty(groupByTemplateIdESAggr.getBucketList())) {

                    handleBucketList(currentTpsMap, groupByTemplateIdESAggr);
                }
            }
        }
    }

    private void handleBucketList(Map<Long, Double> currentTpsMap, ESAggr groupByTemplateIdESAggr) {
        for (ESBucket esBucket : groupByTemplateIdESAggr.getBucketList()) {
            Long templateId = Long.valueOf(esBucket.getUnusedMap().get("key").toString());
            ESAggr avgAggr = esBucket.getAggrMap().get("avg_tps");
            if (avgAggr != null) {
                Map<String, Object> values = (Map<String, Object>) avgAggr.getUnusedMap().get(VALUES);
                currentTpsMap.put(templateId, Double.valueOf(values.get("50.0").toString()));
            }
        }
    }

    private void setMaxTpsTimestamp(TemplateTpsMetricPO tpsMetricPO, Map<String, ESAggr> esAggrMap, String maxTpsValue) {
        if (maxTpsValue != null && esAggrMap != null && esAggrMap.containsKey(GROUP_BY_TEMPLATE_ID)) {
            ESAggr groupByTemplateIdESAggr = esAggrMap.get(GROUP_BY_TEMPLATE_ID);
            if (groupByTemplateIdESAggr != null
                    && CollectionUtils.isNotEmpty(groupByTemplateIdESAggr.getBucketList())) {
                handleBucketList(tpsMetricPO, maxTpsValue, groupByTemplateIdESAggr);
            }
        }
    }

    private void handleBucketList(TemplateTpsMetricPO tpsMetricPO, String maxTpsValue, ESAggr groupByTemplateIdESAggr) {
        for (ESBucket esBucket : groupByTemplateIdESAggr.getBucketList()) {
            ESAggr maxAggr = esBucket.getAggrMap().get(MAX_TPS);
            if (maxAggr != null && maxTpsValue.equals(maxAggr.getUnusedMap().get(VALUE).toString())) {
                Object obj = maxAggr.getUnusedMap().get("keys");
                if (obj instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) obj;
                    for (int i = 0; i < jsonArray.size(); ++i) {
                        Long maxTpsTimestamp = Long.valueOf(jsonArray.get(i).toString());
                        tpsMetricPO.setMaxTpsTimestamp(DateTimeUtil.formatTimestamp(maxTpsTimestamp));
                    }
                }
                break;
            }
        }
    }

    private String getMaxTpsValue(TemplateTpsMetricPO tpsMetricPO, Map<String, ESAggr> esAggrMap) {
        String maxTpsValue = null;
        if (esAggrMap != null && esAggrMap.containsKey("max_tps_templateId")) {
            ESAggr maxTpsESAggr = esAggrMap.get("max_tps_templateId");
            if (maxTpsESAggr != null && maxTpsESAggr.getUnusedMap() != null
                    && maxTpsESAggr.getUnusedMap().get(VALUE) != null) {
                maxTpsValue = maxTpsESAggr.getUnusedMap().get(VALUE).toString();
                tpsMetricPO.setMaxTps(Double.valueOf(maxTpsValue));
                Object obj = maxTpsESAggr.getUnusedMap().get("keys");
                if (obj instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) obj;
                    for (int i = 0; i < jsonArray.size(); ++i) {
                        tpsMetricPO.setMaxTpsTemplateId(Long.valueOf(jsonArray.get(i).toString()));
                    }
                }
            }
        }
        return maxTpsValue;
    }
}
