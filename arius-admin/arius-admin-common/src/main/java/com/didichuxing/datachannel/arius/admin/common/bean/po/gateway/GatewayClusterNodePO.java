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
    
    /**
     * 集群名称
     */
    private String  clusterName;
    
    /**
     * 主机名
     */
    private String  hostName;
    
    /**
     * 港口
     */
    private Integer port;
    
    /**
     * 心跳时间
     */
    private Date    heartbeatTime;
    
    /**
     * 节点名称
     */
    private String nodeName;

}