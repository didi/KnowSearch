package com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板别名")
public class ConsoleAliasDTO extends BaseDTO {

    @ApiModelProperty("模板别名名称")
    private String alias;

    @ApiModelProperty("别名过滤器")
    private JSONObject filter;
}
