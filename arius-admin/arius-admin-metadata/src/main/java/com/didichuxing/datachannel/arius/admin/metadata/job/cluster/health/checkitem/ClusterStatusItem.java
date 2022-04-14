package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.constant.ResultLevel;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckRecordPO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ClusterStatusItem extends AbstractCheckerItem {

    private ClusterHealthEnum clusterHealthEnum;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.CLUSTER_STATUS;
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPO> healthCheckErrInfos = new ArrayList<>();

        //获取集群健康状态信息
        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();
        ESClusterHealthResponse response = esClient.admin().cluster().prepareHealth().execute().actionGet();

        clusterHealthEnum = ClusterHealthEnum.valuesOf(response.getStatus());
        if (ClusterHealthEnum.GREEN != clusterHealthEnum) {
            HealthCheckErrInfoPO healthCheckErrInfo = new HealthCheckErrInfoPO();
            healthCheckErrInfo.setCheckTypeName(getType().getName());
            healthCheckErrInfo.setExtendInfo("cluster status is " + clusterHealthEnum);
            healthCheckErrInfo.setValue(String.valueOf(clusterHealthEnum));
            healthCheckErrInfo.setStatus(1);

            healthCheckErrInfos.add(healthCheckErrInfo);
        }

        return healthCheckErrInfos;
    }

    @Override
    protected long getCheckerTotalNu() {
        return 1;
    }

    @Override
    protected ResultLevel genResultLevel() {
        if (ClusterHealthEnum.GREEN == clusterHealthEnum) {
            return ResultLevel.FINE;
        } else if (ClusterHealthEnum.YELLOW == clusterHealthEnum) {
            return ResultLevel.NORMAL;
        } else {
            return ResultLevel.ERROR;
        }
    }

    @Override
    protected HealthCheckRecordPO genHealthCheckRecordPoInner(HealthCheckRecordPO healthCheckRecordPo){
        healthCheckRecordPo.setErrRate(ClusterHealthEnum.GREEN == clusterHealthEnum ? 0 : 100);
        healthCheckRecordPo.setCheckCount(1);
        healthCheckRecordPo.setErrCount(ClusterHealthEnum.GREEN == clusterHealthEnum ? 0 : 1);
        return healthCheckRecordPo;
    }
}
