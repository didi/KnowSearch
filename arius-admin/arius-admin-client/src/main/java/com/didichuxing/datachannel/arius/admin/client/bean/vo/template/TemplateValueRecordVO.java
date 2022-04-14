package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "TemplateValueRecordVO", description = "索引的价值分")
public class TemplateValueRecordVO {

    @ApiModelProperty(value = "统计时间")
    private Long timestamp;

    @ApiModelProperty(value = "索引模板id")
    private Integer logicTemplateId;

    @ApiModelProperty(value = "价值")
    private Integer value;

    @ApiModelProperty(value = "访问量")
    private Long accessCount;

    @ApiModelProperty(value = "大小G")
    private Double sizeG;

    @ApiModelProperty(value = "逻辑集群")
    private String logicCluster;
}
