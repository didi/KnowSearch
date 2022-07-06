package com.didichuxing.datachannel.arius.admin.biz.worktask.content;

import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicLevelEnum;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ohushenglin_v
 * @date 2022-05-24
 */
@Data
@NoArgsConstructor
public class ClusterNewHostContent extends ClusterHostContent {
    /**
     * 数据中心
     */
    private String                  dataCenter;
    /**
     * 机器节点
     */
    private String                  nsTree;

    /**
     * 机房
     */
    private String                  idc;

    /**
     * es版本
     */
    private String                  esVersion;

    /**
     * 插件包ID列表
     */
    private String                  plugs;

    /**
     * 集群创建人
     */
    private String                  creator;

    /**
     * 描述
     */
    private String                  desc;

    /**
     * 单节点实例数
     */
    private Integer                 pidCount;

    /**
     * 机器规格
     */
    private String                  machineSpec;

    /**
     * 服务等级
     * @see ResourceLogicLevelEnum
     */
    private Integer                 level;

    /**
     * 集群展示用属性标签，如「集群所属资源类型」等等
     */
    private String  tags;

    private String                    platformType;

    /**
     * @see ClusterResourceTypeEnum
     */
    private Integer                    resourceType;

}
