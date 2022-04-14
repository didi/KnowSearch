package com.didichuxing.datachannel.arius.admin.common.event.resource;

/**
 * Created by linyunan on 2021-06-03
 */
public class ClusterPhyEvent extends ClusterEvent {

    private String  clusterPhyName;

    private Integer appId;

    public ClusterPhyEvent(String clusterPhyName, Integer appId) {
        super(clusterPhyName);
        this.clusterPhyName = clusterPhyName;
        this.appId = appId;
    }

    public String getClusterPhyName() {
        return clusterPhyName;
    }

    public void setClusterPhyName(String clusterPhyName) {
        this.clusterPhyName = clusterPhyName;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }
}
