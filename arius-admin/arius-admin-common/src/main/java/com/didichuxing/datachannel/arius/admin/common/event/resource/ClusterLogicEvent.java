package com.didichuxing.datachannel.arius.admin.common.event.resource;

/**
 * Created by linyunan on 2021-06-03
 */
public class ClusterLogicEvent extends ClusterEvent {

    private Long    clusterLogicId;

    private Integer appId;

    public ClusterLogicEvent(Long clusterLogicId, Integer appId) {
        super(clusterLogicId);
        this.clusterLogicId = clusterLogicId;
        this.appId = appId;
    }

    public Long getClusterLogicId() {
        return clusterLogicId;
    }

    public void setClusterLogicId(Long clusterLogicId) {
        this.clusterLogicId = clusterLogicId;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public ClusterLogicEvent(Object source) {
        super(source);
    }
}
