package com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("ES聚合指标")
public class ESAggMetricsVO implements Serializable, Comparable<ESAggMetricsVO> {
    @ApiModelProperty("时间戳")
    private Long timeStamp;

    @Override
    public int compareTo(ESAggMetricsVO o) {
        if (null == o) {
            return 0;
        }

        return this.getTimeStamp().intValue() - o.getTimeStamp().intValue();
    }
}
