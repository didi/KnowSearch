package com.didichuxing.datachannel.arius.admin.common.event.resource;

/**
 * Created by linyunan on 2021-06-03
 */
public class ClusterLogicEvent extends ClusterEvent {

    private Long    clusterLogicId;

    private Integer projectId;

    public ClusterLogicEvent(Long clusterLogicId, Integer projectId) {
        super(clusterLogicId);
        this.clusterLogicId = clusterLogicId;
        this.projectId = projectId;
    }

    public Long getClusterLogicId() {
        return clusterLogicId;
    }

    public void setClusterLogicId(Long clusterLogicId) {
        this.clusterLogicId = clusterLogicId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public ClusterLogicEvent(Object source) {
        super(source);
    }
}