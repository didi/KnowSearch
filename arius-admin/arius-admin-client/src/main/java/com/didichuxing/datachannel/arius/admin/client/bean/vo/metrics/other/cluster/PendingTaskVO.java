package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.other.cluster;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("Pending任务")
public class PendingTaskVO implements Serializable {

    @ApiModelProperty("顺序")
    private Long   insertOrder;

    @ApiModelProperty("队列耗时")
    private String timeInQueue;

    @ApiModelProperty("优先级, HIGH")
    private String priority;

    @ApiModelProperty("原因")
    private String source;
}
