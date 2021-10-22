package com.didichuxing.datachannel.arius.admin.client.bean.common;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-07-26
 */
@Data
public class GatewayHeartbeat {

    /**
     * "集群名称"
     */
    private String clusterName;

    /**
     * 主机名
     */
    private String hostName;

    /**
     * 端口
     */
    private int    port;

}
