package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.percentiles.ESPercentilesMetricsVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-01-13 5:43 下午
 */
@Data
@NoArgsConstructor
@ApiModel("task耗时指标信息")
public class TaskCostMetricVO extends ESPercentilesMetricsVO {
}
