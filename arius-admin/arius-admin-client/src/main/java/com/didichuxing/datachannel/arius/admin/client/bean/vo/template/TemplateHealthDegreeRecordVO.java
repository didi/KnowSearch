package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

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

    /**
     * 统计时间
     */
    @ApiModelProperty("统计时间")
    private Long timestamp;

    /**
     * 索引模板id
     */
    @ApiModelProperty("逻辑模板Id")
    private Integer logicTemplateId;

    /**
     * 健康分
     */
    @ApiModelProperty("健康分")
    private Integer healthDegree;

    /**
     * 标签
     */
    @ApiModelProperty("标签")
    List<TemplateLabelVO> labelPOS;
}
