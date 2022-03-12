package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.ALL_CLUSTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.PHY_CLUSTER;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.N9eData;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicWithRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsCells;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.constant.PercentilesEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpHostUtil;
import com.didichuxing.datachannel.arius.admin.core.component.MonitorDataSender;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.MonitorMetricsSender;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateAccessESDAO;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 集群维度采集监控数据，包含 es节点存活检查；es集群tps/qps掉底报警
 */
@Component
public class ClusterMonitorJobHandler extends AbstractMetaDataJob {

    private static final double      SLA               = 0.9999;

    @Autowired
    private ClusterLogicService      logicClusterService;

    @Autowired
    private ClusterPhyService        clusterPhyService;

    @Autowired
    private TemplatePhyService       templatePhyService;

    @Autowired
    private AppService               appService;

    @Autowired
    private ESClusterService         esClusterService;

    @Autowired
    private MonitorDataSender        monitorDataSender;

    @Autowired
    private TemplateAccessESDAO      templateAccessEsDao;

    @Autowired
    private MonitorMetricsSender     monitorMetricsSender;

    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsIndexInfoEsDao;

    @Autowired
    private AriusStatsNodeInfoESDAO  ariusStatsNodeInfoEsDao;

    private Set<String>              notMonitorCluster = Sets.newHashSet();
    
    private static final FutureUtil<Void>  futureUtil = FutureUtil.initBySystemAvailableProcessors("ClusterMonitorJobHandler",100);
    
    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=ClusterMonitorJobHandler||method=handleJobTask||params={}", params);

        // 处理逻辑集群的统计数据
        List<ESClusterStats> esClusterStatsList = Lists.newCopyOnWriteArrayList();

        // 处理集群的统计数据
        Map<ClusterPhy, ESClusterHealthResponse> clusterHealthResponseMap = handlePhysicalClusterStats(esClusterStatsList);

        //处理保留集群状态至DB, 后端分页条件中需要使用状态字段
        handleSaveClusterHealthToDB(clusterHealthResponseMap);

