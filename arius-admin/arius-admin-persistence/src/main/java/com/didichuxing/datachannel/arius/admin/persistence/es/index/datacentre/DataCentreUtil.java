package com.didichuxing.datachannel.arius.admin.persistence.es.index.datacentre;

import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@NoArgsConstructor
public class DataCentreUtil {
    private static final ILog LOGGER = LogFactory.getLog(DataCentreUtil.class);

    @Value("${arius.template.qutoa.notiry.record}")
    private String ariusTemplateQutoaNotiryRecord;

    @Value("${arius.template.quota.usage}")
    private String ariusTemplateQuotaUsage;

    @Value("${arius.template.record.quota.usage}")
    private String ariusTemplateQuotaUsageRecord;

    @Value("${arius.template.capacity.plan.region.statis}")
    private String ariusTemplateCapacityPlanRegionStatis;

    @Value("${es.dsl.analyze.index.name}")
    private String ariusDslAnalyzeResult;
    @Value("${es.dsl.metrics.index.name}")
    private String ariusDslMetrices;
    @Value("${arius.gateway.join.name}")
    private String ariusGatewayJoin;

    @Value("${es.dsl.template.index.name}")
    private String ariusDslTemplate;
    @Value("${es.template.field.index.name}")
    private String ariusTemplateField;
    @Value("${es.dsl.field.use.index.name}")
    public String ariusDslFieldUse;
    @Value("${es.template.mapping.index.name}")
    private String ariusTemplateMapping;
    @Value("${es.index.size.index.name}")
    private String ariusIndexSize;
    @Value("${es.index.cat.info}")
    private String ariusCatIndexInfo;
    @Value("${es.template.hit.index.name}")
    private String ariusTemplateHit;

    @Value("${es.appid.template.access.index.name}")
    private String  ariusAppidTemplateAccess;
    @Value("${es.indexname.access.index.name}")
    private String  ariusIndexNameAccess;
    @Value("${es.template.access.index.name}")
    private String  ariusTemplateAccess;
    @Value("${es.template.label.index.name}")
    private String  ariusTemplateLabel;

    @Value("${es.health.degress.index.name}")
    private String ariusIndexHealthDegress;
    @Value("${es.health.check.info.index.name}")
    private String ariusHealthCheck;

    @Value("${arius.gateway.original.log.index.name}")
    private String ariusCNGatewayOriginalLog;

    @Value("${es.node_index.info.index.name}")
    private String ariusStatsNodeIndexInfo;

    @Value("${es.node.info.index.name}")
    private String ariusStatsNodeInfo;

    @Value("${es.cluster.info.index.name}")
    private String ariusStatsClusterInfo;

    @Value("${es.dcdr.info.index.name}")
    private String ariusStatsDcdrInfo;

    @Value("${es.index.info.index.name}")
    private String ariusStatsIndexInfo;

    @Value("${es.index_node.info.index.name}")
    private String ariusStatsIndexNodeInfo;

    @Value("${es.ingest.info.index.name}")
    private String ariusStatsIngestInfo;

    @Value("${es.template.health.degree.index.name}")
    private String ariusTemplateHealthDegree;

    @Value("${es.template.value.index.name}")
    private String ariusTemplateValue;

    @Value("${es.template.value.record.index.name}")
    private String ariusTemplateValueRecord;

    @Value("${es.gateway.info.index.name}")
    private String ariusStatsGatewayInfo;

    @Value("${es.cluster.task.info.index.name:arius_stats_cluster_task_info}")
    private String ariusStatsClusterTaskInfo;

