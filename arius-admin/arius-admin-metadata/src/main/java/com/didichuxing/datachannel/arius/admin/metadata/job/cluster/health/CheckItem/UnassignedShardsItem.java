package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.CheckItem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.constant.ResultLevel;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPo;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.ESClusterHealthResponse;

import java.util.ArrayList;
import java.util.List;

public class UnassignedShardsItem extends AbstractCheckerItem {
    private long unassignedShardsNum;
    private long activeshardsNum;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.CLUSTER_STATUS;
    }

    @Override
    protected long getCheckerTotalNu() {
        return unassignedShardsNum + activeshardsNum;
    }

    @Override
    protected long getErrorSize(){return unassignedShardsNum;}

    @Override
    public ResultLevel genResultLevel() {
        return (unassignedShardsNum == 0) ? ResultLevel.FINE : ResultLevel.ERROR;
    }

    @Override
    protected List<HealthCheckErrInfoPo> execCheckRecordErrInfo() {
        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();

        ESClusterHealthResponse response = esClient.admin().cluster().prepareHealth().execute().actionGet();
        unassignedShardsNum = response.getUnassignedShards();
        activeshardsNum     = response.getActiveShards();

        List<HealthCheckErrInfoPo> healthCheckErrInfoPos = new ArrayList<>();
        if (unassignedShardsNum > 0) {
            String shard = String.valueOf(unassignedShardsNum + activeshardsNum);

            HealthCheckErrInfoPo healthCheckErrInfoPo = new HealthCheckErrInfoPo();
            healthCheckErrInfoPo.setCheckTypeName(getType().getName());
            healthCheckErrInfoPo.setShard(shard);
            healthCheckErrInfoPo.setExtendInfo("unassigned shards num is " + unassignedShardsNum);
            healthCheckErrInfoPo.setValue(String.valueOf(unassignedShardsNum));
            healthCheckErrInfoPo.setStatus(1);

            healthCheckErrInfoPos.add(healthCheckErrInfoPo);
        }
        return healthCheckErrInfoPos;
    }
}
