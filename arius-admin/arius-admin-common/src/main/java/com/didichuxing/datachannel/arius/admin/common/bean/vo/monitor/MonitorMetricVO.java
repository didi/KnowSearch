package com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorMetricVO extends BaseVO {
    private String metric;

    private Integer step;

    private List<MonitorMetricPoint> values;

    private Integer comparison;

    private Integer delta;

    private Boolean origin;
}