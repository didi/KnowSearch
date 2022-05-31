package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
import com.didiglobal.logi.elasticsearch.client.response.model.indices.CommonStat;
import com.didiglobal.logi.elasticsearch.client.response.model.indices.Docs;
import com.didiglobal.logi.elasticsearch.client.response.model.indices.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Deprecated
/**
 * 检查索引平均每条记录的大小
 */
public class IndexDocsSizeItem extends AbstractCheckerItem {
    private int indicesNum = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.INDEX_PER_DOCS_SIZE;
    }

    @Override
    protected long getCheckerTotalNu() {
        return indicesNum;
    }

    @Override
    protected String[] getLevelConfig() {
        return getClusterHealthCheckJobConfig().getIndexDocSizeResultLevel().split(",");
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPO> checkErrInfoPos = new ArrayList<>();

        ESClient esClient  = getClusterHealthCheckJobConfig().getEsClient();
        int averageSize    = getClusterHealthCheckJobConfig().getIndexDocSizeAverageSize();

        ESIndicesStatsResponse response       = esClient.admin().indices().prepareStats().execute().actionGet();
        Map<String, IndexNodes> indexStatsMap = response.getIndicesMap();

        for(Map.Entry<String,IndexNodes> entry : indexStatsMap.entrySet()){
            String     index      = entry.getKey();
            IndexNodes indexNodes = entry.getValue();

            if(isWhiteIndex(index) || iskibanaIndex(index)){continue;}
            indicesNum++;

            CommonStat totalInfo  = indexNodes.getTotal();
            Docs docsStats  = totalInfo.getDocs();
            Store storeStats = totalInfo.getStore();

            long size  = storeStats.getSizeInBytes();
            long count = docsStats.getCount();

            double average = 0.0;
            if (count > 0) {
                average = CommonUtils.formatDouble((double) size / count, 1);
            }

            if (average > averageSize) {
                String info = "Index : " + index + " perDocsSize : " + average + "";

                IndexTemplatePhy indexTemplate = getIndexTemplateByIndex(index);
                if(null == indexTemplate){continue;}

                String templateName = indexTemplate.getName();
                String shardnum     = indexTemplate.getShard().toString();
                String rack         = indexTemplate.getRack();

                HealthCheckErrInfoPO healthCheckErrInfoPo = new HealthCheckErrInfoPO();
                healthCheckErrInfoPo.setCheckTypeName(getType().getName());
                healthCheckErrInfoPo.setTemplate(templateName);
                healthCheckErrInfoPo.setIdx(index);
                healthCheckErrInfoPo.setShard(shardnum);
                healthCheckErrInfoPo.setRack(rack);
                healthCheckErrInfoPo.setExtendInfo(info);
                healthCheckErrInfoPo.setValue(String.valueOf(average));
                healthCheckErrInfoPo.setStatus(1);

                checkErrInfoPos.add(healthCheckErrInfoPo);
            }
        }

        return checkErrInfoPos;
    }
}