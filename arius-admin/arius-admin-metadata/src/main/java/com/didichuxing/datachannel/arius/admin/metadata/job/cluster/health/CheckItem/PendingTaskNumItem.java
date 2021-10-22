package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.CheckItem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.constant.ResultLevel;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPo;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.cluster.ESClusterHealthResponse;

import java.util.ArrayList;
import java.util.List;

public class PendingTaskNumItem extends AbstractCheckerItem {
    private long pendingTaskNum = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.CLUSTER_PENDING_TASK;
    }

    @Override
    protected long getCheckerTotalNu() {
        return 1;
    }

    @Override
    public ResultLevel genResultLevel() {
        return (pendingTaskNum == 0) ? ResultLevel.FINE : ResultLevel.ERROR;
    }

    @Override
    protected List<HealthCheckErrInfoPo> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPo> checkErrInfoPos = new ArrayList<>();

        ESClient esClient  = getClusterHealthCheckJobConfig().getEsClient();

        //获取集群健康状态信息
        ESClusterHealthResponse response = esClient.admin().cluster().prepareHealth().execute().actionGet();
        pendingTaskNum = response.getNumberOfPendingTasks();

        if (pendingTaskNum > 0) {
            StringBuilder inf = new StringBuilder();
            inf.append("cluster:").append(getClusterHealthCheckJobConfig().getClusterName());
            inf.append(",pendingTaskNum:").append(pendingTaskNum);

            HealthCheckErrInfoPo healthCheckErrInfoPo = new HealthCheckErrInfoPo();
            healthCheckErrInfoPo.setCheckTypeName(getType().getName());
            healthCheckErrInfoPo.setValue(String.valueOf(pendingTaskNum));
            healthCheckErrInfoPo.setStatus(1);
            healthCheckErrInfoPo.setExtendInfo(inf.toString());

            checkErrInfoPos.add(healthCheckErrInfoPo);
        }
        return checkErrInfoPos;
    }
}
