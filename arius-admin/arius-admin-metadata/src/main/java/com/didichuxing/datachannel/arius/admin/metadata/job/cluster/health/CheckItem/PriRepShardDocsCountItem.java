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
    protected List<HealthCheckErrInfoPo> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPo> checkErrInfoPos = new ArrayList<>();

        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();

        ESIndicesStatsResponse response       = esClient.admin().indices().prepareStats().setLevel( IndicesStatsLevel.SHARDS).execute().actionGet();
        Map<String, IndexNodes> indexNodesMap = response.getIndicesMap();

        for(String index : indexNodesMap.keySet()) {
            if(isWhiteIndex(index) || iskibanaIndex(index)){continue;}

            IndexNodes indexNodes = indexNodesMap.get(index);
            Map<String, List<CommonStat>> indexShardsStatMap = indexNodes.getShards();
            for(String shardId : indexShardsStatMap.keySet()){
                shardNu++;

                List<CommonStat> commonStats = indexShardsStatMap.get(shardId);

                if(CollectionUtils.isEmpty(commonStats) || 1 == commonStats.size()){continue;}

                CommonStat priShard = commonStats.get(0);
                CommonStat repShard = commonStats.get(1);

                long priShardDocNu  = priShard.getDocs().getCount();
                long repShardDocNu  = repShard.getDocs().getCount();

                String priShardNode = priShard.getRouting().getNode();

                if(priShardDocNu != repShardDocNu){
                    StringBuilder inf = new StringBuilder();
                    inf.append("index:").append(index);
                    inf.append(",priShardDocNu:").append(priShardDocNu);
                    inf.append(",repShardDocNu:").append(repShardDocNu);

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
                    healthCheckErrInfoPo.setValue(priShardNode);
                    healthCheckErrInfoPo.setStatus(1);

                    checkErrInfoPos.add(healthCheckErrInfoPo);
                }
            }
        }

        return checkErrInfoPos;
    }
}
