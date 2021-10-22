package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleHost;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author fengqiongfeng
 * @date 2020/8/24
 */
@Data
public class ClusterOpNewHostOrderDetail extends AbstractOrderDetail {

    /**
     * type 3:docker 4:host
     */
    private int type;

    /**
     * 数据中心
     */
    private String  dataCenter;

    /**
     * 物理集群名称
     */
    private String  phyClusterName;

    /**
     * 机器节点
     */
    private String nsTree;

    /**
     * 机房
     */
    private String  idc;

    /**
     * es版本
     */
    private String  esVersion;

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
    private String  desc;

    /**
     * 单节点实例数
     */
    private Integer  pidCount;

    /**
     * 机器规格
     */
    private String  machineSpec;

    /**
     * 集群角色 对应主机列表
     */
    private List<ESClusterRoleHost> roleClusterHosts;
}