package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslBase;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslMetricsPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ESConstant;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * dsl的指标信息，游flink任务解析dsl语句的时候写入数据
 */
@Component
@NoArgsConstructor
public class DslMetricsESDAO extends BaseESDAO {

    private static final String VALUE    = "value";

    /**
     * 查询模板聚合数据的索引名称
     */
    private String              indexName;
    /**
     * type名称，主键id是topic_partition_offset
     */
    private String              typeName = "type";

    @PostConstruct
    public void init() {
        this.indexName = dataCentreUtil.getAriusDslMetrices();
    }

    /**
     * 根据时间范围查询某个projectId的记录数
     *
     * @param projectId
     * @param startDate
     * @param endDate
     * @return
     */
    public Long queryTotalHitsByProjectIdDate(Integer projectId, String startDate, String endDate) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_HITS_BY_PROJECT_ID, projectId,
            startDate, endDate);

        return gatewayClient.performRequestAndGetTotalCount(indexName, typeName, dsl);
    }

    /**
     * 查询某一天出现的projectid和dslTemplateMd5
     *
     * @param date
     * @return
     */
    public List<DslMetricsPO> getProjectIdTemplateMd5InfoByDate(String date) {
        List<DslMetricsPO> list = Lists.newLinkedList();

        String realIndexName = String.format("%s_%s", this.indexName.replace("*", ""), date);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_PROJECT_ID_TEMPLATE_MD5_INFO);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(realIndexName, typeName, dsl);
        if (esAggrMap != null && esAggrMap.getEsAggrMap() != null) {
            ESAggr esAggr = esAggrMap.getEsAggrMap().get("appidDslTemplateMd5");
            if (esAggr != null) {
                List<ESBucket> esBucketList = esAggr.getBucketList();
                if (esBucketList != null) {
                    handleBucketList(list, esBucketList);
                }
            }
        }

        return list;
    }

    private void handleBucketList(List<DslMetricsPO> list, List<ESBucket> esBucketList) {
        DslMetricsPO dslMetricsPo;
        String key;
        int index;
        for (ESBucket esBucket : esBucketList) {
            if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                continue;
            }

            key = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();
            index = key.indexOf("_");
            if (index > 0) {
                dslMetricsPo = new DslMetricsPO();
                dslMetricsPo.setProjectId(Integer.valueOf(key.substring(0, index)));
                dslMetricsPo.setDslTemplateMd5(key.substring(index + 1));

                list.add(dslMetricsPo);
            }
        }
    }

    /**
     * 获取最大一分钟查询量
     *
     * @param dslBase
     * @return
     */
    public DslMetricsPO getMaxProjectIdTemplateQpsInfoByProjectIdTemplateMd5(DslBase dslBase) {
        if (null == dslBase) {
            return null;
        }

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_MAX_QPS_BY_KEY, dslBase.getProjectId(),
            dslBase.getDslTemplateMd5());
        String realIndexName = IndexNameUtils.genCurrentDailyIndexName(indexName);
        return gatewayClient.performRequestAndTakeFirst(realIndexName, typeName, dsl, DslMetricsPO.class);
    }

    public List<DslMetricsPO> getDslDetailMetricByProjectIdAndDslTemplateMd5(int projectId, String dslTemplteMd5,
                                                                             long startDate, long endDate) {
        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startDate, endDate);

        String dsl = dslLoaderUtil.getFormatDslByFileName(
            DslsConstant.GET_DSL_DETAIL_METRICS_BY_PROJECT_ID_AND_MD5_AND_RANGE, projectId, dslTemplteMd5, startDate,
            endDate, startDate, endDate);

        return gatewayClient.performRequest(realIndexName, typeName, dsl, s -> {
            List<DslMetricsPO> dslMetricsPos = new ArrayList<>();
            if (s == null) {
                return dslMetricsPos;
            }

            List<ESBucket> esBuckets = s.getAggs().getEsAggrMap().get("groupByTimeStamp").getBucketList();

            if (CollectionUtils.isNotEmpty(esBuckets)) {
                esBuckets.forEach(esBucket -> {
                    try {
                        Map<String, Object> unUsedMap = esBucket.getUnusedMap();
                        Map<String, ESAggr> aggrMap = esBucket.getAggrMap();
                        if (null != unUsedMap && null != aggrMap) {
                            DslMetricsPO dslMetricsPo = new DslMetricsPO();

                            dslMetricsPo.setDslTemplateMd5(dslTemplteMd5);
                            dslMetricsPo.setProjectId(projectId);
                            dslMetricsPo.setDslLenAvg(
                                Double.valueOf(aggrMap.get("dslLenAvg").getUnusedMap().get(VALUE).toString()));
                            dslMetricsPo.setEsCostAvg(
                                Double.valueOf(aggrMap.get("esCostAvg").getUnusedMap().get(VALUE).toString()));
                            dslMetricsPo.setFailedShardsAvg(
                                Double.valueOf(aggrMap.get("failedShardsAvg").getUnusedMap().get(VALUE).toString()));
                            dslMetricsPo.setResponseLenAvg(
                                Double.valueOf(aggrMap.get("responseLenAvg").getUnusedMap().get(VALUE).toString()));
                            dslMetricsPo.setSearchCount(Double
                                .valueOf(aggrMap.get("searchCount").getUnusedMap().get(VALUE).toString()).longValue());
                            dslMetricsPo.setTotalCostAvg(
                                Double.valueOf(aggrMap.get("totalCostAvg").getUnusedMap().get(VALUE).toString()));
                            dslMetricsPo.setTotalHitsAvg(
                                Double.valueOf(aggrMap.get("totalHitsAvg").getUnusedMap().get(VALUE).toString()));
                            dslMetricsPo.setTotalShardsAvg(
                                Double.valueOf(aggrMap.get("totalShardsAvg").getUnusedMap().get(VALUE).toString()));
                            dslMetricsPo.setTimeStamp(Long.valueOf(unUsedMap.get("key").toString()));

                            dslMetricsPos.add(dslMetricsPo);
                        }
                    } catch (Exception e) {
                        LOGGER.error(
                            "class=AriusStatsInfoEsDao||method=getDslDetailMetricByProjectIdAndDslTemplateMd5||exceptionMsg:{}",
                            e);
                    }
                });
            }

            return dslMetricsPos;
        }, 3);
    }

    /**
     * 查询某个projectId一天查询次数
     *
     * @param projectId
     * @param date
     * @return
     */
    public Long getTotalSearchByProjectIdDate(Integer projectId, String date) {
        String index = String.format("%s_%s", indexName.replace("*", ""), date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_SEARCHCOUNT_BY_PROJECT_ID, projectId);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(index, typeName, dsl);
        if (esAggrMap == null) {
            return null;
        }

        ESAggr esAggr = esAggrMap.getEsAggrMap().get("totalSearchCnt");
        Double value = Double.valueOf(esAggr.getUnusedMap().getOrDefault(ESConstant.SUM_VALUE, "0").toString());

        return value.longValue();
    }

    /**
     *
     * 获取一个查询模板的数据
     *
     * @param projectId
     * @param dslTemplateMd5
     * @return
     */
    public DslMetricsPO getNeariestDslMetricsByProjectIdTemplateMd5(Integer projectId, String dslTemplateMd5) {
        String queryDsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_METRICS_BY_KEY, projectId,
            dslTemplateMd5);

        return gatewayClient.performRequestAndTakeFirst(indexName, typeName, queryDsl, DslMetricsPO.class);
    }
}