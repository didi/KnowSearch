package com.didichuxing.datachannel.arius.admin.common.bean.common.ecm;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESClusterRoleHost implements Serializable {

    /**
     * ip:port 主機名:port = hostname:port
     */
    private String address;

    /**
     * 角色名称
     */
    private String role;

    /**
     * 主機名
     */
    private String hostname;

    /**
     * IP
     */
    private String ip;

    /**
     * 端口号
     */
    private String port;

    /**
     * 冷热节点标识
     */
    private Boolean beCold;

    /**
     * 机器规格 例如32C-64G-SSD-6T
     */
    private String machineSpec;

    /**
     * regionId
     */
    private Integer regionId;

    /**
     * attributes, 逗号分隔  key1:value1,key2:value2
     */
    private String attributes;
}