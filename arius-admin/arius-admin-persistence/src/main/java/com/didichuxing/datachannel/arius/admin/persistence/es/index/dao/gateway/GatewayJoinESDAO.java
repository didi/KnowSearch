package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ExceptionDslRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.QueryQpsMetric;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayJoinPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.AppQueryPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ESConstant;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.persistence.component.ScrollResultVisitor;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.didiglobal.logi.elasticsearch.client.response.query.query.hits.ESHit;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
@NoArgsConstructor
public class GatewayJoinESDAO extends BaseESDAO {

    private static final String DOC_COUNT = "doc_count";
    private static final String INDICES = "indices";
    private static final String DOUBLE_STRING_WITH_UNDER_LINE = "%s_%s";

    /**
     * gateway join索引
     */
    private String indexName;
    /**
     * type名称
     */
    private String typeName   = "type";

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusGatewayJoin();
    }

    private static final long ONE_DAY = 24 * 60 * 60 * 1000L;

    /**
     * 根据index获得对应的templateMD5
     *
     * @param templateName
     * @return
     */
    public Map<String/*dslMd5*/, Set<String>/*dsls*/> getTemplateMD5ByrealIndexName(String templateName) {
        Map<String/*dslMd5*/, Set<String>/*dsls*/> dslMap = Maps.newHashMap();
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSLS_BY_INDEX_NAME, templateName);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(getTemplateExpression(), null, dsl);
        if (esAggrMap == null) {
            return dslMap;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("md5s");
        if (esAggr == null) {
            return dslMap;
        }

        try {
            List<ESBucket> esBucketList = esAggr.getBucketList();
            if (esBucketList != null) {
                handleBucketListInGetTemplateMD5ByrealIndexName(dslMap, esBucketList);
            }
        } catch (Exception e) {
            LOGGER.error("class=GatewayJoinEsDao||method=getTemplateMD5ByrealIndexName||errMsg={} fail to get dsls",
                    templateName, e);
        }

        return dslMap;
    }

    /**
     * 根据appid获得对应的99分位查询耗时
     * @param appid
     * @param startTime
     * @param endTime
     * @return ArrayList 排序后返回前10条
     */
    public List<Double> getRtCostByAppId(Integer appid, Long startTime, Long endTime) {
        List<Double> rtCostList = Lists.newArrayList();
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_QUERY_RT_BY_APPID, appid, startTime,
                endTime);
        return gatewayClient.performRequest(getIndexByTimeRange(startTime, endTime),
                typeName, dsl, response -> {
                    Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();
                    if (null != esAggrMap && esAggrMap.containsKey("queryByTimeStamp")) {
                        ESAggr queryByTimeStamp = esAggrMap.get("queryByTimeStamp");
                        if (null != queryByTimeStamp && CollectionUtils.isNotEmpty(queryByTimeStamp.getBucketList())) {
                            handleBucketListInGetRtCostByAppId(rtCostList, queryByTimeStamp);
                        }
                    }
                    if (CollectionUtils.isEmpty(rtCostList) || rtCostList.size() < 10) {
                        return rtCostList;
                    }
                    rtCostList.sort( Comparator.reverseOrder());
                    return rtCostList.subList(0, 10);

                }, 3);
    }

    /**
     * 根据appid获得查询topNum信息
     * @param appid
     * @param startTime
     * @param endTime
     * @param topNum
     * @return
     */
    public List<AppQueryPO> getQueryTopNumInfoByAppid(Integer appid, Long startTime, Long endTime, int topNum) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOP_NUM_QUERY_INFO_BY_APPID, appid,
                startTime, endTime, topNum);
        return gatewayClient.performRequest(getIndexByTimeRange(startTime, endTime),
                typeName, dsl, response -> {
                    List<ESHit> hits = response.getHits().getHits();
                    List<AppQueryPO> quetyInfoList = Lists.newArrayList();
                    if (CollectionUtils.isNotEmpty(hits)) {
                        for (ESHit hit : hits) {
                            try {
                                AppQueryPO appQueryPO = ConvertUtil.obj2ObjByJSON(hit.getSource(),
                                        AppQueryPO.class);
                                if (null != appQueryPO) {
                                    quetyInfoList.add(appQueryPO);
                                }
                            } catch (Exception e) {
                                LOGGER.error("class=GatewayJoinEsDao||method=getQueryTopNumInfoByAppid||errMsg={}", e);
                            }
                        }
                        return quetyInfoList;
                    }
                    return quetyInfoList;
                }, 3);
    }

    /**
     * 根据index获得查询请求
     *
     * @param templateName
     * @return
     */
    public Map<String/*dslMd5*/, GatewayJoinPO> getSearchRequestByrealIndexName(String templateName) {
        Map<String/*dslMd5*/, GatewayJoinPO> dslMap = Maps.newHashMap();
        String dsl = dslLoaderUtil.getFormatDslByFileName( DslsConstant.GET_SEARCH_REQUEST_BY_INDEX_NAME, templateName);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(getTemplateExpression(), null, dsl);
        if (esAggrMap == null) {
            return dslMap;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("md5s");
        if (esAggr == null) {
            return dslMap;
        }

        try {
            List<ESBucket> esBucketList = esAggr.getBucketList();
            if (esBucketList != null) {
                handleESBucketListInGetSearchRequestByrealIndexName(dslMap, esBucketList);
            }
        } catch (Exception e) {
            LOGGER.error("class=GatewayJoinEsDao||method=getSearchRequestByrealIndexName||errMsg={} fail to get dsls",
                    templateName, e);
        }

        return dslMap;
    }

    private void handleESBucketListInGetSearchRequestByrealIndexName(Map<String, GatewayJoinPO> dslMap, List<ESBucket> esBucketList) {
        ESAggr subEsAggr;
        String md5;
        for (ESBucket esBucket : esBucketList) {
            if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                continue;
            }
            md5 = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();

            subEsAggr = esBucket.getAggrMap().get("samples");
            if (subEsAggr == null) {
                continue;
            }

            Object obj = subEsAggr.getUnusedMap().get("hits");
            handleHits(dslMap, md5, obj);
        }
    }

    private void handleHits(Map<String, GatewayJoinPO> dslMap, String md5, Object obj) {
        GatewayJoinPO gatewayJoinPO;
        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray jsonArray = jsonObject.getJSONArray("hits");
            if (jsonArray != null && !jsonArray.isEmpty()) {
                JSONObject hitJsonObj = jsonArray.getJSONObject(0);
                JSONObject sourceJsonObj = hitJsonObj.getJSONObject("_source");
                if (sourceJsonObj != null) {
                    gatewayJoinPO = JSON.parseObject(sourceJsonObj.toJSONString(), GatewayJoinPO.class);
                    if (Objects.nonNull(gatewayJoinPO)) {
                        dslMap.put(md5, gatewayJoinPO);
                    }
                }
            }
        }
    }

    public List<GatewayJoinPO> getGatewaySlowList(Long appid, Long startDate, Long endDate) {
        String realrealIndexName = IndexNameUtils.genDailyIndexName( getTemplateName(), startDate, endDate);
        String dsl = null;
        if (null == appid) {
            dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_SLOS_LIST_BY_RANGE, startDate, endDate);
        } else {
            dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_SLOS_LIST_BY_APPID_AND_RANGE, startDate,
                    endDate, appid);
        }

        return gatewayClient.performRequest(realrealIndexName, typeName, dsl, GatewayJoinPO.class);
    }

    public List<GatewayJoinPO> getGatewayErrorList(Long appid, Long startDate, Long endDate) {
        String realrealIndexName = IndexNameUtils.genDailyIndexName( getTemplateName(), startDate, endDate);
        String dsl = null;
        if (null == appid) {
            dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_ERROR_LIST_BY_RANGE, startDate,
                    endDate);
        } else {
            dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_ERROR_LIST_BY_APPID_AND_RANGE,
                    startDate, endDate, appid);
        }

        return gatewayClient.performRequest(realrealIndexName, typeName, dsl, GatewayJoinPO.class);
    }

    /**
     * 根据appid获取指定数据中心一段时间查询量
     *
     * @param appid
     * @param startDate
     * @param endDate
     * @return
     */
    public Long getSearchCountByAppid(Long appid, Long startDate, Long endDate) {
        String realrealIndexName = IndexNameUtils.genDailyIndexName( getTemplateName(), startDate, endDate);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_SEARCH_COUNT_BY_APPID_TIME_RANGE, appid,
                startDate, endDate);

        return gatewayClient.performRequestAndGetTotalCount(realrealIndexName, typeName, dsl);
    }

    /**
     * 获取指定索引的查询错误个数
     *
     * @param templateName
     * @param startDate
     * @param endDate
     * @return
     */
    public Long getErrorCntByTemplateName(String templateName, Long startDate, Long endDate) {
        String realrealIndexName = IndexNameUtils.genDailyIndexName( getTemplateName(), startDate, endDate);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ERROR_CNT_BY_TEMPLATE_NAME, templateName,
                startDate, endDate);

        return gatewayClient.performRequestAndGetTotalCount(realrealIndexName, typeName, dsl);
    }

    /**
     * 获取指定索引的慢查询个数
     *
     * @param templateName
     * @param startDate
     * @param endDate
     * @return
     */
    public Long getSlowCntByTemplateName(String templateName, Long totalCost, Long startDate, Long endDate) {
        String realrealIndexName = IndexNameUtils.genDailyIndexName( getTemplateName(), startDate, endDate);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_SLOW_CNT_BY_TEMPLATE_NAME, templateName,
                totalCost, startDate, endDate);
        return gatewayClient.performRequestAndGetTotalCount(realrealIndexName, typeName, dsl);
    }

    /**
     * 聚合获取某个应用访问的索引列表
     *
     * @param appid
     * @param dayCount
     * @return
     */
    public Map<String, Long> getAccessrealIndexNameByAppid(Integer appid, Integer dayCount) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ACCESS_INDEX_NAME_BY_APPID, appid);

        StringBuilder stringBuilder = new StringBuilder(64);
        for (int i = 0; i < dayCount; ++i) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(String.format(DOUBLE_STRING_WITH_UNDER_LINE, indexName, DateTimeUtil.getFormatDayByOffset(i)));
        }

        Map<String, Long> realIndexNameCountMap = Maps.newHashMap();
        ESAggrMap esAggrMap = gatewayClient.performAggRequest(stringBuilder.toString(), typeName, dsl);
        if (esAggrMap == null) {
            return realIndexNameCountMap;
        }

        String key = null;
        ESAggr esAggr = esAggrMap.getEsAggrMap().get(INDICES);
        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }

                key = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();
                realIndexNameCountMap.put(key,
                        Long.valueOf(esBucket.getUnusedMap().get(ESConstant.AGG_DOC_COUNT).toString()));
            }
        }

        return realIndexNameCountMap;
    }

    /**
     * 获取指定appid和时间范围下查询方式统计次数
     *
     * @param appid
     * @param dayCount
     * @return
     */
    public Map<String, Long> getRequestTypeByAppid(Integer appid, String indexExp, Integer dayCount) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_REQUEST_TYPE_BY_APPID, appid, indexExp);

        StringBuilder stringBuilder = new StringBuilder(64);
        for (int i = 0; i < dayCount; ++i) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(String.format(DOUBLE_STRING_WITH_UNDER_LINE, indexName, DateTimeUtil.getFormatDayByOffset(i)));
        }

        Map<String, Long> requestTypeMap = Maps.newHashMap();
        ESAggrMap esAggrMap = gatewayClient.performAggRequest(stringBuilder.toString(), typeName, dsl);
        if (esAggrMap == null) {
            return requestTypeMap;
        }

        String key = null;
        ESAggr esAggr = esAggrMap.getEsAggrMap().get("requestType");
        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }

                key = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();
                requestTypeMap.put(key, Long.valueOf(esBucket.getUnusedMap().get(ESConstant.AGG_DOC_COUNT).toString()));
            }
        }

        return requestTypeMap;
    }

    /**
     * 获取某个查询模板一周内查询次数
     *
     * @param md5
     * @return
     */
    public Long getWeekSearchCountByMd5(String md5) {
        Long searchCount = 0L;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_WEEK_SEARCH_COUNT_BY_MD5, md5);
        Long count = gatewayClient.performRequestAndGetTotalCount(getTemplateExpression(), null, dsl);
        if (count != null) {
            searchCount = Math.max(searchCount, count);
        }

        return searchCount;
    }

    /**
     * 根据索引名称和MD5获取一条查询明细中使用的字段信息
     *
     * @param indexDate
     * @param indices
     * @param md5
     * @return
     */
    public Tuple<Long, GatewayJoinPO> getPoByIndicesAndMd5(String indexDate, String indices, String md5) {

        String realIndexName = getIndex(indexDate);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_JOIN_BY_INDICES_MD5, indices,
                md5);

        Tuple<Long, GatewayJoinPO> tuple = gatewayClient.performRequestAndGetTotalCount(realIndexName, null,
                dsl, GatewayJoinPO.class);

        if (tuple != null && tuple.v1() > 0) {
            return tuple;
        }

        return null;
    }

    /**
     * 聚合查询gateway join日志索引，获取到某个索引查询使用的MD5列表
     *
     * @param indexDate
     * @param startTick
     * @param endTick
     */
    public Map<String, Set<String>> aggIndicesDslMd5ByRange(String indexDate, long startTick, long endTick) {

        Map<String/* realIndexName*/, Set<String> /*md5s*/> accessIndicesDslMd5Maps = Maps.newHashMap();

        String realIndexName = getIndex(indexDate);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_INDICES_MD5, startTick, endTick);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, null, dsl);
        if (esAggrMap == null) {
            return accessIndicesDslMd5Maps;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get(INDICES);
        if (esAggr == null) {
            return accessIndicesDslMd5Maps;
        }

        handleBucketListInAggIndicesDslMd5ByRange(accessIndicesDslMd5Maps, esAggr);

        return accessIndicesDslMd5Maps;
    }

    private void handleBucketListInAggIndicesDslMd5ByRange(Map<String, Set<String>> accessIndicesDslMd5Maps, ESAggr esAggr) {
        List<ESBucket> subEsBucketList;
        String indices;
        ESAggr subEsAggr;

        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }
                indices = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();

                subEsAggr = esBucket.getAggrMap().get("md5");
                if (subEsAggr == null) {
                    continue;
                }

                subEsBucketList = subEsAggr.getBucketList();
                handleSubEsBucketList(accessIndicesDslMd5Maps, subEsBucketList, indices);

            }
        }
    }

    private void handleSubEsBucketList(Map<String, Set<String>> accessIndicesDslMd5Maps, List<ESBucket> subEsBucketList, String indices) {
        String md5;
        if (CollectionUtils.isEmpty(subEsBucketList)) {
            return;
        }

        for (ESBucket subEsBucket : subEsBucketList) {
            if (subEsBucket.getUnusedMap() == null || subEsBucket.getUnusedMap().isEmpty()) {
                continue;
            }
            md5 = subEsBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();
            accessIndicesDslMd5Maps.computeIfAbsent(indices, k -> Sets.newHashSet()).add(md5);
        }
    }

    /**
     * 获取查询被限流的MD5
     *
     * @param date
     * @return
     */
    public Set<String> getQueryLimitErrorMd5(String date) {
        String realIndexName = getIndex(date);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_QUERY_LIMIT_ERROR_MD5);

        Set<String> appidMd5Sets = Sets.newLinkedHashSet();

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, null, dsl);
        if (esAggrMap == null) {
            return new HashSet<>();
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("appid");
        if (esAggr == null) {
            return new HashSet<>();
        }

        handleBucketListInGetQueryLimitErrorMd5(appidMd5Sets, esAggr);

        return appidMd5Sets;
    }

    private void handleBucketListInGetQueryLimitErrorMd5(Set<String> appidMd5Sets, ESAggr esAggr) {
        String appid;
        ESAggr subEsAggr;

        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }
                appid = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();

                subEsAggr = esBucket.getAggrMap().get("dslTemplateMd5");
                if (subEsAggr == null) {
                    continue;
                }

                handleSubEsBucketList(appidMd5Sets, appid, subEsAggr);

            }
        }
    }

    private void handleSubEsBucketList(Set<String> appidMd5Sets, String appid, ESAggr subEsAggr) {
        List<ESBucket> subEsBucketList;
        String dslTemplateMd5;
        subEsBucketList = subEsAggr.getBucketList();
        if (CollectionUtils.isEmpty(subEsBucketList)) {
            return;
        }

        for (ESBucket subEsBucket : subEsBucketList) {
            if (subEsBucket.getUnusedMap() == null || subEsBucket.getUnusedMap().isEmpty()) {
                continue;
            }
            dslTemplateMd5 = subEsBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();
            appidMd5Sets.add(String.format(DOUBLE_STRING_WITH_UNDER_LINE, appid, dslTemplateMd5));
        }
    }

    /**
     * 根据appid和MD5获取最新的一次查询记录
     *
     * @param appid
     * @param dslTemplateMd5
     * @return
     */
    public GatewayJoinPO getFirstByAppidAndTemplateMd5(Long appid, String dslTemplateMd5) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ONE_GATEWAY_JOIN_BY_KEY, appid,
                dslTemplateMd5);

        return gatewayClient.performRequestAndTakeFirst(getTemplateExpression(), null,
                dsl, GatewayJoinPO.class);
    }

    /**
     * 获取一小时内查询模板提取失败的次数
     *
     * @param date
     * @return
     */
    public Long getFailedDslTemplateCount(String date) {
        String realIndexName = getIndex(date);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_FAILED_DSL_TEMPLATE);

        return gatewayClient.performRequestAndGetTotalCount(realIndexName, typeName, dsl);
    }

    /**
     * 获取查询模板提取失败的索引信息
     *
     * @param date
     * @return
     */
    public Set<String> getFailedDslTemplateSearchIndices(String date) {
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_FAILED_DSL_TEMPLATE_INDICES);

        Set<String> indicesSets = Sets.newLinkedHashSet();

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, typeName, dsl);
        if (esAggrMap == null) {
            return new HashSet<>();
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get(INDICES);
        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }

                indicesSets.add(esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString());
            }
        }

        return indicesSets;
    }

    /**
     * 查询某个appid 一天查询总量
     *
     * @param appid
     * @param date
     * @return
     */
    public Long getTotalSearchCountByAppidAndDate(Integer appid, String date) {
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_SEARCH_COUNT_BY_APPID, appid);

        return gatewayClient.performRequestAndGetTotalCount(realIndexName, typeName, dsl);
    }

    /**
     * 获取qps信息(最大qps，平均qps，最小qps)
     *
     * @param appid
     * @param date
     * @return
     */
    public QueryQpsMetric getQpsInfoByAppidAndDate(Integer appid, String date) {
        QueryQpsMetric queryQpsMetric = new QueryQpsMetric();
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_QPS_INFO_BY_APPID, appid);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, typeName, dsl);
        if (esAggrMap == null) {
            return queryQpsMetric;
        }

        List<Tuple<Long, Long>> qpsList = Lists.newLinkedList();
        Tuple<Long, Long> tuple;
        ESAggr esAggr = esAggrMap.getEsAggrMap().get("searchCount");
        List<ESBucket> esBucketList = esAggr.getBucketList();
        Long docCount;

        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }
                docCount = Long.valueOf(esBucket.getUnusedMap().get(ESConstant.AGG_DOC_COUNT).toString());
                if (docCount <= 0) {
                    continue;
                }

                tuple = new Tuple<>( docCount,
                        Long.valueOf( esBucket.getUnusedMap().get( ESConstant.AGG_KEY ).toString() ) );

                qpsList.add(tuple);
            }
        }

        if (qpsList.isEmpty()) {
            return queryQpsMetric;
        }

        long totalQps = 0L;
        for (Tuple<Long, Long> t : qpsList) {
            totalQps += t.v1();
        }

        queryQpsMetric.setMaxQps(qpsList.get(0).v1());
        queryQpsMetric.setMaxQpsTime(DateTimeUtil.formatTimestamp(qpsList.get(0).v2()));
        queryQpsMetric.setMinQps(qpsList.get(qpsList.size() - 1).v1());
        queryQpsMetric.setMinQpsTime(DateTimeUtil.formatTimestamp(qpsList.get(qpsList.size() - 1).v2()));
        queryQpsMetric.setAvgQps((long) Math.floor(1.0 * totalQps / qpsList.size()));

        return queryQpsMetric;
    }

    /**
     * 获取查询耗时分位图
     *
     * @param appid
     * @param date
     * @return
     */
    public Map<String, Object> getCostInfoByAppidAndDate(Integer appid, String date) {
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_COST_INFO_BY_APPID, appid);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, typeName, dsl);
        if (esAggrMap == null) {
            return null;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("totalCost");
        if (esAggr == null) {
            return null;
        }

        Map<String, Object> totalCostMap = Maps.newHashMap();
        Object value = esAggr.getUnusedMap().get("values");
        if (value instanceof Map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                if ("NaN".equals(entry.getValue())) {
                    continue;
                }
                // 替换分位中的.
                totalCostMap.put(entry.getKey().replace(".", "_"), entry.getValue());
            }
        }

        return totalCostMap;
    }

    /**
     * 获取慢查语句dslTemplateMd5和次数
     *
     * @param appid
     * @param date
     * @return
     */
    public Map<String, Long> querySlowDslByAppidAndDate(Long appid, String date) {
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_SLOW_DSL_BY_APPID, appid);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, typeName, dsl);
        if (esAggrMap == null) {
            return null;
        }

        Map<String, Long> slowDslMap = Maps.newHashMap();
        ESAggr esAggr = esAggrMap.getEsAggrMap().get("slowDsl");
        if (esAggr == null) {
            return null;
        }

        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }
                slowDslMap.put(esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString(),
                        Long.valueOf(esBucket.getUnusedMap().get(ESConstant.AGG_DOC_COUNT).toString()));
            }
        }

        return slowDslMap;
    }

    /**
     * 根据appid和dsl查询模板MD5获取具体慢查语句
     *
     * @param date
     * @param appid
     * @param dslTemplateMd5
     * @return
     */
    public Tuple<Long, GatewayJoinPO> querySlowDslCountAndDetailByByAppidAndDslTemplate(String date, Integer appid,
                                                                                        String dslTemplateMd5,
                                                                                        Long slowDslThreshold) {
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_SLOW_DSL_BY_KEY, appid, slowDslThreshold,
                dslTemplateMd5);

        return gatewayClient.performRequestAndGetTotalCount(realIndexName, typeName, dsl,
                GatewayJoinPO.class);
    }

    /**
     * 根据appid和查询模板获取某一天查询最大值
     *
     * @param appid
     * @param dslTemplateMd5
     * @return
     */
    @Nullable
    public Tuple<Long, Long> queryMaxSearchQpsByAppIdAndDslTemplate(String date, Integer appid, String dslTemplateMd5) {
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_JOIN_MAX_QPS_BY_KEY, appid,
                dslTemplateMd5);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, typeName, dsl);
        if (esAggrMap == null) {
            return null;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("searchCountPerSecond");
        if (esAggr == null) {
            return null;
        }

        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }

                return new Tuple<>( (Long) esBucket.getUnusedMap().get( ESConstant.AGG_KEY ),
                        Long.valueOf( esBucket.getUnusedMap().get( ESConstant.AGG_DOC_COUNT ).toString() ) );
            }
        }

        return null;
    }

    /**
     * 获取某个appid某一天的查询错误数和错误信息
     *
     * @param appid
     * @param date
     * @return
     */
    public Tuple<Long, List<Tuple<String, Long>>> getErrorSearchCountAndErrorDetailByAppidDate(Integer appid, String date) {
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_EXCEPTION_NAME_BY_APPID, appid);

        ESQueryResponse esQueryResponse = gatewayClient.performRequest(realIndexName, typeName, dsl);
        if (esQueryResponse == null || esQueryResponse.getHits() == null) {
            return null;
        }
        List<Tuple<String, Long>> tupleList = Lists.newLinkedList();

        long errSearchCnt = Long
                .parseLong(esQueryResponse.getHits().getUnusedMap().getOrDefault(ESConstant.HITS_TOTAL, "0").toString());

        if (esQueryResponse.getAggs() == null) {
            return new Tuple<>(errSearchCnt, tupleList);
        }

        ESAggr esAggr = esQueryResponse.getAggs().getEsAggrMap().get("groupByError");
        if (esAggr == null) {
            return new Tuple<>(errSearchCnt, tupleList);
        }

        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }

                tupleList.add(new Tuple<>(esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString(),
                        Long.valueOf(esBucket.getUnusedMap().get(ESConstant.AGG_DOC_COUNT).toString())));
            }
        }

        return new Tuple<>(errSearchCnt, tupleList);
    }

    /**
     * 获取到错误查询语句模板，根据错误名称
     *
     * @param date
     * @param appid
     * @param exceptionName
     * @return
     */
    public Map<String, Long> queryErrorDslByAppidExceptionAndDate(String date, Integer appid, String exceptionName) {
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_MD5_BY_EXCEPTION_NAME, appid, exceptionName);

        Map<String, Long> errorDslMap = Maps.newHashMap();

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, typeName, dsl);
        if (esAggrMap == null) {
            return errorDslMap;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("top_dsltemplate");
        if (esAggr == null) {
            return errorDslMap;
        }

        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }

                errorDslMap.put(esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString(),
                        Long.valueOf(esBucket.getUnusedMap().get(ESConstant.AGG_DOC_COUNT).toString()));
            }
        }

        return errorDslMap;
    }

    /**
     * 根据appid，dslTemplateMd5获取异常查询具体信息
     *
     * @param date
     * @param appid
     * @param dslTemplateMd5
     * @param exceptionName
     * @return
     */
    public GatewayJoinPO queryErrorDslDetailByAppidTemplateAndDate(String date, Integer appid, String dslTemplateMd5,
                                                                   String exceptionName) {
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_EXCEPTION_BY_MD5, appid, dslTemplateMd5,
                exceptionName);

        return gatewayClient.performRequestAndTakeFirst(realIndexName, typeName, dsl, GatewayJoinPO.class);
    }

    /**
     * 获取某个appid某一天的查询gateway 分布情况
     *
     * @param appid
     * @param date
     * @return
     */
    public String getAccessGatewayInfoByAppidDate(Integer appid, String date) {
        String realIndexName = getIndex(date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ACCESS_GATEWAY_INFO_BY_APPID, appid);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, typeName, dsl);
        if (esAggrMap == null) {
            return null;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("groupByGateway");
        if (esAggr == null) {
            return null;
        }

        if (CollectionUtils.isEmpty(esAggr.getBucketList())) {
            return "";
        }

        List<Map<String, Object>> list = Lists.newLinkedList();
        for (ESBucket esBucket : esAggr.getBucketList()) {
            list.add(esBucket.getUnusedMap());
        }
        return JSON.toJSONString(list);
    }

    /**
     *  根据index获得对应的templateMD5
     *
     * @param realIndexName
     * @return
     */
    public Set<String> getDslByIndexAndTemplateMD5(String realIndexName, String templateMD5, int count) {

        String queryDsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_BY_MD5_INDICES, count, realIndexName,
                templateMD5);

        Set<String> dslSet = new HashSet<>();

        List<JSONObject> list = gatewayClient.performRequest(realIndexName + "*", typeName, queryDsl, JSONObject.class);

        if (list != null) {
            for (JSONObject jsonObject : list) {
                dslSet.add(jsonObject.getString( "dsl" ));
            }
        }

        return dslSet;
    }

    /**
     * 获得start和end这个时间段内，有查询请求的appid和对应的templateMD5
     */
    public Map<Integer/*appId*/, Set<String/*templateMD5*/>> getIds(long start, long end, ExceptionDslRequest request) {
        String subDsl = "";
        if (request.getFilterDsl() != null) {
            subDsl = "," + request.getFilterDsl().toJSONString();
        }

        Long minCheckQps = request.getMinCheckQps();
        if (minCheckQps == null) {
            minCheckQps = 1L;
        }

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_APPID_MD5, start, end, subDsl, minCheckQps);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(getIndexByTimeRange(start, end), null, dsl);
        if (esAggrMap == null) {
            return null;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("result");
        if (esAggr == null) {
            return null;
        }

        final Map<Integer, Set<String>> ret = Maps.newHashMap();
        handleBucketListInGetIds(esAggr, ret);

        return ret;
    }

    public Map<String/*md5*/, Map<Long/*timeStamp*/, Long/*qps*/>> getInfoByIds(long start, long end, Integer appId,
                                                                                ExceptionDslRequest request) {
        String subDsl = "";
        if (request.getFilterDsl() != null) {
            subDsl = "," + request.getFilterDsl().toJSONString();
        }

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_MD5_BY_TIMESTAMP, start, end, appId, subDsl,
                request.getInterval(), request.getMinQps());

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(getIndexByTimeRange(start, end), null, dsl);
        if (esAggrMap == null) {
            return null;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("template");
        if (esAggr == null) {
            return null;
        }

        final Map<String, Map<Long, Long>> ret = Maps.newHashMap();

        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            handleBucketListInGetInfoByIds(ret, esBucketList);
        }

        return ret;
    }

    public String matchIndices(Integer appId, String templateMd5, long start, long end,
                               Set<String> needIndexs) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.MATCH_GATEWAY_INDICES, start, end, appId,
                templateMd5);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(getIndexByTimeRange(start, end),
                null, dsl);

        if (esAggrMap == null) {
            return null;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get(INDICES);
        if (esAggr == null) {
            return null;
        }

        AtomicReference<String> matchIndex = new AtomicReference<>(null);
        List<ESBucket> esBucketList = esAggr.getBucketList();
        handleBucketList(needIndexs, matchIndex, esBucketList);

        return matchIndex.get();
    }

    public Map<String, Long> getIndicesForAggsDsl(String date) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_INDICES_FOR_AGGS);
        String realIndexName = getIndex(date);

        Map<String, Long> ret = Maps.newHashMap();

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, null, dsl);

        if (esAggrMap == null) {
            return ret;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("aggName");
        if (esAggr == null) {
            return ret;
        }

        for (ESBucket bucket : esAggr.getBucketList()) {
            String keys = String.valueOf(bucket.getUnusedMap().get("key"));
            Long count = Long.valueOf(bucket.getUnusedMap().get(DOC_COUNT) + "");

            for (String key : keys.split(",")) {
                ret.putIfAbsent(key, 0L);
                ret.put(key, ret.get(key) + count);
            }
        }

        return ret;
    }

    public Map<String, Long> getIndexForNormalDsl(long start, long end, String date) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_INDEX_FOR_NORMALS, start, end);
        String realIndexName = getIndex(date);

        Map<String, Long> ret = new HashMap<>();
        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, null, dsl);

        if (esAggrMap == null) {
            return ret;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("aggName");
        if (esAggr == null) {
            return ret;
        }

        for (ESBucket bucket : esAggr.getBucketList()) {
            String keys = String.valueOf(bucket.getUnusedMap().get("key"));
            Long count = Long.valueOf(bucket.getUnusedMap().get(DOC_COUNT) + "");

            JSONObject keyObj = JSON.parseObject(keys);
            for (String key : keyObj.keySet()) {
                ret.putIfAbsent(key, 0L);
                ret.put(key, ret.get(key) + count);
            }
        }

        return ret;
    }

    /**
     * 根据索引模板逻辑id，获取查询语句中使用的type名称
     *
     * @return
     */
    public Map<String/*typeName*/, Long> getSearchTypesByLogicId(long start, long end, Long logicId) {
        Map<String, Long> result = Maps.newHashMap();

        String realIndexNames = getIndexByTimeRange(start, end);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_SEARCH_TYPES_BY_LOGICID, logicId);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexNames, null, dsl);
        if (Objects.isNull(esAggrMap)) {
            return result;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("typeNames");
        if (Objects.isNull(esAggr)) {
            return result;
        }

        for (ESBucket bucket : esAggr.getBucketList()) {
            String keyName = String.valueOf(bucket.getUnusedMap().get("key"));
            Long   count   = Long.valueOf(bucket.getUnusedMap().get(DOC_COUNT).toString());

            result.put(keyName, count);
        }

        return result;
    }

    /**
     * 根据shard编号获取查询join日志
     *
     * @param indexDate
     * @param shardNo
     * @param scrollResultVisitor
     */
    public void scrollByShardNo(String indexDate, Integer shardNo,
                                ScrollResultVisitor<GatewayJoinPO> scrollResultVisitor) {
        String realIndexName = getIndex(indexDate);
        int scrollSize = 5000;

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_MULITY_TYPE_GATEWAY_JOIN_BY_SHARDNO,
                scrollSize);

        gatewayClient.queryWithScroll(realIndexName, typeName, dsl, scrollSize,
                String.format("_shards:%d", shardNo), GatewayJoinPO.class, scrollResultVisitor);
    }

    /**
     * 根据shard编号获取查询join日志
     *
     * @param indexDate
     * @param shardNo
     * @param scrollResultVisitor
     */
    public void scrollRequestLogByShardNo(String indexDate, Integer shardNo,
                                          ScrollResultVisitor<GatewayJoinPO> scrollResultVisitor) {
        String realIndexName = getIndex(indexDate);
        int scrollSize = 5000;

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_REQUEST_LOG_GATEWAY_JOIN_BY_SHARDNO, scrollSize);

        gatewayClient.queryWithScroll(realIndexName, typeName, dsl, scrollSize, String.format("_shards:%d", shardNo), GatewayJoinPO.class, scrollResultVisitor);
    }

    /**
     * 获取索引名称
     *
     * @param date
     * @return
     */
    public String getIndex(String date) {
        return String.format(DOUBLE_STRING_WITH_UNDER_LINE, getTemplateName(), date);
    }

    public String getTemplateName() {
        return indexName;
    }

    private String buildGatewayJoinSlowQueryCriteriaDsl(Integer appId, GatewayJoinQueryDTO queryDTO) {
        return "[" + buildGatewayJoinSlowQueryCriteriaCell(appId, queryDTO) +"]";
    }

    private String buildGatewayJoinSlowQueryCriteriaCell(Integer appId, GatewayJoinQueryDTO queryDTO) {
        List<String> cellList = Lists.newArrayList();
        // 最近时间范围条件
        cellList.add(DSLSearchUtils.getTermCellForRangeSearch(queryDTO.getStartTime(), queryDTO.getEndTime(), "timeStamp"));
        // appId 条件
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(appId, "appid"));
        // queryIndex 条件
        cellList.add(DSLSearchUtils.getTermCellForPrefixSearch(queryDTO.getQueryIndex(), "indices"));
        // totalCost>=1000即为慢查询
        cellList.add(DSLSearchUtils.getTermCellForRangeSearch(queryDTO.getTotalCost(), null, "totalCost"));
        // 只获取ariusType为type（正常的）的文档
        cellList.add(DSLSearchUtils.getTermCellForExactSearch("type", "ariusType"));
        return ListUtils.strList2String(cellList);
    }

    private String buildGatewayJoinErrorQueryCriteriaDsl(Integer appId, GatewayJoinQueryDTO queryDTO) {
        return "[" + buildGatewayJoinErrorQueryCriteriaCell(appId, queryDTO) +"]";
    }

    private String buildGatewayJoinErrorQueryCriteriaCell(Integer appId, GatewayJoinQueryDTO queryDTO) {
        List<String> cellList = Lists.newArrayList();
        // 最近时间范围条件
        cellList.add(DSLSearchUtils.getTermCellForRangeSearch(queryDTO.getStartTime(), queryDTO.getEndTime(), "timeStamp"));
        // appId 条件
        cellList.add(DSLSearchUtils.getTermCellForExactSearch(appId, "appid"));
        // queryIndex 条件
        cellList.add(DSLSearchUtils.getTermCellForPrefixSearch(queryDTO.getQueryIndex(), "indices"));
        // 只获取 ariusType 为error 即为异常
        cellList.add(DSLSearchUtils.getTermCellForExactSearch("error", "ariusType"));
        return ListUtils.strList2String(cellList);
    }

    /**
     * 获取GatewayJoin慢查询日志
     * @param appId 应用id
     * @param queryDTO 查询条件
     * @return List<GatewayJoinPO>
     */
    public List<GatewayJoinPO> getGatewayJoinSlowList(Integer appId, GatewayJoinQueryDTO queryDTO) {
        String queryCriteriaDsl = buildGatewayJoinSlowQueryCriteriaDsl(appId, queryDTO);
        String realName = IndexNameUtils.genDailyIndexName(indexName, queryDTO.getStartTime(), queryDTO.getEndTime());
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_SLOW_LIST_BY_CONDITION,
                queryCriteriaDsl);
        return gatewayClient.performRequest(realName, typeName, dsl, GatewayJoinPO.class);
    }

    /**
     * 获取GatewayJoin错误日志
     * @param appId 应用id
     * @param queryDTO 查询条件
     * @return List<GatewayJoinPO>
     */
    public List<GatewayJoinPO> getGatewayJoinErrorList(Integer appId, GatewayJoinQueryDTO queryDTO) {
        String queryCriteriaDsl = buildGatewayJoinErrorQueryCriteriaDsl(appId, queryDTO);
        String realName = IndexNameUtils.genDailyIndexName(indexName, queryDTO.getStartTime(), queryDTO.getEndTime());
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_GATEWAY_ERROR_LIST_BY_CONDITION,
                queryCriteriaDsl);
        return gatewayClient.performRequest(realName, typeName, dsl, GatewayJoinPO.class);
    }

    /**************************************************** private methods ****************************************************/
    private String getIndexByTimeRange(long start, long end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();

        String index = getTemplateName();

        start = start / ONE_DAY * ONE_DAY;
        end = end / ONE_DAY * ONE_DAY;

        long time = start;
        while (time <= end) {
            String timeStr = index.trim() + "_" + sdf.format(time);
            sb.append(timeStr).append(",");

            time += ONE_DAY;
        }

        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 1);
        } else {
            return sb.toString();
        }
    }

    /**
     * 获取索引表达式
     *
     * @return
     */
    private String getTemplateExpression() {
        return String.format("%s*", getTemplateName());
    }

    private void handleBucketListInGetTemplateMD5ByrealIndexName(Map<String, Set<String>> dslMap, List<ESBucket> esBucketList) {
        String md5;
        ESAggr subEsAggr;

        for (ESBucket esBucket : esBucketList) {
            if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                continue;
            }
            md5 = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();

            subEsAggr = esBucket.getAggrMap().get("samples");
            if (subEsAggr == null) {
                continue;
            }

            Object obj = subEsAggr.getUnusedMap().get("hits");
            handleHitsByhandleBucketListInGetTemplateMD5ByrealIndexName(dslMap, md5, obj);
        }
    }

    private void handleHitsByhandleBucketListInGetTemplateMD5ByrealIndexName(Map<String, Set<String>> dslMap, String md5, Object obj) {
        String dslSample;

        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray jsonArray = jsonObject.getJSONArray("hits");
            if (jsonArray != null && !jsonArray.isEmpty()) {
                JSONObject hitJsonObj = jsonArray.getJSONObject(0);
                JSONObject sourceJsonObj = hitJsonObj.getJSONObject("_source");
                if (sourceJsonObj != null) {
                    dslSample = sourceJsonObj.getString( "dsl" );
                    dslMap.computeIfAbsent(md5, k -> Sets.newHashSet()).add(dslSample);
                }
            }
        }
    }

    private void handleBucketList(Set<String> needIndexs, AtomicReference<String> matchIndex, List<ESBucket> esBucketList) {
        String indices;
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }
                indices = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();

                for (String i : needIndexs) {
                    if (indices.startsWith(i)) {
                        matchIndex.set(indices);
                    }
                }
            }
        }
    }

    private void handleBucketListInGetInfoByIds(Map<String, Map<Long, Long>> ret, List<ESBucket> esBucketList) {
        ESAggr subEsAggr;
        String md5;
        List<ESBucket> subEsBucketList;
        for (ESBucket esBucket : esBucketList) {
            if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                continue;
            }
            md5 = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();

            subEsAggr = esBucket.getAggrMap().get("minute_hist");
            if (subEsAggr == null) {
                continue;
            }

            subEsBucketList = subEsAggr.getBucketList();
            if (CollectionUtils.isEmpty(subEsBucketList)) {
                continue;
            }

            for (ESBucket subEsBucket : subEsBucketList) {
                if (subEsBucket.getUnusedMap() == null || subEsBucket.getUnusedMap().isEmpty()) {
                    continue;
                }
                Long timeStamp = Long.valueOf(subEsBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString());
                Long count = Long.valueOf(subEsBucket.getUnusedMap().get(ESConstant.AGG_DOC_COUNT).toString());

                ret.computeIfAbsent(md5, k -> Maps.newHashMap()).put(timeStamp, count);
            }
        }
    }

    private void handleBucketListInGetRtCostByAppId(List<Double> rtCostList, ESAggr queryByTimeStamp) {
        queryByTimeStamp.getBucketList().stream().forEach(esBucket -> {
            try {
                List<ESBucket> queryByAppidBucket = esBucket.getAggrMap().get("queryByAppid")
                        .getBucketList();
                if (CollectionUtils.isNotEmpty(queryByAppidBucket)) {
                    for (ESBucket bucket : queryByAppidBucket) {
                        JSONObject valueObj = (JSONObject) bucket.getAggrMap().get("1").getUnusedMap()
                                .get("values");
                        Double value = valueObj.getDouble("99.0");
                        if (null == value) {
                            continue;
                        }
                        rtCostList.add(value);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("class=GatewayJoinEsDao||method=getRtCostByAppId||errMsg={}", e);
            }
        });
    }

    private void handleBucketListInGetIds(ESAggr esAggr, Map<Integer, Set<String>> ret) {
        List<ESBucket> subEsBucketList;
        ESAggr subEsAggr;
        Integer appid;

        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }
                appid = Integer.valueOf(esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString());

                subEsAggr = esBucket.getAggrMap().get("template");
                if (subEsAggr == null) {
                    continue;
                }

                subEsBucketList = subEsAggr.getBucketList();
                handleSubEsBucketList(ret, subEsBucketList, appid);

            }
        }
    }

    private void handleSubEsBucketList(Map<Integer, Set<String>> ret, List<ESBucket> subEsBucketList, Integer appid) {
        String md5;
        if (CollectionUtils.isEmpty(subEsBucketList)) {
            return;
        }

        for (ESBucket subEsBucket : subEsBucketList) {
            if (subEsBucket.getUnusedMap() == null || subEsBucket.getUnusedMap().isEmpty()) {
                continue;
            }
            md5 = subEsBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();
            ret.computeIfAbsent(appid, k -> Sets.newHashSet()).add(md5);
        }
    }
}
