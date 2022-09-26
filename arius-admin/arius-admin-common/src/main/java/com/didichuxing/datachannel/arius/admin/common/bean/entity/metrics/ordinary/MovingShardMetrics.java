package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary;

import java.io.Serializable;

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
public class MovingShardMetrics implements Serializable {

    /**
     * 归属索引
     */
    private String index;

    /**
     * 源节点ip
     */
    private String source_host;

    /**
     * 目标节点ip
     */
    private String target_host;

    /**
     * 覆盖的字节数
     */
    private String bytes_recovered;

    /**
     * 字节占比
     */
    private String bytes_percent;

    private String translog_ops_percent;
}
