package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.clusterOpRestart;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fengqiongfeng
 * @date 2020/8/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterOpRestartOrderDetail extends AbstractOrderDetail {
    /**
     * 物理集群id
     */
    private Long phyClusterId;
    /**
     * 物理集群名称
     */
    private String phyClusterName;
    /**
     * 角色顺序，如：airepo-masternode,airepo-clientnode,airepo-datanode
     */
    private String roleOrder;
}