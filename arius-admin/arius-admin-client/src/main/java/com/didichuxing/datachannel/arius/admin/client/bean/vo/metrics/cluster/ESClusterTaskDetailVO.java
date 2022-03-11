package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author didi
 * @date 2022-01-14 10:36 上午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("task任务详情")
public class ESClusterTaskDetailVO implements Serializable {

    @ApiModelProperty("任务id")
    private String taskId;

    @ApiModelProperty("节点名称")
    private String node;

    @ApiModelProperty("操作类型")
    private String action;

    @ApiModelProperty("开始时间")
    private long startTime;

    @ApiModelProperty("运行时间ms")
    private long runningTime;

    @ApiModelProperty("运行时间string")
    private String runningTimeString;

    @ApiModelProperty("描述")
    private String description;
}
