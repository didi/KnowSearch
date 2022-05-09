package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "TemplateHealthDegreeRecordVO", description = "索引健康分")
public class TemplateHealthDegreeRecordVO {

    @ApiModelProperty("统计时间")
    private Long timestamp;

    @ApiModelProperty("逻辑模板Id")
    private Integer logicTemplateId;

    @ApiModelProperty("健康分")
    private Integer healthDegree;

    @ApiModelProperty("标签")
    List<TemplateLabelVO> labelPOS;
}
