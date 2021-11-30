package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorRuleSummary {
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "策略名称")
    private String name;

    @ApiModelProperty(value = "appId")
    private Long appId;

    @ApiModelProperty(value = "最后修改人")
    private String operator;

    @ApiModelProperty(value = "最后修改时间")
    private Long updateTime;

    @ApiModelProperty(value = "告警级别")
    private Integer priority;

    @ApiModelProperty(value = "监控类型")
    private String categoryName;

    @ApiModelProperty(value = "监控对象")
    private String objectNames;

    @ApiModelProperty(value = "运行状态")
    private Integer status;


}