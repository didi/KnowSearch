package com.didichuxing.datachannel.arius.admin.client.bean.vo.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "告警规则详情")
public class MonitorRuleDetailVO {
    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "名称")
    private String operator;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "修改时间")
    private Long modifyTime;

    @ApiModelProperty(value = "App概览")
    private AppSummaryVO appSummary;

    @ApiModelProperty(value = "App概览")
    private AppMonitorRuleDTO monitorRule;
}