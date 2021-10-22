package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import java.util.Collection;

import lombok.Data;

/**
 * 逻辑集群聚合所有Rack信息
 * @author d06679
 * @date 2019/3/22
 */
@Data
public class ESClusterLogicWithRack extends ESClusterLogic {

    /**
     * 逻辑集群对应Rack详情列表
     */
    Collection<ESClusterLogicRackInfo> items;
}
