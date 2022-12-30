package com.didichuxing.datachannel.arius.admin.biz.metrics.handle;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.biz.component.MetricsValueConvertUtils;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.percentiles.BasePercentileMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.*;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterPhyStatsService;
import com.didiglobal.knowframework.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.knowframework.elasticsearch.client.response.model.fs.FSNode;
import com.didiglobal.knowframework.elasticsearch.client.response.model.fs.FSTotal;
import com.didiglobal.knowframework.elasticsearch.client.response.model.jvm.JvmMem;
import com.didiglobal.knowframework.elasticsearch.client.response.model.jvm.JvmNode;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.BIG_SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.LOGIC_CLUSTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.PHY_CLUSTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum.*;

/**
 * Created by linyunan on 2021-08-02
 */
@Component
public class ClusterLogicOverviewMetricsHandle {

    private static final ILog LOGGER                        = LogFactory
        .getLog(ClusterLogicOverviewMetricsHandle.class);

    @Autowired
    private ESClusterPhyStatsService      esClusterPhyStatsService;

    @Autowired
    private ESClusterService              esClusterService;

    @Autowired
    private ESClusterNodeService          esClusterNodeService;

    @Autowired
    private ESShardService                esShardService;

    @Autowired
    private ClusterRoleHostService        clusterRoleHostService;

    @Autowired
    private ESTemplateService             esTemplateService;

    @Autowired
    private AriusConfigInfoService        ariusConfigInfoService;
    @Autowired
    private ESIndexCatService             esIndexCatService;

    @Autowired
    private IndexTemplatePhyService       indexTemplatePhyService;
    @Autowired
    private ESIndexService                esIndexService;

    @Autowired
    private ClusterLogicService           clusterLogicService;

    @Autowired
    private ClusterRegionService          clusterRegionService;

    @Autowired
    private IndexTemplateService          indexTemplateService;


    private static final FutureUtil<Void> getMultipleMetricFutureUtil   = FutureUtil.init("getMultipleMetricFutureUtil",
        10, 10, 100);
    private static final FutureUtil<Void> getClusterBasicInfoFutureUtil = FutureUtil
        .init("getClusterBasicInfoFutureUtil", 10, 10, 50);
    private static final FutureUtil<Void> optimizeQueryBurrFutureUtil   = FutureUtil.init("optimizeQueryBurrFutureUtil",
        10, 10, 50);

    /**
     *         metricsTypes 物理集群二级指标类型
     * @see    ClusterPhyClusterMetricsEnum
     * @return ESClusterOverviewMetricsVO
     */
    public ESClusterOverviewMetricsVO buildClusterLogicOverviewMetrics(MetricsClusterPhyDTO metricsClusterPhyDTO) {

        //1. building base objects
        ESClusterOverviewMetricsVO esClusterOverviewMetricsVO = initESClusterPhyOverviewMetricsVO(metricsClusterPhyDTO);

        //2. 从ES中获取指标, 同时获取多个
        for (String metricsType : metricsClusterPhyDTO.getMetricsTypes()) {
            getMultipleMetricFutureUtil.runnableTask(() -> aggClusterLogicOverviewMetrics(esClusterOverviewMetricsVO,
                metricsType, metricsClusterPhyDTO.getAggType(), metricsClusterPhyDTO.getStartTime(),
                metricsClusterPhyDTO.getEndTime(),metricsClusterPhyDTO.getItemNamesUnderClusterLogic(),
                    metricsClusterPhyDTO.getClusterLogicName(),metricsClusterPhyDTO.getProjectId()));
        }
        getMultipleMetricFutureUtil.waitExecute();
        //3.非超级项目进行大索引过滤
        filterESClusterOverviewMetricsVOByProjectIdAndClusterLogicName(esClusterOverviewMetricsVO,
                metricsClusterPhyDTO.getProjectId(), metricsClusterPhyDTO.getClusterLogicName());
        //4. uniform percentage unit
        MetricsValueConvertUtils.convertClusterOverviewMetricsPercent(esClusterOverviewMetricsVO);

        //5. optimize query burr
        optimizeQueryBurrForClusterOverviewMetrics(esClusterOverviewMetricsVO);
        
        return esClusterOverviewMetricsVO;
    }

    /******************************************private*******************************************************/

