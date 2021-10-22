package com.didichuxing.datachannel.arius.admin.common.converter;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.*;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.AppMonitorRulePO;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;

import java.util.ArrayList;

/**
 * @author zengqiao
 * @date 20/5/21
 */
public class CommonConverter {
    public static Strategy convert2Strategy(Long strategyId, AppMonitorRuleDTO monitorDTO) {
        Strategy strategy = new Strategy();

        strategy.setId(strategyId);
        strategy.setName(monitorDTO.getName());
        strategy.setPriority(monitorDTO.getPriority());
        strategy.setPeriodHoursOfDay(monitorDTO.getPeriodHoursOfDay());
        strategy.setPeriodDaysOfWeek(monitorDTO.getPeriodDaysOfWeek());
        strategy.setStrategyExpressionList(new ArrayList<>());
        strategy.setStrategyFilterList(new ArrayList<>());
        strategy.setStrategyActionList(new ArrayList<>());

        for (MonitorStrategyExpressionDTO elem: monitorDTO.getStrategyExpressionList()) {
            StrategyExpression strategyExpression = new StrategyExpression();
            strategyExpression.setMetric(elem.getMetric());
            strategyExpression.setFunc(elem.getFunc());
            strategyExpression.setEopt(elem.getEopt());
            strategyExpression.setThreshold(elem.getThreshold());
            strategyExpression.setParams(elem.getParams());
            strategy.getStrategyExpressionList().add(strategyExpression);
        }

        for (MonitorStrategyFilterDTO elem: monitorDTO.getStrategyFilterList()) {
            StrategyFilter strategyFilter = new StrategyFilter();
            strategyFilter.setTkey(elem.getTkey());
            strategyFilter.setTopt(elem.getTopt());
            strategyFilter.setTval( CommonUtils.strList2String(elem.getTval()));
            strategy.getStrategyFilterList().add(strategyFilter);
        }

        for (MonitorStrategyActionDTO elem: monitorDTO.getStrategyActionList()) {
            StrategyAction strategyAction = new StrategyAction();
            strategyAction.setNotifyGroup(CommonUtils.strList2String(elem.getNotifyGroup()));
            strategyAction.setConverge(elem.getConverge());
            strategyAction.setCallback(elem.getCallback());
            strategy.getStrategyActionList().add(strategyAction);
        }
        return strategy;
    }

    public static AppMonitorRuleDTO convert2MonitorRuleDTO(AppMonitorRulePO monitorRuleDO, Strategy strategy) {
        AppMonitorRuleDTO appMonitorRuleDTO = new AppMonitorRuleDTO();

        appMonitorRuleDTO.setId(monitorRuleDO.getId());
        appMonitorRuleDTO.setAppId(monitorRuleDO.getAppId());
        appMonitorRuleDTO.setName(strategy.getName());
        appMonitorRuleDTO.setPriority(strategy.getPriority());
        appMonitorRuleDTO.setPeriodHoursOfDay(strategy.getPeriodHoursOfDay());
        appMonitorRuleDTO.setPeriodDaysOfWeek(strategy.getPeriodDaysOfWeek());
        appMonitorRuleDTO.setStrategyExpressionList(new ArrayList<>());
        appMonitorRuleDTO.setStrategyFilterList(new ArrayList<>());
        appMonitorRuleDTO.setStrategyActionList(new ArrayList<>());

        for (StrategyExpression elem: strategy.getStrategyExpressionList()) {
            MonitorStrategyExpressionDTO strategyExpression = new MonitorStrategyExpressionDTO();
            strategyExpression.setMetric(elem.getMetric());
            strategyExpression.setFunc(elem.getFunc());
            strategyExpression.setEopt(elem.getEopt());
            strategyExpression.setThreshold(elem.getThreshold());
            strategyExpression.setParams(elem.getParams());
            appMonitorRuleDTO.getStrategyExpressionList().add(strategyExpression);
        }

        for (StrategyFilter elem: strategy.getStrategyFilterList()) {
            MonitorStrategyFilterDTO strategyFilter = new MonitorStrategyFilterDTO();
            strategyFilter.setTkey(elem.getTkey());
            strategyFilter.setTopt(elem.getTopt());
            strategyFilter.setTval(CommonUtils.string2StrList(elem.getTval()));
            appMonitorRuleDTO.getStrategyFilterList().add(strategyFilter);
        }

        for (StrategyAction elem: strategy.getStrategyActionList()) {
            MonitorStrategyActionDTO strategyAction = new MonitorStrategyActionDTO();
            strategyAction.setNotifyGroup(CommonUtils.string2StrList(elem.getNotifyGroup()));
            strategyAction.setConverge(elem.getConverge());
            strategyAction.setCallback(elem.getCallback());
            appMonitorRuleDTO.getStrategyActionList().add(strategyAction);
        }
        return appMonitorRuleDTO;
    }

    public static Silence convert2Silence(AppMonitorRulePO monitorRuleDO, MonitorSilenceDTO monitorSilenceDTO) {
        Silence silence = new Silence();
        silence.setSilenceId(monitorSilenceDTO.getId());
        silence.setStrategyId(monitorRuleDO.getStrategyId());
        silence.setBeginTime(monitorSilenceDTO.getStartTime());
        silence.setEndTime(monitorSilenceDTO.getEndTime());
        silence.setDescription(monitorSilenceDTO.getDescription());
        return silence;
    }
}