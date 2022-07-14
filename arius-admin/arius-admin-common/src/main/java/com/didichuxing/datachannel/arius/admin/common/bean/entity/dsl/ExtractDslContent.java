package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "提取查询模板")
@Deprecated
public class ExtractDslContent {

    @ApiModelProperty(value = "查询语句")
    private String dslContent;

}