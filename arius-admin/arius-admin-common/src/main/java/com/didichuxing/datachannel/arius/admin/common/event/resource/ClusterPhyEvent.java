package com.didichuxing.datachannel.arius.admin.common.event.resource;

/**
 * @author linyunan
 * @date 2021-06-03
 */
public class ClusterPhyEvent extends ClusterEvent {

    private String  clusterPhyName;

    private String operator;

    public ClusterPhyEvent(String clusterPhyName, String operator) {
        super(clusterPhyName);
        this.clusterPhyName = clusterPhyName;
        this.operator = operator;
    }

    public String getClusterPhyName() {
        return clusterPhyName;
    }

    public void setClusterPhyName(String clusterPhyName) {
        this.clusterPhyName = clusterPhyName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
