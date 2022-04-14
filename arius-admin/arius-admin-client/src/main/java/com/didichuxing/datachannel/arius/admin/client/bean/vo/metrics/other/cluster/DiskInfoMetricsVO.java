package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("磁盘使用指标信息")
public class DiskInfoMetricsVO extends ESAggMetricsVO {

    @ApiModelProperty("集群总容量, 单位G")
    private Double totalStoreSize;

    @ApiModelProperty("集群已使用容量, 单位G")
    private Double storeSize;

    @ApiModelProperty("集群空余容量, 单位G")
    private Double freeStoreSize;
}
