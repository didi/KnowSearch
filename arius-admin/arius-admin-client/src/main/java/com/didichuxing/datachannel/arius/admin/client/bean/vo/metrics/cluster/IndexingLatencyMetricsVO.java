package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.ESPercentilesMetricsVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-01
 */
@Data
@NoArgsConstructor
@ApiModel("写入延时")
public class IndexingLatencyMetricsVO extends ESPercentilesMetricsVO {
}