        // 处理需要上传给夜莺的数据
        Map<String, ESClusterStatsCells> statsCellsMap = esClusterStatsList.stream().map(ESClusterStats::getStatis)
                .collect(Collectors.toMap(ESClusterStatsCells::getClusterName, u -> u, (k1, k2) -> k1));
        handleN9eData(clusterHealthResponseMap, statsCellsMap);
        return JOB_SUCCESS;
    }

    /**************************************** inner class ****************************************/
    @Data
    @NoArgsConstructor
    public class LogicClusterMetric {
        private Long    clusterId;
        private Integer status;
        private Long    pendingTask;
        private Long    unassignedShards;
        private Integer clusterLevel;
        private Long    timestamp;
    }

    /**************************************** private methods ****************************************/
    private Map<ClusterPhy, ESClusterHealthResponse> handlePhysicalClusterStats(List<ESClusterStats> esClusterStatsList) {
        List<ClusterPhy> phyClusters = clusterPhyService.listAllClusters();
        if (CollectionUtils.isEmpty(phyClusters)) {
            LOGGER.warn("class=ClusterMonitorJobHandler||method=handlePhysicalClusterStats||msg=phyClusters is empty");
            return null;
        }

        final Map<String, Integer> clusterPhyName2TemplateCountMap = templatePhyService.getClusterTemplateCountMap();

        int appIdCount = calcAppNu();

        Map<ClusterPhy, ESClusterHealthResponse> clusterHealthResponseMap = Maps.newConcurrentMap();

        // 1. build multiple clusters status
        phyClusters.stream().filter(item -> !notMonitorCluster.contains(item.getCluster())).forEach(dataSource -> {

            try {
                if (EnvUtil.getDC().getCode().equals(dataSource.getDataCenter())) {
                    ESClusterHealthResponse clusterHealthResponse = esClusterService.syncGetClusterHealth(dataSource.getCluster());

                    List<ESClusterStats> esClusterStatusList = buildEsClusterStatusWithPercentiles(phyClusters.size(), dataSource,
                        clusterPhyName2TemplateCountMap, appIdCount, clusterHealthResponse);
                    esClusterStatsList.addAll(esClusterStatusList);

                    if (clusterHealthResponse == null) {
                        clusterHealthResponse = new ESClusterHealthResponse();
                        clusterHealthResponse.setClusterName(dataSource.getCluster());
                        clusterHealthResponse.setStatus(ClusterHealthEnum.UNKNOWN.getDesc());
                    }
                    clusterHealthResponseMap.put(dataSource, clusterHealthResponse);
                } else {
                    LOGGER.error(
                            "class=ClusterMonitorJobHandler||method=handlePhysicalClusterStats||clusterPhyName={}||clusterPhyDataCenter={}"
                                    + "||errMsg= dataSource mismatch",
                            dataSource.getCluster(), dataSource.getDataCenter());
                }
            } catch (Exception e) {
                LOGGER.error(
                        "class=ClusterMonitorJobHandler||method=handlePhysicalClusterStats||clusterPhyName={}||clusterPhyDataCenter={}"
                                + "||errMsg= dataSource mismatch",
                        dataSource.getCluster(), dataSource.getDataCenter(), e);
            }

        });

        // 2. build total cluster status

        // 3. send cluster status to es
        monitorMetricsSender.sendClusterStats(esClusterStatsList);

        return clusterHealthResponseMap;
    }

    /**
     * 获取 提交到ES数据格式 集群状态
     * @param clusterNum
     * @param dataSource
     * @param clusterPhyName2TemplateCountMap
     * @param appIdCount
     * @return
     */
    private List<ESClusterStats> buildEsClusterStatusWithPercentiles(Integer clusterNum, ClusterPhy dataSource,
                                                                     Map<String, Integer> clusterPhyName2TemplateCountMap,
                                                                     Integer appIdCount, ESClusterHealthResponse healthResponse) {
        
        List<ESClusterStats> esClusterStatsList = Lists.newArrayList();

        //获取不同分位值的指标
        Map<String, ESClusterStatsCells> percentilesType2ESClusterStatsCellsMap = getPhysicalClusterStatsPercentiles(
            dataSource, healthResponse, clusterPhyName2TemplateCountMap, appIdCount, clusterNum);

        percentilesType2ESClusterStatsCellsMap.forEach((percentilesType, esClusterStatsCells) -> {
            ESClusterStats esClusterStats = new ESClusterStats();
            esClusterStats.setStatis(esClusterStatsCells);
            esClusterStats.setCluster(dataSource.getCluster());
            esClusterStats.setPercentilesType(percentilesType);
            esClusterStats.setPhysicCluster(PHY_CLUSTER);
            esClusterStats.setTimestamp(System.currentTimeMillis());
            esClusterStats.setDataCenter(dataSource.getDataCenter());

            esClusterStatsList.add(esClusterStats);
        });

        return esClusterStatsList;
    }

    /**
     * 采集不同分位图的指标数据
     * @param dataSource
     * @param healthResponse
     * @param clusterPhyName2TemplateCountMap
     * @param appIdCount
     * @param clusterNum
     * @return
     */
    private Map<String, ESClusterStatsCells> getPhysicalClusterStatsPercentiles(ClusterPhy dataSource, ESClusterHealthResponse healthResponse,
                                                                                Map<String, Integer> clusterPhyName2TemplateCountMap,
                                                                                Integer appIdCount, Integer clusterNum) {
        Map<String, ESClusterStatsCells> percentilesType2ESClusterStatsCellsMap = Maps.newHashMap();
        String clusterName = dataSource.getCluster();
        if (AriusObjUtils.isNull(clusterName)) {
            LOGGER.warn("class=ClusterMonitorJobHandler||method=getPhysicalClusterStatsPercentiles||errMsg=clusterName is empty");
            return percentilesType2ESClusterStatsCellsMap;
        }

        ESClusterStatsCells esClusterStatsCells = buildForBasicInfo(dataSource, healthResponse,
                clusterPhyName2TemplateCountMap, appIdCount, clusterNum);

        AtomicReference<Map<String, Double>> clusterCpuAvgAndPercentilesAtomic                  = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterDiskFreeUsagePercentAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterSearchLatencyAvgAndPercentilesAtomic        = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterIndexingLatencyAvgAndPercentilesAtomic      = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterCpuLoad1MinAvgAndPercentilesAtomic          = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterCpuLoad5MinAvgAndPercentilesAtomic          = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterCpuLoad15MinAvgAndPercentilesAtomic         = new AtomicReference<>();

        futureUtil.runnableTask(() -> clusterCpuAvgAndPercentilesAtomic.set(ariusStatsNodeInfoEsDao.getClusterCpuAvgAndPercentiles(clusterName)))
                  .runnableTask(() -> clusterDiskFreeUsagePercentAvgAndPercentilesAtomic.set(ariusStatsNodeInfoEsDao.getClusterDiskFreeUsagePercentAvgAndPercentiles(clusterName)))
                  .runnableTask(() -> clusterSearchLatencyAvgAndPercentilesAtomic.set(ariusStatsNodeInfoEsDao.getClusterSearchLatencyAvgAndPercentiles(clusterName)))
                  .runnableTask(() -> clusterIndexingLatencyAvgAndPercentilesAtomic.set(ariusStatsNodeInfoEsDao.getClusterIndexingLatencyAvgAndPercentiles(clusterName)))
                  .runnableTask(() -> clusterCpuLoad1MinAvgAndPercentilesAtomic.set(ariusStatsNodeInfoEsDao.getClusterCpuLoad1MinAvgAndPercentiles(clusterName)))
                  .runnableTask(() -> clusterCpuLoad5MinAvgAndPercentilesAtomic.set(ariusStatsNodeInfoEsDao.getClusterCpuLoad5MinAvgAndPercentiles(clusterName)))
                  .runnableTask(() -> clusterCpuLoad15MinAvgAndPercentilesAtomic.set(ariusStatsNodeInfoEsDao.getClusterCpuLoad15MinAvgAndPercentiles(clusterName)))
                  .waitExecute();

        for (String type : PercentilesEnum.listUsefulType()) {
            ESClusterStatsCells esClusterStatsCellDeepCopy = ConvertUtil.obj2Obj(esClusterStatsCells, ESClusterStatsCells.class);

            buildForPercentiles(esClusterStatsCellDeepCopy, type,
                    clusterCpuAvgAndPercentilesAtomic.get(),
                    clusterCpuLoad1MinAvgAndPercentilesAtomic.get(),
                    clusterCpuLoad5MinAvgAndPercentilesAtomic.get(),
                    clusterCpuLoad15MinAvgAndPercentilesAtomic.get(),
                    clusterDiskFreeUsagePercentAvgAndPercentilesAtomic.get(),
                    clusterSearchLatencyAvgAndPercentilesAtomic.get(),
                    clusterIndexingLatencyAvgAndPercentilesAtomic.get());

            percentilesType2ESClusterStatsCellsMap.put(type, esClusterStatsCellDeepCopy);

        }

        return percentilesType2ESClusterStatsCellsMap;
    }

    /**
     * @param esClusterStatsCellDeepCopy                       集群维度统计信息
     * @param type                                             avg、分位类型(99、95、75、55)
     * @param clusterCpuAvgAndPercentiles                      集群cpu平均值和分位值(key:99, value:值)
     * @param clusterDiskFreeUsagePercentAvgAndPercentiles     集群节点磁盘空闲率平均值和分位值(key:99, value:值)
     * @param clusterSearchLatencyAvgAndPercentiles            集群节点查询耗时平均值和分位值(key:99, value:值)
     * @param clusterIndexingLatencyAvgAndPercentiles          集群节点写入耗时平均值和分位值(key:99, value:值)
     * @param clusterCpuLoad1MinAvgAndPercentiles              集群cpu load1平均值和分位值(key:99, value:值)
     * @param clusterCpuLoad5MinAvgAndPercentiles              集群cpu load5平均值和分位值(key:99, value:值)
     * @param clusterCpuLoad15MinAvgAndPercentiles             集群cpu load15平均值和分位值(key:99, value:值)
     */
    private void buildForPercentiles(ESClusterStatsCells esClusterStatsCellDeepCopy, String type,
                                     Map<String, Double> clusterCpuAvgAndPercentiles,
                                     Map<String, Double> clusterCpuLoad1MinAvgAndPercentiles,
                                     Map<String, Double> clusterCpuLoad5MinAvgAndPercentiles,
                                     Map<String, Double> clusterCpuLoad15MinAvgAndPercentiles,
                                     Map<String, Double> clusterDiskFreeUsagePercentAvgAndPercentiles,
                                     Map<String, Double> clusterSearchLatencyAvgAndPercentiles,
                                     Map<String, Double> clusterIndexingLatencyAvgAndPercentiles) {
        if (null != clusterCpuAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuUsage(clusterCpuAvgAndPercentiles.get(type));
        }

        if (null != clusterCpuLoad1MinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuLoad1M(clusterCpuLoad1MinAvgAndPercentiles.get(type));
        }

        if (null != clusterCpuLoad5MinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuLoad5M(clusterCpuLoad5MinAvgAndPercentiles.get(type));
        }

        if (null != clusterCpuLoad15MinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuLoad15M(clusterCpuLoad15MinAvgAndPercentiles.get(type));
        }

        String realType = convertSpecialTypeForDiskFreeUsage(type);
        if (null != clusterDiskFreeUsagePercentAvgAndPercentiles.get(realType)) {
            esClusterStatsCellDeepCopy.setDiskUsage(1 - clusterDiskFreeUsagePercentAvgAndPercentiles.get(realType));
        }

        if (null != clusterSearchLatencyAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setSearchLatency(clusterSearchLatencyAvgAndPercentiles.get(type));
        }

        if (null != clusterIndexingLatencyAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setIndexingLatency(clusterIndexingLatencyAvgAndPercentiles.get(type));
        }
    }

    private String convertSpecialTypeForDiskFreeUsage(String type) {
        if (PercentilesEnum.ST99.getType().equals(type)) {
            return PercentilesEnum.ST1.getType();
        }

        if (PercentilesEnum.ST95.getType().equals(type)) {
            return PercentilesEnum.ST5.getType();
        }

        if (PercentilesEnum.ST75.getType().equals(type)) {
            return PercentilesEnum.ST25.getType();
        }

        if (PercentilesEnum.ST55.getType().equals(type)) {
            return PercentilesEnum.ST45.getType();
        }

        return PercentilesEnum.AVG.getType();
    }

    /**
     * 构建集群相关统计信息
     * @param dataSource
     * @param healthResponse
     * @param clusterPhyName2TemplateCountMap
     * @param appIdCount
     * @param clusterNum
     * @return
     */
    private ESClusterStatsCells buildForBasicInfo(ClusterPhy dataSource, ESClusterHealthResponse healthResponse,
                                                  Map<String, Integer> clusterPhyName2TemplateCountMap,
                                                  Integer appIdCount, Integer clusterNum) {
        ESClusterStatsCells esClusterStatsBean = new ESClusterStatsCells();
        if (null != healthResponse) {
            esClusterStatsBean.setStatus(healthResponse.getStatus());
            esClusterStatsBean.setStatusType(ClusterHealthEnum.valuesOf(healthResponse.getStatus()).getCode());
        }else {
            esClusterStatsBean.setStatus(ClusterHealthEnum.UNKNOWN.getDesc());
            esClusterStatsBean.setStatusType(ClusterHealthEnum.UNKNOWN.getCode());
        }

        if (null != dataSource) {
            esClusterStatsBean.setClusterName(dataSource.getCluster());
            esClusterStatsBean.setLevel(dataSource.getLevel());
            esClusterStatsBean.setClusterNu(clusterNum);
            esClusterStatsBean.setAppNu(appIdCount);
            esClusterStatsBean.setSla(SLA);

            int totalTemplateNu = clusterPhyName2TemplateCountMap.getOrDefault(dataSource.getCluster(), 0);
            esClusterStatsBean.setTotalTemplateNu(totalTemplateNu);

            handlePhysicalClusterStatsForSum(dataSource.getCluster(), esClusterStatsBean);
        }
        return esClusterStatsBean;
    }

    private ESClusterStats handleAllClusterStats(int clusterNu, List<ESClusterStats> esClusterStates) {
        ESClusterStatsCells allClusterTempBean = new ESClusterStatsCells();

        allClusterTempBean.setStoreSize(esClusterStates.stream().mapToDouble(item -> item.getStatis().getStoreSize()).sum());
        allClusterTempBean.setTotalStoreSize(esClusterStates.stream().mapToDouble(item -> item.getStatis().getTotalStoreSize()).sum());
        allClusterTempBean.setFreeStoreSize(esClusterStates.stream().mapToDouble(item -> item.getStatis().getFreeStoreSize()).sum());
        allClusterTempBean.setIndexStoreSize(esClusterStates.stream().mapToDouble(item -> item.getStatis().getIndexStoreSize()).sum());
        allClusterTempBean.setTotalIndicesNu(esClusterStates.stream().mapToDouble(item -> item.getStatis().getTotalIndicesNu()).sum());
        allClusterTempBean.setTotalTemplateNu(esClusterStates.stream().mapToInt(item -> item.getStatis().getTotalTemplateNu()).sum());
        allClusterTempBean.setTotalDocNu(esClusterStates.stream().mapToLong(item -> item.getStatis().getTotalDocNu()).sum());

        allClusterTempBean.setShardNu(esClusterStates.stream().mapToLong(item -> item.getStatis().getShardNu()).sum());
        allClusterTempBean.setUnAssignedShards(esClusterStates.stream().mapToLong(item -> item.getStatis().getUnAssignedShards()).sum());

        allClusterTempBean.setRecvTransSize(esClusterStates.stream().mapToDouble(item -> item.getStatis().getRecvTransSize()).sum());
        allClusterTempBean.setSendTransSize(esClusterStates.stream().mapToDouble(item -> item.getStatis().getSendTransSize()).sum());
        allClusterTempBean.setWriteTps(esClusterStates.stream().mapToDouble(item -> item.getStatis().getWriteTps()).sum());
        allClusterTempBean.setReadTps(esClusterStates.stream().mapToDouble(item -> item.getStatis().getReadTps()).sum());

        allClusterTempBean.setEsNodeNu(esClusterStates.stream().mapToDouble(item -> item.getStatis().getEsNodeNu()).sum());

        allClusterTempBean.setNumberPendingTasks(esClusterStates.stream().mapToLong(item -> item.getStatis().getNumberPendingTasks()).sum());
        allClusterTempBean.setNumberDataNodes(esClusterStates.stream().mapToLong(item -> item.getStatis().getNumberDataNodes()).sum());

        allClusterTempBean.setNumberNodes(esClusterStates.stream().mapToLong(item -> item.getStatis().getNumberNodes()).sum());
        allClusterTempBean.setNumberMasterNodes(esClusterStates.stream().mapToLong(item -> item.getStatis().getNumberMasterNodes()).sum());
        allClusterTempBean.setNumberClientNodes(esClusterStates.stream().mapToLong(item -> item.getStatis().getNumberClientNodes()).sum());
        allClusterTempBean.setNumberDataNodes(esClusterStates.stream().mapToLong(item -> item.getStatis().getNumberDataNodes()).sum());

        allClusterTempBean.setMemUsed(esClusterStates.stream().mapToLong(item -> item.getStatis().getMemUsed()).sum());
        allClusterTempBean.setMemFree(esClusterStates.stream().mapToLong(item -> item.getStatis().getMemFree()).sum());
        allClusterTempBean.setMemTotal(esClusterStates.stream().mapToLong(item -> item.getStatis().getMemTotal()).sum());
        double allMemUsed = esClusterStates.stream().mapToDouble(item -> item.getStatis().getMemUsedPercent()).sum();
        allClusterTempBean.setMemUsedPercent(allMemUsed / esClusterStates.size());

        double allMemFree = esClusterStates.stream().mapToDouble(item -> item.getStatis().getMemFreePercent()).sum();
        allClusterTempBean.setMemFreePercent(allMemFree / esClusterStates.size());

        //集群查询、写入耗时
        allClusterTempBean.setSearchLatency(esClusterStates.stream().mapToDouble(item -> item.getStatis().getSearchLatency()).sum());
        allClusterTempBean.setIndexingLatency(esClusterStates.stream().mapToDouble(item -> item.getStatis().getIndexingLatency()).sum());

        double allCupUsage = esClusterStates.stream().mapToDouble(item -> item.getStatis().getCpuUsage()).sum();
        int allAlivePercent = esClusterStates.stream().mapToInt(item -> item.getStatis().getAlivePercent()).sum();

        allClusterTempBean.setAlivePercent((int) (allAlivePercent * 1.0 / esClusterStates.size()));
        allClusterTempBean.setCpuUsage(allCupUsage / esClusterStates.size());
        if (allClusterTempBean.getTotalStoreSize() > 0) {
            allClusterTempBean.setDiskUsage(allClusterTempBean.getStoreSize() / allClusterTempBean.getTotalStoreSize());
        }
        allClusterTempBean.setClusterNu(clusterNu);
        allClusterTempBean.setAppNu(calcAppNu());
        allClusterTempBean.setClusterName(ALL_CLUSTER);
        allClusterTempBean.setSla(SLA);

        ESClusterStats esClusterStats = new ESClusterStats();
        esClusterStats.setStatis(allClusterTempBean);
        esClusterStats.setCluster(ALL_CLUSTER);
        esClusterStats.setTimestamp(System.currentTimeMillis());

        return esClusterStats;
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
                    ClusterLogic clusterLogic = logicClusterService.getClusterLogicById(item.getLogicClusterId());

                    List<ClusterLogic> logicList = physicalLogicMap.get(clusterLogic.getName());

                    if (logicList == null) {
                        logicList = Lists.newArrayList();
                    }
                    if (!logicList.contains(logic)) {
                        logicList.add(logic);
                    }
                    physicalLogicMap.put(clusterLogic.getName(), logicList);
                }
            }
        }
        return physicalLogicMap;
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
                String.valueOf(esClusterService.syncGetClientAlivePercent(dataSource.getCluster(),dataSource.getHttpAddress()))
                , dataSource.getLevel(), timestamp));

        long qps = ariusStatsIndexInfoEsDao.getClusterQps(dataSource.getCluster());
        n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.qps.total", String.valueOf(qps),
                dataSource.getLevel(), timestamp));

        long tps = ariusStatsIndexInfoEsDao.getClusterTps(dataSource.getCluster());
        n9eDataList.add(getN9eDataFormat(dataSource.getCluster(), "es.cluster.tps.total", String.valueOf(tps),
                dataSource.getLevel(), timestamp));

        if (response == null || response.isTimedOut()) {
            return n9eDataList;
        }

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
     * 更新 逻辑集群指标
     * @param response
     * @param datasource
     * @param timestamp
     * @param physicalNameLogicClusterListMap
     * @param logicClusterMetricMap
     */
    private void updateLogicClusterMetric(ESClusterHealthResponse response, ClusterPhy datasource, long timestamp,
                                          Map<String /*phyClusterName*/, List<ClusterLogic>> physicalNameLogicClusterListMap,
                                          Map<ClusterLogic, LogicClusterMetric> logicClusterMetricMap) {
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

    private void handleLogicCluster(ClusterPhy datasource, long timestamp, Map<ClusterLogic, LogicClusterMetric> logicClusterMetricMap, int status, long unAssignedShards, long numberPendingTasks, ClusterLogic logicCluster) {
        LogicClusterMetric logicClusterMetric = logicClusterMetricMap.get(logicCluster);
        if (logicClusterMetric == null) {
            logicClusterMetric = new LogicClusterMetric();
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
     * 发送物理机群指标到odin
     * @param odinDataFormats
     */
    private void sendPhysicalClusterMetric2N9e(Collection<List<N9eData>> odinDataFormats) {
        if (CollectionUtils.isNotEmpty(odinDataFormats)) {
            for (List<N9eData> n9eDataFormatList : odinDataFormats) {
                monitorDataSender.batchSend(n9eDataFormatList);
            }
        }
    }

    /**
     * 发送逻辑集群指标到odin
     * @param logicClusterMetrics
     */
    private void sendLogicClusterMetric2N9e(Collection<LogicClusterMetric> logicClusterMetrics) {
        if (CollectionUtils.isNotEmpty(logicClusterMetrics)) {
            for (LogicClusterMetric logicClusterMetric : logicClusterMetrics) {
                List<N9eData> logicN9eDataFormats = Lists.newArrayList();

                logicN9eDataFormats.add(getN9eDataFormat(String.valueOf(logicClusterMetric.clusterId),
                        "es.logic.cluster.health.status", String.valueOf(logicClusterMetric.getStatus()),
                        logicClusterMetric.getClusterLevel(), logicClusterMetric.getTimestamp()));

                logicN9eDataFormats.add(getN9eDataFormat(String.valueOf(logicClusterMetric.clusterId),
                        "es.logic.cluster.health.pendingTask", String.valueOf(logicClusterMetric.getPendingTask()),
                        logicClusterMetric.getClusterLevel(), logicClusterMetric.getTimestamp()));

                logicN9eDataFormats.add(getN9eDataFormat(String.valueOf(logicClusterMetric.clusterId),
                        "es.logic.cluster.health.unassignedShards",
                        String.valueOf(logicClusterMetric.getUnassignedShards()), logicClusterMetric.getClusterLevel(),
                        logicClusterMetric.getTimestamp()));

                monitorDataSender.batchSend(logicN9eDataFormats);
            }
        }
    }

    private void handlePhysicalClusterStatsForSum(String clusterName, ESClusterStatsCells esClusterStats) {
        // 这里会有多次es查询，做成并发的以免http接口超时
        futureUtil.runnableTask(()-> esClusterStats.setReadTps(ariusStatsIndexInfoEsDao.getClusterQps(clusterName)))
                .runnableTask(()->  esClusterStats.setWriteTps(ariusStatsIndexInfoEsDao.getClusterTps(clusterName)))
                .runnableTask(() -> esClusterStats.setRecvTransSize(ariusStatsNodeInfoEsDao.getClusterRx(clusterName)))
                .runnableTask(() -> esClusterStats.setSendTransSize(ariusStatsNodeInfoEsDao.getClusterTx(clusterName)))
                .runnableTask(() -> setClusterOtherStats(clusterName, esClusterStats))
                .waitExecute();
    }

    /**
     * 设置集群其他状态值
     *
     * @param clusterName
     * @param esClusterStats
     */
    private void setClusterOtherStats(String clusterName, ESClusterStatsCells esClusterStats) {
        ESClusterStatsResponse clusterStats = null;
        try {

            clusterStats = esClusterService.syncGetClusterStats(clusterName);
            if (Objects.isNull(clusterStats)) {
                return;
            }

            //索引相关
            esClusterStats.setTotalIndicesNu(clusterStats.getIndexCount());
            esClusterStats.setShardNu(clusterStats.getTotalShard());
            esClusterStats.setTotalDocNu(clusterStats.getDocsCount());
            esClusterStats.setEsNodeNu(clusterStats.getTotalNodes());

            //节点相关
            esClusterStats.setNumberNodes(clusterStats.getTotalNodes());
            esClusterStats.setNumberMasterNodes(clusterStats.getNumberMasterNodes());
            esClusterStats.setNumberClientNodes(clusterStats.getNumberClientNodes());
            esClusterStats.setNumberDataNodes(clusterStats.getNumberDataNodes());
            esClusterStats.setNumberIngestNodes(clusterStats.getNumberIngestNodes());

            //节点使用情况
            esClusterStats.setTotalStoreSize(clusterStats.getTotalFs().getBytes());
            esClusterStats.setFreeStoreSize(clusterStats.getFreeFs().getBytes());

            esClusterStats.setStoreSize(esClusterStats.getTotalStoreSize() - esClusterStats.getFreeStoreSize());
            esClusterStats.setIndexStoreSize(esClusterStats.getTotalStoreSize() - esClusterStats.getFreeStoreSize());

            //集群内存相关
            esClusterStats.setMemUsed(clusterStats.getMemUsed().getGb());
            esClusterStats.setMemFree(clusterStats.getMemFree().getGb());
            esClusterStats.setMemTotal(clusterStats.getMemTotal().getGb());
            esClusterStats.setMemFreePercent(clusterStats.getMemFreePercent());
            esClusterStats.setMemUsedPercent(clusterStats.getMemUsedPercent());

            //集群shard相关
            ESClusterHealthResponse clusterHealthResponse = esClusterService.syncGetClusterHealth(clusterName);
            if (null == clusterHealthResponse) {
                return;
            }
            esClusterStats.setUnAssignedShards(clusterHealthResponse.getUnassignedShards());
            esClusterStats.setNumberPendingTasks(clusterHealthResponse.getNumberOfPendingTasks());

        } catch (Exception e) {
            LOGGER.error("class=ClusterMonitorJobHandler||method=setClusterOtherStats||clusterName={}, clusterStats={}",
                    clusterName, clusterStats, e);
        }
    }


    //获取应用数量
    private int calcAppNu() {
        List<App> queryApps = appService.listApps();
        return CollectionUtils.isEmpty(queryApps) ? 0 : queryApps.size();
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

    private void handleN9eData(Map<ClusterPhy, ESClusterHealthResponse> clusterHealthResponseMap, Map<String, ESClusterStatsCells> esClusterStatsCellsMap) {
        if (MapUtils.isEmpty(clusterHealthResponseMap)) {
            LOGGER.warn("class=ClusterMonitorJobHandler||method=handleN9eData||msg= clusterHealthResponseMap is empty");
            return;
        }

        if (MapUtils.isEmpty(esClusterStatsCellsMap)) {
            LOGGER.warn("class=ClusterMonitorJobHandler||method=handleN9eData||msg= esClusterStatsCellsMap is empty");
            return;
        }

        // 只有线上和预发环境需要将指标上报到odin，线下和自测环境除外
        if (EnvUtil.isOnline() || EnvUtil.isPre()) {
            long timestamp = DateTimeUtil.getCurrentTimestampMinute();

            final Map<String, List<ClusterLogic>> physicalNameLogicClusterListMap = getPhysicalNameLogicClusterListMap();

            Map<ClusterLogic, LogicClusterMetric> logicClusterMetricMap = Maps.newHashMap();
            Map<ClusterPhy, List<N9eData>> physicalClusterMetricMap = Maps.newHashMap();

            for (Map.Entry<ClusterPhy, ESClusterHealthResponse> entry : clusterHealthResponseMap.entrySet()) {
                ClusterPhy dataSource = entry.getKey();
                ESClusterHealthResponse response = entry.getValue();
                ESClusterStatsCells esClusterStatsCells = esClusterStatsCellsMap.get(dataSource.getCluster());

                // add physical cluster metric
                physicalClusterMetricMap.put(dataSource, getPhysicalOdinFormatList(response, dataSource, timestamp, esClusterStatsCells));

                // update logic cluster metric
                updateLogicClusterMetric(response, dataSource, timestamp, physicalNameLogicClusterListMap,
                        logicClusterMetricMap);
            }

            // send physical metric to N9e
            sendPhysicalClusterMetric2N9e(physicalClusterMetricMap.values());

            // send logic metric to N9e
            sendLogicClusterMetric2N9e(logicClusterMetricMap.values());
        }
    }

    private void handleSaveClusterHealthToDB(Map<ClusterPhy, ESClusterHealthResponse> clusterHealthResponseMap) {
        if (null == clusterHealthResponseMap) {
            LOGGER.warn("class=ClusterMonitorJobHandler||method=handleSaveClusterHealthToDB||msg= clusterHealthResponseMap is empty");
            return;
        }

        clusterHealthResponseMap.forEach((clusterPhy, eSClusterHealthResponse) -> {
            ESClusterDTO esClusterDTO = new ESClusterDTO();
            try {
                esClusterDTO.setId(clusterPhy.getId());

                if (null == eSClusterHealthResponse) {
                    esClusterDTO.setHealth(ClusterHealthEnum.UNKNOWN.getCode());
                }else {
                    ClusterHealthEnum clusterHealthEnum = ClusterHealthEnum.valuesOf(eSClusterHealthResponse.getStatus());
                    esClusterDTO.setHealth(clusterHealthEnum.getCode());
                }
                clusterPhyService.editCluster(esClusterDTO, AriusUser.SYSTEM.getDesc());
            } catch (Exception e) {
                LOGGER.error(
                        "class=ClusterMonitorJobHandler||method=handleSaveClusterHealthToDB||clusterName={}, clusterStats={}",
                        clusterPhy.getCluster(), eSClusterHealthResponse.getStatus(), e);
            }
        });
    }
}