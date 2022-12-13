package com.didichuxing.datachannel.arius.admin.persistence.es.index.datacentre;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

import lombok.NoArgsConstructor;

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
    
    

   
 

    // projectId维度访问次数索引
    @Value("${es.projectId.template.access.index.name:arius.appid.template.access}")
    private String ariusProjectIdTemplateAccess;
    // 索引模板维度访问次数索引
    @Value("${es.template.access.index.name:arius.template.access}")
    private String  ariusTemplateAccess;


    
    @Value("${es.node.info.index.name:arius_stats_node_info}")
    private String ariusStatsNodeInfo;
    @Value("${es.dashboard.info.name:arius_stats_dashboard_info}")
    private String ariusStatsDashboardInfo;
    @Value("${es.index.info.index.name:arius_stats_index_info}")
    private String ariusStatsIndexInfo;
    @Value("${es.cat.index.info:arius_cat_index_info}")
    private String ariusCatIndexInfo;
    @Value("${es.cat.shard.info:arius_cat_shard_info}")
    private String ariusCatShardInfo;
    @Value("${es.cluster.info.index.name:arius_stats_cluster_info}")
    private String ariusStatsClusterInfo;
  
   
   


  
    
 

    // gateway 访问统计索引
    @Value("${es.gateway.info.index.name:arius.gateway.join}")
    private String ariusStatsGatewayInfo;

    @Value("${es.cluster.task.info.index.name:arius_stats_cluster_task_info}")
    private String ariusStatsClusterTaskInfo;

    @Value("${es.fast.dump.metrics.index.name:fastdump_move_metrics_info}")
    private String fastDumpMetrics;

    @PostConstruct
    public void init() {
        LOGGER.info("class=DataCentreUtil||method=init||DataCentreUtil init start.");

        LOGGER.info("class=DataCentreUtil||method=init||env info={}",                           EnvUtil.getStr());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslAnalyzeResult={}",              getAriusDslAnalyzeResult());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslMetrices={}",                   getAriusDslMetrices());
        LOGGER.info("class=DataCentreUtil||method=init||ariusGatewayJoin={}",                   getAriusGatewayJoin());
        LOGGER.info("class=DataCentreUtil||method=init||ariusDslTemplate={}",                   getAriusDslTemplate());
        LOGGER.info("class=DataCentreUtil||method=init||ariusProjectIdTemplateAccess={}",           getAriusProjectIdTemplateAccess());
       
        LOGGER.info("class=DataCentreUtil||method=init||ariusTemplateAccess={}",                getAriusTemplateAccess());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsClusterInfo={}",              getAriusStatsClusterInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsIndexInfo={}",                getAriusStatsIndexInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsGatewayInfo={}",              getAriusStatsGatewayInfo());
        LOGGER.info("class=DataCentreUtil||method=init||ariusStatsDashBoardInfo={}",            getAriusStatsDashBoardInfo());
        LOGGER.info("class=DataCentreUtil||method=init||DataCentreUtil init finished.");

    }
    
   

    public String getAriusStatsIndexInfo(){
        return getIndexName(ariusStatsIndexInfo);
    }

   

    public String getAriusStatsClusterInfo(){
        return getIndexName(ariusStatsClusterInfo);
    }

    public String getAriusStatsNodeInfo(){
        return getIndexName(ariusStatsNodeInfo);
    }

 

  
    public String getAriusTemplateAccess(){
        return getIndexName(ariusTemplateAccess);
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

    

   

    

 
   

    public String getAriusStatsGatewayInfo(){ return ariusStatsGatewayInfo;}

    public String getAriusCatIndexInfo(){return getIndexName(ariusCatIndexInfo);}

    public String getAriusCatShardInfo(){return getIndexName(ariusCatShardInfo);}

    public String getAriusStatsClusterTaskInfo(){return getIndexName(ariusStatsClusterTaskInfo);}

    public String getAriusStatsDashBoardInfo(){ return ariusStatsDashboardInfo;}

    public String getFastDumpMetrics() {
        return getIndexName(fastDumpMetrics);
    }

    /*************************************** private method ***************************************/
    private String getIndexName(String originIndexName) {
        return originIndexName;
    }
}