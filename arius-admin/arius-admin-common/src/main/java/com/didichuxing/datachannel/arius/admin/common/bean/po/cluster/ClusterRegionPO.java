package com.didichuxing.datachannel.arius.admin.common.bean.po.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 物理集群Region PO
 * 对应表名es_cluster_region
 * @author wangshu
 * @date 2020/09/15
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClusterRegionPO extends BasePO {
    /**
     * region ID
     */
    private Long id;

    /**
     * 绑定到的逻辑集群ID列表，用逗号隔开, 为空则没有被绑定
     */
    private String logicClusterIds;

    /**
     * 物理集群名称
     */
    private String phyClusterName;

    /**
     * Rack列表
     */
    private String racks;

}
