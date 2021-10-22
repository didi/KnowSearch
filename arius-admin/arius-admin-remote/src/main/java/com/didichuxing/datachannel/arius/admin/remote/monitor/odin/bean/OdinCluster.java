package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.Data;

@Data
public class OdinCluster {
    /**
     * cluster节点
     */
    private String   name;
    /**
     * 域信息
     */
    private String   region;
    /**
     * su信息
     */
    private String   su;
    /**
     * 机房信息
     */
    private String[] cluster;
}
