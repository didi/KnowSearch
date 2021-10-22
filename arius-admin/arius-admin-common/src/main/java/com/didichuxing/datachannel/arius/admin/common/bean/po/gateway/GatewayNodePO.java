package com.didichuxing.datachannel.arius.admin.common.bean.po.gateway;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import lombok.Data;

/**
 * @author d06679
 */
@Data
public class GatewayNodePO extends BasePO {

    private Integer id;

    private String  clusterName;

    private String  hostName;

    private Integer port;

    private Date    heartbeatTime;

}
