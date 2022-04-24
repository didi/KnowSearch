package com.didichuxing.datachannel.arius.admin.client.bean.dto.template;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@ApiModel("索引模板限流调整")
public class ConsoleTemplateRateLimitDTO {
    @ApiModelProperty("索引模板ID")
    private Integer logicId;
    @ApiModelProperty("索引模板当前限流值")
    private Integer curRateLimit;
    @ApiModelProperty("索引模板调整后限流值")
    private Integer adjustRateLimit;
    @ApiModelProperty("限流变更人")
    private String submitor;
}
