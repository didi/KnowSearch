package com.didichuxing.datachannel.arius.admin.common.converter;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.monitor.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.*;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.AppMonitorRulePO;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zengqiao
 * @date 20/5/21
 */
public class MonitorRuleConverter {
    public static MonitorRuleDetailVO convert2MonitorRuleDetailVO(AppMonitorRulePO monitorRuleDO,
                                                                  AppMonitorRuleDTO appMonitorRuleDTO,
                                                                  App app) {
        MonitorRuleDetailVO vo = new MonitorRuleDetailVO();
        vo.setId(monitorRuleDO.getId());
        vo.setName(monitorRuleDO.getStrategyName());
        vo.setOperator(monitorRuleDO.getOperator());
        vo.setCreateTime(monitorRuleDO.getCreateTime().getTime());
        vo.setModifyTime(monitorRuleDO.getUpdateTime().getTime());
        vo.setMonitorRule(appMonitorRuleDTO);
        if(null == app){return vo;}

        AppSummaryVO appSummaryVO = new AppSummaryVO();
        appSummaryVO.setAppId(String.valueOf(app.getId()));
        appSummaryVO.setName(app.getName());
        appSummaryVO.setPrincipals(app.getResponsible());
        vo.setAppSummary(appSummaryVO);
        return vo;
    }

    public static List<MonitorAlertVO> convert2MonitorAlertVOList(List<Alert> alertList) {
        if (CollectionUtils.isEmpty(alertList)) {
            return new ArrayList<>();
        }

        List<MonitorAlertVO> voList = new ArrayList<>();
        for (Alert alert: alertList) {
            voList.add(convert2MonitorAlertVO(alert));
        }
        return voList;
    }

    public static MonitorAlertDetailVO convert2MonitorAlertDetailVO(MonitorAlertDetail monitorAlertDetail) {
        MonitorAlertDetailVO monitorAlertDetailVO = new MonitorAlertDetailVO();
        monitorAlertDetailVO.setMonitorAlert(convert2MonitorAlertVO(monitorAlertDetail.getAlert()));
        monitorAlertDetailVO.setMonitorMetric(convert2MonitorMetricVO(monitorAlertDetail.getMetric()));
        return monitorAlertDetailVO;
    }

    private static MonitorAlertVO convert2MonitorAlertVO(Alert alert) {
        if(null == alert){return null;}

        MonitorAlertVO vo = new MonitorAlertVO();
        vo.setAlertId(alert.getId());
        vo.setMonitorId(alert.getMonitorId());
        vo.setMonitorName(alert.getStrategyName());
        vo.setMonitorPriority(alert.getPriority());
        vo.setAlertStatus("alert".equals(alert.getType())? 0: 1);
        vo.setStartTime(alert.getStartTime() * 1000);
        vo.setEndTime(alert.getEndTime() * 1000);
        vo.setMetric(alert.getMetric());
        vo.setValue(alert.getValue());
//        vo.setPoints(alert.getPoints());
        vo.setGroups(alert.getGroups());
        vo.setInfo(alert.getInfo());
        return vo;
    }

    private static MonitorMetricVO convert2MonitorMetricVO(Metric metric) {
        MonitorMetricVO vo = new MonitorMetricVO();
        vo.setMetric(metric.getMetric());
        vo.setStep(metric.getStep());
        vo.setValues(new ArrayList<>());
        vo.setComparison(metric.getComparison());
        vo.setDelta(metric.getDelta());
        vo.setOrigin(metric.getOrigin());

        for (MetricPoint metricPoint: metric.getValues()) {
            vo.getValues().add(new MonitorMetricPoint(metricPoint.getTimestamp(), metricPoint.getValue()));
        }
        return vo;
    }

    public static List<MonitorSilenceVO> convert2MonitorSilenceVOList(AppMonitorRulePO monitorRuleDO,
                                                                      List<Silence> silenceList) {
        if (CollectionUtils.isEmpty(silenceList)) {
            return new ArrayList<>();
        }
        List<MonitorSilenceVO> voList = new ArrayList<>();
        for (Silence silence: silenceList) {
            voList.add(convert2MonitorSilenceVO(monitorRuleDO, silence));
        }
        return voList;
    }

    public static MonitorSilenceVO convert2MonitorSilenceVO(AppMonitorRulePO monitorRuleDO, Silence silence) {
        if(null == silence){return null;}

        MonitorSilenceVO vo = new MonitorSilenceVO();
        vo.setSilenceId(silence.getSilenceId());
        vo.setMonitorId(monitorRuleDO.getId());
        vo.setMonitorName(monitorRuleDO.getStrategyName());
        vo.setStartTime(silence.getBeginTime());
        vo.setEndTime(silence.getEndTime());
        vo.setDescription(silence.getDescription());
        return vo;
    }

    public static List<MonitorNotifyGroupVO> convert2MonitorNotifyGroupVOList(List<NotifyGroup> notifyGroupList) {
        if (null == notifyGroupList) {
            return new ArrayList<>();
        }
        List<MonitorNotifyGroupVO> voList = new ArrayList<>();
        for (NotifyGroup notifyGroup: notifyGroupList) {
            MonitorNotifyGroupVO vo = new MonitorNotifyGroupVO();
            vo.setId(notifyGroup.getId());
            vo.setName(notifyGroup.getName());
            vo.setComment(notifyGroup.getComment());
            voList.add(vo);
        }
        return voList;
    }
}