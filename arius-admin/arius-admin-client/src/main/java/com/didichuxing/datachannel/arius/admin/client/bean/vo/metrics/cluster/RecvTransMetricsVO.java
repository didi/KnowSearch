package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.ESAggMetricsVO;
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
@ApiModel("每秒接受总流量")
public class RecvTransMetricsVO extends ESAggMetricsVO {

    @ApiModelProperty("集群每秒接受总流量")
    private Double recvTransSize;
}
