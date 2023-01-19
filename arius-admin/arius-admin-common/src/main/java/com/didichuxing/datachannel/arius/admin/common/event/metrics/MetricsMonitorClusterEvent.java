package com.didichuxing.datachannel.arius.admin.common.event.metrics;

import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESClusterStats;
import com.didiglobal.knowframework.elasticsearch.client.response.cluster.ESClusterHealthResponse;

public class MetricsMonitorClusterEvent extends MetaDataMetricsEvent {

    private List<ESClusterStats>                     esClusterStatsList;

    private Map<ClusterPhy, ESClusterHealthResponse> clusterHealthResponseMap;

    private String                                   hostName;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MetricsMonitorClusterEvent(Object source, List<ESClusterStats> esClusterStatsList,
                                      Map<ClusterPhy, ESClusterHealthResponse> clusterHealthResponseMap,
                                      String hostName) {
        super(source);
        this.esClusterStatsList = esClusterStatsList;
        this.clusterHealthResponseMap = clusterHealthResponseMap;
        this.hostName = hostName;
    }

    public List<ESClusterStats> getEsClusterStatsList() {
        return esClusterStatsList;
    }

    public Map<ClusterPhy, ESClusterHealthResponse> getClusterHealthResponseMap() {
        return clusterHealthResponseMap;
    }

    public String getHostName() {
        return hostName;
    }
}