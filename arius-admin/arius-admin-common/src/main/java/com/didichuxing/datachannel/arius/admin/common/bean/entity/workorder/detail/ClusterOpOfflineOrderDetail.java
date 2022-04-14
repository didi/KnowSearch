package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

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