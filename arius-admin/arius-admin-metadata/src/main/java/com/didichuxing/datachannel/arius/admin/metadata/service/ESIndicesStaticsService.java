package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyIndicesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.google.common.collect.Maps;

/**
 * Created by linyunan on 2021-08-16
 */
@Service
public class ESIndicesStaticsService {
    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsIndexInfoESDAO;

    public List<VariousLineChartMetrics> getAggClusterPhyIndicesMetrics(MetricsClusterPhyIndicesDTO param) {
        Integer topNu                =   param.getTopNu();
        String topMethod         =   param.getTopMethod();
        Integer topTimeStep       =   param.getTopTimeStep();
        String searchIndexName       =   param.getIndexName();
        String clusterPhyName        =   param.getClusterPhyName();
        String aggType               =   param.getAggType();
        Long endTime                 =   param.getEndTime();
        Long startTime               =   param.getStartTime();
        List<String> metricsTypes    =   param.getMetricsTypes();

        if (!AriusObjUtils.isBlack(param.getIndexName())) {
            return ariusStatsIndexInfoESDAO.getAggSingleIndexMetrics(clusterPhyName, metricsTypes, searchIndexName, aggType,
                startTime, endTime);
        }

        return ariusStatsIndexInfoESDAO.getTopNIndicesAggMetricsWithStep(clusterPhyName, metricsTypes, topNu,topMethod,topTimeStep, aggType, startTime, endTime);
    }

    /**
     * 获取并且聚合对应的物理集群的模板指标类型
     * @param param 模板指标类型
     * @return 聚合完成的指标类型列表
     */
    public List<VariousLineChartMetrics> getAggClusterPhyTemplateMetrics(MetricsClusterPhyTemplateDTO param) {
        Integer topNu = param.getTopNu();
        Integer logicTemplateId = param.getLogicTemplateId();
        String clusterPhyName = param.getClusterPhyName();
        String aggType = param.getAggType();
        Long endTime = param.getEndTime();
        Long startTime = param.getStartTime();
        List<String> metricsTypes = param.getMetricsTypes();

        if (!AriusObjUtils.isNull(param.getLogicTemplateId())) {
            return ariusStatsIndexInfoESDAO.getAggSingleTemplateMetrics(clusterPhyName, metricsTypes, logicTemplateId, aggType,
                    startTime, endTime);
        }

        return ariusStatsIndexInfoESDAO.getTopNTemplateAggMetrics(clusterPhyName, metricsTypes, topNu, aggType,
                startTime, endTime);
    }

    /**
     * 获取索引search-query数突增量
     * 上个时间值(now-3,now-2)/(now-5,now-4)上个时间间隔值的两倍 >=2.0
     *
     * @param cluster      集群
     * @param indexList   索引列表
     * @return {@code Map<String, Double>}
     */
    public Map<String, Double> getIndex2CurrentSearchQueryMap(String cluster, List<String> indexList){
        if (AriusObjUtils.isBlack(cluster) || CollectionUtils.isEmpty(indexList)) { return Maps.newHashMap();}
        return ariusStatsIndexInfoESDAO.getIndex2CurrentSearchQueryMap(cluster, indexList);
    }

    /**
     * 获取索引indexing-index数突增量
     * 上个时间值(now-3,now-2)/(now-5,now-4)上个时间间隔值的两倍 >=2.0
     *
     * @param cluster     集群
     * @param indexList   索引列表
     * @return {@code Map<String, Double>}
     */
    public Map<String, Double> getIndex2CurrentIndexingIndexMap(String cluster, List<String> indexList) {
        if (AriusObjUtils.isBlack(cluster) || CollectionUtils.isEmpty(indexList)) { return Maps.newHashMap();}
        return ariusStatsIndexInfoESDAO.getIndex2CurrentIndexingIndexMap(cluster, indexList);
    }


}
