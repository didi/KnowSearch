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

    @ApiModelProperty("容量")
    private String store;

    @ApiModelProperty("所属节点Ip")
    private String ip;

    @ApiModelProperty("所属节点名称")
    private String node;

}
