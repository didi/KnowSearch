package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.Data;

/**
 * @author fengqiongfeng
 * @date 2020/8/24
 */
@Data
public class ClusterOpOfflineOrderDetail extends AbstractOrderDetail {
    /**
     * 物理集群id
     */
    private Long phyClusterId;
    /**
     * 物理集群名称
     */
    private String phyClusterName;
}