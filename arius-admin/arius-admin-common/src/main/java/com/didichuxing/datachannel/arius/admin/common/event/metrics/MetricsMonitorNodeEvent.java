package com.didichuxing.datachannel.arius.admin.common.event.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESDataTempBean;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESNodeStats;

import java.util.List;

public class MetricsMonitorNodeEvent extends MetaDataMetricsEvent {

    private List<ESDataTempBean> esDataTempBeans;

    private List<ESNodeStats>    esNodeStatsList;

    private Integer              clusterLevel;

    private String               hostName;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MetricsMonitorNodeEvent(Object source, List<ESDataTempBean> esDataTempBeans,
                                   List<ESNodeStats> esNodeStatsList, Integer clusterLevel, String hostName) {
        super(source);
        this.esDataTempBeans = esDataTempBeans;
        this.clusterLevel = clusterLevel;
        this.esNodeStatsList = esNodeStatsList;
        this.hostName = hostName;
    }

    public List<ESDataTempBean> getEsDataTempBeans() {
        return esDataTempBeans;
    }

    public Integer getClusterLevel() {
        return clusterLevel;
    }

    public String getHostName() {
        return hostName;
    }

    public List<ESNodeStats> getEsNodeStats() {
        return esNodeStatsList;
    }
}
