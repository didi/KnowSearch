package com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "告警屏蔽")
public class MonitorSilenceVO extends BaseVO {
    @ApiModelProperty(value = "屏蔽ID")
    private Long silenceId;

    @ApiModelProperty(value = "告警ID")
    private Long monitorId;

    @ApiModelProperty(value = "监控名称")
    private String monitorName;

    @ApiModelProperty(value = "开始时间")
    private Long startTime;

    @ApiModelProperty(value = "结束时间")
    private Long endTime;

    @ApiModelProperty(value = "备注")
    private String description;
}