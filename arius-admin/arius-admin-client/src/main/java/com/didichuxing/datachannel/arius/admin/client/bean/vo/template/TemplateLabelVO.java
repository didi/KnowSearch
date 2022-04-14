package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "TemplateLabelVO", description = "索引标签")
public class TemplateLabelVO {

    @ApiModelProperty(value = "索引模板id")
    private Integer indexTemplateId;

    @ApiModelProperty(value = "标签id，有一定的规则")
    private String labelId;

    @ApiModelProperty(value = "标签名称")
    private String labelName;

    @ApiModelProperty(value = "标记时间")
    private Date markTime;
}
