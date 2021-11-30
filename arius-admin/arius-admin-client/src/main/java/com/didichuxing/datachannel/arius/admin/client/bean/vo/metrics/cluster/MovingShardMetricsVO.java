package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-29
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("正在迁移shard信息")
public class MovingShardMetricsVO implements Serializable {

    @ApiModelProperty("承载索引")
    private String i;

    @ApiModelProperty("shard序号")
    private Long   s;

    @ApiModelProperty("耗时")
    private String t;

    @ApiModelProperty("源节点Ip")
    private String shost;

    @ApiModelProperty("目标节点ip")
    private String thost;

    @ApiModelProperty("状态 init、index、start、translog、finalize、done")
    private String st;
}
