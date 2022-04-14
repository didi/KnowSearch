package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleHost;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseClusterHostOrderDetail extends AbstractOrderDetail{

    /**
     * 物理集群名称
     */
    private String  phyClusterName;

    /**
     * type 3:docker 4:host
     */
    private int type;

    /**
     * 单机实例数
     */
    private Integer                 pidCount;


    /**
     * 集群角色 对应的变动的主机列表
     */
    private List<ESClusterRoleHost> roleClusterHosts;
}
