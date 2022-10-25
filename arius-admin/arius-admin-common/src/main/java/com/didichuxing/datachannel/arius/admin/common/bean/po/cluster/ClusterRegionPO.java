package com.didichuxing.datachannel.arius.admin.common.bean.po.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import io.swagger.annotations.ApiModelProperty;
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
    private Long   id;

    /**
     * region 名称
     */
    private String name;

    /**
     * 绑定到的逻辑集群ID列表，用逗号隔开, 为空则没有被绑定
     */
    private String logicClusterIds;

    /**
     * 物理集群名称
     */
    private String phyClusterName;

    /**
     * 配置项
     */
    private String config;

    /**
     * Region划分方式，为空是根据节点名称划分，否则根据attribute属性划分
     */
    private String divideAttributeKey;

}
