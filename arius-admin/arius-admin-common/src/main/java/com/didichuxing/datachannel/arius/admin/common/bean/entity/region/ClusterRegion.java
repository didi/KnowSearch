package com.didichuxing.datachannel.arius.admin.common.bean.entity.region;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/13
 * @Comment:
 */
@Data
@ToString
public class ClusterRegion extends BaseEntity {
    /**
     * region ID
     */
    private Long id;

    /**
     * 绑定到的逻辑集群ID, 为0则没有被绑定
     */
    private Long logicClusterId;

    /**
     * 物理集群名称
     */
    private String phyClusterName;

    /**
     * Rack列表
     */
    private String racks;
}
