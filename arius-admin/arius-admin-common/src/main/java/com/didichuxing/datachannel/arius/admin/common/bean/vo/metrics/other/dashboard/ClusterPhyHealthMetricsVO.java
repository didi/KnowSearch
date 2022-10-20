package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.dashboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by linyunan on 3/14/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("dashboard物理集群健康指标信息")
public class ClusterPhyHealthMetricsVO {
    @ApiModelProperty("当前时间")
    private Long         timestamp;

    @ApiModelProperty("总物理集群数量")
    private Integer      totalNum;

    @ApiModelProperty("green状态集群数")
    private Integer      greenNum;

    @ApiModelProperty("yellow状态集群数")
    private Integer      yellowNum;

    @ApiModelProperty("red状态集群数")
    private Integer      redNum;

    @ApiModelProperty("未知状态集群数")
    private Integer      unknownNum;

    @ApiModelProperty("green状态集群名称列表")
    private List<String> greenClusterList;

    @ApiModelProperty("yellow状态集群名称列表")
    private List<String> yellowClusterList;

    @ApiModelProperty("red状态集群名称列表")
    private List<String> redClusterList;

    @ApiModelProperty("未知状态集群名称列表")
    private List<String> unknownClusterList;

    @ApiModelProperty("未知状态集群百分比")
    private Double       unknownPercent;

    @ApiModelProperty("green状态集群百分比")
    private Double       greenPercent;

    @ApiModelProperty("yellow状态集群百分比")
    private Double       yellowPercent;

    @ApiModelProperty("red状态集群百分比")
    private Double       redPercent;
}