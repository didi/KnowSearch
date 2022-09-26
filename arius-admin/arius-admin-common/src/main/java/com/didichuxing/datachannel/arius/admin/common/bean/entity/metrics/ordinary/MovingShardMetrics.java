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
    private String i;

    /**
     * 源节点ip
     */
    private String shost;

    /**
     * 目标节点ip
     */
    private String thost;

    /**
     * 覆盖的字节数
     */
    private String br;

    /**
     * 字节占比
     */
    private String bp;

    /**
     * 转换日志操作占比
     */
    private String top;
}
