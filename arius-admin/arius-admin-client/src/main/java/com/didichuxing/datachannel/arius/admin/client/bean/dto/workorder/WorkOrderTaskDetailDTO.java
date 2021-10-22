package com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "工单任务详细")
public class WorkOrderTaskDetailDTO extends BaseDTO {

    /**
     * ID主键自增
     */
    @ApiModelProperty("ID")
    private Long id;

    /**
     *  工单任务ID
     */
    @ApiModelProperty("工单任务ID")
    private Long workOrderTaskId;

    /**
     *  所属角色
     */
    @ApiModelProperty("所属角色")
    private String role;

    /**
     * 节点名称/主机名称
     */
    @ApiModelProperty("节点名称/主机名称")
    private String hostname;

    /**
     * 分组
     */
    @ApiModelProperty("分组")
    private Integer grp;
    /**
     * 顺序
     */
    @ApiModelProperty("顺序")
    private Integer idx;

    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private String status;

    /**
     * 容器云/物理机 接口返回任务ID
     */
    @ApiModelProperty("容器云/物理机 接口返回任务ID")
    private Long taskId;


}
