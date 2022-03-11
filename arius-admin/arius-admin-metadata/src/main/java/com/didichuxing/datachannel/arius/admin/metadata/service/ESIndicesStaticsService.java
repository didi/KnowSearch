package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyTemplateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyIndicesDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;

/**
 * Created by linyunan on 2021-08-16
 */
@Service
public class ESIndicesStaticsService {
    @Value("${es.metrics.indices.buckets.max.num}")
    private int                      indicesBucketsMaxNum;

    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsIndexInfoESDAO;

    public List<VariousLineChartMetrics> getAggClusterPhyIndicesMetrics(MetricsClusterPhyIndicesDTO param) {
        Integer topNu                =   param.getTopNu();
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

        return ariusStatsIndexInfoESDAO.getTopNIndicesAggMetrics(clusterPhyName, metricsTypes, topNu, aggType,
                indicesBucketsMaxNum, startTime, endTime);
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
                indicesBucketsMaxNum, startTime, endTime);
    }
}
