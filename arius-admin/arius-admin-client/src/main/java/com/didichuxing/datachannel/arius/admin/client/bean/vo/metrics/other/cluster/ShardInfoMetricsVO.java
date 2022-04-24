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
@ApiModel("shard数量(总数, 未分配Shard)")
public class ShardInfoMetricsVO extends ESAggMetricsVO {

    @ApiModelProperty("集群shard数量")
    private Long shardNu;

    @ApiModelProperty("未分配shard")
    private Long unAssignedShards;
}
