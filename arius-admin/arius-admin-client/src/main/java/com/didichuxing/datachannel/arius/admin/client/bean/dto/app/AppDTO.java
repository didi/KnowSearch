package com.didichuxing.datachannel.arius.admin.client.bean.dto.app;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@ApiModel(description = "应用信息")
public class AppDTO extends BaseDTO {

    @ApiModelProperty("应用ID")
    private Integer id;

    @ApiModelProperty("应用名称")
    private String  name;

    @ApiModelProperty("root用户")
    private Integer isRoot;

    @ApiModelProperty("验证码")
    private String  verifyCode;

    @ApiModelProperty("部门ID")
    private String  departmentId;

    @ApiModelProperty("部门名称")
    private String  department;

    @ApiModelProperty("责任人")
    private String  responsible;

    @ApiModelProperty("备注")
    private String  memo;

    @ApiModelProperty("删除标记（1:正常；0：删除）")
    private Integer isActive;

    @ApiModelProperty("限流值")
    private Integer queryThreshold;

    @ApiModelProperty("查询集群")
    private String  cluster;

    @ApiModelProperty("查询模式（0:集群模式；1:索引模式）")
    private Integer searchType;

    @ApiModelProperty("数据中心")
    private String  dataCenter;

}
