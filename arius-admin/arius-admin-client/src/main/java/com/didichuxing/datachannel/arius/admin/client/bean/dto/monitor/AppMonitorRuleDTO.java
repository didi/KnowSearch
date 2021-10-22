package com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@ApiModel(description = "监控告警")
public class AppMonitorRuleDTO extends BaseDTO {
    @ApiModelProperty(value = "所属AppID")
    private Integer appId;

    @ApiModelProperty(value = "ID, 修改时使用")
    private Long id;

    @ApiModelProperty(value = "报警名称")
    private String name;

    @ApiModelProperty(value = "报警等级，1级：电话+短信+钉钉+邮件，2：短信+钉钉+邮件 3：钉钉+邮件")
    private Integer priority;

    @ApiModelProperty(value = "报警生效时间，小时，24小时计，以逗号分隔")
    private String periodHoursOfDay;

    @ApiModelProperty(value = "报警生效时间，星期，7天计")
    private String periodDaysOfWeek;

    @ApiModelProperty(value = "报警规则")
    private List<MonitorStrategyExpressionDTO> strategyExpressionList;

    @ApiModelProperty(value = "过滤规则")
    private List<MonitorStrategyFilterDTO> strategyFilterList;

    @ApiModelProperty(value = "通知方式")
    private List<MonitorStrategyActionDTO> strategyActionList;

    public boolean paramLegal() {
        if (null == appId
                || StringUtils.isBlank(name)
                || null == priority
                || StringUtils.isBlank(periodHoursOfDay)
                || StringUtils.isBlank(periodDaysOfWeek)
                || CollectionUtils.isEmpty(strategyExpressionList)
                || null == strategyFilterList
                || CollectionUtils.isEmpty(strategyActionList)) {
            return false;
        }

        for (MonitorStrategyExpressionDTO dto: strategyExpressionList) {
            if (!dto.paramLegal()) {
                return false;
            }
        }

        for (MonitorStrategyFilterDTO dto: strategyFilterList) {
            if (!dto.paramLegal()) {
                return false;
            }
        }
        for (MonitorStrategyActionDTO dto: strategyActionList) {
            if (!dto.paramLegal()) {
                return false;
            }
        }
        return true;
    }
}