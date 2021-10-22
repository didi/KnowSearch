package com.didichuxing.datachannel.arius.admin.core.service.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.MonitorSilenceDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.*;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.AppMonitorRulePO;

import java.util.List;
import java.util.Properties;

public interface MonitorService {
    /**
     * 创建一个监控告警的规则
     * @param monitorDTO
     * @param operator
     * @return
     */
    Result<Boolean> createMonitorRule(AppMonitorRuleDTO monitorDTO, String operator);

    /**
     * 删除一个监控告警的规则
     * @param id
     * @param operator
     * @return
     */
    Result<Boolean> deleteMonitorRule(Long id, String operator);

    /**
     * 更新一个监控告警的规则
     * @param monitorDTO
     * @param operator
     * @return
     */
    Result<Boolean> modifyMonitorRule(AppMonitorRuleDTO monitorDTO, String operator);

    /**
     * 获取所有的监控告警的规则
     * @param appid
     * @return
     */
    List<MonitorRuleSummary> getMonitorRules(Integer appid);

    /**
     * 获取一个监控规则的详情
     * @param monitorRuleDO
     * @return
     */
    Result<AppMonitorRuleDTO> getMonitorRuleDetail(AppMonitorRulePO monitorRuleDO);

    /**
     * 根据id获取一个监控规则的详情
     * @param id
     * @return
     */
    AppMonitorRulePO getById(Long id);

    /**
     * 获取一个监控策略
     * @param strategyId
     * @return
     */
    AppMonitorRulePO getByStrategyId(Long strategyId);

    /**
     * 获取一段时间内的告警历史
     * @param id
     * @param startTime
     * @param endTime
     * @return
     */
    Result<List<Alert>> getMonitorAlertHistory(Long id, Long startTime, Long endTime);

    /**
     * 获取一个告警的详情
     * @param alertId
     * @return
     */
    Result<MonitorAlertDetail> getMonitorAlertDetail(Long alertId);

    /**
     * 创建一个屏蔽告警的规则
     * @param monitorSilenceDTO
     * @param operator
     * @return
     */
    Result<Boolean> createSilence(MonitorSilenceDTO monitorSilenceDTO, String operator);

    /**
     * 释放一个屏蔽告警
     * @param silenceId
     * @return
     */
    boolean releaseSilence(Long silenceId);

    /**
     * 更新一个屏蔽告警的规则
     * @param monitorSilenceDTO
     * @param operator
     * @return
     */
    Result modifySilence(MonitorSilenceDTO monitorSilenceDTO, String operator);

    /**
     * 获取一个告警规则的屏蔽规则
     * @param strategyId
     * @return
     */
    Result<List<Silence>> getSilences(Long strategyId);

    /**
     * 根据silenceId获取一个屏蔽告警的规则
     * @param silenceId
     * @return
     */
    Silence getSilenceById(Long silenceId);

    /**
     * 指标的上报和查询
     */
    Boolean sinkMetrics(List<MetricSinkPoint> pointList);

    /**
     * 获取指标
     * @param metric
     * @param startTime
     * @param endTime
     * @param step
     * @param tags
     * @return
     */
    Metric getMetrics(String metric, Long startTime, Long endTime, Integer step, Properties tags);

    /**
     * 获取告警组
     */
    List<NotifyGroup> getNotifyGroups();
}
