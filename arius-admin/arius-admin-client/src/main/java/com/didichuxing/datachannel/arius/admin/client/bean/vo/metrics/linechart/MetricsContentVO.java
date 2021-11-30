package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.linechart;

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
@ApiModel("集群索引指标详情")
public class MetricsContentVO implements Serializable {

    @ApiModelProperty("索引名称")
    private String                     name;

    @ApiModelProperty("指标列表")
    private List<MetricsContentCellVO> metricsContentCells;
}
