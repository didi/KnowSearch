package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.common.OdinData;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicWithRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterTempBean;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpHostUtil;
import com.didichuxing.datachannel.arius.admin.core.component.OdinSender;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.MonitorMetricsSender;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateAccessESDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.gateway.direct.DirectRequest;
import com.didichuxing.datachannel.arius.elasticsearch.client.gateway.direct.DirectResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.cluster.health.ESClusterHealthRequest;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.util.NamedThreadFactory;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;


/**
 * 集群维度采集监控数据，包含 es节点存活检查；es集群tps/qps掉底报警
 */
@Component
public class ClusterMonitorJobHandler extends AbstractMetaDataJob {

    private static final int ODIN_STEP = 60;
    private static final double SLA = 0.9999;

    @Autowired
    private ESClusterLogicService logicClusterService;

    @Autowired
    private ESClusterPhyService esClusterPhyService;

    @Autowired
    private TemplatePhyService templatePhyService;

    @Autowired
    private ESOpClient          esOpClient;

    @Autowired
    private AppService          appService;

    @Autowired
    private ESClusterService    esClusterService;

    @Autowired
    private OdinSender          odinSender;

    @Autowired
    private TemplateAccessESDAO templateAccessEsDao;

    @Autowired
    private MonitorMetricsSender monitorMetricsSender;

    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsIndexInfoEsDao;

    @Autowired
    private AriusStatsNodeInfoESDAO ariusStatsNodeInfoEsDao;

    private Set<String> notMonitorCluster = Sets.newHashSet();

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(20,
        40, 3000, TimeUnit.MILLISECONDS,
        new LinkedBlockingDeque<>(100),
        new NamedThreadFactory("Arius-meta-AriusBoardCollector"),
        (r, executor) -> LOGGER.error("AriusBoardCollector.ThreadPoolExecutor RejectedExecution!"));

    private final static long PHY_CLUSTER = 1;

    private final static long LOGIC_CLUSTER = 0;

    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=ClusterMonitorJobHandler||method=handleJobTask||params={}", params);

        // 处理逻辑集群的统计数据
        executor.execute(() -> handleLogicClusterStats());

        // 处理集群的统计数据
        Map<ESClusterPhy, ESClusterHealthResponse> clusterHealthResponseMap = handlePhysicalClusterStats();

        // 处理需要上传给odin的数据
        handleOdinData(clusterHealthResponseMap);