    @PostConstruct
    public void init() {
        LOGGER.info("class=DataCentreUtil||method=init||DataCentreUtil init start.");

        LOGGER.info("class=DataCentreUtil||method=init||env info={}",                           EnvUtil.getStr());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateQutoaNotiryRecord={}",     getAriusTemplateQutoaNotiryRecord());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateQuotaUsage={}",            getAriusTemplateQuotaUsage());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateQuotaUsageRecord={}",      getAriusTemplateQuotaUsageRecord());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateCapacityPlanRegionStatis={}", getAriusTemplateCapacityPlanRegionStatis());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslAnalyzeResult={}",              getAriusDslAnalyzeResult());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslMetrices={}",                   getAriusDslMetrices());
        LOGGER.info("class=DataCentreUtil||method=init||ariusGatewayJoin={}",                   getAriusGatewayJoin());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslTemplate={}",                   getAriusDslTemplate());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateField={}",                 getAriusTemplateField());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslFieldUse={}",                   getAriusDslFieldUse());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateMapping={}",               getAriusTemplateMapping());
        LOGGER.info("class=DataCentreUtil||method=init||ariusIndexSize={}",                     getAriusIndexSize());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateHit={}",                   getAriusTemplateHit());
        LOGGER.info("class=DataCentreUtil||method=init||ariusAppidTemplateAccess={}",           getAriusAppidTemplateAccess());
        LOGGER.info("class=DataCentreUtil||method=init||ariusIndexNameAccess={}",               getAriusIndexNameAccess());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateAccess={}",                getAriusTemplateAccess());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateLabel={}",                 getAriusTemplateLabel());
        LOGGER.info("class=DataCentreUtil||method=init||ariusIndexHealthDegress={}",            getAriusIndexHealthDegress());
        LOGGER.info("class=DataCentreUtil||method=init||ariusHealthCheck={}",                   getAriusHealthCheck());
        LOGGER.info("class=DataCentreUtil||method=init||ariusGatewayOriginalLog={}",            getAriusGatewayOriginalLog());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsNodeIndexInfo={}",            getAriusStatsNodeIndexInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsNodeInfo={}",                 getAriusStatsNodeInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsClusterInfo={}",              getAriusStatsClusterInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsDcdrInfo={}",                 getAriusStatsDcdrInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsIndexInfo={}",                getAriusStatsIndexInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsIndexNodeInfo={}",            getAriusStatsIndexNodeInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsIngestInfo={}",               getAriusStatsIngestInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateHealthDegree={}",          getAriusTemplateHealthDegree());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateValue={}",                 getAriusTemplateValue());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateValueRecord={}",           getAriusTemplateValueRecord());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsGatewayInfo={}",              getAriusStatsGatewayInfo());
        LOGGER.info("class=DataCentreUtil||method=init||DataCentreUtil init finished.");

    }

    public String getAriusTemplateQutoaNotiryRecord(){
        return getIndexName(ariusTemplateQutoaNotiryRecord);
    }

    public String getAriusTemplateQuotaUsage(){
        return getIndexName(ariusTemplateQuotaUsage);
    }

    public String getAriusTemplateQuotaUsageRecord(){
        return getIndexName(ariusTemplateQuotaUsageRecord);
    }

    public String getAriusTemplateCapacityPlanRegionStatis(){
        return getIndexName(ariusTemplateCapacityPlanRegionStatis);
    }

    public String getAriusTemplateValue(){
        return getIndexName(ariusTemplateValue);
    }

    public String getAriusTemplateValueRecord(){
        return getIndexName(ariusTemplateValueRecord);
    }

    public String getAriusTemplateHealthDegree(){
        return getIndexName(ariusTemplateHealthDegree);
    }

    public String getAriusStatsIngestInfo(){
        return getIndexName(ariusStatsIngestInfo);
    }

    public String getAriusStatsIndexNodeInfo(){
        return getIndexName(ariusStatsIndexNodeInfo);
    }

    public String getAriusStatsIndexInfo(){
        return getIndexName(ariusStatsIndexInfo);
    }

    public String getAriusStatsDcdrInfo(){
        return getIndexName(ariusStatsDcdrInfo);
    }

    public String getAriusStatsClusterInfo(){
        return getIndexName(ariusStatsClusterInfo);
    }

    public String getAriusStatsNodeInfo(){
        return getIndexName(ariusStatsNodeInfo);
    }

    public String getAriusStatsNodeIndexInfo(){
        return getIndexName(ariusStatsNodeIndexInfo);
    }

    public String getAriusTemplateLabel(){
        return getIndexName(ariusTemplateLabel);
    }

    public String getAriusTemplateAccess(){
        return getIndexName(ariusTemplateAccess);
    }

    public String getAriusIndexNameAccess(){
        return getIndexName(ariusIndexNameAccess);
    }

    public String getAriusAppidTemplateAccess(){
        return getIndexName(ariusAppidTemplateAccess);
    }

    public String getAriusGatewayJoin() { return getIndexName(ariusGatewayJoin); }

    public String getAriusGatewayOriginalLog(){ return getIndexName(ariusCNGatewayOriginalLog);}

    public String getAriusHealthCheck(){
        return getIndexName(ariusHealthCheck);
    }

    public String getAriusIndexHealthDegress(){
        return getIndexName(ariusIndexHealthDegress);
    }

    public String getAriusDslAnalyzeResult(){
        return getIndexName(ariusDslAnalyzeResult);
    }

    public String getAriusDslMetrices(){
        return getIndexName(ariusDslMetrices);
    }

    public String getAriusDslTemplate(){
        return getIndexName(ariusDslTemplate);
    }

    public String getAriusTemplateField(){
        return getIndexName(ariusTemplateField);
    }

    public String getAriusDslFieldUse(){
        return getIndexName(ariusDslFieldUse);
    }

    public String getAriusTemplateMapping(){
        return getIndexName(ariusTemplateMapping);
    }

    public String getAriusIndexSize(){return getIndexName(ariusIndexSize);}

    public String getAriusTemplateHit(){
        return getIndexName(ariusTemplateHit);
    }

    public String getAriusStatsGatewayInfo(){
        return ariusStatsGatewayInfo;
    }

    public String getAriusCatIndexInfo(){return getIndexName(ariusCatIndexInfo);}

    public String getAriusStatsClusterTaskInfo(){return getIndexName(ariusStatsClusterTaskInfo);}

    /*************************************** private method ***************************************/
    private String getIndexName(String originIndexName) {
        return originIndexName;
    }
}
