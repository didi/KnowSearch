package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.AriusClusterStatisPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggr;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class AriusStatsClusterInfoESDAO extends BaseAriusStatsESDAO {

    @PostConstruct
    public void init(){
        super.indexName   = dataCentreUtil.getAriusStatsClusterInfo();
        BaseAriusStatsESDAO.register( AriusStatsEnum.CLUSTER_INFO,this);
    }

    /**
     * 获取集群级别统计数据
     *
     * @param cluster
     * @param startTime
     * @param endTime
     * @return
     */
    public AriusClusterStatisPO getMaxClusterStatisByRange(String cluster, Long startTime, Long endTime) {
        cluster = StringUtils.isBlank(cluster) ? "allCluster" : cluster;

        String realIndexName = IndexNameUtils.genDailyIndexName(indexName, startTime, endTime);
        String dsl           = dslLoaderUtil.getFormatDslByFileName( DslsConstant.GET_MAX_CLUSTER_STATIS_BY_TIME_RANGE_AND_CLUSTER, cluster, startTime, endTime);

        return gatewayClient.performRequest(realIndexName, TYPE, dsl, response -> {
            AriusClusterStatisPO ariusClusterStatisPO = new AriusClusterStatisPO();

            if (response == null || response.getAggs() == null){return ariusClusterStatisPO;}

            try {
                Map<String, ESAggr> esAggrMap = response.getAggs().getEsAggrMap();

                ESAggr maxStoreSizeESAggr       = esAggrMap.get("storeSize_max");
                ESAggr maxTotalStoreSizeESAggr  = esAggrMap.get("totalStoreSize_max");
                ESAggr maxFreeStoreSizeESAggr   = esAggrMap.get("freeStoreSize_max");
                ESAggr maxTotalIndexNuESAggr    = esAggrMap.get("totalIndex_max");
                ESAggr maxTotalDocNuESAggr      = esAggrMap.get("totalDocNu_max");
                ESAggr maxWriteTpsESAggr        = esAggrMap.get("writeTps_max");
                ESAggr maxIndexStoreSizeESAggr  = esAggrMap.get("indexStoreSize_max");
                ESAggr maxTotalTemplateESAggr   = esAggrMap.get("totalTemplateNu_max");
                ESAggr maxReadTpsESAggr         = esAggrMap.get("readTps_max");
                ESAggr maxClusterNuESAggr       = esAggrMap.get("clusterNu_max");

                if(null != maxStoreSizeESAggr.getUnusedMap() && null != maxStoreSizeESAggr.getUnusedMap().get("value")){
                    ariusClusterStatisPO.setStoreSize(Double.valueOf(maxStoreSizeESAggr.getUnusedMap().get("value").toString()).longValue());
                }

                if(null != maxTotalStoreSizeESAggr.getUnusedMap() && null != maxTotalStoreSizeESAggr.getUnusedMap().get("value")){
                    ariusClusterStatisPO.setTotalStoreSize(Double.valueOf(maxTotalStoreSizeESAggr.getUnusedMap().get("value").toString()).longValue());
                }

                if(null != maxFreeStoreSizeESAggr.getUnusedMap() && null != maxFreeStoreSizeESAggr.getUnusedMap().get("value")){
                    ariusClusterStatisPO.setFreeStoreSize(Double.valueOf(maxFreeStoreSizeESAggr.getUnusedMap().get("value").toString()).longValue());
                }

                if(null != maxTotalIndexNuESAggr.getUnusedMap() && null != maxTotalIndexNuESAggr.getUnusedMap().get("value")){
                    ariusClusterStatisPO.setTotalIndicesNu(Double.valueOf(maxTotalIndexNuESAggr.getUnusedMap().get("value").toString()).longValue());
                }

                if(null != maxTotalDocNuESAggr.getUnusedMap() && null != maxTotalDocNuESAggr.getUnusedMap().get("value")){
                    ariusClusterStatisPO.setTotalDocNu(Double.valueOf(maxTotalDocNuESAggr.getUnusedMap().get("value").toString()).longValue());
                }

                if(null != maxWriteTpsESAggr.getUnusedMap() && null != maxWriteTpsESAggr.getUnusedMap().get("value")){
                    ariusClusterStatisPO.setWriteTps(Double.valueOf(maxWriteTpsESAggr.getUnusedMap().get("value").toString()));
                }

                if(null != maxIndexStoreSizeESAggr.getUnusedMap() && null != maxIndexStoreSizeESAggr.getUnusedMap().get("value")){
                    ariusClusterStatisPO.setIndexStoreSize(Double.valueOf(maxIndexStoreSizeESAggr.getUnusedMap().get("value").toString()));
                }

                if(null != maxTotalTemplateESAggr.getUnusedMap() && null != maxTotalTemplateESAggr.getUnusedMap().get("value")){
                    ariusClusterStatisPO.setTotalTemplateNu(Double.valueOf(maxTotalTemplateESAggr.getUnusedMap().get("value").toString()).intValue());
                }

                if(null != maxReadTpsESAggr.getUnusedMap() && null != maxReadTpsESAggr.getUnusedMap().get("value")){
                    ariusClusterStatisPO.setReadTps(Double.valueOf(maxReadTpsESAggr.getUnusedMap().get("value").toString()));
                }

                if(null != maxClusterNuESAggr.getUnusedMap() && null != maxClusterNuESAggr.getUnusedMap().get("value")){
                    ariusClusterStatisPO.setClusterNu(Double.valueOf(maxClusterNuESAggr.getUnusedMap().get("value").toString()).intValue());
                }
            } catch (Exception e){
                LOGGER.error("class=AriusStatsClusterInfoEsDao||method=getMaxClusterStatisByRange||errMsg=exception! response:{}",
                        response.toString(), e);
            }

            return ariusClusterStatisPO;
        }, 3);
    }
}
