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

    @Value("${arius.template.qutoa.notiry.record:arius_template_qutoa_notiry_record}")
    private String ariusTemplateQutoaNotiryRecord;
    @Value("${arius.template.quota.usage:arius_template_quota_usage}")
    private String ariusTemplateQuotaUsage;
    @Value("${arius.template.record.quota.usage:record_arius_template_quota_usage}")
    private String ariusTemplateQuotaUsageRecord;
    @Value("${arius.template.capacity.plan.region.statis:arius_capacity_plan_region_statis}")
    private String ariusTemplateCapacityPlanRegionStatis;

    @Value("${es.dsl.analyze.index.name:arius.dsl.analyze.result}")
    private String ariusDslAnalyzeResult;
    @Value("${es.dsl.metrics.index.name:arius.dsl.metrics}")
    private String ariusDslMetrices;
    @Value("${arius.gateway.join.name:arius.gateway.join}")
    private String ariusGatewayJoin;

    @Value("${es.dsl.template.index.name:arius.dsl.template}")
    private String ariusDslTemplate;
    @Value("${es.template.field.index.name:arius.template.field}")
    private String ariusTemplateField;
    @Value("${es.dsl.field.use.index.name:arius.dsl.field.use}")
    public String ariusDslFieldUse;
    @Value("${es.template.mapping.index.name:arius.template.mapping}")
    private String ariusTemplateMapping;
    @Value("${es.index.size.index.name:arius.index.size}")
    private String ariusIndexSize;
    @Value("${es.template.hit.index.name:arius.template.hit}")
    private String ariusTemplateHit;

    // appid维度访问次数索引
    @Value("${es.appid.template.access.index.name:arius.appid.template.access}")
    private String  ariusAppidTemplateAccess;
    // 索引维度访问次数索引
    @Value("${es.indexname.access.index.name:arius.indexname.access}")
    private String  ariusIndexNameAccess;
    // 索引模板维度访问次数索引
    @Value("${es.template.access.index.name:arius.template.access}")
    private String  ariusTemplateAccess;

    // es 索引标签数据
    @Value("${es.template.label.index.name:v2.arius.template.label}")
    private String  ariusTemplateLabel;

    // 国内原始gateway日志索引
    @Value("${arius.gateway.original.log.index.name:foundation_fd.data-online.arius.gateway}")
    private String ariusCNGatewayOriginalLog;

    // 不同维度的es监控数据索引
    @Value("${es.node_index.info.index.name:arius_stats_node_index_info}")
    private String ariusStatsNodeIndexInfo;
    @Value("${es.node.info.index.name:arius_stats_node_info}")
    private String ariusStatsNodeInfo;
    @Value("${es.dashboard.info.name:arius_stats_dashboard_info}")
    private String ariusStatsDashboardInfo;
    @Value("${es.index.info.index.name:arius_stats_index_info}")
    private String ariusStatsIndexInfo;
    @Value("${es.index.cat.info:arius_cat_index_info}")
    private String ariusCatIndexInfo;
    @Value("${es.cluster.info.index.name:arius_stats_cluster_info}")
    private String ariusStatsClusterInfo;
    @Value("${es.index_node.info.index.name:arius_stats_index_node_info}")
    private String ariusStatsIndexNodeInfo;
    @Value("${es.ingest.info.index.name:arius_stats_ingest_info}")
    private String ariusStatsIngestInfo;
    @Value("${es.dcdr.info.index.name:arius_stats_dcdr_info}")
    private String ariusStatsDcdrInfo;


    // es 索引健康分
    @Value("${es.template.health.degree.index.name:arius.template.health.degree}")
    private String ariusTemplateHealthDegree;

    // es 索引价值
    @Value("${es.template.value.index.name:arius.template.value}")
    private String ariusTemplateValue;
    @Value("${es.template.value.record.index.name:record.arius.template.value}")
    private String ariusTemplateValueRecord;

    // gateway 访问统计索引
    @Value("${es.gateway.info.index.name:arius.gateway.join}")
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
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsDashBoardInfo={}",            getAriusStatsDashBoardInfo());
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

    public String getAriusStatsGatewayInfo(){ return ariusStatsGatewayInfo;}

    public String getAriusCatIndexInfo(){return getIndexName(ariusCatIndexInfo);}

    public String getAriusStatsClusterTaskInfo(){return getIndexName(ariusStatsClusterTaskInfo);}

    public String getAriusStatsDashBoardInfo(){ return ariusStatsDashboardInfo;}

    /*************************************** private method ***************************************/
    private String getIndexName(String originIndexName) {
        return originIndexName;
    }
}
