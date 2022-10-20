package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-06-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterDeleteOrderDetail extends AbstractOrderDetail {
    /**
     * type 3:docker 4:host
     */
    private Integer type;

    /**
     * 物理集群名称
     */
    private String  phyClusterName;
}
