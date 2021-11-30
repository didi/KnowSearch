package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.linechart;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("某个时刻指标项详情")
public class MetricsContentCellVO extends BaseVO implements Comparable<MetricsContentCellVO> {

    @ApiModelProperty("指标值")
    private Double value;

    @ApiModelProperty("时间戳")
    private Long   timeStamp;

    @Override
    public int compareTo(MetricsContentCellVO o) {
        if (null == o) {return 0;}

        return this.getTimeStamp().intValue() - o.getTimeStamp().intValue();
    }
}
