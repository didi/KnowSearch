package com.didichuxing.datachannel.arius.admin.common.bean.entity.region;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/13
 * @Comment:
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClusterRegion extends BaseEntity {
    /**
     * region ID
     */
    private Long   id;

    /**
     * region 名称
     */
    private String name;

    /**
     * 绑定到的逻辑集群ID列表, 为-1则没有被绑定
     */
    private String logicClusterIds;

    /**
     * 物理集群名称
     */
    private String phyClusterName;

    /**
     * 配置
     */
    private String config;
}
