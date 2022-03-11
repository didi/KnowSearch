package com.didichuxing.datachannel.arius.admin.metadata.listen;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESNodeStats;
import com.didichuxing.datachannel.arius.admin.common.event.metrics.*;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.lucene.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 招行内部，把这个实现移动zh-extend中去
 * 这里注释的代码是之前指标对接到行内kafka的实现方式
 * 如果行内有新的指标系统对接，那需要行内同学自己在这个类中实现
 */
@Service
public class MetaDataMonitorMetrics2ZHListener implements ApplicationListener<MetaDataMetricsEvent> {
    protected static final ILog LOGGER = LogFactory.getLog(MetaDataMonitorMetrics2N9eListener.class);

    //@Autowired
//    private ZHKafkaWriterService ZHKafkaWriterService;

    //@Value("${zh.metrics.kafka.topic}")
    private String zhMetricsKafkaTopic;

    private ThreadPoolExecutor esExecutor = new ThreadPoolExecutor(10, 20, 1000, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(4000),
            new NamedThreadFactory("Arius-Meta-MonitorMetricsSender-ZH"),
            (r, e) -> LOGGER.warn("class=MetaDataMonitorMetrics2ZHListener||msg=Arius-Meta-MonitorMetricsSender-ZH Deque is blocked, taskCount:{}" + e.getTaskCount()));

    @Override
    public void onApplicationEvent(MetaDataMetricsEvent event) {
        esExecutor.execute(() -> {
            if(event instanceof MetricsMonitorClusterEvent){
                MetricsMonitorClusterEvent monitorClusterEvent = (MetricsMonitorClusterEvent)event;
                sendMetrics(monitorClusterEvent);
            }

            if(event instanceof MetricsMonitorCollectTimeEvent){
                MetricsMonitorCollectTimeEvent collectTimeEvent = (MetricsMonitorCollectTimeEvent)event;
                sendMetrics(collectTimeEvent);
            }

            if(event instanceof MetricsMonitorIndexEvent){
                MetricsMonitorIndexEvent monitorIndexEvent = (MetricsMonitorIndexEvent)event;
                sendMetrics(monitorIndexEvent);
            }

            if(event instanceof MetricsMonitorNodeEvent){
                MetricsMonitorNodeEvent monitorNodeEvent = (MetricsMonitorNodeEvent)event;
                sendMetrics(monitorNodeEvent);
            }
        } );
    }

    private void sendMetrics(MetricsMonitorClusterEvent event){
        List<ESClusterStats> esIndexStatsList = event.getEsClusterStatsList();

//        for(ESClusterStats esClusterStats : esIndexStatsList){
//            Map<String, String> metrics = CommonUtils.objectToMap(esClusterStats.getStatis());
//            if(MapUtils.isEmpty(metrics)){return;}
//
//            ZHMetricsData zhMetricsData = new ZHMetricsData();
//            zhMetricsData.setName("elasticsearch_cluster");
//            zhMetricsData.setTimestamp(System.currentTimeMillis() / 1000);
//            zhMetricsData.putTag("host",                event.getHostName());
//            zhMetricsData.putTag("cluster_name",        esClusterStats.getCluster());
//            zhMetricsData.putTag("service_instance",    esClusterStats.getCluster());
//            zhMetricsData.putTag("service_type",        "elasticsearch");
//            zhMetricsData.setFields(metrics);
//
//            sendData(Arrays.asList(zhMetricsData));
//        }
    }

    private void sendMetrics(MetricsMonitorCollectTimeEvent event){
//        ZHMetricsData zhMetricsData = new ZHMetricsData();
//        zhMetricsData.setName("elasticsearch_node_collect_time");
//        zhMetricsData.setTimestamp(System.currentTimeMillis() / 1000);
//        zhMetricsData.putTag("host",                event.getHostName());
//        zhMetricsData.putTag("cluster_name",        event.getCluster());
//        zhMetricsData.putTag("type",                event.getType());
//        zhMetricsData.putTag("level",               String.valueOf(event.getClusterLevel()));
//        zhMetricsData.putTag("service_instance",    event.getCluster());
//        zhMetricsData.putTag("service_type",        "elasticsearch");
//        zhMetricsData.putField("value", String.valueOf(event.getTime()));
//
//        sendData(Arrays.asList(zhMetricsData));
    }

