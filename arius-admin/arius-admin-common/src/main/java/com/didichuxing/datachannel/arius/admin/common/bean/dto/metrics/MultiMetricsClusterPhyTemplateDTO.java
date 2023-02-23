package com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Authoer: zyl
 * @Date: 2022/11/23
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "指标信息")
public class MultiMetricsClusterPhyTemplateDTO extends MetricsClusterPhyDTO{

    @ApiModelProperty("集群多个逻辑模板Id")
    private List<Integer> templateIdList;
}