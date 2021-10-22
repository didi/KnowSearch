package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.ESClusterRoleDocker;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicLevelEnum;
import lombok.Data;

import java.util.List;

@Data
public class ClusterOpNewDockerContent extends ClusterOpBaseContent {

    /**
     * 数据中心
     */
    private String              dataCenter;
    /**
     * 机器节点
     */
    private String              nsTree;

    /**
     * 机房
     */
    private String              idc;

    /**
     * es版本
     */
    private String              esVersion;

    /**
     * 插件包ID列表
     */
    private String              plugs;

    /**
     * 集群创建人
     */
    private String              creator;

    /**
     * 描述
     */
    private String              desc;

    /**
     * 服务等级
     * @see ResourceLogicLevelEnum
     */
    private Integer             level;

    /**
     * 集群角色列表
     */
    private List<ESClusterRoleDocker> roleClusters;

}
