package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.RackMetaMetric;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.NodeRackStatisPO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;

@Service
public class NodeStatisService {

    @Value("${es.metrics.nodes.max.num}")
    private int                      esNodesMaxNum;

    @Autowired
    private AriusStatsNodeInfoESDAO ariusStatsNodeInfoEsDao;

    public Result<List<RackMetaMetric>> getRackStatis(String cluster, Collection<String> racks) {
        List<String> rackList = new ArrayList<>(racks);
        List<NodeRackStatisPO> nodeRackStatisPOS = ariusStatsNodeInfoEsDao.getRackStatis(cluster, rackList);

        return Result.buildSucc(ConvertUtil.list2List(nodeRackStatisPOS, RackMetaMetric.class));
    }

    public List<VariousLineChartMetrics> getAggClusterPhyNodeMetrics(MetricsClusterPhyNodeDTO param) {
        Integer topNu             =   param.getTopNu();
        String nodeName           =   param.getNodeName();
        String clusterPhyName     =   param.getClusterPhyName();
        String aggType            =   param.getAggType();
        Long endTime              =   param.getEndTime();
        Long startTime            =   param.getStartTime();
        List<String> metricsTypes =   param.getMetricsTypes();

        if (!AriusObjUtils.isBlack(param.getNodeName())) {
            return ariusStatsNodeInfoEsDao.getAggClusterPhySingleNodeMetrics(clusterPhyName, metricsTypes, nodeName,
                aggType, startTime, endTime);
        }
        return ariusStatsNodeInfoEsDao.getTopNNodeAggMetrics(clusterPhyName, metricsTypes, topNu,
            aggType, esNodesMaxNum, startTime, endTime);
    }
}
