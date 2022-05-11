package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.request.index.stats.IndicesStatsLevel;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.elasticsearch.client.response.model.indices.CommonStat;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 检查每个索引的shard文档数是否大多, 每个shard的文档数不能大于5000万条
 */
public class ShardDocCountLargeItem extends AbstractCheckerItem {
    private int shardNu = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.SHARD_COUNT_LARGE;
    }

    @Override
    protected long getCheckerTotalNu() {
        return shardNu;
    }

    @Override
    protected String[] getLevelConfig() {
        if(StringUtils.isBlank(getClusterHealthCheckJobConfig().getLargeShardDocNumResultLevel())){
            return new String[]{};
        }else {
            return getClusterHealthCheckJobConfig().getLargeShardDocNumResultLevel().split(",");
        }
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPO> checkErrInfoPos = new ArrayList<>();

        int      largeDoc = getClusterHealthCheckJobConfig().getLargeShardDocNum();
        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();

        ESIndicesStatsResponse response      = esClient.admin().indices().prepareStats().setLevel( IndicesStatsLevel.SHARDS).execute().actionGet();
        Map<String, IndexNodes> indexNodesMap = response.getIndicesMap();

        for(Map.Entry<String, IndexNodes> entry : indexNodesMap.entrySet()){
            String index = entry.getKey();

            if(isWhiteIndex(index) || iskibanaIndex(index)){continue;}

            IndexNodes indexNodes = indexNodesMap.get(index);

            Map<String, List<CommonStat>> indexShardsStatMap = indexNodes.getShards();

            for(Map.Entry<String, List<CommonStat>> indexEntry : indexShardsStatMap.entrySet()){
                handleIndexShardsStatMap(checkErrInfoPos, largeDoc, index, indexShardsStatMap, indexEntry);
            }
        }

        return checkErrInfoPos;
    }

    private void handleIndexShardsStatMap(List<HealthCheckErrInfoPO> checkErrInfoPos, int largeDoc, String index, Map<String, List<CommonStat>> indexShardsStatMap, Map.Entry<String, List<CommonStat>> indexEntry) {
        String shardId = indexEntry.getKey();
        shardNu++;

        List<CommonStat> commonStats = indexShardsStatMap.get(shardId);

        if(CollectionUtils.isEmpty(commonStats)){
            return;
        }

        CommonStat shardCommonStat = commonStats.get(0);
        long   shardDocNu = shardCommonStat.getDocs().getCount();
        String noede      = shardCommonStat.getRouting().getNode();

        if(shardDocNu > largeDoc){
            StringBuilder inf = new StringBuilder();
            inf.append("index:").append(index);
            inf.append(",currentNodeId:").append(noede);
            inf.append(",shardDocNu:").append(shardDocNu);

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
            healthCheckErrInfoPo.setNode(noede);
            healthCheckErrInfoPo.setValue(String.valueOf(shardDocNu));
            healthCheckErrInfoPo.setExtendInfo(inf.toString());
            healthCheckErrInfoPo.setStatus(1);

            checkErrInfoPos.add(healthCheckErrInfoPo);
        }
    }
}
