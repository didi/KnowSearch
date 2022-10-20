package com.didichuxing.datachannel.arius.admin.common.bean.vo.order;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author fengqiongfeng
 * @date 2020/8/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderVO extends BaseVO {
    @ApiModelProperty(value = "工单ID")
    private Long    id;

    @ApiModelProperty(value = "工单标题")
    private String  title;

    @ApiModelProperty(value = "工单类型")
    private String  type;

    @ApiModelProperty(value = "申请人")
    private String  applicant;

    @ApiModelProperty(value = "描述信息")
    private String  description;

    @ApiModelProperty(value = "工单状态, 0:待审批, 1:通过, 2:拒绝, 3:取消")
    private Integer status;

    @ApiModelProperty(value = "申请/审核时间")
    private Date    createTime;

    @ApiModelProperty(value = "审批人ProjectId")
    private Integer approverProjectId;

    @ApiModelProperty(value = "扩展字段")
    private String  extensions;
}