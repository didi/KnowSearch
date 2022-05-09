package com.didichuxing.datachannel.arius.admin.task.dashboard.collector;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHostInfo;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsClusterTaskInfoESDAO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.DashBoardStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard.NodeMetrics;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterHostInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.logi.elasticsearch.client.response.model.fs.FSTotal;
import com.didiglobal.logi.elasticsearch.client.response.model.jvm.JvmMem;
import com.didiglobal.logi.elasticsearch.client.response.model.os.OsCpu;
import com.didiglobal.logi.elasticsearch.client.response.model.threadpool.ThreadPoolNode;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created by linyunan on 3/11/22
 *
 * dashboard单个集群节点采集器
 */
@Component
public class NodeDashBoardCollector extends BaseDashboardCollector {
    private static final int                                                NODE_FREE_DISK_THRESHOLD     = 15;
    private static final int                                                HEAD_USED_PERCENT_THRESHOLD  = 80;
    private static final int                                                CPU_PERCENT_THRESHOLD        = 80;

    private static final long                                               LARGE_HEAD_USED_PERCENT_TIME = 10 * 60 * 1000;
    private static final long                                               LARGE_CPU_PERCENT_TIME       = 30 * 60 * 1000;
    private static final ILog                                               LOGGER                       = LogFactory
        .getLog(NodeDashBoardCollector.class);
    private static final Map<String/*cluster@node*/, NodeMetrics>                           nodeName2NodeMetricsMap      = Maps.newConcurrentMap();
    private static final Map<String/*cluster@node*/, Tuple<Long/*最新采集时间*/, Long/*超过红线的堆内存利用率*/>> nodeName2LargeHeadUsedPerTupleMap = Maps.newConcurrentMap();
    private static final Map<String/*cluster@node*/, Tuple<Long/*最新采集时间*/, Long/*超过红线的cpu利用率*/>> nodeName2LargeCpuUsedPerTupleMap  = Maps.newConcurrentMap();
    private static final Map<String/*cluster@node*/, Long/*write-reject-num*/>  nodeName2WriteRejectNumMap   = Maps.newConcurrentMap();
    private static final Map<String/*cluster@node*/, Long/*search-reject-num*/> nodeName2SearchRejectNumMap  = Maps.newConcurrentMap();

    @Autowired
    protected ESClusterNodeService                                          esClusterNodeService;

    @Autowired
    protected RoleClusterHostInfoService roleClusterHostInfoService;

    @Autowired
    protected AriusStatsClusterTaskInfoESDAO                                ariusStatsClusterTaskInfoESDAO;

    private static final FutureUtil futureUtil                              = FutureUtil.init("NodeDashBoardCollector",10,10,100);

