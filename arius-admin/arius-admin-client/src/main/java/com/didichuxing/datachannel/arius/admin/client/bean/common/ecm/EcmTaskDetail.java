package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EcmTaskDetail {
    /**
     * ID主键自增
     */
    private Long    id;

    /**
     *  工单任务ID
     */
    private Long    workOrderTaskId;

    /**
     * 角色
     */
    private String  role;

    /**
     * 节点名称/主机名称
     */
    private String  hostname;

    /**
     * 分组
     */
    private Integer grp;
    /**
     * 顺序
     */
    private Integer idx;

    /**
     * 状态
     */
    private String  status;

    /**
     * 容器云/物理机 接口返回任务ID
     */
    @ApiModelProperty("容器云/物理机 接口返回任务ID")
    private Long    taskId;
}
