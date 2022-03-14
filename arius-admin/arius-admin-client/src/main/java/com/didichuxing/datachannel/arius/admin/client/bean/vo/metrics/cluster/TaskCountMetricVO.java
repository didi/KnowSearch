package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.ESAggMetricsVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-01-13 5:44 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("task数量")
public class TaskCountMetricVO extends ESAggMetricsVO {

    @ApiModelProperty("集群task数量")
    private long taskCount;
}