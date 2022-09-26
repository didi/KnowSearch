package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

public class UnAssignShardMetricsVO implements Serializable {

    @ApiModelProperty("归属索引")
    private String index;

    @ApiModelProperty("shard标识")
    private Long   shard;

    @ApiModelProperty("主/备")
    private String prirep;

    @ApiModelProperty("状态")
    private String state;

}
