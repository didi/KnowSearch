package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 逻辑集群聚合所有Rack信息
 * @author d06679
 * @date 2019/3/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterLogicWithRack extends ClusterLogic {

    /**
     * 逻辑集群对应Rack详情列表
     */
    private Collection<ClusterLogicRackInfo> items;
}
