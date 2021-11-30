package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.List;

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
}
