package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESNodeStats;
import com.didichuxing.datachannel.arius.admin.common.bean.po.stats.NodeRackStatisPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AriusStatsNodeInfoESDAO extends BaseAriusStatsESDAO {

    @PostConstruct
    public void init(){
        super.indexName   = dataCentreUtil.getAriusStatsNodeInfo();

        BaseAriusStatsESDAO.register( AriusStatsEnum.NODE_INFO,this);
    }

    /**
     * 根据集群名称，获取集群[now-2m, now-1m]总的接收的流量
     * @param cluster
     * @return
     */
    public Double getClusterRx(String cluster) {
        String dsl       = dslLoaderUtil.getFormatDslByFileName( DslsConstant.GET_CLUSTER_REAL_TIME_RX_TX_INFO,
                cluster, "now-2m", "now-1m", METRICS_TRANS_RX);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
                s -> getSumFromESQueryResponse(s, "sum"), 3);
    }

    /**
     * 根据集群名称，获取集群[now-2m, now-1m]总的发送的流量
     * @param cluster
     * @return
     */
    public Double getClusterTx(String cluster) {
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_REAL_TIME_RX_TX_INFO,
                cluster, "now-2m", "now-1m", METRICS_TRANS_TX);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
                s -> getSumFromESQueryResponse(s, "sum"), 3);
    }

    /**
     * 根据集群名称，获取集群[now-2m, now-1m]  cpu平均使用率
     * @param cluster
     * @return
     */
    public Double getClusterCpuAvg(String cluster) {
        String dsl       = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_CLUSTER_REAL_TIME_CPU_AVG_INFO,
                cluster, "now-2m", "now-1m", OS_CPU);
        String realIndex = IndexNameUtils.genCurrentDailyIndexName(indexName);

        return gatewayClient.performRequest(realIndex, TYPE, dsl,
                s -> getSumFromESQueryResponse(s, "avg"), 3);
    }

    /**
     * 根据集群和rack信息获取rack相关的统计信息
     * @param clusterName
     * @param rackList
     * @return
     */
    public List<NodeRackStatisPO> getRackStatis(String clusterName, List<String> rackList) {
        Map<String/*rackName*/, NodeRackStatisPO> nodeRackStatisMap = Maps.newHashMap();
        NodeRackStatisPO nodeRackStatisPO = null;

        // 由于近15分钟存在跨天情况，需要获取最近2天对应索引名称
        String indexNames = genIndexNames(2);
        String rackFormat = CommonUtils.strConcat(rackList);

        int minuteSpan = 15;

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.AGG_RECENT_NODE_METRICS_BY_CLUSTER, clusterName, rackFormat, minuteSpan);

        ESQueryResponse esQueryResponse = gatewayClient.performRequest(indexNames, TYPE, dsl);
        if (esQueryResponse != null && esQueryResponse.getAggs() != null) {
            Map<String, ESAggr> esAggrMap = esQueryResponse.getAggs().getEsAggrMap();
            if (esAggrMap != null && esAggrMap.containsKey("minute_bucket")) {
                ESAggr minuteBucketESAggr = esAggrMap.get("minute_bucket");

                if (minuteBucketESAggr != null && CollectionUtils.isNotEmpty(minuteBucketESAggr.getBucketList())) {
                    for (ESBucket esBucket : minuteBucketESAggr.getBucketList()) {
                        ESAggr groupByRackAggr = esBucket.getAggrMap().get("groupByRack");

                        if (groupByRackAggr != null && CollectionUtils.isNotEmpty(groupByRackAggr.getBucketList())) {
                            for (ESBucket rackBucket : groupByRackAggr.getBucketList()) {
                                ESAggr sumFreeDiskAggr  = rackBucket.getAggrMap().get("sumFreeDisk");
                                ESAggr sumTotalDiskAggr = rackBucket.getAggrMap().get("sumTotalDisk");
                                ESAggr avgCpuUsageAggr  = rackBucket.getAggrMap().get("avgCpuUsage");
                                ESAggr docsCountAggr    = rackBucket.getAggrMap().get("docsCount");
                                String rackName = rackBucket.getUnusedMap().get("key").toString();

                                if (!nodeRackStatisMap.containsKey(rackName)) {
                                    nodeRackStatisPO = new NodeRackStatisPO(clusterName, rackName, 0.0, 0.0, 0, 0d, 0);

                                    if (sumTotalDiskAggr != null && sumTotalDiskAggr.getUnusedMap().containsKey("value") && sumTotalDiskAggr.getUnusedMap().get("value") != null) {
                                        nodeRackStatisPO.setTotalDiskG(Double.valueOf(sumTotalDiskAggr.getUnusedMap().get("value").toString()) / ONE_GB);
                                    }
                                    if (sumFreeDiskAggr != null && sumFreeDiskAggr.getUnusedMap().containsKey("value") && sumFreeDiskAggr.getUnusedMap().get("value") != null) {
                                        nodeRackStatisPO.setDiskFreeG(Double.valueOf(sumFreeDiskAggr.getUnusedMap().get("value").toString()) / ONE_GB);
                                    }
                                    if (avgCpuUsageAggr != null && avgCpuUsageAggr.getUnusedMap().containsKey("value") && avgCpuUsageAggr.getUnusedMap().get("value") != null) {
                                        nodeRackStatisPO.setCpuUsedPercent(Double.valueOf(avgCpuUsageAggr.getUnusedMap().get("value").toString()));
                                    }
                                    if (docsCountAggr != null && docsCountAggr.getUnusedMap().containsKey("value") && docsCountAggr.getUnusedMap().get("value") != null) {
                                        nodeRackStatisPO.setDocNu(Double.valueOf(docsCountAggr.getUnusedMap().get("value").toString()).longValue());
                                    }

                                    nodeRackStatisMap.put(rackName, nodeRackStatisPO);
                                }
                            }
                        }
                    }
                }
            }
        }

        List<NodeRackStatisPO> nodeRackStatisPOS = Lists.newLinkedList();
        for (String rack : rackList) {
            if (nodeRackStatisMap.containsKey(rack)) {
                nodeRackStatisPOS.add(nodeRackStatisMap.get(rack));
            } else {
                LOGGER.warn("class=AriusStatsNodeInfoEsDao||method=getRackStatis||msg={} {} set default value", clusterName, rack);
                nodeRackStatisPO = new NodeRackStatisPO(clusterName, rack, 0.0, 0.0, 0, 0d, 0);

                nodeRackStatisPOS.add(nodeRackStatisPO);
            }
        }

        return nodeRackStatisPOS;
    }

    /**
     * 获取所有集群节点的物理存储空间大小
     * @return
     */
    public Double getAllClusterNodePhyStoreSize(final boolean bWithOutCeph) {
        String realIndexName = IndexNameUtils.genCurrentDailyIndexName(indexName);
        String dsl           = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ALL_CLUSTER_NODE_PHY_STORE_SIZE, SCROLL_SIZE);

        final Set<String> nodeNameSet       = new HashSet<>();
        final double[]      totalPhySize      = new double[1];
        final String        fs_total_size_key = "fs-total-total_in_bytes";

        gatewayClient.queryWithScroll(realIndexName,
                TYPE, dsl, SCROLL_SIZE, null, ESNodeStats.class, resultList -> {
                    if (CollectionUtils.isEmpty(resultList)) {return;}

                    for (ESNodeStats stats : resultList) {
                        if(null == stats.getMetrics()
                                || StringUtils.isBlank(stats.getMetrics().get(fs_total_size_key))){
                            continue;
                        }

                        String node   = stats.getNode();
                        String fsSize = stats.getMetrics().get(fs_total_size_key);

                        if (bWithOutCeph && node.contains("ceph")) {continue;}

                        //由于物理机上可能存在节点混部，而混部的节点es在统计的时候获取的是物理机的整个磁盘空间，所以获取一个节点统计的信息即可
                        if (!nodeNameSet.contains(node)) {
                            totalPhySize[0] += Double.valueOf(fsSize);
                            nodeNameSet.add(node);
                        }
                    }
                });

        return totalPhySize[0];
    }
}
