package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
