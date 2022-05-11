package com.didichuxing.datachannel.arius.admin.common.bean.po.gateway;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayClusterNodePO extends BasePO {

    private Integer id;

    private String  clusterName;

    private String  hostName;

    private Integer port;

    private Date    heartbeatTime;

}
