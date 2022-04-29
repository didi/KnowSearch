package com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.AppMonitorRuleDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "告警规则详情")
public class MonitorRuleDetailVO {
    @ApiModelProperty(value = "项目名称")
    private String appName;

    @ApiModelProperty(value = "监控类型")
    private String categoryName;

    @ApiModelProperty(value = "监控对象名称")
    private List<String> objectNames;

    @ApiModelProperty(value = "告警接收组名称")
    private List<String> notifyGroups;

    @ApiModelProperty(value = "告警接收人名称")
    private List<String> notifyUsers;

    @ApiModelProperty(value = "告警策略")
    private AppMonitorRuleDTO monitorRule;
}