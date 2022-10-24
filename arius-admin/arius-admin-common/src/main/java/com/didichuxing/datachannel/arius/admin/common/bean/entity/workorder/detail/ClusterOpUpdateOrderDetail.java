package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author fengqiongfeng
 * @date 2020/8/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterOpUpdateOrderDetail extends AbstractOrderDetail {
    /**
     * 物理集群id
     */
    private Long                    phyClusterId;
    /**
     * 物理集群名称
     */
    private String                  phyClusterName;
    /**
     * 角色顺序，如：airepo-masternode,airepo-clientnode,airepo-datanode
     */
    private String                  roleOrder;
    /**
     * 集群角色 对应主机列表
     */
    private List<ESClusterRoleHost> roleClusterHosts;
    /**
     * 集群版本
     */
    private String                  esVersion;
}