package com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引模板别名创建信息")
public class ConsoleLogicTemplateAliasesDTO extends BaseDTO {
    @ApiModelProperty("索引ID")
    private Integer               logicId;

    @ApiModelProperty("索引别名列表信息")
    private List<ConsoleAliasDTO> aliases;
}
