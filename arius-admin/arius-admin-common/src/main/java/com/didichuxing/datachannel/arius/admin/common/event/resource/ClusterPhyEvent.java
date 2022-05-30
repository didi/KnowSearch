package com.didichuxing.datachannel.arius.admin.common.event.resource;

/**
 * Created by linyunan on 2021-06-03
 */
public class ClusterPhyEvent extends ClusterEvent {

    private String  clusterPhyName;

    private Integer projectId;

    public ClusterPhyEvent(String clusterPhyName, Integer projectId) {
        super(clusterPhyName);
        this.clusterPhyName = clusterPhyName;
        this.projectId = projectId;
    }

    public String getClusterPhyName() {
        return clusterPhyName;
    }

    public void setClusterPhyName(String clusterPhyName) {
        this.clusterPhyName = clusterPhyName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
}