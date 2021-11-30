package com.didichuxing.datachannel.arius.admin.client.bean.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-07-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
