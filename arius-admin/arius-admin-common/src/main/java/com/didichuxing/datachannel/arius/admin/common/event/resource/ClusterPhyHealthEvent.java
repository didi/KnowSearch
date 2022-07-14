package com.didichuxing.datachannel.arius.admin.common.event.resource;

import org.springframework.context.ApplicationEvent;

public class ClusterPhyHealthEvent extends ApplicationEvent {

    private String clusterPhyName;

    public ClusterPhyHealthEvent(Object source, String clusterPhyName) {
        super(source);
        this.clusterPhyName = clusterPhyName;
    }

    public String getClusterPhyName() {
        return clusterPhyName;
    }

    public void setClusterPhyName(String clusterPhyName) {
        this.clusterPhyName = clusterPhyName;
    }
}
