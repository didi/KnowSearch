package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引模板setting信息")
public class ConsoleTemplateSettingDTO extends BaseDTO {

    @ApiModelProperty("索引ID")
    private Integer                   logicId;

    @ApiModelProperty("索引分词器")
    private AriusIndexTemplateSetting setting;
}
