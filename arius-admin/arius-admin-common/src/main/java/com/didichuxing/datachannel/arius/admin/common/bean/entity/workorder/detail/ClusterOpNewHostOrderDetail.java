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
public class ClusterOpNewHostOrderDetail extends BaseClusterHostOrderDetail {

    /**
     * 数据中心
     */
    private String dataCenter;

    /**
     * 机器节点
     */
    private String nsTree;

    /**
     * 机房
     */
    private String idc;

    /**
     * es版本
     */
    private String esVersion;

    /**
     * 插件包ID列表
     */
    private String plugs;

    /**
     * 集群创建人
     */
    private String creator;

    /**
     * 描述
     */
    private String desc;

    /**
     * 机器规格
     */
    private String machineSpec;

}