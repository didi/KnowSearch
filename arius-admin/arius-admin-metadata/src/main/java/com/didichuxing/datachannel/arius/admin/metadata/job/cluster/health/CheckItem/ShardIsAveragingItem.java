package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.CheckItem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPo;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.request.index.stats.IndicesStatsLevel;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.model.indices.CommonStat;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检查每个索引的shard是否分配均匀
 * 索引的在某个节点上shard的个数大于该索引所有sharad除以索引所在索引节点的平均值时为不平均分布
 */
public class ShardIsAveragingItem extends AbstractCheckerItem {
    private int  indexNu = 0;
    public final Integer AVERAGING_THRESHOLD = 10;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.INDEX_SHARD_IS_AVERAGE;
    }

    @Override
    protected long getCheckerTotalNu() {
        return indexNu;
    }

    @Override
    protected List<HealthCheckErrInfoPo> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPo> checkErrInfoPos = new ArrayList<>();

        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();

        ESIndicesStatsResponse response      = esClient.admin().indices().prepareStats().setLevel( IndicesStatsLevel.SHARDS).execute().actionGet();
        Map<String, IndexNodes> indexNodesMap = response.getIndicesMap();

        for(String index : indexNodesMap.keySet()){
            if(isWhiteIndex(index) || iskibanaIndex(index)){continue;}
            indexNu++;

            Map<String, Integer> nodeShardMap = new HashMap<>();

            IndexNodes indexNodes = indexNodesMap.get(index);
            Map<String, List<CommonStat>> indexShardsStatMap = indexNodes.getShards();

            //获取索引所有shard数量以及每个节点的shand个数
            int indexShardNu = 0;

            for(String shardId : indexShardsStatMap.keySet()) {
                List<CommonStat> commonStats = indexShardsStatMap.get(shardId);
                if(CollectionUtils.isEmpty(commonStats)){continue;}

                for(CommonStat commonStat : commonStats){
                    indexShardNu++;

                    String  node = commonStat.getRouting().getNode();
                    Integer nodeShardNu = nodeShardMap.get(node);
                    if(null == nodeShardNu){
                        nodeShardMap.put(node, 1);
                    }else {
                        nodeShardMap.put(node, nodeShardNu + 1);
                    }
                }
            }

            int indexNodeNu = indexShardsStatMap.size();
            if(indexNodeNu < 1){continue;}

            double avgShard = indexShardNu/indexNodeNu;

            for(String node : nodeShardMap.keySet()){
                Integer nodeShardNu = nodeShardMap.get(node);

                if(null != nodeShardNu && Math.abs(nodeShardNu - avgShard) > AVERAGING_THRESHOLD){
                    StringBuilder inf = new StringBuilder();
                    inf.append("index:").append(index);
                    inf.append(",shardNu:").append(indexShardNu);
                    inf.append(",avgShard:").append(avgShard);
                    inf.append(",node:").append(node);
                    inf.append(",nodeShard:").append(nodeShardNu);

                    IndexTemplatePhyWithLogic template = getIndexTemplateByIndex(index);
                    if(null == template){continue;}

                    String templateName = template.getName();
                    String shardNu      = template.getShard().toString();
                    String rack         = template.getRack();

                    HealthCheckErrInfoPo healthCheckErrInfoPo = new HealthCheckErrInfoPo();
                    healthCheckErrInfoPo.setCheckTypeName(getType().getName());
                    healthCheckErrInfoPo.setTemplate(templateName);
                    healthCheckErrInfoPo.setIdx(index);
                    healthCheckErrInfoPo.setRack(rack);
                    healthCheckErrInfoPo.setShard(shardNu);
                    healthCheckErrInfoPo.setExtendInfo(inf.toString());
                    healthCheckErrInfoPo.setValue(String.valueOf(nodeShardNu));
                    healthCheckErrInfoPo.setStatus(1);

                    checkErrInfoPos.add(healthCheckErrInfoPo);
                    break;
                }
            }
        }

        return checkErrInfoPos;
    }
}
