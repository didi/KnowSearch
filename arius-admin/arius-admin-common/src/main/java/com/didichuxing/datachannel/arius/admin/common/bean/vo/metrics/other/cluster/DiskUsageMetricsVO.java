package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.percentiles.ESPercentilesMetricsVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-07-31
 */
@Data
@NoArgsConstructor
@ApiModel("集群磁盘使用率指标信息")
public class DiskUsageMetricsVO extends ESPercentilesMetricsVO {
}
