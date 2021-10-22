package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslBase;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslMetricsPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ESConstant;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;
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
public class DslMetricsESDAO extends BaseESDAO {

    /**
     * 查询模板聚合数据的索引名称
     */
    private String            indexName;
    /**
     * type名称，主键id是topic_partition_offset
     */
    private String            typeName  = "type";

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusDslMetrices();
    }

    /**
     * 根据时间范围查询某个appid的记录数
     *
     * @param appId
     * @param startDate
     * @param endDate
     * @return
     */
    public Long queryTotalHitsByAppIdDate(Integer appId, String startDate, String endDate) {
        String dsl = dslLoaderUtil.getFormatDslByFileName( DslsConstant.GET_TOTAL_HITS_BY_APPID, appId, startDate, endDate);

        return gatewayClient.performRequestAndGetTotalCount(indexName, typeName, dsl);
    }

    /**
     * 查询某一天出现的appid和dslTemplateMd5
     *
     * @param date
     * @return
     */
    public List<DslMetricsPO> getAppIdTemplateMd5InfoByDate(String date) {
        List<DslMetricsPO> list = Lists.newLinkedList();

        String indexName = String.format("%s_%s", this.indexName.replace("*", ""), date);
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_APPID_TEMPLATE_MD5_INFO);
        String key = null;
        int index = -1;
        DslMetricsPO dslMetricsPo = null;

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(indexName, typeName, dsl);
        if (esAggrMap != null && esAggrMap.getEsAggrMap() != null) {
            ESAggr esAggr = esAggrMap.getEsAggrMap().get("appidDslTemplateMd5");
            if (esAggr != null) {
                List<ESBucket> esBucketList = esAggr.getBucketList();
                if (esBucketList != null) {
                    for (ESBucket esBucket : esBucketList) {
                        if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                            continue;
                        }

                        key = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();
                        index = key.indexOf("_");
                        if (index > 0) {
                            dslMetricsPo = new DslMetricsPO();
                            dslMetricsPo.setAppid(Integer.valueOf(key.substring(0, index)));
                            dslMetricsPo.setDslTemplateMd5(key.substring(index + 1));

                            list.add(dslMetricsPo);
                        }
                    }
                }
            }
        }

        return list;
    }

    /**
     * 获取最大一分钟查询量
     *
     * @param dslBase
     * @return
     */
    public DslMetricsPO getMaxAppidTemplateQpsInfoByAppidTemplateMd5(DslBase dslBase) {
        if(null == dslBase){return null;}

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_MAX_QPS_BY_KEY, dslBase.getAppid(), dslBase.getDslTemplateMd5());

        return gatewayClient.performRequestAndTakeFirst(indexName, typeName, dsl, DslMetricsPO.class);
    }

    public List<DslMetricsPO> getDslDetailMetricByAppidAndDslTemplateMd5(int appid, String dslTemplteMd5, long startDate, long endDate){
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_DETAIL_METRICS_BY_APPID_AND_MD5_AND_RANGE, appid, dslTemplteMd5, startDate, endDate, startDate, endDate);

        return gatewayClient.performRequest(indexName, typeName, dsl, s -> {
            List<DslMetricsPO> dslMetricsPos = new ArrayList<>();
            if (s == null){return dslMetricsPos;}

            List<ESBucket> esBuckets = s.getAggs().getEsAggrMap().get("groupByTimeStamp").getBucketList();

            if(CollectionUtils.isNotEmpty(esBuckets)){
                esBuckets.forEach(esBucket -> {
                    try {
                        Map<String, Object> unUsedMap = esBucket.getUnusedMap();
                        Map<String, ESAggr> aggrMap   = esBucket.getAggrMap();
                        if(null != unUsedMap && null != aggrMap) {
                            DslMetricsPO dslMetricsPo = new DslMetricsPO();

                            dslMetricsPo.setDslTemplateMd5(dslTemplteMd5);
                            dslMetricsPo.setAppid(appid);
                            dslMetricsPo.setDslLenAvg(Double.valueOf(aggrMap.get("dslLenAvg").getUnusedMap().get("value").toString()));
                            dslMetricsPo.setEsCostAvg(Double.valueOf(aggrMap.get("esCostAvg").getUnusedMap().get("value").toString()));
                            dslMetricsPo.setFailedShardsAvg(Double.valueOf(aggrMap.get("failedShardsAvg").getUnusedMap().get("value").toString()));
                            dslMetricsPo.setResponseLenAvg(Double.valueOf(aggrMap.get("responseLenAvg").getUnusedMap().get("value").toString()));
                            dslMetricsPo.setSearchCount(Double.valueOf(aggrMap.get("searchCount").getUnusedMap().get("value").toString()).longValue());
                            dslMetricsPo.setTotalCostAvg(Double.valueOf(aggrMap.get("totalCostAvg").getUnusedMap().get("value").toString()));
                            dslMetricsPo.setTotalHitsAvg(Double.valueOf(aggrMap.get("totalHitsAvg").getUnusedMap().get("value").toString()));
                            dslMetricsPo.setTotalShardsAvg(Double.valueOf(aggrMap.get("totalShardsAvg").getUnusedMap().get("value").toString()));
                            dslMetricsPo.setTimeStamp(Long.valueOf(unUsedMap.get("key").toString()));

                            dslMetricsPos.add(dslMetricsPo);
                        }
                    }catch (Exception e){
                        LOGGER.error("AriusStatsInfoEsDao.getDslDetailMetricByAppidAndDslTemplateMd5 exception! s:{}", s.toString(), e);
                    }
                });
            }

            return dslMetricsPos;
        }, 3);
    }

    /**
     * 查询某个appid一天查询次数
     *
     * @param appid
     * @param date
     * @return
     */
    public Long getTotalSearchByAppidDate(Integer appid, String date) {
        String index = String.format("%s_%s", indexName.replace("*", ""), date);

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_TOTAL_SEARCHCOUNT_BY_APPID, appid);

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
     * @param appid
     * @param dslTemplateMd5
     * @return
     */
    public DslMetricsPO getNeariestDslMetricsByappidTemplateMd5(Integer appid, String dslTemplateMd5) {
        String queryDsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_DSL_METRICS_BY_KEY, appid, dslTemplateMd5);

        return gatewayClient.performRequestAndTakeFirst(indexName, typeName, queryDsl,
                DslMetricsPO.class);
    }
}
