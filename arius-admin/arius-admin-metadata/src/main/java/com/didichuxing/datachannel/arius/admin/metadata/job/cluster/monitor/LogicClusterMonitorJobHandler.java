package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.LOGIC_CLUSTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.PHY_CLUSTER;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.stats.ClusterLogicStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsCells;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.ClusterLogicStatsPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.PercentilesEnum;
import com.didichuxing.datachannel.arius.admin.common.event.metrics.MetricsMonitorLogicClusterEvent;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.monitortask.AriusMetaJobClusterDistributeService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.MonitorMetricsSender;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterLogicStatsService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsClusterTaskInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsNodeInfoESDAO;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.clusterindex.IndexStatusResult;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 集群维度采集监控数据，包含 es节点存活检查；es集群tps/qps掉底报警
 *
 * @author ohushenglin_v
 */
@Component
public class LogicClusterMonitorJobHandler extends AbstractMetaDataJob {

    @Value("${monitorJob.threadPool.maxsize:30}")
    private int maxPoolSize;
    @Autowired
    private ClusterLogicService clusterLogicService;
    @Autowired
    private AriusMetaJobClusterDistributeService ariusMetaJobClusterDistributeService;
    @Autowired
    private MonitorMetricsSender monitorMetricsSender;
    @Autowired
    private ESClusterLogicStatsService clusterLogicStaticsService;
    @Autowired
    private ClusterRegionService clusterRegionService;
    @Autowired
    private ClusterPhyService clusterPhyService;
    @Autowired
    private ClusterRoleHostService clusterRoleHostService;
    @Autowired
    private ESIndexCatService esIndexCatService;
    @Autowired
    private AriusStatsNodeInfoESDAO ariusStatsNodeInfoEsDao;
    @Autowired
    private AriusStatsClusterTaskInfoESDAO ariusStatsClusterTaskInfoESDAO;
    private final String hostName = HttpHostUtil.HOST_NAME;
    private FutureUtil<ESClusterStats> clusterLogicFutureUtil;
    private static final Integer SEARCH_SIZE = 5000;

    private long                                 timestamp = CommonUtils
            .monitorTimestamp2min(System.currentTimeMillis());

    @Override
    public Object handleJobTask(String params) {
        handleLogicClusterStats();
        return JOB_SUCCESS;
    }

    @PostConstruct
    public void init() {
        clusterLogicFutureUtil = FutureUtil.init("LogicClusterMonitorJobHandler", 3 * maxPoolSize, 3 * maxPoolSize, 100);
    }

    private void handleLogicClusterStats() {
        // 获取单台机器监控采集的集群名称列表, 当分布式部署分组采集，可分摊采集压力
        List<ClusterPhy> monitorCluster = ariusMetaJobClusterDistributeService.getSingleMachineMonitorCluster(hostName);
//        monitorCluster = clusterPhyService.listAllClusters();
        // 2. do handle
        if (CollectionUtils.isNotEmpty(monitorCluster)) {
            Set<String> monitorClusterSet = monitorCluster.stream().map(ClusterPhy::getCluster)
                    .collect(Collectors.toSet());
            doHandleLogicClusterStats(monitorClusterSet);
        }
    }

