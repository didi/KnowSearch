package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfoWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmptyIndicesItem extends AbstractCheckerItem {

    private int tempalteNu = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.EMPTY_INDICES;
    }

    @Override
    protected long getCheckerTotalNu() {
        return tempalteNu;
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        int thresholdDay  = getClusterHealthCheckJobConfig().getEmptyIndicesThresholdDay();
        String cluster    = getClusterHealthCheckJobConfig().getClusterName();

        AriusStatsIndexInfoESDAO ariusStatsInfoEsDao = getClusterHealthCheckJobConfig().getAriusStatsInfoEsDao();

        List<HealthCheckErrInfoPO> healthCheckErrInfoPOS = new ArrayList<>();

        Map<String, IndexTemplatePhyInfoWithLogic> templateMap = getClusterHealthCheckJobConfig().getIndexTemplateMap();
        for(Map.Entry<String, IndexTemplatePhyInfoWithLogic> entry : templateMap.entrySet()){
            String key = entry.getKey();

            if(isWhiteIndex(key) || iskibanaIndex(key)){continue;}

            IndexTemplatePhyInfoWithLogic template = templateMap.get(key);
            if(null == template){continue;}

            tempalteNu++;

            String templateName = template.getName();
            String shardNu      = template.getShard().toString();
            String rack         = template.getRack();
            Long   createTime   = template.getCreateTime().getTime();
            Long   now          = System.currentTimeMillis();

            //最近thresholdDay文档为空
            Long difference = (now - createTime) / 1000 * 24 * 60 * 60;
            if(difference > thresholdDay){
                try {
                    long docNu = ariusStatsInfoEsDao.getTemplateTotalDocNu(templateName, cluster);
                    if (docNu <= 0) {
                        HealthCheckErrInfoPO healthCheckErrInfo = new HealthCheckErrInfoPO();
                        healthCheckErrInfo.setCheckTypeName(getType().getName());
                        healthCheckErrInfo.setTemplate(templateName);
                        healthCheckErrInfo.setShard(shardNu);
                        healthCheckErrInfo.setRack(rack);
                        healthCheckErrInfo.setExtendInfo(cluster);
                        healthCheckErrInfo.setStatus(1);
                        healthCheckErrInfoPOS.add(healthCheckErrInfo);
                    }
                } catch (Exception e) {
                    logger.error("class=EmptyIndicesItem||method=execCheckRecordErrInfo||template={}||aggSearchException:", templateName, e);
                }
            }
        }

        return healthCheckErrInfoPOS;
    }
}
