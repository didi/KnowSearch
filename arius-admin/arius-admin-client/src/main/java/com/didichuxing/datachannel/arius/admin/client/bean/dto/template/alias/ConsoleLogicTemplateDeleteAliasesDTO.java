package com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "删除逻辑模板别名列表DTO")
public class ConsoleLogicTemplateDeleteAliasesDTO implements Serializable {
    @ApiModelProperty("索引ID")
    private Integer logicId;

    @ApiModelProperty("索引别名列表信息")
    private List<String> aliases;
}
