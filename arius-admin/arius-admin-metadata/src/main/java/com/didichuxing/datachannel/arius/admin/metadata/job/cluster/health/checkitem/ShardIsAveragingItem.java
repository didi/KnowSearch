package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfoWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.request.index.stats.IndicesStatsLevel;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.elasticsearch.client.response.model.indices.CommonStat;
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
    public static final Integer AVERAGING_THRESHOLD = 10;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.INDEX_SHARD_IS_AVERAGE;
    }

    @Override
    protected long getCheckerTotalNu() {
        return indexNu;
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPO> checkErrInfoPos = new ArrayList<>();

        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();

        ESIndicesStatsResponse response      = esClient.admin().indices().prepareStats().setLevel( IndicesStatsLevel.SHARDS).execute().actionGet();
        Map<String, IndexNodes> indexNodesMap = response.getIndicesMap();

        for(Map.Entry<String, IndexNodes> entry : indexNodesMap.entrySet()){
            String index = entry.getKey();

            if(isWhiteIndex(index) || iskibanaIndex(index)){continue;}
            indexNu++;

            Map<String, Integer> nodeShardMap = new HashMap<>();

            IndexNodes indexNodes = indexNodesMap.get(index);
            Map<String, List<CommonStat>> indexShardsStatMap = indexNodes.getShards();

            //获取索引所有shard数量以及每个节点的shand个数
            int indexShardNu = 0;

            indexShardNu = getIndexShardNu(nodeShardMap, indexShardsStatMap, indexShardNu);

            int indexNodeNu = indexShardsStatMap.size();
            if(indexNodeNu < 1){continue;}

            double avgShard = (double) indexShardNu/indexNodeNu;

            handleNodeShardMap(checkErrInfoPos, index, nodeShardMap, indexShardNu, avgShard);
        }

        return checkErrInfoPos;
    }

    private void handleNodeShardMap(List<HealthCheckErrInfoPO> checkErrInfoPos, String index, Map<String, Integer> nodeShardMap, int indexShardNu, double avgShard) {
        for(Map.Entry<String, Integer> nodeEntry : nodeShardMap.entrySet()){
            String node = nodeEntry.getKey();

            Integer nodeShardNu = nodeShardMap.get(node);

            if(null != nodeShardNu && Math.abs(nodeShardNu - avgShard) > AVERAGING_THRESHOLD){
                StringBuilder inf = new StringBuilder();
                inf.append("index:").append(index);
                inf.append(",shardNu:").append(indexShardNu);
                inf.append(",avgShard:").append(avgShard);
                inf.append(",node:").append(node);
                inf.append(",nodeShard:").append(nodeShardNu);

                IndexTemplatePhyInfoWithLogic template = getIndexTemplateByIndex(index);
                if(null == template){continue;}

                String templateName = template.getName();
                String shardNu      = template.getShard().toString();
                String rack         = template.getRack();

                HealthCheckErrInfoPO healthCheckErrInfoPo = new HealthCheckErrInfoPO();
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

    private int getIndexShardNu(Map<String, Integer> nodeShardMap, Map<String, List<CommonStat>> indexShardsStatMap, int indexShardNu) {
        for(Map.Entry<String, List<CommonStat>> indexEntry : indexShardsStatMap.entrySet()){
            String shardId = indexEntry.getKey();

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
        return indexShardNu;
    }
}