    @Override
    public void collectSingleCluster(String cluster, long currentTime) {
        List<RoleClusterHostInfo> roleClusterHostInfoList = roleClusterHostInfoService.getNodesByCluster(cluster);
        if (CollectionUtils.isEmpty(roleClusterHostInfoList)) { return;}

        AtomicReference<Map<String, ClusterNodeStats>> clusterNodeStatsMapAtomic       = new AtomicReference<>(Maps.newHashMap());
        AtomicReference<Map<String, Long>>             node2ShardNumMapAtomic          = new AtomicReference<>(Maps.newHashMap());
        AtomicReference<Map<String, Double>>           clusterNodesTaskTotalCostAtomic = new AtomicReference<>(Maps.newHashMap());
        futureUtil
                // 注意这里单集群节点比较多会比较慢
                .runnableTask(() -> clusterNodeStatsMapAtomic.set(esClusterNodeService.syncGetNodePartStatsMap(cluster)))
                .runnableTask(() -> node2ShardNumMapAtomic.set(esClusterNodeService.syncGetNode2ShardNumMap(cluster)))
                .runnableTask(() -> clusterNodesTaskTotalCostAtomic.set(ariusStatsClusterTaskInfoESDAO.getClusterNodesTaskTotalCost(cluster)))
                .waitExecute();

        if (MapUtils.isEmpty(clusterNodeStatsMapAtomic.get())) {
            LOGGER.error("class=NodeDashBoardCollector||method=collectSingleCluster||errMsg=clusterNodeStatsMap is null");
        }

        List<ClusterNodeStats> clusterNodeStatsList = Lists.newArrayList(clusterNodeStatsMapAtomic.get().values());
        Map<String, ClusterNodeStats> name2NodeStatsMap = ConvertUtil.list2Map(clusterNodeStatsList,
                ClusterNodeStats::getName);

        if (MapUtils.isEmpty(node2ShardNumMapAtomic.get())) {
            LOGGER.error("class=NodeDashBoardCollector||method=collectSingleCluster||errMsg=clusterNodeStatsMap is null");
        }

        if (MapUtils.isEmpty(clusterNodesTaskTotalCostAtomic.get())) {
            LOGGER.error("class=NodeDashBoardCollector||method=collectSingleCluster||errMsg=clusterNodesTaskTotalCost is null");
        }

        List<DashBoardStats> dashBoardStatsList = Lists.newArrayList();
        for (RoleClusterHostInfo roleClusterHostInfo : roleClusterHostInfoList) {
            DashBoardStats dashBoardStats = buildInitDashBoardStats(currentTime);
            String nodeName = roleClusterHostInfo.getNodeSet();

            String uniqueNodeKey    = CommonUtils.getUniqueKey(cluster, nodeName);
            NodeMetrics nodeMetrics = nodeName2NodeMetricsMap.getOrDefault(uniqueNodeKey, new NodeMetrics());
            nodeMetrics.setTimestamp(currentTime);
            nodeMetrics.setCluster(cluster);
            nodeMetrics.setNode(nodeName);

            ClusterNodeStats clusterNodeStats = name2NodeStatsMap.get(nodeName);
            // 1. 是否Dead节点
            buildDeadInfo(nodeMetrics, clusterNodeStats);
            // 2. 是否磁盘利用率超红线 （阈值85%）
            buildLargeDiskUsageInfo(nodeMetrics, clusterNodeStats);
            // 3. 是否堆内存利用率超红线 （阈值80% 且持续10分钟）
            buildLargeHead(nodeMetrics, clusterNodeStats, uniqueNodeKey);
            // 4. 是否CPU利用率超红线 （80%  持续30分钟）
            buildLargeCpuUsage(nodeMetrics, clusterNodeStats, uniqueNodeKey);
            // 5. 节点shard个数
            nodeMetrics.setShardNum(node2ShardNumMapAtomic.get().getOrDefault(nodeName, 0L));
            // 6. WriteRejected数
            buildWriteRejectedNum(nodeMetrics, clusterNodeStats, uniqueNodeKey);
            // 7. SearchRejected数
            buildSearchRejectedNum(nodeMetrics, clusterNodeStats, uniqueNodeKey);
            //8. 节点执行任务耗时
            nodeMetrics.setTaskConsuming(clusterNodesTaskTotalCostAtomic.get().getOrDefault(nodeName, 0d).longValue());

            // 设置dashboard中节点维度指标数据
            dashBoardStats.setNode(nodeMetrics);

            dashBoardStatsList.add(dashBoardStats);

            // 暂存当前节点指标信息 针对特殊场景，即节点掉线后, 当前的策略是会使用上一次采集到的数据
            nodeName2NodeMetricsMap.put(uniqueNodeKey, nodeMetrics);
        }

        if (CollectionUtils.isEmpty(dashBoardStatsList)) { return;}

        monitorMetricsSender.sendDashboardStats(dashBoardStatsList);
    }

    @Override
    public void collectAllCluster(List<String> clusterList, long currentTime) {

    }

    @Override
    public String getName() { return "NodeDashBoardCollector";}

    /****************************************************private********************************************************/
    private void buildSearchRejectedNum(NodeMetrics nodeMetrics, ClusterNodeStats clusterNodeStats, String uniqueNodeKey) {
        if (null == clusterNodeStats) { return;}
        if (null == clusterNodeStats.getThreadPool()) { return;}
        if (null == clusterNodeStats.getThreadPool().getSearch()) { return;}

        ThreadPoolNode search = clusterNodeStats.getThreadPool().getSearch();
        // get diff
        long diff = search.getRejected() - nodeName2SearchRejectNumMap.getOrDefault(uniqueNodeKey, 0L);
        nodeMetrics.setSearchRejectedNum(diff <= 0 ? 0 : diff);
        // update node search reject
        nodeName2SearchRejectNumMap.put(uniqueNodeKey, search.getRejected());
    }

    private void buildWriteRejectedNum(NodeMetrics nodeMetrics, ClusterNodeStats clusterNodeStats, String uniqueNodeKey) {
        if (null == clusterNodeStats) { return;}
        if (null == clusterNodeStats.getThreadPool()) { return;}
        if (null == clusterNodeStats.getThreadPool().getWrite()) { return;}
        ThreadPoolNode write = clusterNodeStats.getThreadPool().getWrite();
        // get diff
        long diff = write.getRejected() - nodeName2WriteRejectNumMap.getOrDefault(uniqueNodeKey, 0L);
        nodeMetrics.setWriteRejectedNum(diff <= 0 ? 0 : diff);
        // update node write reject
        nodeName2WriteRejectNumMap.put(uniqueNodeKey, write.getRejected());
    }

