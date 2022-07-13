package com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "工单任务详细")
@Deprecated
public class WorkOrderTaskDetailDTO extends BaseDTO {

    @ApiModelProperty("ID")
    private Long    id;

    @ApiModelProperty("工单任务ID")
    private Long    workOrderTaskId;

    @ApiModelProperty("所属角色")
    private String  role;

    @ApiModelProperty("节点名称/主机名称")
    private String  hostname;

    @ApiModelProperty("分组")
    private Integer grp;

    @ApiModelProperty("顺序")
    private Integer idx;

    @ApiModelProperty("状态")
    private String  status;

    @ApiModelProperty("容器云/物理机 接口返回任务ID")
    private Long    taskId;

}