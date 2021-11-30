package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.ESAggMetricsVO;
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
@ApiModel("查询QPS")
public class ReadQPSMetricsVO extends ESAggMetricsVO {

    @ApiModelProperty("集群读取tps")
    private Double readTps;
}
