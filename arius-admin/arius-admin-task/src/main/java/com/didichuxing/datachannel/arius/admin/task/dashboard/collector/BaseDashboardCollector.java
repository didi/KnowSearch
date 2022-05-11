package com.didichuxing.datachannel.arius.admin.task.dashboard.collector;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.PHY_CLUSTER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.DashBoardStats;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.MonitorMetricsSender;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsDashBoardInfoESDAO;

/**
 * Created by linyunan on 3/11/22
 */
public abstract class BaseDashboardCollector {
    @Autowired
    protected ClusterPhyService            clusterPhyService;

    @Autowired
    protected ClusterLogicService          clusterLogicService;

    @Autowired
    protected IndexTemplateService indexTemplateService;

    @Autowired
    protected ESClusterService             esClusterService;

    @Autowired
    protected ESIndexService               esIndexService;

    @Autowired
    protected ESTemplateService            esTemplateService;

    @Autowired
    protected MonitorMetricsSender         monitorMetricsSender;

    @Autowired
    protected AriusStatsDashBoardInfoESDAO ariusStatsDashBoardInfoESDAO;

    public abstract void collectSingleCluster(String cluster, long currentTime);

    public abstract void collectAllCluster(List<String> clusterList, long currentTime);

    public abstract String getName();

    protected DashBoardStats buildInitDashBoardStats(Long timestamp){
        DashBoardStats dashBoardStats = new DashBoardStats();
        dashBoardStats.setPhysicCluster(PHY_CLUSTER);
        dashBoardStats.setTimestamp(timestamp);
        return dashBoardStats;
    }

}