    private void sendMetrics(MetricsMonitorIndexEvent event){
        List<ESIndexStats> esIndexStatsList = event.getEsIndexStatsList();
        if(CollectionUtils.isEmpty(esIndexStatsList)){
            return;
        }

        String hostName = event.getHostName();

        for(ESIndexStats esIndexStats : esIndexStatsList){
//            if(MapUtils.isEmpty(esIndexStats.getMetrics())){continue;}
//
//            ZHMetricsData zhMetricsData = new ZHMetricsData();
//            zhMetricsData.setName("elasticsearch_index");
//            zhMetricsData.setTimestamp(System.currentTimeMillis() / 1000);
//            zhMetricsData.putTag("host",                hostName);
//            zhMetricsData.putTag("cluster_name",        esIndexStats.getCluster());
//            zhMetricsData.putTag("index",               esIndexStats.getIndex());
//            zhMetricsData.putTag("service_instance",    esIndexStats.getTemplate());
//            zhMetricsData.putTag("service_type",        "es_index");
//
//            for(String key : esIndexStats.getMetrics().keySet()){
//                zhMetricsData.putField(key, esIndexStats.getMetrics().get(key));
//            }
//            sendData(Arrays.asList(zhMetricsData));
        }
    }

    private void sendMetrics(MetricsMonitorNodeEvent event){
        List<ESNodeStats> esNodeStatsList = event.getEsNodeStats();
        if(CollectionUtils.isEmpty(esNodeStatsList)){
            return;
        }

        String hostName = event.getHostName();

        for(ESNodeStats esNodeStats : esNodeStatsList){
            if(MapUtils.isEmpty(esNodeStats.getMetrics())){continue;}

//            ZHMetricsData zhMetricsData = new ZHMetricsData();
//            zhMetricsData.setName("elasticsearch_node");
//            zhMetricsData.setTimestamp(System.currentTimeMillis() / 1000);
//            zhMetricsData.putTag("host",                hostName);
//            zhMetricsData.putTag("node_host",           esNodeStats.getIp());
//            zhMetricsData.putTag("node_port",           esNodeStats.getPort());
//            zhMetricsData.putTag("node_id",             esNodeStats.getNode());
//            zhMetricsData.putTag("cluster_name",        esNodeStats.getCluster());
//            zhMetricsData.putTag("service_instance",    esNodeStats.getCluster());
//            zhMetricsData.putTag("service_type",        "elasticsearch");
//
//            for(String key : esNodeStats.getMetrics().keySet()){
//                zhMetricsData.putField(key, esNodeStats.getMetrics().get(key));
//            }
//            sendData(Arrays.asList(zhMetricsData));
        }
    }

//    public void sendData(List<ZHMetricsData> datas){
//        if(CollectionUtils.isEmpty(datas)){return;}
//
//        for(ZHMetricsData zhMetricsData : datas){
//            if(null == zhMetricsData){continue;}
//
//            if(!EnvUtil.isOnline()) {
//                LOGGER.warn("class=ZHMetricsSenderService||method=sendData||datas={}", JSON.toJSONString(zhMetricsData));
//            }
//
//            ZHKafkaWriterService.sendMessage(zhMetricsKafkaTopic, JSON.toJSONString(zhMetricsData), (recordMetadata, e) -> {
//                if(null != e){
//                    LOGGER.warn("class=ZHMetricsSenderService||method=sendData||datas={}||msg=kafka sendMessage failed!",
//                            JSON.toJSONString(zhMetricsData), e);
//                    return;
//                }
//            });
//        }
//
//        return;
//    }
}
