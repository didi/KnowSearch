package com.didichuxing.datachannel.arius.admin.common.bean.po.task.ecm;

import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.Data;

@Data
public class EcmTaskPO extends BasePO {

    /**
     * ID主键自增
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 工单ID
     */
    private Long workOrderId;

    /**
     * 物理集群ID
     */
    private Long physicClusterId;

    /**
     * 集群节点角色 执行顺序
     */
    private String clusterNodeRole;

    /**
     * 状态
     * @see EcmTaskStatusEnum
     */
    private String status;

    /**
     * 类型  docker容器云/host 物理机
     */
    private Integer type;

    /**
     * 类型  1 集群新增  2 集群扩容 3 集群缩容 4 集群重启 5 集群升级
     */
    private Integer orderType;

    /**
     *  工单数据
     */
    private String handleData;

    /**
     * 创建人
     */
    private String creator;

}