    private void doHandleLogicClusterStats(Set<String> monitorClusterSet) {
        List<ClusterRegion> regionList = clusterRegionService.listRegionByPhyClusterNames(Lists.newArrayList(monitorClusterSet)).stream()
                .filter(clusterRegion -> StringUtils.isNotBlank(clusterRegion.getLogicClusterIds())
                        && !AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID.equals(clusterRegion.getLogicClusterIds()))
                .collect(Collectors.toList());
        List<Long> logicClusterIds = regionList.stream().map(clusterRegion -> ListUtils.string2StrList(clusterRegion.getLogicClusterIds()))
                .flatMap(Collection::stream).distinct().map(Long::parseLong).collect(Collectors.toList());
        List<ClusterLogic> clusterLogicList = clusterLogicService.getClusterLogicListByIds(logicClusterIds);
        List<ClusterRoleHost> clusterRoleHostsList = clusterRoleHostService.listNodesByClusters(Lists.newArrayList(monitorClusterSet)).stream()
                .filter(i -> Objects.nonNull(i.getRegionId()) && !Objects.equals(-1, i.getRegionId())).collect(Collectors.toList());
        List<ClusterPhy> phyClusterList = clusterPhyService.listClustersByNames(Lists.newArrayList(monitorClusterSet));
        List<IndexCatCell> allCatIndexNameList = esIndexCatService.syncGetAllCatIndexNameListByClusters(SEARCH_SIZE,Lists.newArrayList(monitorClusterSet))
                .stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(clusterLogicList) || CollectionUtils.isEmpty(regionList)
                || CollectionUtils.isEmpty(clusterRoleHostsList) || CollectionUtils.isEmpty(phyClusterList)) {
            LOGGER.info("class=LogicClusterMonitorJobHandler||method=doHandleLogicClusterStats||msg=logicClusters is empty");
            return;
        }
        if (CollectionUtils.isEmpty(allCatIndexNameList)) {
            LOGGER.info("class=LogicClusterMonitorJobHandler||method=doHandleLogicClusterStats||msg=indices is empty");
            return;
        }
        //物理集群和region
        Map<String, List<ClusterRegion>> phyClusterRegionMap = ConvertUtil.list2MapOfList(regionList,
                clusterRegion -> clusterRegion.getPhyClusterName(), clusterRegionList -> clusterRegionList);
        //regionId和nodeName
        Map<Integer, List<String>> regionNodeMap = ConvertUtil.list2MapOfList(clusterRoleHostsList, ClusterRoleHost::getRegionId,
                ClusterRoleHost::getNodeSet);
        //物理集群和物理信息的关系
        Map<String, ClusterPhy> ClusterPhyMap = ConvertUtil.list2Map(phyClusterList, clusterPhy -> clusterPhy.getCluster(),
                clusterPhy -> clusterPhy);
        //逻辑集群和索引的关系
        Map<Long, List<String>> logicClusterIndicesNameMap = ConvertUtil.list2MapOfList(allCatIndexNameList,
                IndexCatCell::getResourceId, IndexCatCell::getIndex);
        //逻辑集群对象
        Map<Long, ClusterLogic> longClusterLogicMap = ConvertUtil.list2Map(clusterLogicList,
                ClusterLogic::getId, clusterLogic -> clusterLogic);
        //物理集群和逻辑集群
        Map<String, List<ClusterLogic>> phyClusterNameLogicCluster = Maps.newHashMap();
        Map<String, List<String>> phyClusterNameLogicCLusterIds = regionList.stream()
                .collect(Collectors.toMap(ClusterRegion::getPhyClusterName, clusterRegion ->
                        Lists.newArrayList(StringUtils.split(clusterRegion.getLogicClusterIds(), ","))
                        , (oldList, newList) -> {
            oldList.addAll(newList);
            return oldList;
        }));
        for (Map.Entry<String, List<String>> entry : phyClusterNameLogicCLusterIds.entrySet()) {
            List<ClusterLogic> logicList = entry.getValue().stream().
                    map(logicId -> longClusterLogicMap.get(Long.parseLong(logicId))).filter(Objects::nonNull).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(logicList)) {
                phyClusterNameLogicCluster.put(entry.getKey(), logicList);
            }
        }
        //维护逻辑集群和节点名称之间的关系，物理机群节点统计信息的关系，逻辑集群和索引统计信息的关系，方便后面计算
        Map<Integer, List<String>> logicClusterNodesNameMap = Maps.newConcurrentMap();
        Map<String, List<ClusterLogicStats>> nodeStatsMap = Maps.newConcurrentMap();
        Map<Long, List<IndexStatusResult>> logicClusterIndicesStatsMap = Maps.newConcurrentMap();
        for (String phyClusterName : monitorClusterSet) {
            clusterLogicFutureUtil.runnableTask(() -> {
                clusterLogicStaticsService.buildLogicClusterStats(phyClusterRegionMap, regionNodeMap,
                        logicClusterIndicesNameMap, phyClusterName, logicClusterNodesNameMap,
                        nodeStatsMap, logicClusterIndicesStatsMap);
                //指标采集
                List<ESClusterStats> esLogicClusterStatsList = Lists.newArrayList();
                long collectTime = CommonUtils.monitorTimestamp2min(System.currentTimeMillis());
                List<ClusterLogic> logicClusterListByPhyClusterName = phyClusterNameLogicCluster.get(phyClusterName);
                if (CollectionUtils.isNotEmpty(logicClusterListByPhyClusterName)) {
                    logicClusterListByPhyClusterName.stream()
                            .filter(logicCluster -> logicClusterNodesNameMap.containsKey(logicCluster.getId().intValue()))
                            .forEach(logicCluster -> {
                        ClusterLogicStatsPO clusterLogicStatisPO = clusterLogicStaticsService.getLogicClusterStats(logicCluster.getId(),
                                ClusterPhyMap.get(phyClusterName), logicClusterNodesNameMap,
                                nodeStatsMap, logicClusterIndicesStatsMap, true);
                        ESClusterStatsCells esClusterStatsBean = new ESClusterStatsCells();
                        esClusterStatsBean.setStatus(clusterLogicStatisPO.getStatus());
                        esClusterStatsBean.setStatusType(clusterLogicStatisPO.getStatusType());
                        esClusterStatsBean.setClusterName(logicCluster.getName());
                        esClusterStatsBean.setLevel(logicCluster.getLevel());
                        esClusterStatsBean.setClusterNu(1);
                        esClusterStatsBean.setTotalIndicesNu(clusterLogicStatisPO.getIndexNu());
                        esClusterStatsBean.setTotalDocNu(clusterLogicStatisPO.getDocNu());
                        Optional.ofNullable(clusterLogicStatisPO.getUsedDisk())
                                .ifPresent(usedDisk->esClusterStatsBean.setIndexStoreSize(usedDisk.longValue()));
                        Optional.ofNullable(clusterLogicStatisPO.getUsedDisk())
                                .ifPresent(usedDisk->esClusterStatsBean.setStoreSize(usedDisk.longValue()));
                        Optional.ofNullable(clusterLogicStatisPO.getFreeDisk())
                                .ifPresent(freeDisk->esClusterStatsBean.setFreeStoreSize(freeDisk.longValue()));
                        Optional.ofNullable(clusterLogicStatisPO.getTotalDisk())
                                .ifPresent(totalDisk->esClusterStatsBean.setTotalStoreSize(totalDisk.longValue()));
                        esClusterStatsBean.setNumberDataNodes(clusterLogicStatisPO.getNumberDataNodes());
                        esClusterStatsBean.setNumberPendingTasks(clusterLogicStatisPO.getNumberPendingTasks());
                        esClusterStatsBean.setUnAssignedShards(clusterLogicStatisPO.getUnAssignedShards());
                        esClusterStatsBean.setCpuUsage(clusterLogicStatisPO.getCpuUsedPercent());
                        esClusterStatsBean.setAlivePercent(clusterLogicStatisPO.getAlivePercent());
                        if (Objects.nonNull(esClusterStatsBean.getStoreSize()) && Objects.nonNull(esClusterStatsBean.getTotalStoreSize())
                                && 0.0 < esClusterStatsBean.getTotalStoreSize()) {
                            esClusterStatsBean.setDiskUsage(esClusterStatsBean.getStoreSize().doubleValue() / esClusterStatsBean.getTotalStoreSize());
                        }
                                List<String> nodes = getNodesWithClusterLogic(logicCluster.getId());
                                handlePhysicalClusterStatsForSum(phyClusterName,nodes, esClusterStatsBean);


                        //获取不同分位值的指标
                        Map<String, ESClusterStatsCells> percentilesType2ESClusterStatsCellsMap = getPhysicalClusterStatsPercentiles(logicCluster.getName(), nodes, esClusterStatsBean,phyClusterName);

                        percentilesType2ESClusterStatsCellsMap.forEach((percentilesType, esClusterStatsCells) -> {
                                 ESClusterStats esClusterStats = new ESClusterStats();
                                 esClusterStats.setStatis(esClusterStatsCells);
                                 esClusterStats.setCluster(logicCluster.getName());
                                 esClusterStats.setPercentilesType(percentilesType);
                                 esClusterStats.setPhysicCluster(LOGIC_CLUSTER);
                                 esClusterStats.setTimestamp(collectTime);
                                 esClusterStats.setDataCenter(logicCluster.getDataCenter());

                                 esLogicClusterStatsList.add(esClusterStats);
                        });
                    });
                }
                // send cluster status to es and kafka
                if (CollectionUtils.isNotEmpty(esLogicClusterStatsList)) {
                    monitorMetricsSender.sendClusterStats(esLogicClusterStatsList);
                    SpringTool.publish(new MetricsMonitorLogicClusterEvent(this, esLogicClusterStatsList, hostName));
                }
            });
        }
        clusterLogicFutureUtil.waitExecute();
    }

    /**
     * 采集系统指标
     * @param nodes
     * @param esClusterStats
     */
    private void handlePhysicalClusterStatsForSum(String phyClusterName,List<String> nodes, ESClusterStatsCells esClusterStats) {
        // 这里会有多次es查询，做成并发的以免http接口超时
        clusterLogicFutureUtil.runnableTask(() -> esClusterStats.setReadTps(ariusStatsNodeInfoEsDao.getClusterLogicQps(phyClusterName,nodes)))
                .runnableTask(() -> esClusterStats.setWriteTps(ariusStatsNodeInfoEsDao.getClusterLogicTps(phyClusterName,nodes)))
                .runnableTask(() -> esClusterStats.setRecvTransSize(ariusStatsNodeInfoEsDao.getClusterLogicRx(phyClusterName,nodes)))
                .runnableTask(() -> esClusterStats.setSendTransSize(ariusStatsNodeInfoEsDao.getClusterLogicTx(phyClusterName,nodes)))
                .runnableTask(() -> esClusterStats.setSearchLatency(calcSearchLatencyAvg(phyClusterName,nodes)))
                .runnableTask(() -> esClusterStats.setIndexingLatency(calcIndexingLatencyAvg(phyClusterName,nodes)))
                .waitExecute();
    }

    /**
     * 计算SearchLatency
     *    计算逻辑：
     *    （集群下的所有节点,间隔时间内通过_node/stats命令获取nodes.{nodeName}.indices.search.query_time_in_millis差值累加值）
     *        除以
     *    （节点间隔时间nodes.{nodeName}.indices.search.query_total差值累加值）
     *
     * @param nodes   集群下的节点
     * @return
     */
    private double calcSearchLatencyAvg(String phyClusterName,List<String> nodes){
        // 获取分子：所有节点的indices.search.query_time_in_millis差值累加值
        double searchLatencySum = ariusStatsNodeInfoEsDao.getClusterLogicSearchLatencySum(phyClusterName,nodes);
        // 获取分母：所有节点indices.search.query_total差值累加值
        double searchQueryTotal = ariusStatsNodeInfoEsDao.getClusterLogicSearchQueryTotal(phyClusterName,nodes);
        return searchQueryTotal == 0 ? 0 : (searchLatencySum / searchQueryTotal);
    }

    /**
     * 计算IndexingLatency
     *    计算逻辑：
     *    （集群下的所有节点,间隔时间内通过_node/stats命令获取nodes.{nodeName}.indices.indexing.index_time_in_millis差值累加值）
     *         除以
     *    （节点间隔时间nodes.{nodeName}.indices.docs.count差值累加值）
     *
     * @param phyClusterName
     * @return
     */
    private double calcIndexingLatencyAvg(String phyClusterName,List<String> nodes){
        // 获取分子：所有节点的indices.indexing.index_time_in_millis差值累加值
        double indexingLatencySum = ariusStatsNodeInfoEsDao.getClusterLogicIndexingLatencySum(phyClusterName,nodes);
        // 获取分母：所有节点的indices.docs.count差值累加值
        double indexingDocSum = ariusStatsNodeInfoEsDao.getClusterLogicIndexingDocSum(phyClusterName,nodes);
        return indexingDocSum == 0 ? 0 : (indexingLatencySum / indexingDocSum);
    }


    /**
     * 获取逻辑集群下的nodes
     * @param clusterLogicId
     * @return
     */
    private List<String> getNodesWithClusterLogic(Long  clusterLogicId) {
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogicId);
        Result<List<ClusterRoleHost>> result = clusterRoleHostService
                .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        List<String> nodes = result.getData().stream().map(ClusterRoleHost::getNodeSet).collect(Collectors.toList());
        return nodes;
    }

    /**
     * 采集不同分位图的指标数据
     * @return  Map<String, ESClusterStatsCells>
     */
    private Map<String, ESClusterStatsCells> getPhysicalClusterStatsPercentiles(String clusterLogicName,List<String> nodes,ESClusterStatsCells esClusterStatsCells,String phyClusterName) {
        Map<String, ESClusterStatsCells> percentilesType2ESClusterStatsCellsMap = Maps.newHashMap();
        if (AriusObjUtils.isNull(clusterLogicName)) {
            LOGGER.warn(
                    "class=ClusterMonitorJobHandler||method=getPhysicalClusterStatsPercentiles||errMsg=clusterName is empty");
            return percentilesType2ESClusterStatsCellsMap;
        }
        AtomicReference<Map<String, Double>> clusterCpuAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterDiskFreeUsagePercentAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterCpuLoad1MinAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterCpuLoad5MinAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterCpuLoad15MinAvgAndPercentilesAtomic = new AtomicReference<>();
        AtomicReference<Map<String, Double>> clusterTaskCostMinAvgAndPercentilesAtomic = new AtomicReference<>();

        clusterLogicFutureUtil
                .runnableTask(() -> clusterCpuAvgAndPercentilesAtomic
                        .set(ariusStatsNodeInfoEsDao.getClusterLogicCpuAvgAndPercentiles(nodes,phyClusterName)))
                .runnableTask(() -> clusterDiskFreeUsagePercentAvgAndPercentilesAtomic
                        .set(ariusStatsNodeInfoEsDao.getClusterLogicDiskFreeUsagePercentAvgAndPercentiles(nodes,phyClusterName)))
                .runnableTask(() -> clusterCpuLoad1MinAvgAndPercentilesAtomic
                        .set(ariusStatsNodeInfoEsDao.getClusterLogicCpuLoad1MinAvgAndPercentiles(nodes,phyClusterName)))
                .runnableTask(() -> clusterCpuLoad5MinAvgAndPercentilesAtomic
                        .set(ariusStatsNodeInfoEsDao.getClusterLogicCpuLoad5MinAvgAndPercentiles(nodes,phyClusterName)))
                .runnableTask(() -> clusterCpuLoad15MinAvgAndPercentilesAtomic
                        .set(ariusStatsNodeInfoEsDao.getClusterLogicCpuLoad15MinAvgAndPercentiles(nodes,phyClusterName)))
                .runnableTask(() -> clusterTaskCostMinAvgAndPercentilesAtomic
                        .set(ariusStatsClusterTaskInfoESDAO.getTaskCostMinAvgAndPercentilesWithNodes(nodes,phyClusterName)))
                .waitExecute();

        for (String type : PercentilesEnum.listUsefulType()) {
            ESClusterStatsCells esClusterStatsCellDeepCopy = ConvertUtil.obj2Obj(esClusterStatsCells,
                    ESClusterStatsCells.class);

            buildForPercentiles(esClusterStatsCellDeepCopy, type, clusterCpuAvgAndPercentilesAtomic.get(),
                    clusterCpuLoad1MinAvgAndPercentilesAtomic.get(), clusterCpuLoad5MinAvgAndPercentilesAtomic.get(),
                    clusterCpuLoad15MinAvgAndPercentilesAtomic.get(),
                    clusterDiskFreeUsagePercentAvgAndPercentilesAtomic.get(),
                    clusterTaskCostMinAvgAndPercentilesAtomic.get());

            percentilesType2ESClusterStatsCellsMap.put(type, esClusterStatsCellDeepCopy);

        }

        return percentilesType2ESClusterStatsCellsMap;
    }

    /**
     * @param esClusterStatsCellDeepCopy                       集群维度统计信息
     * @param type                                             avg、分位类型(99、95、75、55)
     * @param clusterCpuAvgAndPercentiles                      集群cpu平均值和分位值(key:99, value:值)
     * @param clusterDiskFreeUsagePercentAvgAndPercentiles     集群节点磁盘空闲率平均值和分位值(key:99, value:值)
     * @param clusterCpuLoad1MinAvgAndPercentiles              集群cpu load1平均值和分位值(key:99, value:值)
     * @param clusterCpuLoad5MinAvgAndPercentiles              集群cpu load5平均值和分位值(key:99, value:值)
     * @param clusterCpuLoad15MinAvgAndPercentiles             集群cpu load15平均值和分位值(key:99, value:值)
     * @param clusterTaskCostMinAvgAndPercentiles              集群task cost平均值和分位值(key:99, value:值)
     */
    private void buildForPercentiles(ESClusterStatsCells esClusterStatsCellDeepCopy, String type,
                                     Map<String, Double> clusterCpuAvgAndPercentiles,
                                     Map<String, Double> clusterCpuLoad1MinAvgAndPercentiles,
                                     Map<String, Double> clusterCpuLoad5MinAvgAndPercentiles,
                                     Map<String, Double> clusterCpuLoad15MinAvgAndPercentiles,
                                     Map<String, Double> clusterDiskFreeUsagePercentAvgAndPercentiles,
                                     Map<String, Double> clusterTaskCostMinAvgAndPercentiles) {
        if (null != clusterCpuAvgAndPercentiles && null != clusterCpuAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuUsage(clusterCpuAvgAndPercentiles.get(type));
        }

        if (null != clusterCpuLoad1MinAvgAndPercentiles && null != clusterCpuLoad1MinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuLoad1M(clusterCpuLoad1MinAvgAndPercentiles.get(type));
        }

        if (null != clusterCpuLoad5MinAvgAndPercentiles && null != clusterCpuLoad5MinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuLoad5M(clusterCpuLoad5MinAvgAndPercentiles.get(type));
        }

        if (null != clusterCpuLoad15MinAvgAndPercentiles && null != clusterCpuLoad15MinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setCpuLoad15M(clusterCpuLoad15MinAvgAndPercentiles.get(type));
        }

        String realType = convertSpecialTypeForDiskFreeUsage(type);
        if (null != clusterDiskFreeUsagePercentAvgAndPercentiles
                && null != clusterDiskFreeUsagePercentAvgAndPercentiles.get(realType)) {
            esClusterStatsCellDeepCopy.setDiskUsage(1 - clusterDiskFreeUsagePercentAvgAndPercentiles.get(realType));
        }

        if (null != clusterTaskCostMinAvgAndPercentiles && null != clusterTaskCostMinAvgAndPercentiles.get(type)) {
            esClusterStatsCellDeepCopy.setTaskCost(clusterTaskCostMinAvgAndPercentiles.get(type));
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
}