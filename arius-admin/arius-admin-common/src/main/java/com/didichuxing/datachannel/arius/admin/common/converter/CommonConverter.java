package com.didichuxing.datachannel.arius.admin.common.converter;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.MonitorExpressionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.AlertRule;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.Exp;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.Expression;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.TagFilter;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.MonitorRulePO;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author zengqiao
 * @date 20/5/21
 */
public class CommonConverter {

    private CommonConverter(){}

    public static AlertRule convert2AlertRule(Long alertRuleId, AppMonitorRuleDTO monitorDTO) {
        AlertRule alertRule = new AlertRule();
        alertRule.setId(alertRuleId);
        alertRule.setGroupId(27);//先写死
        alertRule.setName(monitorDTO.getName());
        alertRule.setType(0);
        alertRule.setStatus(0);
        alertRule.setAlertDuration(60);

        Expression expression = new Expression();
        expression.setTogetherOrAny(monitorDTO.getTogetherOrAny());
        expression.setTriggerConditions(Lists.newArrayList());
        expression.setTagFilters(Lists.newArrayList());

        alertRule.setExpression(expression);
        alertRule.setEnableStime(monitorDTO.getEnableStime());
        alertRule.setEnableEtime(monitorDTO.getEnableEtime());
        alertRule.setEnableDaysOfWeek(transformSeparator(monitorDTO.getEnableDaysOfWeek(),","," "));
        alertRule.setPriority(monitorDTO.getPriority());
        alertRule.setNotifyChannels(transformSeparator(monitorDTO.getNotifyChannels(),","," "));
        alertRule.setNotifyGroups(monitorDTO.getNotifyGroups());
        alertRule.setNotifyUsers(transformSeparator(monitorDTO.getNotifyUsers(),","," "));
        alertRule.setCallbacks(transformSeparator(monitorDTO.getCallbacks(),","," "));

        for (MonitorExpressionDTO elem: monitorDTO.getTriggerConditions()) {
            Exp exp = new Exp();
            exp.setOptr(elem.getOptr());
            exp.setFunc(elem.getFunc());
            exp.setMetric(elem.getMetric());
            exp.setParams(Lists.newArrayList(elem.getParams()));
            exp.setThreshold(elem.getThreshold());
            expression.getTriggerConditions().add(exp);
        }

        TagFilter tagFilter = new TagFilter();
        //cluster||template||node
        tagFilter.setKey(monitorDTO.getCategory());
        tagFilter.setParams(Lists.newArrayList(monitorDTO.getObjectNames().split(",")));
        expression.getTagFilters().add(tagFilter);

        return alertRule;
    }

    public static AppMonitorRuleDTO convert2MonitorRuleDTO(MonitorRulePO monitorRulePO, AlertRule alertRule) {
        AppMonitorRuleDTO appMonitorRuleDTO = new AppMonitorRuleDTO();
        appMonitorRuleDTO.setId(monitorRulePO.getId());
        appMonitorRuleDTO.setAppId(monitorRulePO.getAppId());
        appMonitorRuleDTO.setName(monitorRulePO.getName());
        appMonitorRuleDTO.setCategory(monitorRulePO.getCategory());
        appMonitorRuleDTO.setObjectNames(monitorRulePO.getObjectNames());
        appMonitorRuleDTO.setMetrics(monitorRulePO.getMetrics());
        appMonitorRuleDTO.setEnableStime(alertRule.getEnableStime());
        appMonitorRuleDTO.setEnableEtime(alertRule.getEnableEtime());
        appMonitorRuleDTO.setEnableDaysOfWeek(alertRule.getEnableDaysOfWeek());
        appMonitorRuleDTO.setTriggerConditions(Lists.newArrayList());
        appMonitorRuleDTO.setPriority(monitorRulePO.getPriority());
        appMonitorRuleDTO.setNotifyChannels(alertRule.getNotifyChannels());
        appMonitorRuleDTO.setNotifyGroups(alertRule.getNotifyGroups());
        appMonitorRuleDTO.setNotifyUsers(alertRule.getNotifyUsers());
        appMonitorRuleDTO.setCallbacks(alertRule.getCallbacks());
        for (Exp exp : alertRule.getExpression().getTriggerConditions()) {
            MonitorExpressionDTO dto = new MonitorExpressionDTO();
            dto.setOptr(exp.getOptr());
            dto.setFunc(exp.getFunc());
            dto.setMetric(exp.getMetric());
            dto.setParams(exp.getParams().stream().map(String::valueOf).collect(Collectors.joining(",")));
            dto.setThreshold(exp.getThreshold());
            appMonitorRuleDTO.getTriggerConditions().add(dto);
        }
        return appMonitorRuleDTO;
    }

    public static String transformSeparator(String s, String separatorFrom, String separatorTo) {
        if (StringUtils.isBlank(s)) {
            return "";
        }
        return Arrays.stream(s.trim().split(separatorFrom)).filter(StringUtils::isNotBlank).collect(Collectors.joining(separatorTo));
    }
}