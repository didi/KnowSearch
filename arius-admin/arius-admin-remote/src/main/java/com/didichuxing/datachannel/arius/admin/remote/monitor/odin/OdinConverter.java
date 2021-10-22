package com.didichuxing.datachannel.arius.admin.remote.monitor.odin;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.*;
import com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class OdinConverter {
    private static final String SLIENCE_TYPE = "strategy";

    public static OdinStrategy convert2OdinStrategy(Strategy strategy, String namespace) {
        OdinStrategy odinStrategy = new OdinStrategy();

        odinStrategy.setId(strategy.getId());
        odinStrategy.setNs(namespace);
        odinStrategy.setName(strategy.getName());
        odinStrategy.setPriority(strategy.getPriority());
        odinStrategy.setPeriod_hours_of_day(strategy.getPeriodHoursOfDay());
        odinStrategy.setPeriod_days_of_week(strategy.getPeriodDaysOfWeek());
        odinStrategy.setStrategy_expressions(new ArrayList<>());
        odinStrategy.setStrategy_filters(new ArrayList<>());
        odinStrategy.setStrategy_actions(new ArrayList<>());

        for (StrategyExpression elem: strategy.getStrategyExpressionList()) {
            OdinStrategyExpression strategyExpression = new OdinStrategyExpression();
            strategyExpression.setMetric(elem.getMetric());
            strategyExpression.setFunc(elem.getFunc());
            strategyExpression.setEopt(elem.getEopt());
            strategyExpression.setThreshold(elem.getThreshold());
            strategyExpression.setParams(elem.getParams());
            odinStrategy.getStrategy_expressions().add(strategyExpression);
        }

        for (StrategyFilter elem: strategy.getStrategyFilterList()) {
            OdinStrategyFilter strategyFilter = new OdinStrategyFilter();
            strategyFilter.setTkey(elem.getTkey());
            strategyFilter.setTopt(elem.getTopt());
            strategyFilter.setTval(elem.getTval());
            odinStrategy.getStrategy_filters().add(strategyFilter);
        }

        for (StrategyAction elem: strategy.getStrategyActionList()) {
            OdinStrategyAction strategyAction = new OdinStrategyAction();
            strategyAction.setNotify_group(elem.getNotifyGroup());
            strategyAction.setConverge(elem.getConverge());
            strategyAction.setCallback(elem.getCallback());
            odinStrategy.getStrategy_actions().add(strategyAction);
        }
        return odinStrategy;
    }

    public static List<Strategy> convert2StrategyList(List<OdinStrategy> odinStrategies) {
        if (odinStrategies == null) {
            return new ArrayList<>();
        }
        List<Strategy> strategies = new ArrayList<>();
        for (OdinStrategy odinStrategy: odinStrategies) {
            strategies.add(convert2Strategy(odinStrategy));
        }
        return strategies;
    }

    public static Strategy convert2Strategy(OdinStrategy odinStrategy) {
        if (odinStrategy == null) {
            return null;
        }
        Strategy strategy = new Strategy();

        strategy.setId(odinStrategy.getId());
        strategy.setName(odinStrategy.getName());
        strategy.setPriority(odinStrategy.getPriority());
        strategy.setPeriodHoursOfDay(odinStrategy.getPeriod_hours_of_day());
        strategy.setPeriodDaysOfWeek(odinStrategy.getPeriod_days_of_week());
        strategy.setStrategyExpressionList(new ArrayList<>());
        strategy.setStrategyFilterList(new ArrayList<>());
        strategy.setStrategyActionList(new ArrayList<>());

        for (OdinStrategyExpression elem: odinStrategy.getStrategy_expressions()) {
            StrategyExpression strategyExpression = new StrategyExpression();
            strategyExpression.setMetric(elem.getMetric());
            strategyExpression.setFunc(elem.getFunc());
            strategyExpression.setEopt(elem.getEopt());
            strategyExpression.setThreshold(elem.getThreshold());
            strategyExpression.setParams(elem.getParams());
            strategy.getStrategyExpressionList().add(strategyExpression);
        }

        for (OdinStrategyFilter elem: odinStrategy.getStrategy_filters()) {
            StrategyFilter strategyFilter = new StrategyFilter();
            strategyFilter.setTkey(elem.getTkey());
            strategyFilter.setTopt(elem.getTopt());
            strategyFilter.setTval(elem.getTval());
            strategy.getStrategyFilterList().add(strategyFilter);
        }

        for (OdinStrategyAction elem: odinStrategy.getStrategy_actions()) {
            StrategyAction strategyAction = new StrategyAction();
            strategyAction.setNotifyGroup(elem.getNotify_group());
            strategyAction.setConverge(elem.getConverge());
            strategyAction.setCallback(elem.getCallback());
            strategy.getStrategyActionList().add(strategyAction);
        }
        return strategy;
    }

    public static List<Alert> convert2AlertList(List<OdinAlert> odinAlertList) {
        if (odinAlertList == null) {
            return new ArrayList<>();
        }

        List<Alert> alertList = new ArrayList<>();
        for (OdinAlert odinAlert: odinAlertList) {
            alertList.add(convert2Alert(odinAlert));
        }
        return alertList;
    }

    public static Alert convert2Alert(OdinAlert odinAlert) {
        if (odinAlert == null) {
            return null;
        }
        Alert alert = new Alert();
        alert.setId(odinAlert.getId());
        alert.setStrategyId(odinAlert.getSid());
        alert.setStrategyName(odinAlert.getSname());
        alert.setType(odinAlert.getType());
        alert.setPriority(odinAlert.getPriority());
        alert.setMetric(odinAlert.getMetric());
        alert.setTags(odinAlert.getTags());
        alert.setStartTime(odinAlert.getStime());
        alert.setEndTime(odinAlert.getEtime());
        alert.setValue(odinAlert.getValue());
        alert.setPoints(odinAlert.getPoints());
        alert.setGroups(odinAlert.getGroups());
        alert.setInfo(odinAlert.getInfo());
        return alert;
    }

    public static OdinSilence convert2OdinSilenceCreation(Silence silence, String namespace) {
        OdinSilence odinSilenceCreation = new OdinSilence();
        odinSilenceCreation.setNs(namespace);
        odinSilenceCreation.setType(SLIENCE_TYPE);
        odinSilenceCreation.setSids(String.valueOf(silence.getStrategyId()));
        odinSilenceCreation.setBegin_ts(silence.getBeginTime() / 1000);
        odinSilenceCreation.setEnd_ts(silence.getEndTime() / 1000);
        odinSilenceCreation.setNote(silence.getDescription());
        return odinSilenceCreation;
    }

    public static List<Silence> convert2SilenceList(List<OdinSilence> odinSilenceCreationList) {
        if (odinSilenceCreationList == null) {
            return new ArrayList<>();
        }
        List<Silence> silenceList = new ArrayList<>();
        for (OdinSilence odinSilenceCreation: odinSilenceCreationList) {
            silenceList.add(convert2Silence(odinSilenceCreation));
        }
        return silenceList;
    }

    public static Silence convert2Silence(OdinSilence odinSilenceCreation) {
        if (odinSilenceCreation == null) {
            return null;
        }
        Silence silence = new Silence();
        silence.setSilenceId(odinSilenceCreation.getId().longValue());
        silence.setStrategyId(Long.valueOf(odinSilenceCreation.getSids()));
        silence.setBeginTime(odinSilenceCreation.getBegin_ts());
        silence.setEndTime(odinSilenceCreation.getEnd_ts());
        silence.setDescription(odinSilenceCreation.getNote());
        return silence;
    }

    public static String convert2TagStr(Properties tags) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Object> entry: tags.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append(',');
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}