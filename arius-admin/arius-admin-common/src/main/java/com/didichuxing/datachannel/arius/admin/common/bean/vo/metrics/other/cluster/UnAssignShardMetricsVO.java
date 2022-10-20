package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("未分配的shard信息")
public class UnAssignShardMetricsVO implements Serializable {

    @ApiModelProperty("归属索引")
    private String index;

    @ApiModelProperty("shard标识")
    private long   shard;

    @ApiModelProperty("主/备")
    private String prirep;

    @ApiModelProperty("状态")
    private String state;

}
