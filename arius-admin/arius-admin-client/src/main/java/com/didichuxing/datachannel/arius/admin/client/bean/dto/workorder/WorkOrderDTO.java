package com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Data
@ApiModel(description = "工单内容")
public class WorkOrderDTO extends BaseDTO {

    /**
     * 工单类型
     */
    @ApiModelProperty("工单类型（路径参数）")
    private String  type;

    /**
     * 结构化的工单内容 json
     */
    @ApiModelProperty("工单内容")
    private Object  contentObj;

    /**
     * 提交人
     */
    @ApiModelProperty("提交人")
    private String  submitor;

    /**
     * 提交人appid
     */
    @ApiModelProperty("提交APPID")
    private Integer submitorAppid;

    /**
     * 数据中心
     */
    @ApiModelProperty("数据中心")
    private String  dataCenter;
    
    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String description;

}
