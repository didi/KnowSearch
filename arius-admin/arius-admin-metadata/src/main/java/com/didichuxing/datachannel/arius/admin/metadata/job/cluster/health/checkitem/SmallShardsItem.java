package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class SmallShardsItem extends AbstractCheckerItem {
    private int indicesNum;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.INDEX_SHARD_NUM_SMALL;
    }

    @Override
    protected long getCheckerTotalNu() {
        return indicesNum;
    }

    @Override
    protected String[] getLevelConfig() {
        if(StringUtils.isBlank(getClusterHealthCheckJobConfig().getSmallShardResultLevel())){
            return new String[]{};
        }else {
            return getClusterHealthCheckJobConfig().getSmallShardResultLevel().split(",");
        }
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPO> healthCheckErrInfoPOS = new ArrayList<>();

        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();

        int    smallSize = getClusterHealthCheckJobConfig().getSmallShardSize();
        double maxSize   = smallSize * 1000000000d;

        Map<String, IndexTemplatePhyWithLogic> indexTemplateMap = getClusterHealthCheckJobConfig().getIndexTemplateMap();

        for(Map.Entry<String, IndexTemplatePhyWithLogic> entry : indexTemplateMap.entrySet()){
            String template = entry.getKey();

            if(isWhiteIndex(template) || iskibanaIndex(template)){continue;}

            IndexTemplatePhyWithLogic indexTemplate = indexTemplateMap.get(template);
            if(null == indexTemplate){continue;}

            String expression = indexTemplate.getExpression();

            ESIndicesStatsResponse response;
            Map<String, IndexNodes> indexNodesMap = new HashMap<>();

            try {
                response      = esClient.admin().indices().prepareStats(expression).execute().actionGet();
                indexNodesMap = response.getIndicesMap();
            }catch (Exception e){
                logger.error("class=SmallShardsItem||method=execCheckRecordErrInfo||errMsg=template:{}", e);
            }

            long indexShard = 0;
            long indexSize  = 1;
            indicesNum++;

            for(Map.Entry<String, IndexNodes> indexEntry : indexNodesMap.entrySet()){
                String     index      = indexEntry.getKey();
                IndexNodes indexNodes = indexNodesMap.get(index);

                if(null == indexNodes.getPrimaries() || null == indexNodes.getPrimaries().getStore()
                        || null == indexNodes.getPrimaries().getShards()){continue;}

                indexSize  += indexNodes.getPrimaries().getStore().getSizeInBytes();
                indexShard += indexNodes.getPrimaries().getShards().size();
            }

            if (1 == indexSize || 0 == indexShard){continue;}

            double sizePerShard = CommonUtils.formatDouble(indexSize/indexShard, 1);

            if (sizePerShard > maxSize) {
                long newShardNum = (indexSize / 50000000000L) + 1;
                if (newShardNum == indexShard){continue;}

                double newSizePerShard = CommonUtils.formatDouble((double) indexSize / newShardNum, 1);

                StringBuilder inf = new StringBuilder();
                inf.append("index:").append(expression);
                inf.append(",indexShard:").append(indexShard);
                inf.append(",indexSize:").append(indexSize);
                inf.append(",sizePerShard:").append(sizePerShard);
                inf.append(",newShardNum:").append(newShardNum);
                inf.append(",newSizePerShard:").append(newSizePerShard);

                HealthCheckErrInfoPO healthCheckErrInfoPo = new HealthCheckErrInfoPO();
                healthCheckErrInfoPo.setCheckTypeName(getType().getName());
                healthCheckErrInfoPo.setTemplate(template);
                healthCheckErrInfoPo.setShard(indexTemplate.getShard().toString());
                healthCheckErrInfoPo.setRack(indexTemplate.getRack());
                healthCheckErrInfoPo.setExtendInfo(inf.toString());
                healthCheckErrInfoPo.setValue(String.valueOf(sizePerShard));
                healthCheckErrInfoPo.setStatus(1);

                healthCheckErrInfoPOS.add(healthCheckErrInfoPo);
            }
        }

        return healthCheckErrInfoPOS;
    }
}
