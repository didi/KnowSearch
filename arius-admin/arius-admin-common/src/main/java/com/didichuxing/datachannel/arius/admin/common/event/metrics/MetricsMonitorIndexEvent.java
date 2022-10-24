package com.didichuxing.datachannel.arius.admin.common.event.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESDataTempBean;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;

import java.util.List;

public class MetricsMonitorIndexEvent extends MetaDataMetricsEvent {

    private List<ESDataTempBean> esDataTempBeans;

    private List<ESIndexStats>   esIndexStatsList;

    private Integer              clusterLevel;

    private String               hostName;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MetricsMonitorIndexEvent(Object source, List<ESDataTempBean> esDataTempBeans,
                                    List<ESIndexStats> esIndexStatsList, Integer clusterLevel, String hostName) {
        super(source);
        this.esDataTempBeans = esDataTempBeans;
        this.esIndexStatsList = esIndexStatsList;
        this.clusterLevel = clusterLevel;
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

    public List<ESIndexStats> getEsIndexStatsList() {
        return esIndexStatsList;
    }
}
