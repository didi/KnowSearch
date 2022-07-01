package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyNodeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyNodeTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.ESClusterTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsClusterTaskInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;

@Service
public class NodeStatisService {
    @Autowired
    private AriusStatsNodeInfoESDAO        ariusStatsNodeInfoEsDao;

    @Autowired
    private AriusStatsClusterTaskInfoESDAO ariusStatsClusterTaskInfoESDAO;

    public List<VariousLineChartMetrics> getAggClusterPhyNodeMetrics(MetricsClusterPhyNodeDTO param) {
        Integer topNu             =   param.getTopNu();
        String topMethod          =   param.getTopMethod();
        Integer topTimeStep       =   param.getTopTimeStep();
        String nodeName           =   param.getNodeName();
        String clusterPhyName     =   param.getClusterPhyName();
        String aggType            =   param.getAggType();
        Long endTime              =   param.getEndTime();
        Long startTime            =   param.getStartTime();
        List<String> metricsTypes =   param.getMetricsTypes();
        List<String> nodeNamesUnderClusterLogic = param.getItemNamesUnderClusterLogic();
        if (!AriusObjUtils.isBlack(param.getNodeName())) {
            return ariusStatsNodeInfoEsDao.getAggClusterPhySingleNodeMetrics(clusterPhyName, metricsTypes, nodeName,
                    aggType, startTime, endTime);
        }

        return ariusStatsNodeInfoEsDao.getTopNNodeAggMetricsWithStep(clusterPhyName,nodeNamesUnderClusterLogic, metricsTypes, topNu,topMethod,topTimeStep,
            aggType, startTime, endTime);
    }

    public List<ESClusterTaskDetail> getClusterTaskDetail(String cluster, String node, long startTime, long endTime) {
        return ariusStatsClusterTaskInfoESDAO.getTaskDetailByNode(cluster, node, startTime, endTime);
    }

    public List<VariousLineChartMetrics> getAggClusterPhyNodeTaskMetrics(MetricsClusterPhyNodeTaskDTO param) {
        Integer topNu             =   param.getTopNu();
        String nodeName           =   param.getNodeName();
        String clusterPhyName     =   param.getClusterPhyName();
        Long endTime              =   param.getEndTime();
        Long startTime            =   param.getStartTime();
        List<String> metricsTypes =   param.getMetricsTypes();
        List<String> aggTypes     = param.getAggTypes();
        List<String> nodeNamesUnderClusterLogic     = param.getItemNamesUnderClusterLogic();


        if (!AriusObjUtils.isBlack(param.getNodeName())) {
            return ariusStatsClusterTaskInfoESDAO.getAggClusterPhySingleNodeMetrics(clusterPhyName, metricsTypes, nodeName,
                    aggTypes, startTime, endTime);
        }

        return ariusStatsClusterTaskInfoESDAO.getTopNNodeAggMetrics(
                    clusterPhyName, metricsTypes, topNu, aggTypes, startTime, endTime);
    }
}
