package com.didichuxing.datachannel.arius.admin.client.bean.common;

import com.alibaba.fastjson.JSONObject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "MappingOptimizeItem", description = "优化信息")
public class MappingOptimizeItem {

    @ApiModelProperty("type名称")
    private String     typeName;

    @ApiModelProperty("字段名称")
    private String     fieldName;

    @ApiModelProperty("源属性")
    private JSONObject initial;

    @ApiModelProperty("优化后属性")
    private JSONObject optimize;
}