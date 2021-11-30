package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.constant.ResultLevel;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
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
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();

        ESClusterHealthResponse response = esClient.admin().cluster().prepareHealth().execute().actionGet();
        unassignedShardsNum = response.getUnassignedShards();
        activeshardsNum     = response.getActiveShards();

        List<HealthCheckErrInfoPO> healthCheckErrInfoPOS = new ArrayList<>();
        if (unassignedShardsNum > 0) {
            String shard = String.valueOf(unassignedShardsNum + activeshardsNum);

            HealthCheckErrInfoPO healthCheckErrInfoPo = new HealthCheckErrInfoPO();
            healthCheckErrInfoPo.setCheckTypeName(getType().getName());
            healthCheckErrInfoPo.setShard(shard);
            healthCheckErrInfoPo.setExtendInfo("unassigned shards num is " + unassignedShardsNum);
            healthCheckErrInfoPo.setValue(String.valueOf(unassignedShardsNum));
            healthCheckErrInfoPo.setStatus(1);

            healthCheckErrInfoPOS.add(healthCheckErrInfoPo);
        }
        return healthCheckErrInfoPOS;
    }
}
