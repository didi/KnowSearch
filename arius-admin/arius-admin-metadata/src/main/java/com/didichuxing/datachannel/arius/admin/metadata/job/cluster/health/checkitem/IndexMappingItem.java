package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.indices.gettemplate.ESIndicesGetTemplateResponse;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.TypeConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.MultiTemplatesConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.TemplateConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Deprecated
public class IndexMappingItem extends AbstractCheckerItem {
    private int templateNum    = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.INDEX_TYPE_MAPPING_SIZE;
    }

    @Override
    protected long getCheckerTotalNu() {
        return templateNum;
    }

    @Override
    protected String[] getLevelConfig() {
        return getClusterHealthCheckJobConfig().getIndexMappingResultLevel().split(",");
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        List<HealthCheckErrInfoPO> checkErrInfoPos = new ArrayList<>();

        int largeIndexMappingSize = getClusterHealthCheckJobConfig().getIndexMappingLargeSize();

        ESClient esClient  = getClusterHealthCheckJobConfig().getEsClient();
        ESIndicesGetTemplateResponse response = esClient.admin().indices().prepareGetTemplate("*").execute().actionGet();

        MultiTemplatesConfig multiTemplatesConfig     = response.getMultiTemplatesConfig();
        Map<String, TemplateConfig> templateConfigMap = multiTemplatesConfig.getTemplateConfigMap();

        for(Map.Entry<String, TemplateConfig> entry : templateConfigMap.entrySet()){
            String template = entry.getKey();

            if(isWhiteIndex(template) || iskibanaIndex(template)){continue;}

            templateNum++;

            TemplateConfig templateConfig = templateConfigMap.get(template);
            MappingConfig mappingConfig  = templateConfig.getMappings();

            int fieldsSize = 0;

            Map<String, TypeConfig> typeConfigMap = mappingConfig.getMapping();
            for(String type : templateConfigMap.keySet()){
                TypeConfig typeConfig = typeConfigMap.get(type);
                if(null == typeConfig || null == typeConfig.getProperties()
                        || null == typeConfig.getProperties().getPropertyMap()){continue;}

                fieldsSize += typeConfig.getProperties().getPropertyMap().size();
            }

            if(fieldsSize > largeIndexMappingSize){
                StringBuilder inf = new StringBuilder();
                inf.append("index:").append(template);
                inf.append(",fieldsSize:").append(fieldsSize);

                IndexTemplatePhyWithLogic indexTemplate = getIndexTemplateByIndex(template);
                if(null == template){continue;}

                String templateName = indexTemplate.getName();
                String shardNu      = indexTemplate.getShard().toString();
                String rack         = indexTemplate.getRack();

                HealthCheckErrInfoPO healthCheckErrInfoPo = new HealthCheckErrInfoPO();
                healthCheckErrInfoPo.setCheckTypeName(getType().getName());
                healthCheckErrInfoPo.setTemplate(templateName);
                healthCheckErrInfoPo.setIdx(template);
                healthCheckErrInfoPo.setRack(rack);
                healthCheckErrInfoPo.setShard(shardNu);
                healthCheckErrInfoPo.setExtendInfo(inf.toString());
                healthCheckErrInfoPo.setValue(String.valueOf(fieldsSize));
                healthCheckErrInfoPo.setStatus(1);

                checkErrInfoPos.add(healthCheckErrInfoPo);
            }
        }

        return checkErrInfoPos;
    }
}