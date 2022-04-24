package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.list;

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
@ApiModel("列表类型的指标类")
public class MetricListVO implements Serializable {
    @ApiModelProperty("当前时间")
    private Long   currentTime;

    @ApiModelProperty("指标类型")
    private String type;

    @ApiModelProperty("具体指标信息")
    private List<MetricListContentVO> metricListContents;
}
