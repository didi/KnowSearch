package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.CheckItem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.constant.ResultLevel;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckRecordPo;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import org.elasticsearch.cluster.health.ClusterHealthStatus;

import java.util.ArrayList;
import java.util.List;

public class ClusterStatusItem extends AbstractCheckerItem {

    private ClusterHealthStatus clusterStatus;

    public ClusterStatusItem(){}

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.CLUSTER_STATUS;
    }

    @Override
    protected List<HealthCheckErrInfoPo> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPo> healthCheckErrInfos = new ArrayList<>();

        //获取集群健康状态信息
        ESClient esClient = getClusterHealthCheckJobConfig().getEsClient();
        ESClusterHealthResponse response = esClient.admin().cluster().prepareHealth().execute().actionGet();

        clusterStatus = ClusterHealthStatus.fromString(response.getStatus());
        if (ClusterHealthStatus.GREEN != clusterStatus) {
            HealthCheckErrInfoPo healthCheckErrInfo = new HealthCheckErrInfoPo();
            healthCheckErrInfo.setCheckTypeName(getType().getName());
            healthCheckErrInfo.setExtendInfo("cluster status is " + clusterStatus);
            healthCheckErrInfo.setValue(String.valueOf(clusterStatus));
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
        if (ClusterHealthStatus.GREEN != clusterStatus) {
            return ResultLevel.FINE;
        } else if (ClusterHealthStatus.YELLOW != clusterStatus) {
            return ResultLevel.NORMAL;
        } else {
            return ResultLevel.ERROR;
        }
    }

    @Override
    protected HealthCheckRecordPo genHealthCheckRecordPoInner(HealthCheckRecordPo healthCheckRecordPo){
        healthCheckRecordPo.setErrRate(ClusterHealthStatus.GREEN == clusterStatus ? 0 : 100);
        healthCheckRecordPo.setCheckCount(1);
        healthCheckRecordPo.setErrCount(ClusterHealthStatus.GREEN == clusterStatus ? 0 : 1);
        return healthCheckRecordPo;
    }
}
