package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserConfigInfoDTO extends BaseDTO {

    @ApiModelProperty("用户名")
    private String       userName;

    @ApiModelProperty("一级目录下的配置类型,如集群看板，网关看板")
    private String       firstUserConfigType;

    @ApiModelProperty("二级目录下的配置类型,如集群看板下的总览指标类型")
    private String       secondUserConfigType;

    @ApiModelProperty("二级目录配置下具体的配置列表,如cpu利用率")
    private List<String> userConfigTypes;

    @ApiModelProperty("应用ID")
    private Integer projectId;

    @ApiModelProperty("配置类型")
    private Integer configType;
}