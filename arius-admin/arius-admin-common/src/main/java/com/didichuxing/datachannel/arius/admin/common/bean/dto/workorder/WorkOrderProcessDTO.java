package com.didichuxing.datachannel.arius.admin.common.bean.dto.workorder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author d06679
 * @date 2018/10/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "工单处理信息")
public class WorkOrderProcessDTO {

    @ApiModelProperty("工单id")
    private Long              orderId;

    @ApiModelProperty("审批结果，agree/disagree/submit")
    private String              outcome;

    @ApiModelProperty("审批意见")
    private String              comment;

    @ApiModelProperty("审批人")
    private String              assignee;

    @ApiModelProperty("审批人appid")
    private Integer             assigneeAppid;

    @ApiModelProperty("是否自动审批")
    private Boolean             checkAuthority;

    @ApiModelProperty("业务参数")
    private Object              contentObj;
}
