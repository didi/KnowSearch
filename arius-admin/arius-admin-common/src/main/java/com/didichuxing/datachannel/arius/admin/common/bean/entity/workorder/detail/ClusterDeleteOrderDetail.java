package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.Data;

/**
 * Created by linyunan on 2021-06-11
 */
@Data
public class ClusterDeleteOrderDetail extends AbstractOrderDetail {
    /**
     * type 3:docker 4:host
     */
    private Integer    type;

    /**
     * 物理集群名称
     */
    private String phyClusterName;
}
