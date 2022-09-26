package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

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
    private String index;

    @ApiModelProperty("源节点Ip")
    private String source_host;

    @ApiModelProperty("目标节点ip")
    private String target_host;

    @ApiModelProperty("恢复的字节数")
    private String bytes_recovered;

    @ApiModelProperty("字节数占比")
    private String bytes_percent;

    @ApiModelProperty("转换日志操作占比")
    private String translog_ops_percent;
}
