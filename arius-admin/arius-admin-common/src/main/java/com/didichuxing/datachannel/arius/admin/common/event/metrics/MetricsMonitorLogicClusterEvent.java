package com.didichuxing.datachannel.arius.admin.common.event.metrics;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;

public class MetricsMonitorLogicClusterEvent extends MetaDataMetricsEvent {
    private List<ESClusterStats> esClusterStatsList;
    private String hostName;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MetricsMonitorLogicClusterEvent(Object source, List<ESClusterStats> esClusterStatsList, String hostName) {
        super(source);
        this.esClusterStatsList = esClusterStatsList;
        this.hostName = hostName;
    }

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MetricsMonitorLogicClusterEvent(Object source) {
        super(source);
    }

    public List<ESClusterStats> getEsClusterStatsList() {
        return esClusterStatsList;
    }

    public String getHostName() {
        return hostName;
    }
}