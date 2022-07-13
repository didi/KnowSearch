package com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "索引别名切换")
public class ConsoleTemplateAliasSwitchDTO {
    @ApiModelProperty("索引应用ID")
    private Integer      projectId;
    @ApiModelProperty("别名名称")
    private String       aliasName;
    @ApiModelProperty("需要添加别名的索引")
    private List<String> addAliasIndices;
    @ApiModelProperty("需要删除别名的索引")
    private List<String> delAliasIndices;
}