package com.didichuxing.datachannel.arius.admin.biz.metrics.handle;

import com.didichuxing.datachannel.arius.admin.biz.component.MetricsValueConvertUtils;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.percentiles.BasePercentileMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStatsResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESShardService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterPhyStatsService;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.PHY_CLUSTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyClusterMetricsEnum.*;

/**
 * Created by linyunan on 2021-08-02
 */
@Component
public class ClusterOverviewMetricsHandle {

    private static final ILog             LOGGER                        = LogFactory
        .getLog(ClusterOverviewMetricsHandle.class);

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
    public ESClusterOverviewMetricsVO buildClusterPhyOverviewMetrics(MetricsClusterPhyDTO metricsClusterPhyDTO) {

        //1. building base objects
        ESClusterOverviewMetricsVO esClusterOverviewMetricsVO = initESClusterPhyOverviewMetricsVO(metricsClusterPhyDTO);

        //2. 从ES中获取指标, 同时获取多个
        for (String metricsType : metricsClusterPhyDTO.getMetricsTypes()) {
            getMultipleMetricFutureUtil.runnableTask(() -> aggClusterPhyOverviewMetrics(esClusterOverviewMetricsVO,
                metricsType, metricsClusterPhyDTO.getAggType(), metricsClusterPhyDTO.getStartTime(),
                metricsClusterPhyDTO.getEndTime()));
        }
        getMultipleMetricFutureUtil.waitExecute();

        //3. uniform percentage unit
        MetricsValueConvertUtils.convertClusterOverviewMetricsPercent(esClusterOverviewMetricsVO);

        //4. optimize query burr
        optimizeQueryBurrForClusterOverviewMetrics(esClusterOverviewMetricsVO);
        return esClusterOverviewMetricsVO;
    }

