package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm;

import java.io.Serializable;

import lombok.Data;

@Data
public class ESClusterRoleDocker implements Serializable {
    /**
     * 集群角色(masternode/datanode/clientnode)
     */
    private String  role;

    /**
     * pod数量
     */
    private Integer podNumber;

    /**
     * 单机实例数
     */
    private Integer pidCount;

    /**
     * 机器规格
     */
    private String  machineSpec;
}
