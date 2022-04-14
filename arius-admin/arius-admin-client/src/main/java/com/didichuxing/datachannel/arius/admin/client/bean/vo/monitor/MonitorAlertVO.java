package com.didichuxing.datachannel.arius.admin.client.bean.vo.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "告警信息")
public class MonitorAlertVO extends BaseVO {
    @ApiModelProperty(value = "告警ID")
    private Long alertId;

    @ApiModelProperty(value = "监控ID")
    private Long monitorId;

    @ApiModelProperty(value = "监控名称")
    private String monitorName;

    @ApiModelProperty(value = "监控级别")
    private Integer monitorPriority;

    @ApiModelProperty(value = "告警状态")
    private Integer alertStatus;

    @ApiModelProperty(value = "告警开始时间")
    private Long startTime;

    @ApiModelProperty(value = "告警结束时间")
    private Long endTime;

    @ApiModelProperty(value = "告警的指标")
    private String metric;

    @ApiModelProperty(value = "触发值")
    private Double value;

    @ApiModelProperty(value = "告警组")
    private List<String> groups;

    @ApiModelProperty(value = "表达式")
    private String info;
}