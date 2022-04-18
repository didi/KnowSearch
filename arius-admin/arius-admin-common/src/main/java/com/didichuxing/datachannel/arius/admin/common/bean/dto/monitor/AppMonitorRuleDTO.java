package com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "告警策略")
public class AppMonitorRuleDTO extends BaseDTO {
    @ApiModelProperty(value = "所属AppID")
    private Long appId;

    @ApiModelProperty(value = "ID, 修改时使用")
    private Long id;

    @ApiModelProperty(value = "告警策略名称")
    private String name;

    @ApiModelProperty(value = "监控类型: cluster=物理集群。 template=物理模版. node=节点")
    private String category;

    @ApiModelProperty(value = "监控对象name, 用逗号隔开")
    private String objectNames;

    @ApiModelProperty(value = "指标")
    private String metrics;

    //触发规则部分
    @ApiModelProperty(value = "告警策略起始生效时间 格式为 00:00 - 23:59")
    private String enableStime;

    @ApiModelProperty(value = "告警策略结束生效时间 格式为 00:00 - 23:59")
    private String enableEtime;

    @ApiModelProperty(value = "1,2,3 生效的星期数，多个用逗号分隔")
    private String enableDaysOfWeek;

    @ApiModelProperty(value = "所有触发还是触发一条即可，=0所有， =1一条")
    private Integer togetherOrAny = 1;

    @ApiModelProperty(value = "触发规则")
    private List<MonitorExpressionDTO> triggerConditions;

    //告警规则部分
    @ApiModelProperty(value = "报警等级，1级：电话+短信+钉钉+邮件，2：短信+钉钉+邮件 3：钉钉+邮件")
    private Integer priority;

    @ApiModelProperty(value = "通知方式, 多个用逗号隔开；voice,sms,dingtalk,email,wecom")
    private String notifyChannels;

    @ApiModelProperty(value = "告警接收组id，多个用逗号分隔")
    private String notifyGroups;

    @ApiModelProperty(value = "告警接收人id，多个用逗号分隔")
    private String notifyUsers;

    @ApiModelProperty(value = "回调地址，多个用逗号分隔")
    private String callbacks;

    public boolean paramLegal() {
        if (null == appId
                || StringUtils.isBlank(name)
                || StringUtils.isBlank(metrics)
                || null == priority
                || StringUtils.isBlank(enableStime)
                || StringUtils.isBlank(enableEtime)
                || StringUtils.isBlank(enableDaysOfWeek)
                || CollectionUtils.isEmpty(triggerConditions)) {
            return false;
        }

        for (MonitorExpressionDTO dto: triggerConditions) {
            dto.setMetric(metrics);
            if (!dto.paramLegal()) {
                return false;
            }
        }
        return true;
    }
}