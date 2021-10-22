package com.didichuxing.datachannel.arius.admin.core.service.monitor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.MonitorSilenceDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Alert;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Metric;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.MetricSinkPoint;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.MonitorAlertDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.MonitorRuleSummary;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.NotifyGroup;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Silence;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Strategy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.AppMonitorRulePO;
import com.didichuxing.datachannel.arius.admin.common.converter.CommonConverter;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.monitor.MonitorService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor.AppMonitorRuleDAO;
import com.didichuxing.datachannel.arius.admin.remote.monitor.RemoteMonitorService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * Created by dengshan on 2020/10/31
 */
@Service("monitorServiceImpl")
public class MonitorServiceImpl implements MonitorService {

    protected final ILog         LOGGER = LogFactory.getLog(MonitorServiceImpl.class);

    @Autowired
    private RemoteMonitorService remoteMonitorService;

    @Autowired
    private AppMonitorRuleDAO    appMonitorRuleDAO;

    @Autowired
    private AppService           appService;

    @Override
    public Result<Boolean> createMonitorRule(AppMonitorRuleDTO monitorDTO, String operator) {
        Integer strategyId = remoteMonitorService.createStrategy(CommonConverter.convert2Strategy(null, monitorDTO));

        if (null == strategyId) {
            return Result.buildFail("获取odin的策略id为空");
        }

        AppMonitorRulePO appMonitorRulePO = new AppMonitorRulePO();
        appMonitorRulePO.setAppId(monitorDTO.getAppId());
        appMonitorRulePO.setStrategyName(monitorDTO.getName());
        appMonitorRulePO.setOperator(operator);
        appMonitorRulePO.setStrategyId(strategyId.longValue());

        return Result.build(appMonitorRuleDAO.insert(appMonitorRulePO) > 0);
    }

    @Override
    public Result<Boolean> deleteMonitorRule(Long id, String operator) {
        AppMonitorRulePO monitorRulePO = this.getById(id);
        if (null == monitorRulePO) {
            return Result.buildFail("获取monitorRulePO为空");
        }

        Boolean status = remoteMonitorService.deleteStrategyById(monitorRulePO.getStrategyId());
        if (!status) {
            return Result.buildFail("从odin删除策略失败");
        }

        return Result.build(appMonitorRuleDAO.deleteById(id) > 0);
    }

    @Override
    public Result<Boolean> modifyMonitorRule(AppMonitorRuleDTO monitorDTO, String operator) {
        AppMonitorRulePO monitorRuleDO = this.getById(monitorDTO.getId());
        if (null == monitorRuleDO) {
            return Result.buildFail("获取monitorRulePO为空");
        }
        Integer strategyId = remoteMonitorService
            .modifyStrategy(CommonConverter.convert2Strategy(monitorRuleDO.getStrategyId(), monitorDTO));
        if (strategyId == null) {
            return Result.buildFail("从odin更改策略失败");
        }

        return Result.build(appMonitorRuleDAO.updateById(monitorDTO.getId(), monitorDTO.getAppId(), operator) > 0);
    }

    @Override
    public List<MonitorRuleSummary> getMonitorRules(Integer appid) {
        List<AppMonitorRulePO> monitorRuleDOList = this.listAll();
        if (CollectionUtils.isEmpty(monitorRuleDOList)) {
            return new ArrayList<>();
        }

        App app = appService.getAppById(appid);
        if (null == app) {
            return new ArrayList<>();
        }

        List<MonitorRuleSummary> summaryList = new ArrayList<>();
        for (AppMonitorRulePO elem : monitorRuleDOList) {
            if (app.getId().intValue() != elem.getAppId().intValue()) {
                continue;
            }

            MonitorRuleSummary summary = new MonitorRuleSummary();
            summary.setId(elem.getId());
            summary.setName(elem.getStrategyName());
            summary.setAppId(elem.getAppId());
            summary.setAppName(app.getName());
            summary.setPrincipals(app.getResponsible());
            summary.setOperator(elem.getOperator());
            summary.setCreateTime(elem.getCreateTime().getTime());
            summaryList.add(summary);
        }
        return summaryList;
    }

    @Override
    public Result<AppMonitorRuleDTO> getMonitorRuleDetail(AppMonitorRulePO monitorRulePO) {
        if (null == monitorRulePO) {
            return Result.buildFail("monitorRule为空");
        }
        Strategy strategy = remoteMonitorService.getStrategyById(monitorRulePO.getStrategyId());
        if (null == strategy) {
            return Result.buildFail("从odin获取策略失败");
        }

        AppMonitorRuleDTO appMonitorRuleDTO = CommonConverter.convert2MonitorRuleDTO(monitorRulePO, strategy);
        appMonitorRuleDTO.setId(monitorRulePO.getId());
        appMonitorRuleDTO.setAppId(monitorRulePO.getAppId());
        return Result.buildSucc(appMonitorRuleDTO);
    }

