package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.top;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("折线图(TopN类型)指标信息")
public class VariousLineChartMetricsVO implements Serializable {

    @ApiModelProperty("指标类型")
    private String                 type;

    @ApiModelProperty("指标数据信息")
    private List<MetricsContentVO> metricsContents;

}
