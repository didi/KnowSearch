package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Deprecated
public class InvalidIndicesInEsItem extends AbstractCheckerItem {
    private Integer indicesNum = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.INVALID_INDICES_IN_ES;
    }

    @Override
    protected long getCheckerTotalNu() {
        return indicesNum;
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPO> checkErrInfoPos = new ArrayList<>();

        ESClient esClient  = getClusterHealthCheckJobConfig().getEsClient();

        ESIndicesStatsResponse response       = esClient.admin().indices().prepareStats().execute().actionGet();
        Map<String, IndexNodes> indexStatsMap = response.getIndicesMap();

        for(String index : indexStatsMap.keySet()) {
            if(isWhiteIndex(index) || iskibanaIndex(index)){continue;}
            indicesNum++;

            IndexTemplatePhyWithLogic indexTemplate = getIndexTemplateByIndex(index);
            if(null == indexTemplate){
                StringBuilder inf = new StringBuilder();
                inf.append("index:").append(index);
                inf.append(",cluster:").append(getClusterHealthCheckJobConfig().getClusterName());

                HealthCheckErrInfoPO healthCheckErrInfoPo = new HealthCheckErrInfoPO();
                healthCheckErrInfoPo.setCheckTypeName(getType().getName());
                healthCheckErrInfoPo.setIdx(index);
                healthCheckErrInfoPo.setExtendInfo(inf.toString());
                healthCheckErrInfoPo.setStatus(1);

                checkErrInfoPos.add(healthCheckErrInfoPo);
            }
        }
        return checkErrInfoPos;
    }
}