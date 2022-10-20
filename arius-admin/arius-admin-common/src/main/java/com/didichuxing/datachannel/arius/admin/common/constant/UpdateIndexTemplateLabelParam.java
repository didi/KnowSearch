package com.didichuxing.datachannel.arius.admin.common.constant;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Label;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by d06679 on 2018/1/17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "UpdateIndexTemplateLabelParam", description = "更新索引标签参数")
public class UpdateIndexTemplateLabelParam {

    @ApiModelProperty("索引标签列表")
    private List<Label> indexTemplateLabels;

    @ApiModelProperty("索引模板ID")
    private Integer     templateId;

    @ApiModelProperty("操作人")
    private String      operator;

}
