package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

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
@ApiModel("每秒发送总流量")
public class SendTransMetricsVO extends ESAggMetricsVO {

    @ApiModelProperty("集群每秒发送总流量")
    private Double sendTransSize;
}
