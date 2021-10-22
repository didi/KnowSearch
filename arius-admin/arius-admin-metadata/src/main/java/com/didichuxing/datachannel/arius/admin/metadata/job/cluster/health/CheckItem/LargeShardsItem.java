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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LargeShardsItem extends AbstractCheckerItem {
    private int shardsNum = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.INDEX_SHARD_NUM_LARGE;
    }

    @Override
    protected long getCheckerTotalNu() {
        return shardsNum;
    }

    @Override
    protected String[] getLevelConfig() {
        if(StringUtils.isBlank(getClusterHealthCheckJobConfig().getLargeShardDocNumResultLevel())){
            return null;
        }else {
            return getClusterHealthCheckJobConfig().getLargeShardDocNumResultLevel().split(",");
        }
    }

    @Override
    protected List<HealthCheckErrInfoPo> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPo> healthCheckErrInfoPos = new ArrayList<>();

        int largeShardSize = getClusterHealthCheckJobConfig().getLargeShardSize();

        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();
        ESIndicesStatsResponse response = esClient.admin().indices().prepareStats().setLevel( IndicesStatsLevel.SHARDS).execute().actionGet();

        Map<String, IndexNodes> indexNodesMap = response.getIndicesMap();
        for(String index : indexNodesMap.keySet()) {
            if(isWhiteIndex(index) || iskibanaIndex(index)){continue;}

            IndexNodes indexNodes = indexNodesMap.get(index);
            Map<String, List<CommonStat>> indexShardsStatMap = indexNodes.getShards();
            for(String shardId : indexShardsStatMap.keySet()){
                shardsNum++;

                List<CommonStat> commonStats = indexShardsStatMap.get(shardId);

                if(CollectionUtils.isEmpty(commonStats)){continue;}

                CommonStat priShard     = commonStats.get(0);
                long       priShardSize = priShard.getStore().getSizeInBytes();
                long       priShardDoc  = priShard.getDocs().getCount();
                String     priShardNode = priShard.getRouting().getNode();

                if(priShardSize < largeShardSize){
                    StringBuilder inf = new StringBuilder();
                    inf.append("Index:").append(index);
                    inf.append(",currentNodeId:").append(priShardNode);
                    inf.append(",DocNumber:").append(priShardDoc);
                    inf.append(",ShardSize:").append((double) priShardSize / (1024 * 1024 * 1024));

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
                    healthCheckErrInfoPo.setNode(priShardNode);
                    healthCheckErrInfoPo.setValue(String.valueOf(priShardSize));
                    healthCheckErrInfoPo.setExtendInfo(inf.toString());
                    healthCheckErrInfoPo.setStatus(1);

                    healthCheckErrInfoPos.add(healthCheckErrInfoPo);
                }
            }
        }

        return healthCheckErrInfoPos;
    }
}
