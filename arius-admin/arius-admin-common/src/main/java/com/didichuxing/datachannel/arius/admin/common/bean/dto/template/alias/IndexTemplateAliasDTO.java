package com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(description = "模板别名信息")
public class IndexTemplateAliasDTO extends BaseDTO {

    @ApiModelProperty("逻辑模板ID")
    private Integer logicId;

    @ApiModelProperty("别名名称")
    private String  name;
}
