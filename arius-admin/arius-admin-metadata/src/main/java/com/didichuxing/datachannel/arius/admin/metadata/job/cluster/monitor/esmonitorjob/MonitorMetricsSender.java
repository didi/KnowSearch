package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.DashBoardStats;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.util.NamedThreadFactory;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterTaskStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexDCDRStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexToNodeStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIngestStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESNodeStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESNodeToIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.BaseAriusStatsESDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * 发送monitor指标数据
 */
@Component("monitorMetricsSender")
public class MonitorMetricsSender {

    protected static final ILog LOGGER = LogFactory.getLog(MonitorMetricsSender.class);

    private static final int    THRESHOLD = 100;

    private ThreadPoolExecutor esExecutor = new ThreadPoolExecutor(30, 60, 6000, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(4000),
            new NamedThreadFactory("Arius-Meta-MonitorMetricsSender-ES"),
            (r, e) -> LOGGER.warn("class=MonitorMetricsSender||msg=Arius-Meta-MonitorMetricsSender-ES Deque is blocked, taskCount:{}" + e.getTaskCount()));

    public void sendNodeInfo(List<ESNodeStats> esNodeStatsList) {
        send2es( AriusStatsEnum.NODE_INFO, esNodeStatsList);
    }

    public void sendIndexInfo(List<ESIndexStats> esIndexStats){
        send2es(AriusStatsEnum.INDEX_INFO, esIndexStats);
    }

    public void sendIndexToNodeStats(List<ESIndexToNodeStats> esIndexToNodeStats){
        send2es(AriusStatsEnum.INDEX_NODE_INFO, esIndexToNodeStats);
    }

    public void sendESNodeToIndexStats(List<ESNodeToIndexStats> esNodeToIndexStats){
        send2es(AriusStatsEnum.NODE_INDEX_INFO, esNodeToIndexStats);
    }

    public void sendIngestStats(List<ESIngestStats> esIngestStats) {
        send2es(AriusStatsEnum.INGEST_INFO, esIngestStats);
    }

    public void sendDCDRStats(List<ESIndexDCDRStats> esIndexDCDRStats) {
        send2es(AriusStatsEnum.DCDR_INFO, esIndexDCDRStats);
    }

    public void sendClusterStats(List<ESClusterStats> esClusterStats) {
        send2es(AriusStatsEnum.CLUSTER_INFO, esClusterStats);
    }

    public void sendClusterTaskStats(List<ESClusterTaskStats> esClusterTaskStats) {
        send2es(AriusStatsEnum.TASK_INFO, esClusterTaskStats);
    }

    public void sendDashboardStats(List<DashBoardStats> dashBoardStats) {
        send2es(AriusStatsEnum.DASHBOARD_INFO, dashBoardStats);
    }

    /**
     * 根据不同监控维度来发送
     *
     * @param ariusStats
     * @param statsList
     * @return
     */
    private boolean send2es(AriusStatsEnum ariusStats, List<? extends BaseESPO> statsList){
        if (CollectionUtils.isEmpty(statsList)) {
            return true;
        }

        if (EnvUtil.isPre()) {
            LOGGER.info("class=MonitorMetricsSender||method=send2es||ariusStats={}||size={}",
                    ariusStats.getType(), statsList.size());
        }

        BaseAriusStatsESDAO baseAriusStatsEsDao = BaseAriusStatsESDAO.getByStatsType(ariusStats);
        if (Objects.isNull(baseAriusStatsEsDao)) {
            LOGGER.error("class=MonitorMetricsSender||method=send2es||errMsg=fail to find {}", ariusStats.getType());
            return false;
        }

        int size = statsList.size();
        int num  = (size) % THRESHOLD == 0 ? (size / THRESHOLD) : (size / THRESHOLD + 1);

        if (size < THRESHOLD) {
            esExecutor.execute(() ->
                    baseAriusStatsEsDao.batchInsertStats(statsList));
            return true;
        }

        for (int i = 1; i < num + 1; i++) {
            int end   = (i * THRESHOLD) > size ? size : (i * THRESHOLD);
            int start = (i - 1) * THRESHOLD;

            esExecutor.execute(() ->
                    baseAriusStatsEsDao.batchInsertStats(statsList.subList(start, end)));
        }

        return true;
    }
}
