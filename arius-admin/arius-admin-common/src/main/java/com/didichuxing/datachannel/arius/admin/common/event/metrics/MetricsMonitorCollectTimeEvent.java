package com.didichuxing.datachannel.arius.admin.common.event.metrics;

public class MetricsMonitorCollectTimeEvent extends MetaDataMetricsEvent {

    private String  cluster;

    private Integer clusterLevel;

    private String  type;

    private double  time;

    private String  hostName;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MetricsMonitorCollectTimeEvent(Object source, String type, double time, String cluster, Integer clusterLevel,
                                          String hostName) {
        super(source);
        this.type = type;
        this.time = time;
        this.cluster = cluster;
        this.clusterLevel = clusterLevel;
        this.hostName = hostName;
    }

    public String getType() {
        return type;
    }

    public double getTime() {
        return time;
    }

    public String getCluster() {
        return cluster;
    }

    public Integer getClusterLevel() {
        return clusterLevel;
    }

    public String getHostName() {
        return hostName;
    }
}