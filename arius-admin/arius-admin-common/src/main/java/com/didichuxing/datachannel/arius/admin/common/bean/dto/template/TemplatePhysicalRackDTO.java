package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-01-10 3:44 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板rack信息")
public class TemplatePhysicalRackDTO extends BaseDTO {
    @ApiModelProperty("rack")
    private String rack;
}
