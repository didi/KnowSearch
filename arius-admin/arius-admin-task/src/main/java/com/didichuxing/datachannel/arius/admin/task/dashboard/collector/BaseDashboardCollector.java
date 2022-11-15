package com.didichuxing.datachannel.arius.admin.task.dashboard.collector;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.DashBoardMetricThresholdDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.DashBoardStats;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusUnitUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.MonitorMetricsSender;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsDashBoardInfoESDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_DASHBOARD_THRESHOLD_GROUP;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.PHY_CLUSTER;

/**
 * Created by linyunan on 3/11/22
 */
public abstract class BaseDashboardCollector {
    @Autowired
    protected ClusterPhyService            clusterPhyService;

    @Autowired
    protected ClusterLogicService          clusterLogicService;

    @Autowired
    protected IndexTemplateService         indexTemplateService;

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

    @Autowired
    protected AriusConfigInfoService ariusConfigInfoService;

    private static final ILog LOGGER                            = LogFactory
            .getLog(BaseDashboardCollector.class);


    /**
     * 采集单个集群
     * @param cluster 集群名称
     * @param currentTime   当前时间戳
     */
    public abstract void collectSingleCluster(String cluster, long currentTime) throws ESOperateException;

    /**
     * 采集多个集群
     * @param clusterList   集群列表
     * @param currentTime   时间戳
     */
    public abstract void collectAllCluster(List<String> clusterList, long currentTime);

    /**
     * 获取任务名称
     * @return 任务名称
     */
    public abstract String getName();

    protected DashBoardStats buildInitDashBoardStats(Long timestamp) {
        DashBoardStats dashBoardStats = new DashBoardStats();
        dashBoardStats.setPhysicCluster(PHY_CLUSTER);
        dashBoardStats.setTimestamp(timestamp);
        return dashBoardStats;
    }

    /**
     * 获取dashboard配置值
     * catch:获取和转换都发生错误后，使用系统配置的默认配置项
     * @param valueName    配置名称
     * @param defaultValue 默认值
     * @return
     */
    public long getConfigOrDefaultValue(String valueName, String defaultValue, String unitStyle) {
        DashBoardMetricThresholdDTO configThreshold = ariusConfigInfoService.objectSetting(
                ARIUS_DASHBOARD_THRESHOLD_GROUP, valueName,
                JSON.parseObject(defaultValue, DashBoardMetricThresholdDTO.class), DashBoardMetricThresholdDTO.class);
        return AriusUnitUtil.unitChange(configThreshold.getValue().longValue(), configThreshold.getUnit(), unitStyle);
    }

}
