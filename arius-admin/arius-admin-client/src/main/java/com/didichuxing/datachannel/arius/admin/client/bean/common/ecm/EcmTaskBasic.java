package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm;

import lombok.Data;

import java.util.Date;

@Data
public class EcmTaskBasic {
    /**
     * ID主键自增
     */
    private Long    id;

    /**
     * 标题
     */
    private String  title;

    /**
     * 工单ID
     */
    private Long    workOrderId;

    /**
     * es版本
     */
    private String  esVersion;

    /**
     * 镜像名
     */
    private String  imageName;

    /**
     * 集群名称
     */
    private String  clusterName;

    /**
     * 集群描述
     */
    private String  desc;

    /**
     * 服务节点
     */
    private String  nsTree;

    /**
     * 机房
     */
    private String  idc;

    /**
     * 成本部门
     */
    private String  dept;

    /**
     * 集群节点角色
     */
    private String  clusterNodeRole;

    /**
     * 状态
     */
    private String  status;

    /**
     * 类型  docker容器云/host 物理机
     */
    private Integer type;

    /**
     * 类型  1 集群新增  2 集群扩容 3 集群缩容 4 集群重启 5 集群升级
     */
    private Integer orderType;

    /**
     * 创建人
     */
    private String  creator;

    /**
     * 开始时间
     */
    protected Date  createTime;

    /**
     * 结束时间
     */
    protected Date  updateTime;
}