    private void buildLargeCpuUsage(NodeMetrics nodeMetrics, ClusterNodeStats clusterNodeStats, String uniqueNodeKey) {
        // 如果节点掉线不去设置，保留上一次采集到的disk info
        if (null == clusterNodeStats) { return;}
        if (null == clusterNodeStats.getOs()) { return;}
        if (null == clusterNodeStats.getOs().getCpu()) { return;}

        OsCpu cpu    = clusterNodeStats.getOs().getCpu();
        long  cpuPer = cpu.getPercent();

        // cpuPer大于上限, save to nodeName2LargeCpuUsedPerTupleMap, 若连续出现大于上限, 仅保留第一次超过上限的时间点
        if (cpuPer >= CPU_PERCENT_THRESHOLD) {
            // 连续出现大于上限, tuple中时间保持不变
            nodeName2LargeCpuUsedPerTupleMap.put(uniqueNodeKey,
                    nodeName2LargeCpuUsedPerTupleMap.getOrDefault(uniqueNodeKey,
                            new Tuple<>(System.currentTimeMillis(), cpuPer)).setV2(cpuPer));
        }else { nodeName2LargeCpuUsedPerTupleMap.remove(uniqueNodeKey);}

        Tuple<Long, Long> nodeName2LargeCpuUsedPerTupleFromMap = nodeName2LargeCpuUsedPerTupleMap.get(uniqueNodeKey);
        if (null == nodeName2LargeCpuUsedPerTupleFromMap) { nodeMetrics.setLargeCpuUsage(0D);}
        else {
            long interval = System.currentTimeMillis() - nodeName2LargeCpuUsedPerTupleFromMap.getV1();
            if (interval >= LARGE_CPU_PERCENT_TIME) {
                nodeMetrics.setLargeCpuUsage((double) cpuPer);
            } else {
                nodeMetrics.setLargeCpuUsage(-1D);
            }
        }

    }

    private void buildLargeHead(NodeMetrics nodeMetrics, ClusterNodeStats clusterNodeStats, String uniqueNodeKey) {
        // 如果节点掉线不去设置，保留上一次采集到的head info
        if (null == clusterNodeStats) { return;}
        if (null == clusterNodeStats.getJvm()) { return;}
        if (null == clusterNodeStats.getJvm().getMem()) { return;}
        if (null == clusterNodeStats.getJvm().getMem()) { return;}

        JvmMem jvmMem          = clusterNodeStats.getJvm().getMem();
        long   heapUsedPercent = jvmMem.getHeapUsedPercent();
        
        // heapUsedPercent大于上限, save to nodeName2LargeHeadUsedPerTupleMap, 若连续出现大于上限, 仅保留第一次超过上限的时间点
        if (heapUsedPercent >= HEAD_USED_PERCENT_THRESHOLD) {
            // 连续出现大于上限, tuple中时间保持不变
            nodeName2LargeHeadUsedPerTupleMap.put(uniqueNodeKey, nodeName2LargeHeadUsedPerTupleMap
                .getOrDefault(uniqueNodeKey, new Tuple<>(System.currentTimeMillis(), heapUsedPercent)).setV2(heapUsedPercent));
        }else { nodeName2LargeHeadUsedPerTupleMap.remove(uniqueNodeKey);}

        Tuple<Long, Long> time2HeadUsedPerTupleFromMap = nodeName2LargeHeadUsedPerTupleMap.get(uniqueNodeKey);
        if (null == time2HeadUsedPerTupleFromMap) { nodeMetrics.setLargeHead(0D);}
        else {
            long interval = System.currentTimeMillis() - time2HeadUsedPerTupleFromMap.getV1();
            if (interval >= LARGE_HEAD_USED_PERCENT_TIME) {
                nodeMetrics.setLargeHead((double) heapUsedPercent);
            } else {
                nodeMetrics.setLargeHead(-1D);
            }
        }
    }

    private void buildDeadInfo(NodeMetrics nodeMetrics, ClusterNodeStats clusterNodeStats) {
        nodeMetrics.setDead(null == clusterNodeStats);
    }

    private void buildLargeDiskUsageInfo(NodeMetrics nodeMetrics, ClusterNodeStats clusterNodeStats) {
        // 如果节点掉线不去设置，保留上一次采集到的disk info
        if (null == clusterNodeStats) { return;}
        if (null == clusterNodeStats.getFs()) { return;}
        if (null == clusterNodeStats.getFs().getTotal()) { return;}

        FSTotal total = clusterNodeStats.getFs().getTotal();
        double freeDiskPer = CommonUtils.divideDoubleAndFormatDouble(total.getFreeInBytes(), total.getTotalInBytes(), 5,
            100);
        if (freeDiskPer <= NODE_FREE_DISK_THRESHOLD) {
            nodeMetrics.setLargeDiskUsage(100 - freeDiskPer);
        } else {
            nodeMetrics.setLargeDiskUsage(-1D);
        }
    }
}
