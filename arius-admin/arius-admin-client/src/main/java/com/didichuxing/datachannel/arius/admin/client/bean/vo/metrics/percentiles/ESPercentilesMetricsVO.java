package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.percentiles;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.cluster.ESAggMetricsVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2021-11-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("ES分位图指标")
public class ESPercentilesMetricsVO extends ESAggMetricsVO {
    @ApiModelProperty("均值")
    private Double aggType;

    @ApiModelProperty("99分位值")
    private Double st99;

    @ApiModelProperty("95分位值")
    private Double st95;

    @ApiModelProperty("75分位值")
    private Double st75;

    @ApiModelProperty("55分位值")
    private Double st55;
}
