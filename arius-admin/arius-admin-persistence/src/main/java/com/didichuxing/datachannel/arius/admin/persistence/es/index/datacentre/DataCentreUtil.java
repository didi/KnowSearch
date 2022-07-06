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

    @Value("${es.index.size.index.name:arius.index.size}")
    private String ariusIndexSize;
 

    // projectId维度访问次数索引
    @Value("${es.appid.template.access.index.name:arius.appid.template.access}")
    private String ariusProjectIdTemplateAccess;
    // 索引维度访问次数索引
    @Value("${es.indexname.access.index.name:arius.indexname.access}")
    private String ariusIndexNameAccess;
    // 索引模板维度访问次数索引
    @Value("${es.template.access.index.name:arius.template.access}")
    private String  ariusTemplateAccess;

    // es 索引标签数据
    @Value("${es.template.label.index.name:v2.arius.template.label}")
    private String  ariusTemplateLabel;

   

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


  

    // es 索引价值
    @Value("${es.template.value.index.name:arius.template.value}")
    private String ariusTemplateValue;
 

    // gateway 访问统计索引
    @Value("${es.gateway.info.index.name:arius.gateway.join}")
    private String ariusStatsGatewayInfo;

    @Value("${es.cluster.task.info.index.name:arius_stats_cluster_task_info}")
    private String ariusStatsClusterTaskInfo;

    @PostConstruct
    public void init() {
        LOGGER.info("class=DataCentreUtil||method=init||DataCentreUtil init start.");

        LOGGER.info("class=DataCentreUtil||method=init||env info={}",                           EnvUtil.getStr());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslAnalyzeResult={}",              getAriusDslAnalyzeResult());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslMetrices={}",                   getAriusDslMetrices());
        LOGGER.info("class=DataCentreUtil||method=init||ariusGatewayJoin={}",                   getAriusGatewayJoin());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslTemplate={}",                   getAriusDslTemplate());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslFieldUse={}",                   getAriusDslFieldUse());
        LOGGER.info("class=DataCentreUtil||method=init||ariusIndexSize={}",                     getAriusIndexSize());
        LOGGER.info("class=DataCentreUtil||method=init||ariusProjectIdTemplateAccess={}",           getAriusProjectIdTemplateAccess());
        LOGGER.info("class=DataCentreUtil||method=init||ariusIndexNameAccess={}",               getAriusIndexNameAccess());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateAccess={}",                getAriusTemplateAccess());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateLabel={}",                 getAriusTemplateLabel());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsNodeIndexInfo={}",            getAriusStatsNodeIndexInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsNodeInfo={}",                 getAriusStatsNodeInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsClusterInfo={}",              getAriusStatsClusterInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsDcdrInfo={}",                 getAriusStatsDcdrInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsIndexInfo={}",                getAriusStatsIndexInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsIndexNodeInfo={}",            getAriusStatsIndexNodeInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsIngestInfo={}",               getAriusStatsIngestInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateValue={}",                 getAriusTemplateValue());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsGatewayInfo={}",              getAriusStatsGatewayInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsDashBoardInfo={}",            getAriusStatsDashBoardInfo());
        LOGGER.info("class=DataCentreUtil||method=init||DataCentreUtil init finished.");

    }

   

 

   

   

    public String getAriusTemplateValue(){
        return getIndexName(ariusTemplateValue);
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

    public String getAriusProjectIdTemplateAccess(){
        return getIndexName(ariusProjectIdTemplateAccess);
    }

    public String getAriusGatewayJoin() { return getIndexName(ariusGatewayJoin); }


    public String getAriusDslAnalyzeResult(){
        return getIndexName(ariusDslAnalyzeResult);
    }

    public String getAriusDslMetrices(){
        return getIndexName(ariusDslMetrices);
    }

    public String getAriusDslTemplate(){
        return getIndexName(ariusDslTemplate);
    }

    

    public String getAriusDslFieldUse(){
        return getIndexName(ariusDslFieldUse);
    }

    

    public String getAriusIndexSize(){return getIndexName(ariusIndexSize);}

   

    public String getAriusStatsGatewayInfo(){ return ariusStatsGatewayInfo;}

    public String getAriusCatIndexInfo(){return getIndexName(ariusCatIndexInfo);}

    public String getAriusStatsClusterTaskInfo(){return getIndexName(ariusStatsClusterTaskInfo);}

    public String getAriusStatsDashBoardInfo(){ return ariusStatsDashboardInfo;}

    /*************************************** private method ***************************************/
    private String getIndexName(String originIndexName) {
        return originIndexName;
    }
}