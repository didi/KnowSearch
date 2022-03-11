package com.didichuxing.datachannel.arius.admin.metadata.listen;

import com.didichuxing.datachannel.arius.admin.common.event.metrics.*;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

/**
 * to Prometheus
 */
@Service
public class MetaDataMonitorMetrics2PrometheusListener implements ApplicationListener<MetaDataMetricsEvent> {

    @Override
    public void onApplicationEvent(MetaDataMetricsEvent event) {
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
    }

    private void sendMetrics(MetricsMonitorClusterEvent event){
        //todo：实现指标吐到Prometheus的逻辑
    }

    private void sendMetrics(MetricsMonitorCollectTimeEvent event){
        //todo：实现指标吐到Prometheus的逻辑
    }

    private void sendMetrics(MetricsMonitorIndexEvent event){
        //todo：实现指标吐到Prometheus的逻辑
    }

    private void sendMetrics(MetricsMonitorNodeEvent event){
        //todo：实现指标吐到Prometheus的逻辑
    }
}
