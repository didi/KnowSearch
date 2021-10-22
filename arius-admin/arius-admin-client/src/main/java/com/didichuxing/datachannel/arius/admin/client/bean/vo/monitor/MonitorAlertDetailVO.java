package com.didichuxing.datachannel.arius.admin.client.bean.vo.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "告警详情")
public class MonitorAlertDetailVO extends BaseVO {
    private MonitorAlertVO monitorAlert;

    private MonitorMetricVO monitorMetric;
}