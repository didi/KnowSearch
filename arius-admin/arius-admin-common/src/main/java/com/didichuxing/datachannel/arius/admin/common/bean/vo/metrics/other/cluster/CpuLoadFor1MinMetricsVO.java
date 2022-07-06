package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.percentiles.ESPercentileMetricsVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-31
 */
@Data
@NoArgsConstructor
@ApiModel("CPU1分钟负载指标信息")
public class CpuLoadFor1MinMetricsVO extends ESPercentileMetricsVO {
}
