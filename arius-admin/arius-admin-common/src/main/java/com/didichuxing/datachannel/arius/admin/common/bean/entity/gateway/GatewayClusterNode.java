package com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayClusterNode extends BaseEntity {

    private Integer id;

    private String  clusterName;

    private String  hostName;

    private Integer port;

    private Date    heartbeatTime;

}
