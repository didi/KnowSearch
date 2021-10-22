package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexRealTimeInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.IndexNameQueryAvgRatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.TemplateTpsMetricPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class AriusStatsIndexInfoESDAO extends BaseAriusStatsESDAO {

    @PostConstruct
    public void init(){
        super.indexName   = dataCentreUtil.getAriusStatsIndexInfo();

        BaseAriusStatsESDAO.register( AriusStatsEnum.INDEX_INFO,this);
    }

    /**
     * 根据集群名称，获取集群[now-2m, now-1m]总的qps
     * @param cluster
     * @return
     */
    public long getClusterQps(String cluster) {
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_REAL_TIME_TPS_QPS_INFO,
                cluster, "now-2m", "now-1m", QPS_METRICS);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
                s -> getSumFromESQueryResponse(s, "sum").longValue(), 3);
    }

    /**
     * 根据集群名称，获取集群[now-2m, now-1m]总的tps
     * @param cluster
     * @return
     */
    public long getClusterTps(String cluster) {
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_REAL_TIME_TPS_QPS_INFO,
                cluster, "now-2m", "now-1m", TPS_METRICS);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
                s -> getSumFromESQueryResponse(s, "sum").longValue(), 3);
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
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_TOTAL_SIZE_BY_TIME_RANGE, template, cluster, startTime, endTime);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
                s -> getSumFromESQueryResponse(s, "sizeSum"), 3);
    }

    /**
     * 根据物理模板ID获取模板的总大小
     * @param templateId
     * @return
     */
    public double getTemplateTotalSize(Long templateId) {
        Long   now       = System.currentTimeMillis();
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_TOTAL_SIZE_BY_TEMPLATE_ID, templateId, now - 15 * 60 * 1000, now);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
                s -> getSumFromESQueryResponse(s, "sizeSum"), 3);
    }

    /**
     * 根据逻辑模板ID，获取模板的总大小(单位：byte)，主备情况下，主备索引总量都会统计
     * @param logicTemplateId
     * @return
     */
    public double getLogicTemplateTotalSize(Long logicTemplateId) {
        Long   now       = System.currentTimeMillis();
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_LOGIC_TEMPLATE_TOTAL_SIZE_BY_TEMPLATE_ID, logicTemplateId, now - 15 * 60 * 1000, now);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
                s -> getSumFromESQueryResponse(s, "sizeSum"), 3);
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
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_MAX_INDEX_SIZE, template, cluster, startTime, endTime);
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
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_TOTAL_DOC_BY_TIME_RANGE, template, cluster, startTime, endTime);
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
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_TOTAL_DOC_BY_LOGIC_TEMPLATE_ID_AND_TIME_RANGE, logicTemplateId, startTime, endTime);
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
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_MAX_INDEX_DOC, template, cluster, startTime, endTime);
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
    public double getTemplateMaxTpsByTimeRange(Long logicTemplateId, Long startTime, Long endTime){
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_TOTAL_MAX_TPS_BY_TIME_RANGE, logicTemplateId, startTime, endTime);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
                s -> getSumFromESQueryResponse(s, "tpsMax"), 3);
    }

    public double getTemplateMaxTpsByTimeRangeNoPercent(Long logicTemplateId, Long startTime, Long endTime){
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_TOTAL_MAX_TPS_BY_TIME_RANGE_NO_PERCENT, logicTemplateId, startTime, endTime);
        String realIndex = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
                s -> getSumFromESQueryResponse(s, "tpsMax"), 3);
    }

    /**
     * 根据模板和集群，获取一段时间内的模板分钟级别平均实时数据
     * @param startDate
     * @param endDate
     * @param template
     * @param cluster
     * @return
     */
    public IndexRealTimeInfo getIndexRealTimeInfoByTemplateAndCluster(int offset, long startDate, long endDate, String template, String cluster){
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_REAL_INFO_BY_TEMPLATE_AND_CLUSTER, startDate, endDate, cluster, template);
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
                ESAggr avgQueryAvgRate   = esAggrMap.get("avg_indices-search-query_avg_time");

                if(null != avgQueryTotalRate.getUnusedMap() && null != avgQueryTotalRate.getUnusedMap().get("value")){
                    indexRealTimeInfo.setAvgSearchQueryTotalRate(Double.valueOf(avgQueryTotalRate.getUnusedMap().get("value").toString()));
                }

                if(null != avgIndexTotalRate.getUnusedMap() && null != avgIndexTotalRate.getUnusedMap().get("value")){
                    indexRealTimeInfo.setAvgIndexingIndexTotalRate(Double.valueOf(avgIndexTotalRate.getUnusedMap().get("value").toString()));
                }

                if(null != avgQueryAvgRate.getUnusedMap() && null != avgQueryAvgRate.getUnusedMap().get("value")){
                    indexRealTimeInfo.setAvgIndicesSearchQueryTime(Double.valueOf(avgQueryAvgRate.getUnusedMap().get("value").toString()));
                }
            } catch (Exception e) {
                LOGGER.error("class=AriusStatsIndexInfoEsDao||method=getIndexRealTimeInfoByTemplateAndCluster||errMsg=exception! response:{}", response.toString(), e);
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
        String realIndexName  = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String maxPercentRate = "95.0";
        String dsl            = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_MAXINFO_BY_TIME_RANGE_AND_TEMPLATE,
                startTime, endTime, tempalte, cluster, maxPercentRate, maxPercentRate, maxPercentRate);

        return gatewayClient.performRequest(realIndexName, TYPE, dsl, response -> {
            Map<String, String> ret = new HashMap<>();

            if (response == null){return ret;}

            try {
                Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();

                ESAggr maxTpsESAggr         = esAggrMap.get("max_tps");
                ESAggr maxQueryTimeESAggr   = esAggrMap.get("max_query_time");
                ESAggr maxScrollTimeESAggr  = esAggrMap.get("max_scroll_time");

                if (null != maxTpsESAggr.getUnusedMap() && null != maxTpsESAggr.getUnusedMap().get("values")) {
                    JSONObject values = (JSONObject) maxTpsESAggr.getUnusedMap().get("values");
                    if (Objects.nonNull(values) && values.containsKey(maxPercentRate) && Objects.nonNull(values.get(maxPercentRate))) {
                        ret.put("max_tps", values.get(maxPercentRate).toString());
                    }
                }

                if (null != maxQueryTimeESAggr.getUnusedMap() && null != maxQueryTimeESAggr.getUnusedMap().get("values")) {
                    JSONObject values = (JSONObject) maxQueryTimeESAggr.getUnusedMap().get("values");
                    if (Objects.nonNull(values) && values.containsKey(maxPercentRate) && Objects.nonNull(values.get(maxPercentRate))) {
                        ret.put("max_query_time", values.get(maxPercentRate).toString());
                    }
                }

                if (null != maxScrollTimeESAggr.getUnusedMap() && null != maxScrollTimeESAggr.getUnusedMap().get("values")) {
                    JSONObject values = (JSONObject) maxScrollTimeESAggr.getUnusedMap().get("values");
                    if (Objects.nonNull(values) && values.containsKey(maxPercentRate) && Objects.nonNull(values.get(maxPercentRate))) {
                        ret.put("max_scroll_time", values.get(maxPercentRate).toString());
                    }
                }
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
    public TemplateTpsMetricPO getTemplateTpsMetricInfo(Integer logicId, Long startTime, Long endTime, Long currentStartDate, Long currentEndDate) {

        TemplateTpsMetricPO tpsMetricPO = new TemplateTpsMetricPO();
        Map<Long/*templateId*/, Double> currentTpsMap = Maps.newHashMap();

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        // 获取指定时间范围的指定索引模板写入每小时的最大值
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_HISTORY_MAX_TPS_BY_LOGIC_ID_AND_TIME_RANGE, startTime, endTime, logicId);

        ESQueryResponse esQueryResponse = gatewayClient.performRequest(realIndexName, TYPE, dsl);
        if (esQueryResponse != null && esQueryResponse.getAggs() != null) {
            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();

            String maxTpsValue = null;
            if (esAggrMap != null && esAggrMap.containsKey("max_tps_templateId")) {
                ESAggr maxTpsESAggr = esAggrMap.get("max_tps_templateId");
                if (maxTpsESAggr != null && maxTpsESAggr.getUnusedMap() != null && maxTpsESAggr.getUnusedMap().get("value") != null) {
                    maxTpsValue = maxTpsESAggr.getUnusedMap().get("value").toString();
                    tpsMetricPO.setMaxTps(Double.valueOf(maxTpsValue));
                    Object obj = maxTpsESAggr.getUnusedMap().get("keys");
                    if (obj instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray)obj;
                        for (int i = 0; i < jsonArray.size(); ++i) {
                            tpsMetricPO.setMaxTpsTemplateId(Long.valueOf(jsonArray.get(i).toString()));
                        }
                    }
                }
            }
            if (maxTpsValue != null && esAggrMap != null && esAggrMap.containsKey("groupByTemplateId")) {
                ESAggr groupByTemplateIdESAggr = esAggrMap.get("groupByTemplateId");
                if (groupByTemplateIdESAggr != null && CollectionUtils.isNotEmpty(groupByTemplateIdESAggr.getBucketList())) {
                    for (ESBucket esBucket : groupByTemplateIdESAggr.getBucketList()) {
                        ESAggr maxAggr = esBucket.getAggrMap().get("max_tps");
                        if (maxAggr != null && maxTpsValue.equals(maxAggr.getUnusedMap().get("value").toString())) {
                            Object obj = maxAggr.getUnusedMap().get("keys");
                            if (obj instanceof JSONArray) {
                                JSONArray jsonArray = (JSONArray)obj;
                                for (int i = 0; i < jsonArray.size(); ++i) {
                                    Long maxTpsTimestamp = Long.valueOf(jsonArray.get(i).toString());
                                    tpsMetricPO.setMaxTpsTimestamp( DateTimeUtil.formatTimestamp(maxTpsTimestamp));
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        // 获取指定索引模板写入最近一段时间的总和
        String indexNames = IndexNameUtils.genDailyIndexName(indexName, currentStartDate, currentEndDate);
        dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_AVG_TPS_BY_LOGIC_ID_AND_TIME_RANGE, currentStartDate, currentEndDate, logicId);
        esQueryResponse = gatewayClient.performRequest(indexNames, TYPE, dsl);
        if (esQueryResponse != null && esQueryResponse.getAggs() != null) {
            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();

            if (esAggrMap != null && esAggrMap.containsKey("groupByTemplateId")) {
                ESAggr groupByTemplateIdESAggr = esAggrMap.get("groupByTemplateId");
                if (groupByTemplateIdESAggr != null && CollectionUtils.isNotEmpty(groupByTemplateIdESAggr.getBucketList())) {

                    for (ESBucket esBucket : groupByTemplateIdESAggr.getBucketList()) {
                        Long templateId = Long.valueOf(esBucket.getUnusedMap().get("key").toString());
                        ESAggr avgAggr = esBucket.getAggrMap().get("avg_tps");
                        if (avgAggr != null) {
                            Map values = (Map) avgAggr.getUnusedMap().get("values");
                            currentTpsMap.put(templateId, Double.valueOf(values.get("50.0").toString()));
                        }
                    }
                }
            }
        }

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
        String dsl           = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_QUERY_RATE_BY_INDEX_DATE_RANGE, indexNameFormat);

        ESQueryResponse esQueryResponse = gatewayClient.performRequest(realIndexName, TYPE, dsl);
        if (esQueryResponse != null && esQueryResponse.getAggs() != null) {

            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();
            if (esAggrMap != null && esAggrMap.containsKey("groupByIndex")) {
                ESAggr groupByIndexESAggr = esAggrMap.get("groupByIndex");
                if (groupByIndexESAggr != null && CollectionUtils.isNotEmpty(groupByIndexESAggr.getBucketList())) {

                    for (ESBucket esBucket : groupByIndexESAggr.getBucketList()) {
                        String indexName = esBucket.getUnusedMap().get("key").toString();
                        ESAggr dateBucketESAggr = esBucket.getAggrMap().get("date_bucket");

                        if (dateBucketESAggr != null && CollectionUtils.isNotEmpty(dateBucketESAggr.getBucketList())) {
                            for (ESBucket subBucket : dateBucketESAggr.getBucketList()) {
                                String date = subBucket.getUnusedMap().get("key_as_string").toString();

                                ESAggr queryRateAvgESAggr = subBucket.getAggrMap().get("query_rate_avg");
                                if (queryRateAvgESAggr != null && queryRateAvgESAggr.getUnusedMap().get("value") != null) {
                                    IndexNameQueryAvgRatePO indexNameQueryAvgRatePo = new IndexNameQueryAvgRatePO();
                                    indexNameQueryAvgRatePo.setDate(date);
                                    indexNameQueryAvgRatePo.setIndexName(indexName);
                                    indexNameQueryAvgRatePo.setQueryTotalRate(Double.valueOf(queryRateAvgESAggr.getUnusedMap().get("value").toString()));

                                    indexNameQueryAvgRatePoList.add(indexNameQueryAvgRatePo);
                                }
                            }
                        }
                    }
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
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TEMPLATE_REAL_STATIS_INFO_BY_TEMPLATE_AND_CLUSTER, logicTemplateId, startDate, endDate);

        return gatewayClient.performRequest(realIndex, TYPE, dsl, s -> {
            List<ESIndexStats> esIndexStatsList = Lists.newLinkedList();

            if(null == s) {return esIndexStatsList;}

            List<ESBucket> esBuckets = s.getAggs().getEsAggrMap().get("groupByTimeStamp").getBucketList();

            if (CollectionUtils.isNotEmpty(esBuckets)) {
                esBuckets.forEach(esBucket -> {
                    try {
                        Map<String, Object> unUsedMap = esBucket.getUnusedMap();
                        Map<String, ESAggr> aggrMap   = esBucket.getAggrMap();
                        if (null != unUsedMap && null != aggrMap) {
                            ESIndexStats esIndexStats = new ESIndexStats();

                            esIndexStats.putMetrics("store-size_in_bytes-total",            aggrMap.get("store-size_in_bytes-total").getUnusedMap().get("value").toString());
                            esIndexStats.putMetrics("indexing-index_total_rate",            aggrMap.get("indexing-index_total_rate").getUnusedMap().get("value").toString());
                            esIndexStats.putMetrics("indexing-index_time_in_millis_rate",   aggrMap.get("indexing-index_time_in_millis_rate").getUnusedMap().get("value").toString());
                            esIndexStats.putMetrics("indexing-index_failed_rate",           aggrMap.get("indexing-index_failed_rate").getUnusedMap().get("value").toString());
                            esIndexStats.putMetrics("search-scroll_time_in_millis_rate",    aggrMap.get("search-scroll_time_in_millis_rate").getUnusedMap().get("value").toString());
                            esIndexStats.putMetrics("search-query_time_in_millis_rate",     aggrMap.get("search-query_time_in_millis_rate").getUnusedMap().get("value").toString());
                            esIndexStats.putMetrics("search-query_total_rate",              aggrMap.get("search-query_total_rate").getUnusedMap().get("value").toString());
                            esIndexStats.putMetrics("docs-count-total",                     aggrMap.get("docs-count-total").getUnusedMap().get("value").toString());
                            esIndexStats.setTimestamp(Long.valueOf(unUsedMap.get("key").toString()));
                            esIndexStats.setLogicTemplateId(logicTemplateId);

                            esIndexStatsList.add(esIndexStats);
                        }
                    } catch (Exception e) {
                        LOGGER.error("class=AriusStatsIndexInfoEsDao||method=getTemplateRealStatis||errMsg=get logic id {} error, response:{}", logicTemplateId, s.toString(), e);
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
        if(CollectionUtils.isEmpty(esIndexStats)){return null;}

        final Double[] totalIndexing = {0.0d};

        esIndexStats.forEach(esIndexStats1 -> totalIndexing[0] += Double.valueOf(esIndexStats1.getMetrics().get(TPS_METRICS)));

        // TPS_METRICS已经是毫秒级别的统计数据，monitor每分钟统计一次
        return totalIndexing[0] * 1000 * 60 /(endDate - startDate);
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
        if(CollectionUtils.isEmpty(esIndexStats)){return null;}

        final Double[] totalQps = {0.0d};

        esIndexStats.forEach(s -> totalQps[0] += Double.valueOf(s.getMetrics().get(QPS_METRICS)));

        // TPS_METRICS已经是毫秒级别的统计数据，monitor每分钟统计一次
        return totalQps[0] * 1000 * 60 /(endDate - startDate);
    }



    /**
     * 获取一段时间内索引的index_node统计信息
     * @param startDate
     * @param endDate
     * @return
     */
    public List<ESIndexStats> getIndexStats(Long templateId, Long startDate, Long endDate) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startDate, endDate);
        String dsl           = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_INDEX_STATS_BY_TIME_RANGE_AND_TEMPLATEID, SCROLL_SIZE, startDate, endDate, templateId);

        List<ESIndexStats> esIndexStats = Lists.newLinkedList();
        gatewayClient.queryWithScroll(realIndexName,
                TYPE, dsl, SCROLL_SIZE, null, ESIndexStats.class, resultList -> {
                    if (resultList != null) {
                        esIndexStats.addAll(resultList);
                    }
                });

        return esIndexStats;
    }
}
