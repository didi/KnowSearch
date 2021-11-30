package com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "工单内容")
public class WorkOrderDTO extends BaseDTO {

    private static final long serialVersionUID = -4536847390271645624L;

    @ApiModelProperty("工单类型（路径参数）")
    private String  type;

    @ApiModelProperty("工单内容")
    private Object  contentObj;

    @ApiModelProperty("提交人")
    private String  submitor;

    @ApiModelProperty("提交APPID")
    private Integer submitorAppid;

    @ApiModelProperty("数据中心")
    private String  dataCenter;

    @ApiModelProperty("描述")
    private String description;

}
