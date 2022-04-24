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
@ApiModel("集群索引指标详情")
public class MetricsContentVO implements Serializable {

    @ApiModelProperty("集群名称，仅在节点维度指标、索引维度指标、模板维度指标设置该值，用于前端dashboard跳转至指标看板")
    private String                     cluster;

    @ApiModelProperty("名称： 集群名称/节点名称/模板名称/索引名称")
    private String                     name;

    @ApiModelProperty("指标列表")
    private List<MetricsContentCellVO> metricsContentCells;
}
