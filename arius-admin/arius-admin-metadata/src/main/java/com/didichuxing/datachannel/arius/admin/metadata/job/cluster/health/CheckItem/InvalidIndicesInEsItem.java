package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.CheckItem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPo;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.IndexNodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvalidIndicesInEsItem extends AbstractCheckerItem {
    public Integer indicesNum = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.INVALID_INDICES_IN_ES;
    }

    @Override
    protected long getCheckerTotalNu() {
        return indicesNum;
    }

    @Override
    protected List<HealthCheckErrInfoPo> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPo> checkErrInfoPos = new ArrayList<>();

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

                HealthCheckErrInfoPo healthCheckErrInfoPo = new HealthCheckErrInfoPo();
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
