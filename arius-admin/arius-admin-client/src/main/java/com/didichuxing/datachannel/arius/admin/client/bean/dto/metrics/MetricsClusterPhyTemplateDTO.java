package com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "物理集群模板维度指标信息")
public class MetricsClusterPhyTemplateDTO extends MetricsClusterPhyIndicesDTO {
    @ApiModelProperty("逻辑模板Id")
    private Integer logicTemplateId;
}