    /******************************************private*******************************************************/

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
        return esClusterOverviewMetricsVO;
    }

    private void aggClusterPhyOverviewMetrics(ESClusterOverviewMetricsVO metrics, String metricsType, String aggType,
                                              Long startTime, Long endTime) {
        try {
            switch (ClusterPhyClusterMetricsEnum.valueOfType(metricsType)) {
                /*************************基本状态指标(状态类型)***************************/
                case BASIC:
                    getBasicMetrics(metrics);
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
                    aggPercentilesMetrics(metrics, CPU_USAGE.getType(), aggType, startTime, endTime,
                        b -> metrics.setCpuUsage(ConvertUtil.list2List(b, CpuUsageMetricsVO.class)));
                    return;
                case CPU_LOAD_1M:
                    aggPercentilesMetrics(metrics, CPU_LOAD_1M.getType(), aggType, startTime, endTime,
                        b -> metrics.setCpuLoad1M(ConvertUtil.list2List(b, CpuLoadFor1MinMetricsVO.class)));
                    return;
                case CPU_LOAD_5M:
                    aggPercentilesMetrics(metrics, CPU_LOAD_5M.getType(), aggType, startTime, endTime,
                        b -> metrics.setCpuLoad5M(ConvertUtil.list2List(b, CpuLoadFor5MinMetricsVO.class)));
                    return;
                case CPU_LOAD_15M:
                    aggPercentilesMetrics(metrics, CPU_LOAD_15M.getType(), aggType, startTime, endTime,
                        b -> metrics.setCpuLoad15M(ConvertUtil.list2List(b, CpuLoadFor15MinMetricsVO.class)));
                    return;
                case DISK_USAGE:
                    aggPercentilesMetrics(metrics, DISK_USAGE.getType(), aggType, startTime, endTime,
                        b -> metrics.setDiskUsage(ConvertUtil.list2List(b, DiskUsageMetricsVO.class)));
                    return;
                case SEARCH_LATENCY:
                    aggPercentilesMetrics(metrics, SEARCH_LATENCY.getType(), aggType, startTime, endTime,
                        b -> metrics.setSearchLatency(ConvertUtil.list2List(b, SearchLatencyMetricsVO.class)));
                    return;
                case INDEXING_LATENCY:
                    aggPercentilesMetrics(metrics, INDEXING_LATENCY.getType(), aggType, startTime, endTime,
                        b -> metrics.setIndexingLatency(ConvertUtil.list2List(b, IndexingLatencyMetricsVO.class)));
                    return;

                case TASK_COST:
                    aggPercentilesMetrics(metrics, TASK_COST.getType(), aggType, startTime, endTime,
                        b -> metrics.setTaskCost(ConvertUtil.list2List(b, TaskCostMetricVO.class)));
                    return;

                /********************************普通指标(折线图)*******************************/
                case DISK_INFO:
                    aggDiskInfoMetrics(metrics, aggType, startTime, endTime);
                    return;
                case SHARD_NUM:
                    aggShardNuMetrics(metrics, aggType, startTime, endTime);
                    return;
                case READ_QPS:
                    aggReadTpsMetrics(metrics, aggType, startTime, endTime);
                    return;
                case WRITE_TPS:
                    aggWriteTpsMetrics(metrics, aggType, startTime, endTime);
                    return;
                case RECV_TRANS_SIZE:
                    aggRecvTransMetrics(metrics, aggType, startTime, endTime);
                    return;
                case SEND_TRANS_SIZE:
                    aggSendTransMetrics(metrics, aggType, startTime, endTime);
                    return;
                case NODES_FOR_DISK_USAGE_GTE_75PERCENT:
                    aggNodesForDiskUsageGte75PercentMetrics(metrics);
                    return;
                case TASK_NUM:
                    aggTaskCount(metrics, aggType, startTime, endTime);
                    return;
                default:
            }
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyOverviewMetricsHandle||method=aggClusterPhyOverviewMetrics||errMsg={}",
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

    private void getBigShardsMetrics(ESClusterOverviewMetricsVO metrics) {
        List<ShardMetrics> shardMetrics = esShardService.syncGetBigShards(metrics.getClusterName());
        metrics.setBigShards(ConvertUtil.list2List(shardMetrics, BigShardMetricsVO.class));
    }

    private void getBigIndicesMetrics(ESClusterOverviewMetricsVO metrics) {
        List<BigIndexMetrics> bigIndexMetrics = esClusterNodeService.syncGetBigIndices(metrics.getClusterName());
        metrics.setBigIndices(ConvertUtil.list2List(bigIndexMetrics, BigIndexMetricsVO.class));
    }

    private void getMovingShardsMetrics(ESClusterOverviewMetricsVO metrics) {
        List<MovingShardMetrics> movingShardsMetrics = esShardService.syncGetMovingShards(metrics.getClusterName());
        metrics.setMovingShards(ConvertUtil.list2List(movingShardsMetrics, MovingShardMetricsVO.class));
    }

    private void getPendingTasksMetrics(ESClusterOverviewMetricsVO metrics) {
        List<PendingTask> pendingTaskFromES = esClusterNodeService.syncGetPendingTask(metrics.getClusterName());
        metrics.setPendingTasks(ConvertUtil.list2List(pendingTaskFromES, PendingTaskVO.class));
    }

    private void getElapsedTimeMetrics(ESClusterOverviewMetricsVO metrics) {
        ESClusterPhyBasicMetricsVO basic = metrics.getBasic();
        getClusterBasicInfoFutureUtil
            .runnableTask(() -> buildBasicMetricsFromClusterStats(basic, metrics.getClusterName()))
            .runnableTask(() -> buildBasicMetricsFromEsClusterTemplate(basic, metrics.getClusterName()))
            .runnableTask(() -> buildBasicMetricsFromEsClusterNodeInfo(basic, metrics.getClusterName()))
            .runnableTask(() -> buildBasicMetricsFromEsClusterMemInfo(basic, metrics.getClusterName())).waitExecute();
    }

    private void getBasicMetrics(ESClusterOverviewMetricsVO metrics) {
        ESClusterPhyBasicMetricsVO basic = metrics.getBasic();
        getClusterBasicInfoFutureUtil
            .runnableTask(() -> buildBasicMetricsFromClusterStats(basic, metrics.getClusterName()))
            .runnableTask(() -> buildBasicMetricsFromEsClusterTemplate(basic, metrics.getClusterName()))
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
                "class=ClusterPhyOverviewMetricsHandle||method=buildBasicMetricsFromEsClusterMemInfo||mem info is empty");
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
    private void buildBasicMetricsFromClusterStats(ESClusterPhyBasicMetricsVO basicVO, String clusterName) {
        ESClusterStatsResponse clusterStats = esClusterService.syncGetClusterStats(clusterName);
        if (null == clusterStats) {
            return;
        }

        //设置状态
        basicVO.setStatus(clusterStats.getStatus());

        //设置基础信息
        basicVO.setNumberNodes(clusterStats.getTotalNodes());
        basicVO.setTotalIndicesNu(clusterStats.getIndexCount());
        basicVO.setShardNu(clusterStats.getTotalShard());
        basicVO.setTotalDocNu(clusterStats.getDocsCount());

        //设置集群磁盘信息
        long storeSize = clusterStats.getTotalFs().getBytes() - clusterStats.getFreeFs().getBytes();
        basicVO.setStoreSize(storeSize);
        basicVO.setTotalStoreSize(clusterStats.getTotalFs().getBytes());
        basicVO.setFreeStoreSize(clusterStats.getFreeFs().getBytes());
        //保留小数点后3位
        BigDecimal storeSizeDec = new BigDecimal(storeSize);
        BigDecimal totalSizeDec = new BigDecimal(clusterStats.getTotalFs().getBytes());
        basicVO.setStoreUsage(storeSizeDec.divide(totalSizeDec, 5, 1).doubleValue() * 100);
        basicVO.setStoreFreeUsage((100 - basicVO.getStoreUsage()));

        //设置堆内存使用率信息
        long heapFreeSize = clusterStats.getTotalHeapMem().getBytes() - clusterStats.getUsedHeapMem().getBytes();
        basicVO.setHeapMemFree(heapFreeSize);
        basicVO.setHeapMemTotal(clusterStats.getTotalHeapMem().getBytes());
        basicVO.setHeapMemUsed(clusterStats.getUsedHeapMem().getBytes());
        //保留小数点后3位
        BigDecimal storeHeapMemSizeDec = new BigDecimal(clusterStats.getUsedHeapMem().getBytes());
        BigDecimal totalHeapMemSizeDec = new BigDecimal(clusterStats.getTotalHeapMem().getBytes());
        basicVO.setHeapUsage(storeHeapMemSizeDec.divide(totalHeapMemSizeDec, 5, 1).doubleValue() * 100);
        basicVO.setHeapFreeUsage((100 - basicVO.getHeapUsage()));

        //设置集群节点信息
        basicVO.setNumberMasterNodes(clusterStats.getNumberMasterNodes());
        basicVO.setNumberDataNodes(clusterStats.getNumberDataNodes());
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
    private void buildBasicMetricsFromEsClusterTemplate(ESClusterPhyBasicMetricsVO basicVO, String clusterName) {
        basicVO.setTotalTemplateNu(esTemplateService.synGetTemplateNumForAllVersion(clusterName));
    }

    /**
     * 获取集群维度分位统计信息
     */
    private void aggPercentilesMetrics(ESClusterOverviewMetricsVO metrics, String clusterMetricsType, String aggType,
                                       Long startTime, Long endTime, Consumer<List<BasePercentileMetrics>> function) {

        List<BasePercentileMetrics> aggPercentilesMetrics = esClusterPhyStatsService
            .getAggPercentilesMetrics(metrics.getClusterName(), clusterMetricsType, aggType, startTime, endTime);

        Collections.sort(aggPercentilesMetrics);

        function.accept(aggPercentilesMetrics);
    }

    private void aggNodesForDiskUsageGte75PercentMetrics(ESClusterOverviewMetricsVO metrics) {
        metrics.setNodesForDiskUsageGte75Percent(getNodeInfoForDiskUsageGte75Percent(metrics.getClusterName()));
    }

    private void aggDiskInfoMetrics(ESClusterOverviewMetricsVO metrics, String aggType, Long startTime, Long endTime) {
        List<DiskInfoMetrics> diskInfoMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(), aggType, startTime, endTime, DiskInfoMetrics.class);
        List<DiskInfoMetricsVO> diskInfoMetricsVOS = ConvertUtil.list2List(diskInfoMetrics, DiskInfoMetricsVO.class);
        Collections.sort(diskInfoMetricsVOS);
        metrics.setDiskInfo(diskInfoMetricsVOS);
    }

    private void aggShardNuMetrics(ESClusterOverviewMetricsVO metrics, String aggType, Long startTime, Long endTime) {
        List<ShardInfoMetrics> shardInfoMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(), aggType, startTime, endTime, ShardInfoMetrics.class);

        List<ShardInfoMetricsVO> shardInfoMetricsVOS = ConvertUtil.list2List(shardInfoMetrics,
            ShardInfoMetricsVO.class);
        Collections.sort(shardInfoMetricsVOS);
        metrics.setShardNu(shardInfoMetricsVOS);
    }

    private void aggTaskCount(ESClusterOverviewMetricsVO metrics, String aggType, Long startTime, Long endTime) {
        List<TaskCountMetrics> taskCountMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(), aggType, startTime, endTime, TaskCountMetrics.class);

        List<TaskCountMetricVO> taskCountMetricVOS = ConvertUtil.list2List(taskCountMetrics, TaskCountMetricVO.class);
        Collections.sort(taskCountMetricVOS);
        metrics.setTaskCount(taskCountMetricVOS);
    }

    private void aggWriteTpsMetrics(ESClusterOverviewMetricsVO metrics, String aggType, Long startTime, Long endTime) {
        List<WriteTPSMetrics> writeTPSMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(), aggType, startTime, endTime, WriteTPSMetrics.class);

        List<WriteTPSMetricsVO> writeTPSMetricsVOS = ConvertUtil.list2List(writeTPSMetrics, WriteTPSMetricsVO.class);
        Collections.sort(writeTPSMetricsVOS);
        metrics.setWriteTps(writeTPSMetricsVOS);
    }

    private void aggReadTpsMetrics(ESClusterOverviewMetricsVO metrics, String aggType, Long startTime, Long endTime) {
        List<ReadQPSMetrics> readQPSMetrics = esClusterPhyStatsService.getAggClusterPhyMetrics(metrics.getClusterName(),
            aggType, startTime, endTime, ReadQPSMetrics.class);

        List<ReadQPSMetricsVO> readQPSMetricsVOS = ConvertUtil.list2List(readQPSMetrics, ReadQPSMetricsVO.class);
        Collections.sort(readQPSMetricsVOS);
        metrics.setReadTps(readQPSMetricsVOS);
    }

    private void aggSendTransMetrics(ESClusterOverviewMetricsVO metrics, String aggType, Long startTime, Long endTime) {
        List<SendTransMetrics> readQPSMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(), aggType, startTime, endTime, SendTransMetrics.class);

        List<SendTransMetricsVO> sendTransMetricsVOS = ConvertUtil.list2List(readQPSMetrics, SendTransMetricsVO.class);
        Collections.sort(sendTransMetricsVOS);
        metrics.setSendTransSize(sendTransMetricsVOS);
    }

    private void aggRecvTransMetrics(ESClusterOverviewMetricsVO metrics, String aggType, Long startTime, Long endTime) {
        List<RecvTransMetrics> readQPSMetrics = esClusterPhyStatsService
            .getAggClusterPhyMetrics(metrics.getClusterName(), aggType, startTime, endTime, RecvTransMetrics.class);

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

        clusterNodeStatsMap.values().parallelStream().forEach(nodeStats -> {
            BigDecimal freeInBytesDec = new BigDecimal(nodeStats.getFs().getTotal().getFreeInBytes());
            BigDecimal totalInBytesDec = new BigDecimal(nodeStats.getFs().getTotal().getTotalInBytes());
            double freePercent = freeInBytesDec.divide(totalInBytesDec, 5, 1).doubleValue() * 100;
            if (100 - freePercent >= 75) {
                NodeInfoForDiskUsageGte75PercentVO build = new NodeInfoForDiskUsageGte75PercentVO();
                build.setNodeIp(nodeStats.getHost());
                build.setNodeName(nodeStats.getName());
                nodeInfoForDiskUsageGte75PercentVOS.add(build);
            }
        });

        return nodeInfoForDiskUsageGte75PercentVOS;
    }
}
