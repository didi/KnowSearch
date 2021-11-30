package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster;

import java.io.Serializable;

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
@ApiModel("大shard")
public class BigShardMetricsVO implements Serializable {

    @ApiModelProperty("shard标识")
    private Long   shard;

    @ApiModelProperty("归属索引")
    private String index;

    @ApiModelProperty("主/备")
    private String prirep;

    @ApiModelProperty("所属节点Ip")
    private String ip;

    @ApiModelProperty("所属节点名称")
    private String node;

    @ApiModelProperty("容量")
    private String store;
}
