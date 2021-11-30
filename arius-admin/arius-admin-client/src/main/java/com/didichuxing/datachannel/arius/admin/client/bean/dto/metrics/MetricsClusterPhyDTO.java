package com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "指标信息")
public class MetricsClusterPhyDTO extends BaseDTO {

    @ApiModelProperty("集群名称")
    private String       clusterPhyName;

    @ApiModelProperty("开始时间")
    private Long         startTime;

    @ApiModelProperty("结束时间")
    private Long         endTime;

    @ApiModelProperty("聚合类型")
    private String       aggType;

    @ApiModelProperty("指标类型")
    private List<String> metricsTypes;

    @ApiModelProperty("Top-Level:5,10,15,20")
    private Integer      topNu;
}
