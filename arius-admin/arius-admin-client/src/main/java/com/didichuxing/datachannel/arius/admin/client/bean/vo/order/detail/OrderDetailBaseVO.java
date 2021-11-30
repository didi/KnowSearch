package com.didichuxing.datachannel.arius.admin.client.bean.vo.order.detail;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import java.util.Date;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.user.AriusUserInfoVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fengqiongfeng
 * @date 2020/8/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "工单详情类")
public class OrderDetailBaseVO extends BaseVO {
    @ApiModelProperty(value = "工单ID")
    private Long                  id;

    @ApiModelProperty(value = "工单类型")
    private String                type;

    @ApiModelProperty(value = "申请人")
    private AriusUserInfoVO       applicant;

    @ApiModelProperty(value = "申请人部门")
    private String                appDeptName;

    @ApiModelProperty(value = "申请人使用的appid")
    private Integer               applicantAppId;

    @ApiModelProperty(value = "申请人使用的项目名称")
    private String                applicantAppName;

    @ApiModelProperty(value = "审批人列表, 状态为未处理时返回的是审批人, 状态为处理完成时返回的是审批的人")
    private List<AriusUserInfoVO> approverList;

    @ApiModelProperty(value = "审批时间")
    private Date                  finishTime;

    @ApiModelProperty(value = "审批审批意见")
    private String                opinion;

    @ApiModelProperty(value = "工单状态, 0:待审批, 1:通过, 2:拒绝, 3:取消")
    private Integer               status;

    @ApiModelProperty(value = "备注")
    private String                description;

    @ApiModelProperty(value = "工单title")
    private String                title;

    @ApiModelProperty(value = "工单明细, json字符串")
    private String                detail;

}