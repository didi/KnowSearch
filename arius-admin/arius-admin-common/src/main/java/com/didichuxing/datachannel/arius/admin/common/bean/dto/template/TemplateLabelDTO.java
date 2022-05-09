package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/5/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板标签信息")
public class TemplateLabelDTO {

    @ApiModelProperty("模板ID")
    private Integer      templateId;

    @ApiModelProperty("模板标签列表")
    private List<String> templateLabel;

}