    @Override
    public AppMonitorRulePO getById(Long id) {
        try {
            return appMonitorRuleDAO.selectById(id);
        } catch (Exception e) {
            LOGGER.error("class=LogXOdinMonitorService||method=getById||id={}", id, e);
        }
        return null;
    }

    @Override
    public AppMonitorRulePO getByStrategyId(Long strategyId) {
        try {
            return appMonitorRuleDAO.selectByStrategyId(strategyId);
        } catch (Exception e) {
            LOGGER.error("class=LogXOdinMonitorService||method=getByStrategyId||strategyId={}", strategyId, e);
        }
        return null;
    }

    @Override
    public Result<List<Alert>> getMonitorAlertHistory(Long id, Long startTime, Long endTime) {
        AppMonitorRulePO monitorRuleDO = this.getById(id);
        if (null == monitorRuleDO) {
            return Result.buildFail("获取monitorRule为空");
        }
        List<Alert> alertList = remoteMonitorService.getAlerts(monitorRuleDO.getStrategyId(), startTime / 1000,
            endTime / 1000);
        if (CollectionUtils.isEmpty(alertList)) {
            return Result.buildSucc("无历史告警!");
        }
        return Result.buildSucc(alertList);
    }

    @Override
    public Result<MonitorAlertDetail> getMonitorAlertDetail(Long alertId) {
        Alert alert = remoteMonitorService.getAlertById(alertId);
        if (null == alert) {
            return Result.buildFail("从odin获取告警历史失败");
        }

        AppMonitorRulePO monitorRuleDO = this.getByStrategyId(alert.getStrategyId());
        if (null == monitorRuleDO) {
            return Result.buildFail("获取monitorRule为空");
        }
        alert.setMonitorId(monitorRuleDO.getId());

        Metric metric = remoteMonitorService.getMetrics(alert.getMetric(), (alert.getStartTime() - 3600) * 1000,
            (alert.getEndTime() + 3600) * 1000, 60, alert.getTags());

        return Result.buildSucc(new MonitorAlertDetail(alert, metric));
    }

    @Override
    public Result<Boolean> createSilence(MonitorSilenceDTO monitorSilenceDTO, String operator) {
        AppMonitorRulePO monitorRuleDO = this.getById(monitorSilenceDTO.getMonitorId());
        if (null == monitorRuleDO) {
            return Result.buildFail("获取monitorRule为空");
        }

        return Result.buildSucc(
            remoteMonitorService.createSilence(CommonConverter.convert2Silence(monitorRuleDO, monitorSilenceDTO)));
    }

    @Override
    public boolean releaseSilence(Long silenceId) {
        return remoteMonitorService.releaseSilence(silenceId);
    }

    @Override
    public Result modifySilence(MonitorSilenceDTO monitorSilenceDTO, String operator) {
        AppMonitorRulePO monitorRuleDO = this.getById(monitorSilenceDTO.getMonitorId());
        if (null == monitorRuleDO) {
            return Result.buildFail("获取monitorRule为空");
        }
        return Result.buildSucc(
            remoteMonitorService.modifySilence(CommonConverter.convert2Silence(monitorRuleDO, monitorSilenceDTO)));
    }

    @Override
    public Result<List<Silence>> getSilences(Long strategyId) {
        List<Silence> silenceList = remoteMonitorService.getSilences(strategyId);
        if (null == silenceList) {
            return Result.buildSucc("尚未配置告警屏蔽策略");
        }
        return Result.buildSucc(silenceList);
    }

    @Override
    public Silence getSilenceById(Long silenceId) {
        return remoteMonitorService.getSilenceById(silenceId);
    }

    @Override
    public List<NotifyGroup> getNotifyGroups() {
        return remoteMonitorService.getNotifyGroups();
    }

    @Override
    public Boolean sinkMetrics(List<MetricSinkPoint> pointList) {
        return remoteMonitorService.sinkMetrics(pointList);
    }

    @Override
    public Metric getMetrics(String metricStr, Long startTime, Long endTime, Integer step, Properties tags) {
        return remoteMonitorService.getMetrics(metricStr, (startTime - 3600) * 1000, (endTime + 3600) * 1000, step,
            tags);
    }

    /**************************************************** private methods ****************************************************/
    private List<AppMonitorRulePO> listAll() {
        try {
            return appMonitorRuleDAO.listAll();
        } catch (Exception e) {
            LOGGER.error("class=LogXOdinMonitorService||method=listAll", e);
        }
        return null;
    }
}
