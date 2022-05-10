package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.request.index.stats.IndicesStatsLevel;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.elasticsearch.client.response.model.indices.CommonStat;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 主从分片文档数是否一致
 */
public class PriRepShardDocsCountItem extends AbstractCheckerItem {

    private int shardNu = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.SHARD_PRI_REP_DOCS_COUNT_SAME;
    }

    @Override
    protected long getCheckerTotalNu() {
        return shardNu;
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPO> checkErrInfoPos = new ArrayList<>();

        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();

        ESIndicesStatsResponse response       = esClient.admin().indices().prepareStats().setLevel( IndicesStatsLevel.SHARDS).execute().actionGet();
        Map<String, IndexNodes> indexNodesMap = response.getIndicesMap();

        for(Map.Entry<String, IndexNodes> entry : indexNodesMap.entrySet()){
            String index = entry.getKey();
            if(isWhiteIndex(index) || iskibanaIndex(index)){continue;}

            IndexNodes indexNodes = indexNodesMap.get(index);
            Map<String, List<CommonStat>> indexShardsStatMap = indexNodes.getShards();

            for(Map.Entry<String, List<CommonStat>> comEntry : indexShardsStatMap.entrySet()){
                String shardId = comEntry.getKey();
                shardNu++;

                List<CommonStat> commonStats = indexShardsStatMap.get(shardId);

                if(CollectionUtils.isEmpty(commonStats) || 1 == commonStats.size()){continue;}

                CommonStat priShard = commonStats.get(0);
                CommonStat repShard = commonStats.get(1);

                long priShardDocNu  = priShard.getDocs().getCount();
                long repShardDocNu  = repShard.getDocs().getCount();

                String priShardNode = priShard.getRouting().getNode();

                if(priShardDocNu != repShardDocNu){
                    recordHealthCheckErrInfo(checkErrInfoPos, index, priShardDocNu, repShardDocNu, priShardNode);
                }
            }
        }

        return checkErrInfoPos;
    }

    private void recordHealthCheckErrInfo(List<HealthCheckErrInfoPO> checkErrInfoPos, String index, long priShardDocNu, long repShardDocNu, String priShardNode) {
        StringBuilder inf = new StringBuilder();
        inf.append("index:").append(index);
        inf.append(",priShardDocNu:").append(priShardDocNu);
        inf.append(",repShardDocNu:").append(repShardDocNu);

        IndexTemplatePhyWithLogic template = getIndexTemplateByIndex(index);
        if(null == template){
            return;
        }

        String templateName = template.getName();
        String shard        = template.getShard().toString();
        String rack         = template.getRack();

        HealthCheckErrInfoPO healthCheckErrInfoPo = new HealthCheckErrInfoPO();
        healthCheckErrInfoPo.setCheckTypeName(getType().getName());
        healthCheckErrInfoPo.setTemplate(templateName);
        healthCheckErrInfoPo.setIdx(index);
        healthCheckErrInfoPo.setRack(rack);
        healthCheckErrInfoPo.setShard(shard);
        healthCheckErrInfoPo.setExtendInfo(inf.toString());
        healthCheckErrInfoPo.setValue(priShardNode);
        healthCheckErrInfoPo.setStatus(1);

        checkErrInfoPos.add(healthCheckErrInfoPo);
    }
}
