package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClusterMemInfo implements Serializable {
    @ApiModelProperty("已用内存大小")
    private Long   memUsed;

    @ApiModelProperty("剩余空闲内存大小")
    private Long   memFree;

    @ApiModelProperty("总内存大小")
    private Long   memTotal;

    @ApiModelProperty("已用内存百分比")
    private Double memUsedPercent;

    @ApiModelProperty("剩余空闲内存百分比")
    private Double memFreePercent;
}
