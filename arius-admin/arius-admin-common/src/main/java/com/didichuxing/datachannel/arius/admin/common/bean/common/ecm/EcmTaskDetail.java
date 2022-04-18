package com.didichuxing.datachannel.arius.admin.common.bean.common.ecm;

import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmTaskStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
     * @see EcmTaskStatusEnum
     */
    private String  status;

    /**
     * 容器云/物理机 接口返回任务ID
     */
    @ApiModelProperty("容器云/物理机 接口返回任务ID")
    private Long    taskId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
