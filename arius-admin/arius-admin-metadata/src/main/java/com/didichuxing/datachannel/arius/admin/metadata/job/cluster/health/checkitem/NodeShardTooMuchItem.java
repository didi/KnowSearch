package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ClusterNodeStats;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import com.didiglobal.logi.elasticsearch.client.response.model.indices.CommonStat;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 检查每个节点shard个数是否过多
 */
public class NodeShardTooMuchItem extends AbstractCheckerItem {
    private int nodeSize = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.NODE_SHARD_TOO_MACH;
    }

    @Override
    protected long getCheckerTotalNu() {
        return nodeSize;
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPO> checkErrInfoPos = new ArrayList<>();

        int shardTooMuchShardNu = getClusterHealthCheckJobConfig().getShardTooMuchShardNu();

        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();
        ESClusterNodesStatsResponse clusterStateResponse = esClient.admin().cluster().prepareNodeStats().level("shards").get();

        Map<String, ClusterNodeStats> clusterNodeStatsMap = clusterStateResponse.getNodes();

        for(Map.Entry<String, ClusterNodeStats> entry : clusterNodeStatsMap.entrySet()){
            String node = entry.getKey();
            nodeSize++;
            int nodeShardNu = 0;

            ClusterNodeStats clusterNodeStats = clusterNodeStatsMap.get(node);
            Map<String, List<Map<String, CommonStat>>> nodeShardMap = clusterNodeStats.getIndices().getShards();

            for(Map.Entry<String, List<Map<String, CommonStat>>> comEntry : nodeShardMap.entrySet()){
                String index = comEntry.getKey();
                List<Map<String, CommonStat>> shardCommons = nodeShardMap.get(index);

                if(CollectionUtils.isNotEmpty(shardCommons)){
                    nodeShardNu += shardCommons.size();
                }
            }

            if(nodeShardNu > shardTooMuchShardNu){
                StringBuilder inf = new StringBuilder();
                inf.append("node:").append(node);
                inf.append(",nodeShardNu:").append(nodeShardNu);

                HealthCheckErrInfoPO healthCheckErrInfoPo = new HealthCheckErrInfoPO();
                healthCheckErrInfoPo.setCheckTypeName(getType().getName());
                healthCheckErrInfoPo.setNode(node);
                healthCheckErrInfoPo.setExtendInfo(inf.toString());
                healthCheckErrInfoPo.setValue(String.valueOf(nodeShardNu));
                healthCheckErrInfoPo.setStatus(1);

                checkErrInfoPos.add(healthCheckErrInfoPo);
            }
        }

        return checkErrInfoPos;
    }
}
