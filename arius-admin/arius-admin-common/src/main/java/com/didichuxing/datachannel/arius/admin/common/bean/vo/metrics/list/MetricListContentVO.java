package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.list;

import java.io.Serializable;

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
public class MetricListContentVO implements Serializable {
    @ApiModelProperty("物理集群名称")
    private String clusterPhyName;

    @ApiModelProperty("名称: node Name / index name /template name")
    private String name;

    @ApiModelProperty("指标值, 某些指标项需要展示指标值, 可能为百分比, 考虑客户端做适配")
    private Double value;
}