    private void filterESClusterOverviewMetricsVOByProjectIdAndClusterLogicName(
            ESClusterOverviewMetricsVO esClusterOverviewMetricsVO, Integer projectId, String clusterLogicName) {
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId) && StringUtils.isNotBlank(clusterLogicName)) {
            List<String> belongToProjectIdIndexList = esIndexCatService.syncGetIndexListByProjectId(projectId,
                    clusterLogicName);
            if (CollectionUtils.isEmpty(belongToProjectIdIndexList)) {
                esClusterOverviewMetricsVO.setBigIndices(Collections.emptyList());
                esClusterOverviewMetricsVO.setBigShards(Collections.emptyList());
                return;
            }
            
            //过滤出项目所属大索引, 大于10亿文档数的索引
            List<BigIndexMetricsVO> filterProjectIdBigIndex = Optional.ofNullable(
                            esClusterOverviewMetricsVO.getBigIndices()).orElse(Collections.emptyList()).stream()
                    .filter(i -> belongToProjectIdIndexList.contains(i.getIndexName())).collect(Collectors.toList());
            esClusterOverviewMetricsVO.setBigIndices(filterProjectIdBigIndex);
            //过滤出项目所属大shard列表
            List<BigShardMetricsVO> bigShardMetricsVOS = Optional.ofNullable(esClusterOverviewMetricsVO.getBigShards())
                    .orElse(Collections.emptyList()).stream()
                    .filter(i -> belongToProjectIdIndexList.contains(i.getIndex())).collect(Collectors.toList());
            esClusterOverviewMetricsVO.setBigShards(bigShardMetricsVOS);
        }
    
    }

    /**
     * optimize query burr , compare the corresponding values of the front and
     * back slices of a single time slice to find the maximum value
     * @param esClusterOverviewMetricsVO
     */
    private void optimizeQueryBurrForClusterOverviewMetrics(ESClusterOverviewMetricsVO esClusterOverviewMetricsVO) {
        optimizeQueryBurrFutureUtil
            .runnableTask(
                () -> MetricsValueConvertUtils.doOptimizeForPercentiles(esClusterOverviewMetricsVO.getCpuUsage()))
            .runnableTask(
                () -> MetricsValueConvertUtils.doOptimizeForPercentiles(esClusterOverviewMetricsVO.getCpuLoad1M()))
            .runnableTask(
                () -> MetricsValueConvertUtils.doOptimizeForPercentiles(esClusterOverviewMetricsVO.getCpuLoad5M()))
            .runnableTask(
                () -> MetricsValueConvertUtils.doOptimizeForPercentiles(esClusterOverviewMetricsVO.getCpuLoad15M()))
            .runnableTask(
                () -> MetricsValueConvertUtils.doOptimizeForPercentiles(esClusterOverviewMetricsVO.getDiskUsage()))
            .runnableTask(
                () -> MetricsValueConvertUtils.doOptimizeForPercentiles(esClusterOverviewMetricsVO.getSearchLatency()))
            .runnableTask(() -> MetricsValueConvertUtils
                .doOptimizeForPercentiles(esClusterOverviewMetricsVO.getIndexingLatency()))
            .runnableTask(() -> MetricsValueConvertUtils
                .doOptimizeForRecvTransSize(esClusterOverviewMetricsVO.getRecvTransSize()))
            .runnableTask(() -> MetricsValueConvertUtils
                .doOptimizeForSendTransSize(esClusterOverviewMetricsVO.getSendTransSize()))
            .runnableTask(
                () -> MetricsValueConvertUtils.doOptimizeForDiskInfo(esClusterOverviewMetricsVO.getDiskInfo()))
            .runnableTask(() -> MetricsValueConvertUtils.doOptimizeForShardNu(esClusterOverviewMetricsVO.getShardNu()))
            .runnableTask(() -> MetricsValueConvertUtils.doOptimizeForReadTps(esClusterOverviewMetricsVO.getReadTps()))
            .runnableTask(
                () -> MetricsValueConvertUtils.doOptimizeForWriteTps(esClusterOverviewMetricsVO.getWriteTps()))
            .waitExecute();
    }

    private ESClusterOverviewMetricsVO initESClusterPhyOverviewMetricsVO(MetricsClusterPhyDTO metricsClusterPhyDTO) {
        ESClusterOverviewMetricsVO esClusterOverviewMetricsVO = new ESClusterOverviewMetricsVO();
        ESClusterPhyBasicMetricsVO basicMetricsVO = new ESClusterPhyBasicMetricsVO();
        esClusterOverviewMetricsVO.setClusterName(metricsClusterPhyDTO.getClusterPhyName());
        esClusterOverviewMetricsVO.setPhysicCluster(PHY_CLUSTER);
        esClusterOverviewMetricsVO.setDataCenter(EnvUtil.getDC().getCode());
        esClusterOverviewMetricsVO.setBasic(basicMetricsVO);
        esClusterOverviewMetricsVO.setCurrentTime(DateTimeUtil.formatTimestamp(System.currentTimeMillis()));
        esClusterOverviewMetricsVO.setBigShardThreshold(ariusConfigInfoService.doubleSetting(AriusConfigConstant.ARIUS_COMMON_GROUP,
                AriusConfigConstant.BIG_SHARD_THRESHOLD, BIG_SHARD));
        return esClusterOverviewMetricsVO;
    }

    private void aggClusterLogicOverviewMetrics(ESClusterOverviewMetricsVO metrics, String metricsType, String aggType,
                                                Long startTime, Long endTime, List<String> itemNamesUnderClusterLogic, String clusterLogic, Integer projectId) {
        try {
            switch (ClusterPhyClusterMetricsEnum.valueOfType(metricsType)) {
                /*************************基本状态指标(状态类型)***************************/
                case BASIC:
                    getBasicMetrics(metrics,itemNamesUnderClusterLogic,clusterLogic,projectId);
                    return;
                case ELAPSEDTIME:
                    getElapsedTimeMetrics(metrics);
                    return;

                /****************************业务指标(列表类型)*****************************/
                case PENDING_TASKS:
                    getPendingTasksMetrics(metrics);
                    return;
                case MOVING_SHARDS:
                    getMovingShardsMetrics(metrics);
                    return;
                case UNASSIGN_SHARDS:
                    getUnassignedShardsMetrics(metrics);
                    return;
                case INVALID_NODES:
                    getInvalidNodesMetrics(metrics);
                    return;
                case BIG_INDICES:
                    getBigIndicesMetrics(metrics);
                    return;
                case BIG_SHARDS:
                    getBigShardsMetrics(metrics);
                    return;

                /********************************分位图类型*******************************/
                case CPU_USAGE:
                    aggPercentilesMetrics(metrics,LOGIC_CLUSTER, CPU_USAGE.getType(), aggType, startTime, endTime,
                        b -> metrics.setCpuUsage(ConvertUtil.list2List(b, CpuUsageMetricsVO.class)));
                    return;
                case CPU_LOAD_1M:
                    aggPercentilesMetrics(metrics,LOGIC_CLUSTER, CPU_LOAD_1M.getType(), aggType, startTime, endTime,
                        b -> metrics.setCpuLoad1M(ConvertUtil.list2List(b, CpuLoadFor1MinMetricsVO.class)));
                    return;
                case CPU_LOAD_5M:
                    aggPercentilesMetrics(metrics,LOGIC_CLUSTER, CPU_LOAD_5M.getType(), aggType, startTime, endTime,
                        b -> metrics.setCpuLoad5M(ConvertUtil.list2List(b, CpuLoadFor5MinMetricsVO.class)));
                    return;
                case CPU_LOAD_15M:
                    aggPercentilesMetrics(metrics,LOGIC_CLUSTER, CPU_LOAD_15M.getType(), aggType, startTime, endTime,
                        b -> metrics.setCpuLoad15M(ConvertUtil.list2List(b, CpuLoadFor15MinMetricsVO.class)));
                    return;
                case DISK_USAGE:
                    aggPercentilesMetrics(metrics,LOGIC_CLUSTER, DISK_USAGE.getType(), aggType, startTime, endTime,
                        b -> metrics.setDiskUsage(ConvertUtil.list2List(b, DiskUsageMetricsVO.class)));
                    return;
                case SEARCH_LATENCY:
                    aggPercentilesMetrics(metrics,LOGIC_CLUSTER, SEARCH_LATENCY.getType(), aggType, startTime, endTime,
                        b -> metrics.setSearchLatency(ConvertUtil.list2List(b, SearchLatencyMetricsVO.class)));
                    return;
                case INDEXING_LATENCY:
                    aggPercentilesMetrics(metrics,LOGIC_CLUSTER, INDEXING_LATENCY.getType(), aggType, startTime, endTime,
                        b -> metrics.setIndexingLatency(ConvertUtil.list2List(b, IndexingLatencyMetricsVO.class)));
                    return;

                case TASK_COST:
                    aggPercentilesMetrics(metrics,LOGIC_CLUSTER, TASK_COST.getType(), aggType, startTime, endTime,
                        b -> metrics.setTaskCost(ConvertUtil.list2List(b, TaskCostMetricVO.class)));
                    return;

                /********************************普通指标(折线图)*******************************/
                case DISK_INFO:
                    aggDiskInfoMetrics(metrics,LOGIC_CLUSTER, aggType, startTime, endTime);
                    return;
                case SHARD_NUM:
                    aggShardNuMetrics(metrics,LOGIC_CLUSTER, aggType, startTime, endTime);
                    return;
                case READ_QPS:
                    aggReadTpsMetrics(metrics,LOGIC_CLUSTER, aggType, startTime, endTime);
                    return;
                case WRITE_TPS:
                    aggWriteTpsMetrics(metrics,LOGIC_CLUSTER, aggType, startTime, endTime);
                    return;
                case RECV_TRANS_SIZE:
                    aggRecvTransMetrics(metrics,LOGIC_CLUSTER, aggType, startTime, endTime);
                    return;
                case SEND_TRANS_SIZE:
                    aggSendTransMetrics(metrics,LOGIC_CLUSTER, aggType, startTime, endTime);
                    return;
                case NODES_FOR_DISK_USAGE_GTE_75PERCENT:
                    aggNodesForDiskUsageGte75PercentMetrics(metrics);
                    return;
                case TASK_NUM:
                    aggTaskCount(metrics,LOGIC_CLUSTER, aggType, startTime, endTime);
                    return;
                default:
            }
        } catch (Exception e) {
            LOGGER.error("class=ClusterLogicOverviewMetricsHandle||method=aggClusterLogicOverviewMetrics||errMsg={}",
                e.getMessage());
        }
    }

    private void getInvalidNodesMetrics(ESClusterOverviewMetricsVO metrics) {
        List<String> nodeHostsFromES = esClusterNodeService.syncGetNodeHosts(metrics.getClusterName());
        List<ClusterRoleHost> nodesByCluster = clusterRoleHostService.getNodesByCluster(metrics.getClusterName());
        List<ClusterRoleHost> invalidNodeIps = Lists.newArrayList();
        nodesByCluster.forEach(nodeFromDb -> {
            if (!nodeHostsFromES.contains(nodeFromDb.getIp())) {
                invalidNodeIps.add(nodeFromDb);
            }
        });

        metrics.setInvalidNodes(invalidNodeIps);
    }

    private void getBigShardsMetrics(ESClusterOverviewMetricsVO metrics) throws ESOperateException {
        List<ShardMetrics> shardMetrics = esShardService.syncGetBigShards(metrics.getClusterName());
        metrics.setBigShards(ConvertUtil.list2List(shardMetrics, BigShardMetricsVO.class));
    }

    private void getBigIndicesMetrics(ESClusterOverviewMetricsVO metrics) throws ESOperateException {
        List<BigIndexMetrics> bigIndexMetrics = esClusterNodeService.syncGetBigIndices(metrics.getClusterName());
        metrics.setBigIndices(ConvertUtil.list2List(bigIndexMetrics, BigIndexMetricsVO.class));
    }

    private void getMovingShardsMetrics(ESClusterOverviewMetricsVO metrics) throws ESOperateException {
        List<MovingShardMetrics> movingShardsMetrics = esShardService.syncGetMovingShards(metrics.getClusterName());
        metrics.setMovingShards(ConvertUtil.list2List(movingShardsMetrics, MovingShardMetricsVO.class));
    }

    private void getUnassignedShardsMetrics(ESClusterOverviewMetricsVO metrics) throws ESOperateException {
        List<UnAssignShardMetrics> unAssignShardMetrics = esShardService.syncGetUnAssignShards(metrics.getClusterName());
        metrics.setUnAssignShards(ConvertUtil.list2List(unAssignShardMetrics, UnAssignShardMetricsVO.class));
    }

    private void getPendingTasksMetrics(ESClusterOverviewMetricsVO metrics) throws ESOperateException {
        List<PendingTask> pendingTaskFromES = esClusterNodeService.syncGetPendingTask(metrics.getClusterName());
        metrics.setPendingTasks(ConvertUtil.list2List(pendingTaskFromES, PendingTaskVO.class));
    }

    private void getElapsedTimeMetrics(ESClusterOverviewMetricsVO metrics) {
        ESClusterPhyBasicMetricsVO basic = metrics.getBasic();
        getClusterBasicInfoFutureUtil
            .runnableTask(() -> buildBasicMetricsFromEsClusterNodeInfo(basic, metrics.getClusterName()))
            .runnableTask(() -> buildBasicMetricsFromEsClusterMemInfo(basic, metrics.getClusterName())).waitExecute();
    }

    private void getBasicMetrics(ESClusterOverviewMetricsVO metrics,List<String> itemNamesUnderClusterLogic,String clusterLogic, Integer projectId) {
        ESClusterPhyBasicMetricsVO basic = metrics.getBasic();
        getClusterBasicInfoFutureUtil
            .runnableTask(() -> buildBasicMetricsFromClusterStats(basic, metrics.getClusterName(),itemNamesUnderClusterLogic,clusterLogic,projectId))
            .runnableTask(() -> buildBasicMetricsFromEsClusterTemplate(basic, clusterLogic,projectId))
            .runnableTask(() -> buildBasicMetricsFromEsClusterNodeInfo(basic, metrics.getClusterName()))
            .runnableTask(() -> buildBasicMetricsFromEsClusterMemInfo(basic, metrics.getClusterName())).waitExecute();
    }

    /**
     * 设置集群总览视图中内存的基础信息，兼容2.3.3低版本的es集群
     * @param basicVO 集群总览基础视图
     * @param clusterName  物理集群名称
     */
    private void buildBasicMetricsFromEsClusterMemInfo(ESClusterPhyBasicMetricsVO basicVO, String clusterName) {
        ClusterMemInfo clusterMemInfo = esClusterNodeService.synGetClusterMem(clusterName);
        if (AriusObjUtils.isNull(clusterMemInfo)) {
            LOGGER.warn(
                "class=ClusterLogicOverviewMetricsHandle||method=buildBasicMetricsFromEsClusterMemInfo||mem info is empty");
            return;
        }

        // 设置内存信息
        basicVO.setMemTotal(clusterMemInfo.getMemTotal());
        basicVO.setMemFree(clusterMemInfo.getMemFree());
        basicVO.setMemUsed(clusterMemInfo.getMemUsed());
        basicVO.setMemFreePercent(clusterMemInfo.getMemFreePercent());
        basicVO.setMemUsedPercent(clusterMemInfo.getMemUsedPercent());
    }

    /**
     * 构建集群基本指标信息
     * @param basicVO
     * @param clusterName
     */
    private void buildBasicMetricsFromClusterStats(ESClusterPhyBasicMetricsVO basicVO, String clusterName,
                                                   List<String> itemNamesUnderClusterLogic,String clusterLogic, Integer projectId) {
        List<ClusterNodeStats> nodeStats = esClusterNodeService.syncGetNodeStats(clusterName);
        Map<String, Long> node2ShardNum = esClusterNodeService.syncGetNode2ShardNumMap(clusterName);
        List<String> indies = esIndexCatService.syncGetIndexListByProjectId(projectId,clusterLogic);
        List<CatIndexResult> catIndexResults = esIndexService.syncCatIndex(clusterName, 3);
        ESClusterStatsResponse clusterStats = esClusterService.syncGetClusterStats(clusterName);

        //shard数
        long shardNum = itemNamesUnderClusterLogic.stream().mapToLong(node2ShardNum::get).filter(Objects::nonNull).sum();
        long indicesStoreSize = catIndexResults.stream().filter(index->indies.contains(index.getIndex())).mapToLong(index->SizeUtil.getUnitSize(index.getStoreSize())).sum();
        long docCount =  catIndexResults.stream().filter(index->indies.contains(index.getIndex())).mapToLong(index->Long.parseLong(index.getDocsCount())).sum();
        //磁盘信息
        long totalInBytes = nodeStats.stream().filter(nodeStat->itemNamesUnderClusterLogic.contains(nodeStat.getName())).mapToLong(this::getTotalInBytes).sum();
        long availableInBytes = nodeStats.stream().filter(nodeStat->itemNamesUnderClusterLogic.contains(nodeStat.getName())).mapToLong(this::getAvailableInBytes).sum();
        long freeInBytes = nodeStats.stream().filter(nodeStat->itemNamesUnderClusterLogic.contains(nodeStat.getName())).mapToLong(this::getFreeInBytes).sum();
        //设置堆内存使用率信息
        long heapUsedInBytes = nodeStats.stream().filter(nodeStat->itemNamesUnderClusterLogic.contains(nodeStat.getName())).mapToLong(this::getHeapUsedInBytes).sum();
        long nonHeapUsedInBytes =  nodeStats.stream().filter(nodeStat->itemNamesUnderClusterLogic.contains(nodeStat.getName())).mapToLong(this::getNonHeapUsedInBytes).sum();

        //设置状态
        basicVO.setStatus(clusterStats.getStatus());
        //设置基础信息
        basicVO.setNumberNodes((long) itemNamesUnderClusterLogic.size());
        basicVO.setTotalIndicesNu((long) indies.size());
        basicVO.setShardNu((long) shardNum);
        basicVO.setTotalDocNu(docCount);
        basicVO.setIndicesStoreSize(indicesStoreSize);
        basicVO.setUnassignedShardNum(clusterStats.getUnassignedShardNum());

        //设置集群磁盘信息
        basicVO.setStoreSize(availableInBytes);
        basicVO.setTotalStoreSize(totalInBytes);
        basicVO.setFreeStoreSize(freeInBytes);
        //保留小数点后3位
        BigDecimal storeSizeDec = new BigDecimal(availableInBytes);
        BigDecimal totalSizeDec = new BigDecimal(totalInBytes);
        basicVO.setStoreUsage(storeSizeDec.divide(totalSizeDec, 5, 1).doubleValue() * 100);
        basicVO.setStoreFreeUsage((100 - basicVO.getStoreUsage()));

        //设置堆内存使用率信息
        long heapTotalSize = nonHeapUsedInBytes + heapUsedInBytes;
        basicVO.setHeapMemFree(nonHeapUsedInBytes);
        basicVO.setHeapMemTotal(heapTotalSize);
        basicVO.setHeapMemUsed(heapUsedInBytes);
        //保留小数点后3位
        BigDecimal storeHeapMemSizeDec = new BigDecimal(heapUsedInBytes);
        BigDecimal totalHeapMemSizeDec = new BigDecimal(heapTotalSize);
        basicVO.setHeapUsage(storeHeapMemSizeDec.divide(totalHeapMemSizeDec, 5, 1).doubleValue() * 100);
        basicVO.setHeapFreeUsage((100 - basicVO.getHeapUsage()));
        //设置集群节点信息
        basicVO.setNumberMasterNodes(clusterStats.getNumberMasterNodes());
        basicVO.setNumberDataNodes((long) itemNamesUnderClusterLogic.size());
        basicVO.setNumberClientNodes(clusterStats.getNumberClientNodes());
        basicVO.setNumberIngestNodes(clusterStats.getNumberIngestNodes());
        basicVO.setNumberCoordinatingOnlyNodes(clusterStats.getNumberCoordinatingOnly());
    }

    /**
     * 设置集群有效、无效节点数
     * @param basicVO
     * @param clusterName
     */
    private void buildBasicMetricsFromEsClusterNodeInfo(ESClusterPhyBasicMetricsVO basicVO, String clusterName) {
        List<String> esHost = esClusterNodeService.syncGetNodeHosts(clusterName);
        long invalidNodeCount = 0;
        long activeNodeCount = 0;
        List<ClusterRoleHost> nodesByCluster = clusterRoleHostService.getNodesByCluster(clusterName);
        Set<String> nodeIps = nodesByCluster.stream().map(ClusterRoleHost::getIp).collect(Collectors.toSet());
        for (String ip : nodeIps) {
            if (!esHost.contains(ip)) {
                invalidNodeCount++;
            } else {
                activeNodeCount++;
            }
        }
        basicVO.setActiveNodeNu(activeNodeCount);
        basicVO.setInvalidNodeNu(invalidNodeCount);
        basicVO.setTotalNodeNu(nodeIps.size());

        BigDecimal activeNodeNu = new BigDecimal(activeNodeCount);
        BigDecimal totalNodeNu = new BigDecimal(nodeIps.size());
        basicVO.setActiveNodeNuPercent(activeNodeNu.divide(totalNodeNu, 3, 1).doubleValue() * 100);
        basicVO.setInvalidNodeNuPercent(100 - basicVO.getActiveNodeNuPercent());
    }

    /**
     * 设置集群真实模板数量
     * @param basicVO
     * @param clusterName
     */
    private void buildBasicMetricsFromEsClusterTemplate(ESClusterPhyBasicMetricsVO basicVO, String clusterName, Integer projectId) {

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByNameAndProjectId(clusterName,projectId);
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        if (Objects.isNull(clusterRegion)){
            basicVO.setTotalTemplateNu(0L);
        }else {
            basicVO.setTotalTemplateNu(Long.valueOf(indexTemplateService.listByRegionId(Math.toIntExact(clusterRegion.getId())).getData().size()));
        }
    }

    /**
     * 获取集群维度分位统计信息
     */
    private void aggPercentilesMetrics(ESClusterOverviewMetricsVO metrics,long clusterType, String clusterMetricsType, String aggType,
                                       Long startTime, Long endTime, Consumer<List<BasePercentileMetrics>> function) {

        List<BasePercentileMetrics> aggPercentilesMetrics = esClusterPhyStatsService
            .getAggPercentilesMetrics(metrics.getClusterName(),clusterType, clusterMetricsType, aggType, startTime, endTime);

        Collections.sort(aggPercentilesMetrics);

        function.accept(aggPercentilesMetrics);
    }

    private void aggNodesForDiskUsageGte75PercentMetrics(ESClusterOverviewMetricsVO metrics) {
        metrics.setNodesForDiskUsageGte75Percent(getNodeInfoForDiskUsageGte75Percent(metrics.getClusterName()));
    }

    private void aggDiskInfoMetrics(ESClusterOverviewMetricsVO metrics,long clusterType, String aggType, Long startTime, Long endTime) {
        List<DiskInfoMetrics> diskInfoMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(),clusterType, aggType, startTime, endTime, DiskInfoMetrics.class);
        List<DiskInfoMetricsVO> diskInfoMetricsVOS = ConvertUtil.list2List(diskInfoMetrics, DiskInfoMetricsVO.class);
        Collections.sort(diskInfoMetricsVOS);
        metrics.setDiskInfo(diskInfoMetricsVOS);
    }

    private void aggShardNuMetrics(ESClusterOverviewMetricsVO metrics,long clusterType, String aggType, Long startTime, Long endTime) {
        List<ShardInfoMetrics> shardInfoMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(),clusterType, aggType, startTime, endTime, ShardInfoMetrics.class);

        List<ShardInfoMetricsVO> shardInfoMetricsVOS = ConvertUtil.list2List(shardInfoMetrics,
            ShardInfoMetricsVO.class);
        Collections.sort(shardInfoMetricsVOS);
        metrics.setShardNu(shardInfoMetricsVOS);
    }

    private void aggTaskCount(ESClusterOverviewMetricsVO metrics,long clusterType, String aggType, Long startTime, Long endTime) {
        List<TaskCountMetrics> taskCountMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(),clusterType, aggType, startTime, endTime, TaskCountMetrics.class);

        List<TaskCountMetricVO> taskCountMetricVOS = ConvertUtil.list2List(taskCountMetrics, TaskCountMetricVO.class);
        Collections.sort(taskCountMetricVOS);
        metrics.setTaskCount(taskCountMetricVOS);
    }

    private void aggWriteTpsMetrics(ESClusterOverviewMetricsVO metrics,long clusterType, String aggType, Long startTime, Long endTime) {
        List<WriteTPSMetrics> writeTPSMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(),clusterType, aggType, startTime, endTime, WriteTPSMetrics.class);

        List<WriteTPSMetricsVO> writeTPSMetricsVOS = ConvertUtil.list2List(writeTPSMetrics, WriteTPSMetricsVO.class);
        Collections.sort(writeTPSMetricsVOS);
        metrics.setWriteTps(writeTPSMetricsVOS);
    }

    private void aggReadTpsMetrics(ESClusterOverviewMetricsVO metrics,long clusterType, String aggType, Long startTime, Long endTime) {
        List<ReadQPSMetrics> readQPSMetrics = esClusterPhyStatsService.getAggClusterPhyMetrics(metrics.getClusterName(),clusterType,
            aggType, startTime, endTime, ReadQPSMetrics.class);

        List<ReadQPSMetricsVO> readQPSMetricsVOS = ConvertUtil.list2List(readQPSMetrics, ReadQPSMetricsVO.class);
        Collections.sort(readQPSMetricsVOS);
        metrics.setReadTps(readQPSMetricsVOS);
    }

    private void aggSendTransMetrics(ESClusterOverviewMetricsVO metrics,long clusterType, String aggType, Long startTime, Long endTime) {
        List<SendTransMetrics> readQPSMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(),clusterType, aggType, startTime, endTime, SendTransMetrics.class);

        List<SendTransMetricsVO> sendTransMetricsVOS = ConvertUtil.list2List(readQPSMetrics, SendTransMetricsVO.class);
        Collections.sort(sendTransMetricsVOS);
        metrics.setSendTransSize(sendTransMetricsVOS);
    }

    private void aggRecvTransMetrics(ESClusterOverviewMetricsVO metrics,long clusterType, String aggType, Long startTime, Long endTime) {
        List<RecvTransMetrics> readQPSMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(),clusterType, aggType, startTime, endTime, RecvTransMetrics.class);

        List<RecvTransMetricsVO> recvTransMetricsVOS = ConvertUtil.list2List(readQPSMetrics, RecvTransMetricsVO.class);
        Collections.sort(recvTransMetricsVOS);
        metrics.setRecvTransSize(recvTransMetricsVOS);
    }

    /**
     * 获取磁盘使用率大于75%的节点名称,所在集群ip
     * @param clusterName
     */
    private List<NodeInfoForDiskUsageGte75PercentVO> getNodeInfoForDiskUsageGte75Percent(String clusterName) {
        Map<String, ClusterNodeStats> clusterNodeStatsMap = esClusterNodeService.syncGetNodeFsStatsMap(clusterName);
        List<NodeInfoForDiskUsageGte75PercentVO> nodeInfoForDiskUsageGte75PercentVOS = Lists.newArrayList();

        //可添加使用率
        clusterNodeStatsMap.values().parallelStream().forEach(nodeStats -> {
            BigDecimal freeInBytesDec = new BigDecimal(nodeStats.getFs().getTotal().getFreeInBytes());
            BigDecimal totalInBytesDec = new BigDecimal(nodeStats.getFs().getTotal().getTotalInBytes());
            double freePercent = freeInBytesDec.divide(totalInBytesDec, 5, 1).doubleValue() * 100;
            if (100 - freePercent >= 75) {
                NodeInfoForDiskUsageGte75PercentVO build = new NodeInfoForDiskUsageGte75PercentVO();
                build.setNodeIp(nodeStats.getHost());
                build.setNodeName(nodeStats.getName());
                build.setValue(100 - freePercent);
                nodeInfoForDiskUsageGte75PercentVOS.add(build);
            }
        });
        //倒序排列
        nodeInfoForDiskUsageGte75PercentVOS.stream()
                .sorted(Comparator.comparing(NodeInfoForDiskUsageGte75PercentVO::getValue))
                .collect(Collectors.toList());

        return nodeInfoForDiskUsageGte75PercentVOS;
    }


    /**
     * > 从集群节点统计中获取以字节为单位使用的非堆内存
     *
     * @param clusterNodeStats 包含 JVM 统计信息的 ClusterNodeStats 对象。
     * @return 以字节为单位使用的非堆内存。
     */
    private long getNonHeapUsedInBytes(ClusterNodeStats clusterNodeStats) {
        return Optional.of(clusterNodeStats).map(ClusterNodeStats::getJvm).map(JvmNode::getMem).map(JvmMem::getNonHeapUsedInBytes).orElse(0L);
    }

    /**
     * > 从集群节点统计中获取堆使用的字节数
     *
     * @param clusterNodeStats 包含 JVM 统计信息的 ClusterNodeStats 对象。
     * @return 以字节为单位使用的堆。
     */
    private long getHeapUsedInBytes(ClusterNodeStats clusterNodeStats) {
        return Optional.of(clusterNodeStats).map(ClusterNodeStats::getJvm).map(JvmNode::getMem).map(JvmMem::getHeapUsedInBytes).orElse(0L);
    }

    /**
     * > 从集群节点统计中获取可用空间（以字节为单位）
     *
     * @param clusterNodeStats 包含节点信息的 ClusterNodeStats 对象。
     * @return 集群节点的可用空间（以字节为单位）。
     */
    private long getFreeInBytes(ClusterNodeStats clusterNodeStats) {
        return Optional.of(clusterNodeStats).map(ClusterNodeStats::getFs).map(FSNode::getTotal).map(FSTotal::getFreeInBytes).orElse(0L);
    }

    /**
     * > 从集群节点统计中获取可用空间（以字节为单位）
     *
     * @param clusterNodeStats 包含节点信息的 ClusterNodeStats 对象。
     * @return 可用空间（以字节为单位）。
     */
    private long getAvailableInBytes(ClusterNodeStats clusterNodeStats) {
        return Optional.of(clusterNodeStats).map(ClusterNodeStats::getFs).map(FSNode::getTotal).map(FSTotal::getAvailableInBytes).orElse(0L);
    }

    /**
     * “获取集群节点统计信息的总字节数，如果集群节点统计信息为空，则为 0。”
     *
     * 该函数比这要复杂一些，但这就是它的要点
     *
     * @param clusterNodeStats 包含节点信息的 ClusterNodeStats 对象。
     * @return 文件系统中的总字节数。
     */
    private long getTotalInBytes(ClusterNodeStats clusterNodeStats) {
        return Optional.of(clusterNodeStats).map(ClusterNodeStats::getFs).map(FSNode::getTotal).map(FSTotal::getTotalInBytes).orElse(0L);
    }
}