        return JOB_SUCCESS;
    }

    /**************************************** inner class ****************************************/
    @Data
    public class LogicClusterMetric {
        private Long clusterId;
        private Integer status;
        private Long pendingTask;
        private Long unassignedShards;
        private Integer clusterLevel;
        private Long timestamp;
    }

    /**************************************** private methods ****************************************/

    /**
     * 获取集群模板量统计映射
     * @return
     */
    private Map<String, Integer> getClusterTemplateCountMap() {
        Map<String, Integer> templateCountMap = Maps.newHashMap();
        List<IndexTemplatePhyWithLogic> indexTemplateList = templatePhyService.listTemplateWithLogicWithCache();
        if (CollectionUtils.isNotEmpty(indexTemplateList)) {
            for (IndexTemplatePhyWithLogic indexTemplate : indexTemplateList) {
                Integer templateCount = templateCountMap.get(indexTemplate.getCluster());
                templateCount = templateCount == null ? 1 : templateCount + 1;
                templateCountMap.put(indexTemplate.getCluster(), templateCount);
            }
        }
        return templateCountMap;
    }

    /**
     * 获取物理集群名->逻辑集群列表映射
     * @return
     */
    private Map<String, List<ESClusterLogic>> getPhysicalNameLogicClusterListMap() {
        Map<String, List<ESClusterLogic>> physicalLogicMap = Maps.newHashMap();
        for (ESClusterLogicWithRack logic : logicClusterService.listAllLogicClustersWithRackInfo()) {
            Collection<ESClusterLogicRackInfo> items = logic.getItems();
            if (CollectionUtils.isNotEmpty(items)) {
                for (ESClusterLogicRackInfo item : items) {
                    ESClusterLogic esClusterLogic = logicClusterService.getLogicClusterById(item.getLogicClusterId());

                    List<ESClusterLogic> logicList = physicalLogicMap.get(esClusterLogic);

                    if (logicList == null) {
                        logicList = Lists.newArrayList();
                    }
                    if (!logicList.contains(logic)) {
                        logicList.add(logic);
                    }
                    physicalLogicMap.put(esClusterLogic.getName(), logicList);
                }
            }
        };
        return physicalLogicMap;
    }

    /**
     * 获取集群健康状态
     * @param esClusterPhy
     * @return
     */
    private ESClusterHealthResponse getEsClusterHealthResponse(ESClusterPhy esClusterPhy) {
        ESClusterHealthResponse response = null;
        ESClient esClient = esOpClient.getESClient(esClusterPhy.getCluster());
        if (esClient != null) {
            try {
                response = esClient.admin().cluster().prepareHealth().execute().actionGet(30,
                    TimeUnit.SECONDS);
            } catch (Exception e) {
                LOGGER.error("clusterName={}||msg=exception", esClusterPhy, e);
            }
        }
        return response;
    }

    /**
     * 获取 提交到ES数据格式 集群状态
     * @param clusterNum
     * @param dataSource
     * @param templateCountMap
     * @param appIdCount
     * @return
     */
    private ESClusterStats getEsClusterStatus(Integer clusterNum, ESClusterPhy dataSource,
                                              Map<String, Integer> templateCountMap, Integer appIdCount,
                                              ESClusterHealthResponse healthResponse) {
        String cluster = dataSource.getCluster();
        int level = dataSource.getLevel();
        int alivePercent = getClientAlivePercent(dataSource);

        ESClusterTempBean esClusterStatsBean = new ESClusterTempBean();
        esClusterStatsBean.setClusterName(cluster);
        esClusterStatsBean.setAlivePercent(alivePercent);
        esClusterStatsBean.setLevel(level);
        esClusterStatsBean.setClusterNu(clusterNum);
        esClusterStatsBean.setSla(SLA);
        esClusterStatsBean.setAppNu(appIdCount);
        esClusterStatsBean.setTotalTemplateNu(templateCountMap.containsKey(cluster)
            ? templateCountMap.get(cluster) : 0);
        handlePhysicalClusterStats(cluster, esClusterStatsBean);

        ESClusterStats esClusterStats = new ESClusterStats();
        esClusterStats.setStatis(esClusterStatsBean);
        esClusterStats.setCluster(cluster);
        esClusterStats.setPhysicCluster(PHY_CLUSTER);
        esClusterStats.setTimestamp(System.currentTimeMillis());
        esClusterStats.setDataCenter(dataSource.getDataCenter());

        if(null != healthResponse){
            esClusterStatsBean.setStatus(healthResponse.getStatus());
            esClusterStatsBean.setStatusType(ClusterHealthStatus.fromString(healthResponse.getStatus()).value());
        }

        return esClusterStats;
    }

    /**
     * 获取物理机群 提交到Odin格式 数据
     * @param response
     * @param dataSource
     * @param timestamp
     * @return
     */
    private List<OdinData> getPhysicalOdinFormatList(ESClusterHealthResponse response,
                                                           ESClusterPhy dataSource,
                                                           long timestamp) {
        List<OdinData> odinDataList = Lists.newArrayList();
        odinDataList.add(getOdinDataFormat(dataSource.getCluster(), "es.cluster.node.count",
            String.valueOf(getClientAlivePercent(dataSource)), dataSource.getLevel(), timestamp));

        long qps = ariusStatsIndexInfoEsDao.getClusterQps(dataSource.getCluster());
        odinDataList.add(getOdinDataFormat(dataSource.getCluster(), "es.cluster.qps.total",
            String.valueOf(qps), dataSource.getLevel(), timestamp));

        long tps = ariusStatsIndexInfoEsDao.getClusterTps(dataSource.getCluster());
        odinDataList.add(getOdinDataFormat(dataSource.getCluster(), "es.cluster.tps.total",
            String.valueOf(tps), dataSource.getLevel(), timestamp));

        if (response == null || response.isTimedOut()) {
            return odinDataList;
        }

        byte status = ClusterHealthStatus.fromString(response.getStatus()).value();
        odinDataList.add(getOdinDataFormat(dataSource.getCluster(), "es.cluster.health.status",
            String.valueOf(status), dataSource.getLevel(), timestamp));

        long unAssignedShards = response.getUnassignedShards();
        odinDataList.add(getOdinDataFormat(dataSource.getCluster(), "es.cluster.health.unassignedShards",
            String.valueOf(unAssignedShards), dataSource.getLevel(), timestamp));

        long numberPendingTasks = response.getNumberOfPendingTasks();
        odinDataList.add(getOdinDataFormat(dataSource.getCluster(), "es.cluster.health.pendingTask",
            String.valueOf(numberPendingTasks), dataSource.getLevel(), timestamp));

        String numberDataNodes = String.valueOf(response.getNumberOfDataNodes());
        odinDataList.add(getOdinDataFormat(dataSource.getCluster(), "es.cluster.health.number.of.data.nodes",
            numberDataNodes, dataSource.getLevel(), timestamp));

        String numberNodes = String.valueOf(response.getNumberOfNodes());
        odinDataList.add(getOdinDataFormat(dataSource.getCluster(), "es.cluster.health.number.of.nodes",
            numberNodes, dataSource.getLevel(), timestamp));
        return odinDataList;
    }

    /**
     * 更新 逻辑集群指标
     * @param response
     * @param datasource
     * @param timestamp
     * @param physicalNameLogicClusterListMap
     * @param logicClusterMetricMap
     */
    private void updateLogicClusterMetric(ESClusterHealthResponse response,
                                          ESClusterPhy datasource,
                                          long timestamp,
                                          Map<String /*phyClusterName*/, List<ESClusterLogic>> physicalNameLogicClusterListMap,
                                          Map<ESClusterLogic, LogicClusterMetric> logicClusterMetricMap) {
        if (response == null) {
            return;
        }
        byte status = ClusterHealthStatus.fromString(response.getStatus()).value();
        long unAssignedShards = response.getUnassignedShards();
        long numberPendingTasks = response.getNumberOfPendingTasks();

        final List<ESClusterLogic> logicClusters =
            physicalNameLogicClusterListMap.get(datasource.getCluster());

        if (CollectionUtils.isNotEmpty(logicClusters)) {
            for (ESClusterLogic logicCluster : logicClusters) {
                LogicClusterMetric logicClusterMetric = logicClusterMetricMap.get(logicCluster);
                if (logicClusterMetric == null) {
                    logicClusterMetric = new LogicClusterMetric();
                }

                Integer maxStatus = logicClusterMetric.getStatus() == null
                    ? status : Math.max(status, logicClusterMetric.getStatus());
                logicClusterMetric.setStatus(maxStatus);

                long sumUnassignedShards = logicClusterMetric.getUnassignedShards() == null
                    ? unAssignedShards : logicClusterMetric.getUnassignedShards()
                    + unAssignedShards;
                logicClusterMetric.setUnassignedShards(sumUnassignedShards);

                long sumPendingTask = logicClusterMetric.getPendingTask() == null
                    ? numberPendingTasks : logicClusterMetric.getPendingTask() + numberPendingTasks;
                logicClusterMetric.setPendingTask(sumPendingTask);

                logicClusterMetric.setClusterId(logicCluster.getId());
                logicClusterMetric.setClusterLevel(datasource.getLevel());
                logicClusterMetric.setTimestamp(timestamp);
                logicClusterMetricMap.put(logicCluster, logicClusterMetric);
            }
        }
    }

    /**
     * 发送物理机群指标到odin
     * @param odinDataFormats
     */
    private void sendPhysicalClusterMetric2Odin(Collection<List<OdinData>> odinDataFormats) {
        if (CollectionUtils.isNotEmpty(odinDataFormats)) {
            for (List<OdinData> odinDataFormatList : odinDataFormats) {
                odinSender.batchSend(odinDataFormatList);
            }
        }
    }

    /**
     * 发送逻辑集群指标到odin
     * @param logicClusterMetrics
     */
    private void sendLogicClusterMetric2Odin(Collection<LogicClusterMetric> logicClusterMetrics) {
        if (CollectionUtils.isNotEmpty(logicClusterMetrics)) {
            for (LogicClusterMetric logicClusterMetric : logicClusterMetrics) {
                List<OdinData> logicOdinDataFormats = Lists.newArrayList();

                logicOdinDataFormats.add(getOdinDataFormat(
                    String.valueOf(logicClusterMetric.clusterId),
                    "es.logic.cluster.health.status",
                    String.valueOf(logicClusterMetric.getStatus()),
                    logicClusterMetric.getClusterLevel(),
                    logicClusterMetric.getTimestamp()));

                logicOdinDataFormats.add(getOdinDataFormat(
                    String.valueOf(logicClusterMetric.clusterId),
                    "es.logic.cluster.health.pendingTask",
                    String.valueOf(logicClusterMetric.getPendingTask()),
                    logicClusterMetric.getClusterLevel(),
                    logicClusterMetric.getTimestamp()));

                logicOdinDataFormats.add(getOdinDataFormat(
                    String.valueOf(logicClusterMetric.clusterId),
                    "es.logic.cluster.health.unassignedShards",
                    String.valueOf(logicClusterMetric.getUnassignedShards()),
                    logicClusterMetric.getClusterLevel(),
                    logicClusterMetric.getTimestamp()));

                odinSender.batchSend(logicOdinDataFormats);
            }
        }
    }

    /**
     * 发送指标到ES
     * @param datasourceSize
     * @param esClusterStatsList
     */
    private void sendClusterStatus2Es(Integer datasourceSize,
                                      List<ESClusterStats> esClusterStatsList) {
        if (CollectionUtils.isNotEmpty(esClusterStatsList)) {
            esClusterStatsList.add(handleAllClusterStats(datasourceSize, esClusterStatsList));
            monitorMetricsSender.sendClusterStats(esClusterStatsList);
        }
    }

    private void handlePhysicalClusterStats(String clusterName, ESClusterTempBean esClusterStats) {
        List<Future<Void>> futures = new ArrayList<>();

        // 这里会有多次es查询，做成并发的以免http接口超时
        futures.add(callableTask(() -> esClusterStats.setReadTps(ariusStatsIndexInfoEsDao.getClusterQps(clusterName))));
        futures.add(callableTask(() -> esClusterStats.setWriteTps(ariusStatsIndexInfoEsDao.getClusterTps(clusterName))));
        futures.add(callableTask(() -> esClusterStats.setCpuUsage(ariusStatsNodeInfoEsDao.getClusterCpuAvg(clusterName))));
        futures.add(callableTask(() -> esClusterStats.setRecvTransSize(storeUnitConvert2G(ariusStatsNodeInfoEsDao.getClusterRx(clusterName)))));
        futures.add(callableTask(() -> esClusterStats.setSendTransSize(storeUnitConvert2G(ariusStatsNodeInfoEsDao.getClusterTx(clusterName)))));
        futures.add(callableTask(() -> esClusterStats.setQueryTimesPreDay(
                templateAccessEsDao.getYesterDayAllTemplateAccess(clusterName))));

        futures.add(callableTask(() -> setClusterOtherStats(clusterName, esClusterStats)));

        // 都执行完了再返回
        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
            }
        });
    }

    /**
     * 设置集群其他状态值
     *
     * @param clusterName
     * @param esClusterStats
     */
    private void setClusterOtherStats(String clusterName, ESClusterTempBean esClusterStats) {
        ESClusterStatsResponse clusterStats = null;
        try {

            clusterStats = getClusterStats(clusterName);
            if (Objects.isNull(clusterStats)) {
                return;
            }

            esClusterStats.setTotalIndicesNu(clusterStats.getIndexCount());
            esClusterStats.setShardNu(clusterStats.getTotalShard());
            esClusterStats.setTotalDocNu(clusterStats.getDocsCount());
            esClusterStats.setEsNodeNu(clusterStats.getTotalNodes());
            esClusterStats.setTotalStoreSize(clusterStats.getTotalFs().getGbFrac());
            esClusterStats.setFreeStoreSize(clusterStats.getFreeFs().getGbFrac());

            esClusterStats.setStoreSize(esClusterStats.getTotalStoreSize() - esClusterStats.getFreeStoreSize());
            esClusterStats.setIndexStoreSize(esClusterStats.getTotalStoreSize() - esClusterStats.getFreeStoreSize());

            if (esClusterStats.getTotalStoreSize() > 0) {
                esClusterStats.setDiskUsage(esClusterStats.getStoreSize() / esClusterStats.getTotalStoreSize());
            } else {
                esClusterStats.setDiskUsage(0D);
            }

            ESClusterHealthResponse clusterHealthResponse = getClusterHealth(clusterName);
            if (Objects.isNull(clusterHealthResponse)) {
                return;
            }
            esClusterStats.setUnAssignedShards(clusterHealthResponse.getUnassignedShards());
            esClusterStats.setNumberPendingTasks(clusterHealthResponse.getNumberOfPendingTasks());
            esClusterStats.setNumberDataNodes(clusterHealthResponse.getNumberOfDataNodes());
            esClusterStats.setNumberNodes(clusterHealthResponse.getNumberOfNodes());

        } catch (Exception e) {
            LOGGER.error("class=ClusterMonitorJobHandler||method=setClusterOtherStats||clusterName={}, clusterStats={}",
                    clusterName, clusterStats, e);
        }
    }

    private ESClusterStats handleAllClusterStats(int clusterNu, List<ESClusterStats> esClusterStatss){
        ESClusterTempBean allClusterTempBean = new ESClusterTempBean();

        allClusterTempBean.setStoreSize(esClusterStatss.stream().mapToDouble(item -> item.getStatis().getStoreSize()).sum());
        allClusterTempBean.setTotalStoreSize(esClusterStatss.stream().mapToDouble(item -> item.getStatis().getTotalStoreSize()).sum());
        allClusterTempBean.setFreeStoreSize(esClusterStatss.stream().mapToDouble(item -> item.getStatis().getFreeStoreSize()).sum());
        allClusterTempBean.setIndexStoreSize(esClusterStatss.stream().mapToDouble(item -> item.getStatis().getIndexStoreSize()).sum());
        allClusterTempBean.setTotalIndicesNu(esClusterStatss.stream().mapToDouble(item -> item.getStatis().getTotalIndicesNu()).sum());
        allClusterTempBean.setTotalTemplateNu(esClusterStatss.stream().mapToInt(item -> item.getStatis().getTotalTemplateNu()).sum());
        allClusterTempBean.setTotalDocNu(esClusterStatss.stream().mapToLong(item -> item.getStatis().getTotalDocNu()).sum());
        allClusterTempBean.setShardNu(esClusterStatss.stream().mapToLong(item -> item.getStatis().getShardNu()).sum());
        allClusterTempBean.setRecvTransSize(esClusterStatss.stream().mapToDouble(item -> item.getStatis().getRecvTransSize()).sum());
        allClusterTempBean.setSendTransSize(esClusterStatss.stream().mapToDouble(item -> item.getStatis().getSendTransSize()).sum());
        allClusterTempBean.setWriteTps(esClusterStatss.stream().mapToDouble(item -> item.getStatis().getWriteTps()).sum());
        allClusterTempBean.setReadTps(esClusterStatss.stream().mapToDouble(item -> item.getStatis().getReadTps()).sum());
        allClusterTempBean.setEsNodeNu(esClusterStatss.stream().mapToDouble(item -> item.getStatis().getEsNodeNu()).sum());
        allClusterTempBean.setQueryTimesPreDay(esClusterStatss.stream().mapToLong(item -> item.getStatis().getQueryTimesPreDay()).sum());
        allClusterTempBean.setUnAssignedShards(esClusterStatss.stream().mapToLong(item -> item.getStatis().getUnAssignedShards()).sum());
        allClusterTempBean.setNumberPendingTasks(esClusterStatss.stream().mapToLong(item -> item.getStatis().getNumberPendingTasks()).sum());
        allClusterTempBean.setNumberDataNodes(esClusterStatss.stream().mapToLong(item -> item.getStatis().getNumberDataNodes()).sum());
        allClusterTempBean.setNumberNodes(esClusterStatss.stream().mapToLong(item -> item.getStatis().getNumberNodes()).sum());

        double allCupUsage = esClusterStatss.stream().mapToDouble(item -> item.getStatis().getCpuUsage()).sum();
        int allAlivePercent = esClusterStatss.stream().mapToInt(item -> item.getStatis().getAlivePercent()).sum();

        allClusterTempBean.setAlivePercent((int)(allAlivePercent * 1.0 / esClusterStatss.size()));
        allClusterTempBean.setCpuUsage(allCupUsage / esClusterStatss.size());
        if (allClusterTempBean.getTotalStoreSize() > 0) {
            allClusterTempBean.setDiskUsage(allClusterTempBean.getStoreSize() / allClusterTempBean.getTotalStoreSize());
        }
        allClusterTempBean.setClusterNu(clusterNu);
        allClusterTempBean.setAppNu(calcAppNu());
        allClusterTempBean.setClusterName("allCluster");
        allClusterTempBean.setSla(SLA);

        ESClusterStats esClusterStats = new ESClusterStats();
        esClusterStats.setStatis(allClusterTempBean);
        esClusterStats.setCluster("allCluster");
        esClusterStats.setTimestamp(System.currentTimeMillis());

        return esClusterStats;
    }

    //获取应用数量
    private int calcAppNu(){
        List<App> queryApps = appService.getApps();
        return CollectionUtils.isEmpty(queryApps) ? 0 : queryApps.size();
    }

    private Future<Void> callableTask(Runnable r){
        return executor.submit(() -> {
            r.run();
            return null;
        });
    }

    /**
     * 转换单位为G
     *
     * @param storeSize
     * @return
     */
    private Double storeUnitConvert2G(Double storeSize){
        if(null == storeSize){return 0d;}

        return storeSize/(1024 * 1024 * 1024);
    }

    /**
     * 构建odin数据
     *
     * @param cluster
     * @param odinName
     * @param value
     * @param clusterLevel
     */
    private OdinData getOdinDataFormat(String cluster, String odinName, String value,
                                             int clusterLevel, long timestamp) {
        OdinData dataFormat = new OdinData();
        dataFormat.setName(odinName);
        dataFormat.setValue(value);
        dataFormat.setStep(ODIN_STEP);
        dataFormat.setTimestamp(timestamp);
        dataFormat.putTag("host", HttpHostUtil.HOST_NAME);
        dataFormat.putTag("cluster", cluster);
        dataFormat.putTag("level", String.valueOf(clusterLevel));

        return dataFormat;
    }

    /**
     * 获取 某个es 集群client存活率
     * @param dataSource
     * @return
     */
    private int getClientAlivePercent(ESClusterPhy dataSource) {
        Set<String> addresses = Sets.newHashSet();
        for(String str : dataSource.getHttpAddress().split(",")){
            if (StringUtils.isNotEmpty(str)) {addresses.add(str);}
        }

        // 地址为空，存活率为100%
        if (addresses.isEmpty()) {
            return 100;
        }

        int alive = 0;
        for (String address : addresses) {
            boolean isAlive = esClusterService.judgeClientAlive(dataSource.getCluster(), address);
            if(isAlive){alive++;}
        }

        return alive * 100 / addresses.size();
    }

    private Map<ESClusterPhy, ESClusterHealthResponse> handlePhysicalClusterStats(){
        List<ESClusterPhy> phyClusters = esClusterPhyService.listAllClusters();

        final Map<String, Integer> templateCountMap = getClusterTemplateCountMap();
        int   appIdCount = calcAppNu();

        List<ESClusterStats> esClusterStatsList = new ArrayList<>();

        Map<ESClusterPhy, ESClusterHealthResponse> clusterHealthResponseMap = Maps.newHashMap();

        phyClusters.parallelStream().filter(item -> !notMonitorCluster.contains(item.getCluster()))
                .forEach(dataSource -> {
                    if (EnvUtil.getDC().getCode().equals(dataSource.getDataCenter())) {
                        ESClusterHealthResponse response = getEsClusterHealthResponse(dataSource);

                        ESClusterStats esClusterStatus = getEsClusterStatus(phyClusters.size(), dataSource,
                                templateCountMap, appIdCount, response);
                        esClusterStatsList.add(esClusterStatus);

                        clusterHealthResponseMap.put(dataSource, response);
                    }
                });

        // send cluster status to es
        sendClusterStatus2Es(phyClusters.size(), esClusterStatsList);

        return clusterHealthResponseMap;
    }

    private void handleLogicClusterStats(){
        List<ESClusterStats> esLogicClusterStatsList = new ArrayList<>();

        List<ESClusterLogicWithRack> logicClusterVOS = logicClusterService.listAllLogicClustersWithRackInfo();
        logicClusterVOS.parallelStream().forEach(l -> {
//            ClusterLogicStatisPO clusterStatisPO = clusterLogicService.getLogicClusterStatis(l.getId());

            ESClusterTempBean esClusterStatsBean = new ESClusterTempBean();
//            esClusterStatsBean.setStatus(clusterStatisPO.getStatus());
//            esClusterStatsBean.setStatusType(clusterStatisPO.getStatusType());
            esClusterStatsBean.setClusterName(l.getName());
            esClusterStatsBean.setLevel(l.getLevel());
            esClusterStatsBean.setClusterNu(1);
//            esClusterStatsBean.setTotalDocNu((long)clusterStatisPO.getDocNu());
//            esClusterStatsBean.setIndexStoreSize(clusterStatisPO.getUsedDisk());
//            esClusterStatsBean.setStoreSize(clusterStatisPO.getUsedDisk());
//            esClusterStatsBean.setFreeStoreSize(clusterStatisPO.getFreeDisk());
//            esClusterStatsBean.setTotalStoreSize(clusterStatisPO.getTotalDisk());
//            esClusterStatsBean.setNumberDataNodes(clusterStatisPO.getNumberDataNodes());
//            esClusterStatsBean.setNumberPendingTasks(clusterStatisPO.getNumberPendingTasks());
//            esClusterStatsBean.setUnAssignedShards(clusterStatisPO.getUnAssignedShards());

            if(0 != esClusterStatsBean.getTotalStoreSize()){
                esClusterStatsBean.setDiskUsage(esClusterStatsBean.getStoreSize()/esClusterStatsBean.getTotalStoreSize());
            }

            ESClusterStats esClusterStats = new ESClusterStats();
            esClusterStats.setStatis(esClusterStatsBean);
            esClusterStats.setCluster(l.getName());
            esClusterStats.setPhysicCluster(LOGIC_CLUSTER);
            esClusterStats.setTimestamp(System.currentTimeMillis());
            esClusterStats.setDataCenter(l.getDataCenter());

            esLogicClusterStatsList.add(esClusterStats);
        });

        // send cluster status to es
        sendClusterStatus2Es(logicClusterVOS.size(), esLogicClusterStatsList);
    }

    private void handleOdinData(Map<ESClusterPhy, ESClusterHealthResponse> clusterHealthResponseMap){
        // 只有线上和预发环境(包括美东和国内)需要将指标上报到odin，线下和自测环境除外
        if (EnvUtil.isOnline() || EnvUtil.isPre()) {
            long  timestamp  = DateTimeUtil.getCurrentTimestampMinute();

            final Map<String, List<ESClusterLogic>> physicalNameLogicClusterListMap =
                    getPhysicalNameLogicClusterListMap();

            Map<ESClusterLogic, LogicClusterMetric> logicClusterMetricMap = Maps.newHashMap();
            Map<ESClusterPhy, List<OdinData>> physicalClusterMetricMap = Maps.newHashMap();

            for(ESClusterPhy dataSource : clusterHealthResponseMap.keySet()){
                ESClusterHealthResponse response = clusterHealthResponseMap.get(dataSource);

                // add physical cluster metric
                physicalClusterMetricMap.put(dataSource,
                        getPhysicalOdinFormatList(response, dataSource, timestamp));

                // update logic cluster metric
                updateLogicClusterMetric(response, dataSource, timestamp,
                        physicalNameLogicClusterListMap, logicClusterMetricMap);
            }

            // send logic metric to odin
            sendPhysicalClusterMetric2Odin(physicalClusterMetricMap.values());

            // send physical metric to odin
            sendLogicClusterMetric2Odin(logicClusterMetricMap.values());
        }
    }

    /**
     * 获取集群stats
     *
     * @param clusterName
     * @return
     */
    public ESClusterStatsResponse getClusterStats(String clusterName) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (Objects.isNull(esClient)) {
            LOGGER.error("class=ClusterClientPool||method=getClusterStats||clusterName={}||errMsg=esClient is null", clusterName);
            return null;
        }

        ESClusterStatsResponse responses = null;

        try {
            DirectRequest directRequest = new DirectRequest("GET", "_cluster/stats");
            DirectResponse directResponse = esClient.direct(directRequest).actionGet(30, TimeUnit.SECONDS);

            if (directResponse.getRestStatus() == RestStatus.OK &&
                    StringUtils.isNoneBlank(directResponse.getResponseContent())) {

                responses = new ESClusterStatsResponse();
                JSONObject jsonObject = JSON.parseObject(directResponse.getResponseContent());
                JSONObject indicesObj = jsonObject.getJSONObject("indices");
                responses.setIndexCount(indicesObj.getLongValue("count"));

                JSONObject shardsObj = indicesObj.getJSONObject("shards");
                responses.setTotalShard(shardsObj.getLongValue("total"));

                JSONObject docsObj = indicesObj.getJSONObject("docs");
                responses.setDocsCount(docsObj.getLongValue("count"));

                JSONObject nodesObj = jsonObject.getJSONObject("nodes");
                JSONObject nodesCountObj = nodesObj.getJSONObject("count");
                responses.setTotalNodes(nodesCountObj.getLongValue("total"));

                JSONObject fsObj = nodesObj.getJSONObject("fs");

                responses.setTotalFs(new ByteSizeValue(fsObj.getLongValue("total_in_bytes")));
                responses.setFreeFs(new ByteSizeValue(fsObj.getLongValue("free_in_bytes")));
            }

        } catch (Exception e) {
            LOGGER.error("class=ClusterClientPool||method=getClusterStats||clusterName={}||errMsg=fail to get",
                    clusterName, e);
        }

        return responses;
    }

    /**
     * 获取集群状态信息
     *
     * @param clusterName
     * @return
     */
    public ESClusterHealthResponse getClusterHealth(String clusterName) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (esClient == null) {
            LOGGER.error("class=ClusterClientPool||method=getClusterHealth||clusterName={}||errMsg=esClient is null",
                    clusterName);
            return null;
        }

        try {
            ESClusterHealthRequest request = new ESClusterHealthRequest();
            return esClient.admin().cluster().health(request).actionGet(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class=ClusterClientPool||method=getClusterHealth||clusterName={}||errMsg=query error. ",
                    clusterName, e);
            return null;
        }
    }
}
