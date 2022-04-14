package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.cluster;

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
@ApiModel("写入TPS")
public class WriteTPSMetricsVO extends ESAggMetricsVO {

    @ApiModelProperty("集群写入tps")
    private Double writeTps;
}
