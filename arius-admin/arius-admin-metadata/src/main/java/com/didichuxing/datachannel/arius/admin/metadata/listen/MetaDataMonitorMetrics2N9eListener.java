package com.didichuxing.datachannel.arius.admin.metadata.listen;

import com.didichuxing.datachannel.arius.admin.client.bean.common.N9eData;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicWithRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterLogicStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsCells;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESDataTempBean;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.event.metrics.*;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.metadata.utils.MonitorUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.remote.monitor.RemoteMonitorService;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.lucene.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MetaDataMonitorMetrics2N9eListener implements ApplicationListener<MetaDataMetricsEvent> {
    protected static final ILog LOGGER = LogFactory.getLog(MetaDataMonitorMetrics2N9eListener.class);

    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private ClusterLogicService logicClusterService;

    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsIndexInfoEsDao;

    @Autowired()
    private RemoteMonitorService remoteMonitorService;

    private ThreadPoolExecutor esExecutor = new ThreadPoolExecutor(10, 20, 1000, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(4000),
            new NamedThreadFactory("Arius-Meta-MonitorMetricsSender-N9e"),
            (r, e) -> LOGGER.warn("class=MetaDataMonitorMetrics2N9eListener||msg=Arius-Meta-MonitorMetricsSender-N9e Deque is blocked, taskCount:{}" + e.getTaskCount()));

    @Override
    public void onApplicationEvent(MetaDataMetricsEvent event) {
        esExecutor.execute(() -> {
            if(event instanceof MetricsMonitorClusterEvent){
                MetricsMonitorClusterEvent monitorClusterEvent = (MetricsMonitorClusterEvent)event;
                sendMetrics(monitorClusterEvent);
            }

            if(event instanceof MetricsMonitorCollectTimeEvent){
                MetricsMonitorCollectTimeEvent collectTimeEvent = (MetricsMonitorCollectTimeEvent)event;
                sendMetrics(collectTimeEvent);
            }

            if(event instanceof MetricsMonitorIndexEvent){
                MetricsMonitorIndexEvent monitorIndexEvent = (MetricsMonitorIndexEvent)event;
                sendMetrics(monitorIndexEvent);
            }

            if(event instanceof MetricsMonitorNodeEvent){
                MetricsMonitorNodeEvent monitorNodeEvent = (MetricsMonitorNodeEvent)event;
                sendMetrics(monitorNodeEvent);
            }
        } );
    }

    private void sendMetrics(MetricsMonitorClusterEvent event){
        List<ESClusterStats> esClusterStatsList = event.getEsClusterStatsList();
        if (CollectionUtils.isEmpty(esClusterStatsList)) { return; }

        Map<ClusterPhy, ESClusterHealthResponse> clusterHealthResponseMap = event.getClusterHealthResponseMap();

        Map<String, ESClusterStatsCells> esClusterStatsCellsMap = esClusterStatsList.stream().map(ESClusterStats::getStatis)
                .collect( Collectors.toMap(ESClusterStatsCells::getClusterName, u -> u, (k1, k2) -> k1));

        if (MapUtils.isEmpty(clusterHealthResponseMap)) {
            LOGGER.warn("class=ClusterPhyMonitorDataBuilder||method=buildMonitorData||msg= clusterHealthResponseMap is empty");
            return;
        }

        if (MapUtils.isEmpty(esClusterStatsCellsMap)) {
            LOGGER.warn("class=ClusterPhyMonitorDataBuilder||method=buildMonitorData||msg= esClusterStatsCellsMap is empty");
            return;
        }

        // 只有线上和预发环境需要将指标上报到odin，线下和自测环境除外
        if (EnvUtil.isOnline() || EnvUtil.isPre()) {
            long timestamp = DateTimeUtil.getCurrentTimestampMinute();

            final Map<String, List<ClusterLogic>> physicalNameLogicClusterListMap = getPhysicalNameLogicClusterListMap();

            Map<ClusterLogic, ESClusterLogicStats> logicClusterMetricMap = Maps.newHashMap();

            Map<ClusterPhy, List<N9eData>> physicalClusterMetricMap = Maps.newHashMap();

            for (Map.Entry<ClusterPhy, ESClusterHealthResponse> entry : clusterHealthResponseMap.entrySet()) {
                ClusterPhy              dataSource          = entry.getKey();
                ESClusterHealthResponse response            = entry.getValue();
                ESClusterStatsCells     esClusterStatsCells = esClusterStatsCellsMap.get(dataSource.getCluster());

                // add physical cluster metric
                physicalClusterMetricMap.put(dataSource, getPhysicalOdinFormatList(response, dataSource, timestamp, esClusterStatsCells));

                updateLogicClusterMetric(response, dataSource, timestamp, physicalNameLogicClusterListMap, logicClusterMetricMap);
            }

            // send physical metric to N9e
            sendPhysicalClusterMetricToRemoteMonitor(physicalClusterMetricMap.values());

            // send logic metric to N9e
            sendLogicClusterMetricToRemoteMonitor(logicClusterMetricMap.values());
        }
    }

    /**
     * 发送物理机群指标到odin
     * @param monitorDataList
     */
    private void sendPhysicalClusterMetricToRemoteMonitor(Collection<List<N9eData>> monitorDataList) {
        if (CollectionUtils.isNotEmpty(monitorDataList)) {
            for (List<N9eData> monitorData : monitorDataList) {
                sendMetrics2N9e(monitorData);
            }
        }
    }

    /**
     * 发送逻辑集群指标到odin
     * @param logicClusterMetrics
     */
    private void sendLogicClusterMetricToRemoteMonitor(Collection<ESClusterLogicStats> logicClusterMetrics) {
        if (CollectionUtils.isNotEmpty(logicClusterMetrics)) {
            for (ESClusterLogicStats logicClusterMetric : logicClusterMetrics) {
                List<N9eData> logicN9eDataFormats = Lists.newArrayList();

                logicN9eDataFormats.add(getN9eDataFormat(String.valueOf(logicClusterMetric.getClusterId()),
                        "es.logic.cluster.health.status", String.valueOf(logicClusterMetric.getStatus()),
                        logicClusterMetric.getClusterLevel(), logicClusterMetric.getTimestamp()));

                logicN9eDataFormats.add(getN9eDataFormat(String.valueOf(logicClusterMetric.getClusterId()),
                        "es.logic.cluster.health.pendingTask", String.valueOf(logicClusterMetric.getPendingTask()),
                        logicClusterMetric.getClusterLevel(), logicClusterMetric.getTimestamp()));

                logicN9eDataFormats.add(getN9eDataFormat(String.valueOf(logicClusterMetric.getClusterId()),
                        "es.logic.cluster.health.unassignedShards",
                        String.valueOf(logicClusterMetric.getUnassignedShards()), logicClusterMetric.getClusterLevel(),
                        logicClusterMetric.getTimestamp()));

                sendMetrics2N9e(logicN9eDataFormats);
            }
        }
    }

    private void sendMetrics(MetricsMonitorCollectTimeEvent event){
        N9eData format = new N9eData();
        format.setTime(System.currentTimeMillis() / 1000);
        format.putTag("host",    event.getHostName());
        format.putTag("cluster", event.getCluster());
        format.putTag("type",    event.getType());
        format.putTag("level",   String.valueOf(event.getClusterLevel()));
        format.setMetric("monitor.metadata.collect.time");
        format.setValue(String.valueOf(event.getTime()));

        sendMetrics2N9e(Arrays.asList(format));
    }

    private void sendMetrics(MetricsMonitorIndexEvent event){
        sendMetrics(event.getEsDataTempBeans(), event.getClusterLevel());
    }

    private void sendMetrics(MetricsMonitorNodeEvent event){
        sendMetrics(event.getEsDataTempBeans(), event.getClusterLevel());
    }

    private void sendMetrics(List<ESDataTempBean>  esDataTempBeans, Integer clusterLevel){
        if (CollectionUtils.isEmpty(esDataTempBeans)) {return;}

        List<N9eData> n9eDataList = new ArrayList<>();
        for (ESDataTempBean esDataTempBean : esDataTempBeans) {
            if (esDataTempBean.isSendToN9e()) {
                N9eData n9eDataFormat = MonitorUtil.fillDataformat(esDataTempBean);
                n9eDataFormat.putTag("level", String.valueOf(clusterLevel));
                n9eDataList.add(n9eDataFormat);
            }
        }
    }

    private void sendMetrics2N9e(List<N9eData> n9eDatas){
        new BatchProcessor<N9eData, Boolean>().batchList(n9eDatas).batchSize(30)
                .processor(items -> remoteMonitorService.sendData(n9eDatas))
                .succChecker(succ -> succ).process();
    }

    /**
     * 获取物理机群 提交到Odin格式 数据
     * @param response
     * @param dataSource
     * @param timestamp
     * @return
     */
    private List<N9eData> getPhysicalOdinFormatList(ESClusterHealthResponse response, ClusterPhy dataSource,
                                                    long timestamp, ESClusterStatsCells esClusterStatsCells) {
        List<N9eData> n9eDataList = Lists.newArrayList();
        n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.node.count",
                String.valueOf(esClusterService.syncGetClientAlivePercent(dataSource.getCluster(), dataSource.getPassword(), dataSource.getHttpAddress()))
                , dataSource.getLevel(), timestamp));

        long qps = ariusStatsIndexInfoEsDao.getClusterQps(dataSource.getCluster());
        n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.qps.total", String.valueOf(qps),
                dataSource.getLevel(), timestamp));

        long tps = ariusStatsIndexInfoEsDao.getClusterTps(dataSource.getCluster());
        n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.tps.total", String.valueOf(tps),
                dataSource.getLevel(), timestamp));

        if (response == null || response.isTimedOut()) { return n9eDataList; }

        int status = ClusterHealthEnum.valuesOf(response.getStatus()).getCode();
        n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.health.status", String.valueOf(status),
                dataSource.getLevel(), timestamp));

        long unAssignedShards = response.getUnassignedShards();
        n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.health.unassignedShards",
                String.valueOf(unAssignedShards), dataSource.getLevel(), timestamp));

        long numberPendingTasks = response.getNumberOfPendingTasks();
        n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.health.pendingTask",
                String.valueOf(numberPendingTasks), dataSource.getLevel(), timestamp));

        String numberDataNodes = String.valueOf(response.getNumberOfDataNodes());
        n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.health.number.of.data.nodes",
                numberDataNodes, dataSource.getLevel(), timestamp));

        String numberNodes = String.valueOf(response.getNumberOfNodes());
        n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.health.number.of.nodes", numberNodes,
                dataSource.getLevel(), timestamp));
        //添加 cpu使用率,磁盘利用率
        if (esClusterStatsCells != null) {
            String cpuUsage = String.valueOf(esClusterStatsCells.getCpuUsage());
            n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.cpu.usage", cpuUsage, dataSource.getLevel(), timestamp));

            String diskUsage = String.valueOf(esClusterStatsCells.getDiskUsage());
            n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.disk.usage", diskUsage, dataSource.getLevel(), timestamp));

        }
        return n9eDataList;
    }

    /**
     * 构建odin数据
     *
     * @param cluster
     * @param metric
     * @param value
     * @param clusterLevel
     */
    private N9eData getN9eDataFormat(String cluster, String metric, String value, int clusterLevel,
                                     long timestamp) {
        N9eData dataFormat = new N9eData();
        dataFormat.setMetric(metric);
        dataFormat.setValue(value);
        dataFormat.setTime(timestamp);
        dataFormat.putTag("host", HttpHostUtil.HOST_NAME);
        dataFormat.putTag("cluster", cluster);
        dataFormat.putTag("level", String.valueOf(clusterLevel));

        return dataFormat;
    }

    /**
     * 更新 逻辑集群指标
     * @param response
     * @param datasource
     * @param timestamp
     * @param physicalNameLogicClusterListMap
     * @param logicClusterMetricMap
     */
    private void updateLogicClusterMetric(ESClusterHealthResponse response, ClusterPhy datasource, long timestamp,
                                          Map<String /*phyClusterName*/, List<ClusterLogic>> physicalNameLogicClusterListMap,
                                          Map<ClusterLogic, ESClusterLogicStats> logicClusterMetricMap) {
        if (response == null) {
            return;
        }

        int status = ClusterHealthEnum.valueOf(response.getStatus()).getCode();
        long unAssignedShards = response.getUnassignedShards();
        long numberPendingTasks = response.getNumberOfPendingTasks();

        final List<ClusterLogic> logicClusters = physicalNameLogicClusterListMap.get(datasource.getCluster());

        if (CollectionUtils.isNotEmpty(logicClusters)) {
            for (ClusterLogic logicCluster : logicClusters) {
                handleLogicCluster(datasource, timestamp, logicClusterMetricMap, status, unAssignedShards, numberPendingTasks, logicCluster);
            }
        }
    }

    private void handleLogicCluster(ClusterPhy datasource, long timestamp, Map<ClusterLogic, ESClusterLogicStats> logicClusterMetricMap, int status, long unAssignedShards, long numberPendingTasks, ClusterLogic logicCluster) {
        ESClusterLogicStats logicClusterMetric = logicClusterMetricMap.get(logicCluster);
        if (logicClusterMetric == null) {
            logicClusterMetric = new ESClusterLogicStats();
        }

        Integer maxStatus = logicClusterMetric.getStatus() == null ? status
                : Math.max(status, logicClusterMetric.getStatus());
        logicClusterMetric.setStatus(maxStatus);

        long sumUnassignedShards = logicClusterMetric.getUnassignedShards() == null ? unAssignedShards
                : logicClusterMetric.getUnassignedShards() + unAssignedShards;
        logicClusterMetric.setUnassignedShards(sumUnassignedShards);

        long sumPendingTask = logicClusterMetric.getPendingTask() == null ? numberPendingTasks
                : logicClusterMetric.getPendingTask() + numberPendingTasks;
        logicClusterMetric.setPendingTask(sumPendingTask);

        logicClusterMetric.setClusterId(logicCluster.getId());
        logicClusterMetric.setClusterLevel(datasource.getLevel());
        logicClusterMetric.setTimestamp(timestamp);
        logicClusterMetricMap.put(logicCluster, logicClusterMetric);
    }

    /**
     * 获取物理集群名->逻辑集群列表映射
     * @return
     */
    private Map<String, List<ClusterLogic>> getPhysicalNameLogicClusterListMap() {
        Map<String, List<ClusterLogic>> physicalLogicMap = Maps.newHashMap();
        for (ClusterLogicWithRack logic : logicClusterService.listAllClusterLogicsWithRackInfo()) {
            Collection<ClusterLogicRackInfo> items = logic.getItems();
            if (CollectionUtils.isNotEmpty(items)) {
                for (ClusterLogicRackInfo item : items) {
                    List<Long> logicClusterIds = ListUtils.string2LongList(item.getLogicClusterIds());
                    if (CollectionUtils.isEmpty(logicClusterIds)) {
                        continue;
                    }

                    logicClusterIds.forEach(logicClusterId -> {
                        ClusterLogic clusterLogic    = logicClusterService.getClusterLogicById(logicClusterId);
                        List<ClusterLogic> logicList = physicalLogicMap.get(clusterLogic.getName());

                        if (logicList == null) {
                            logicList = Lists.newArrayList();
                        }
                        if (!logicList.contains(logic)) {
                            logicList.add(logic);
                        }
                        physicalLogicMap.put(clusterLogic.getName(), logicList);
                    });
                }
            }
        }
        return physicalLogicMap;
    }
}
