package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.MonitorSilenceDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.monitor.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.*;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.AppMonitorRulePO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminOdinClusterMetricEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminOdinTemplateMetricEnum;
import com.didichuxing.datachannel.arius.admin.common.converter.MonitorRuleConverter;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.monitor.MonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils.getAppId;
import static com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils.getOperator;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

@RestController
@RequestMapping(V3_NORMAL + "/monitor")
@Api(tags = "Normal-监控告警相关接口(REST)")
public class NormalMonitorController {

    @Autowired
    private AppService     appService;

    @Autowired
    private MonitorService monitorService;

    @GetMapping("/enums")
    @ResponseBody
    @ApiOperation(value = "监控枚举类", notes = "")
    public Result<Map<String, List<String>>> getMonitorEnums() {
        Map<String, List<String>> map = new HashMap<>();

        map.put("cluster",   AdminOdinClusterMetricEnum.getAllAdminOdinMetricName());
        map.put("template",  AdminOdinTemplateMetricEnum.getAllAdminOdinMetricName());

        return Result.buildSucc(map);
    }

    @PostMapping("/strategies")
    @ResponseBody
    @ApiOperation(value = "添加监控策略", notes = "")
    public Result<Boolean> createMonitor(HttpServletRequest request, @RequestBody AppMonitorRuleDTO dto) {
        if (!dto.paramLegal()) {
            return Result.buildParamIllegal("参数不合法!");
        }
        return monitorService.createMonitorRule(dto, getOperator(request));
    }

    @DeleteMapping("/strategies")
    @ResponseBody
    @ApiOperation(value = "删除监控策略", notes = "")
    public Result<Boolean> deleteMonitor(HttpServletRequest request, @RequestParam("monitorId") Long monitorId) {
        return monitorService.deleteMonitorRule(monitorId, getOperator(request));
    }

    @PutMapping("/strategies")
    @ResponseBody
    @ApiOperation(value = "修改监控策略", notes = "")
    public Result<Boolean> modifyMonitors(HttpServletRequest request, @RequestBody AppMonitorRuleDTO dto) {
        if (!dto.paramLegal() || null == dto.getId()) {
            return Result.buildParamIllegal("参数不合法!");
        }
        return monitorService.modifyMonitorRule(dto, getOperator(request));
    }

    @GetMapping("/strategies")
    @ResponseBody
    @ApiOperation(value = "监控策略列表", notes = "")
    public Result<List<MonitorRuleSummaryVO>> getMonitorRules(HttpServletRequest request) {
        List<MonitorRuleSummary> monitorRuleSummaryList = monitorService.getMonitorRules(getAppId(request));
        if (CollectionUtils.isEmpty(monitorRuleSummaryList)) {
            return Result.buildSucc("获取监控列表为空");
        }

        List<MonitorRuleSummaryVO> voList = new ArrayList<>();
        for (MonitorRuleSummary summary: monitorRuleSummaryList) {
            MonitorRuleSummaryVO vo = ConvertUtil.obj2Obj(summary,MonitorRuleSummaryVO.class);
            voList.add(vo);
        }
        return Result.buildSucc(voList);
    }

    @GetMapping("/strategies/{monitorId}")
    @ResponseBody
    @ApiOperation(value = "监控策略详情", notes = "")
    public Result<MonitorRuleDetailVO> getMonitorDetail(@PathVariable("monitorId") Long monitorId) {
        AppMonitorRulePO appMonitorRulePO = monitorService.getById(monitorId);
        if (null == appMonitorRulePO) {
            return Result.buildFail("对应的接口策略详情为空");
        }
        Result<AppMonitorRuleDTO> result = monitorService.getMonitorRuleDetail(appMonitorRulePO);
        if (null == result || result.failed()) {
            return Result.build(result.getCode(), result.getMessage());
        }

        AppMonitorRuleDTO appMonitorRuleDTO = result.getData();
        App app = appService.getAppById(appMonitorRuleDTO.getAppId());
        return Result.buildSucc( MonitorRuleConverter.convert2MonitorRuleDetailVO(appMonitorRulePO, appMonitorRuleDTO, app));
    }

    @GetMapping("/alerts")
    @ResponseBody
    @ApiOperation(value = "告警列表", notes = "")
    public Result<List<MonitorAlertVO>> getMonitorAlertHistory(@RequestParam("monitorId") Long monitorId,
                                                               @RequestParam("startTime") Long startTime,
                                                               @RequestParam("endTime") Long endTime) {
        Result<List<Alert>> result = monitorService.getMonitorAlertHistory(monitorId, startTime, endTime);
        if (null == result || result.failed()) {
            return Result.build(result.getCode(), result.getMessage());
        }
        return Result.buildSucc(MonitorRuleConverter.convert2MonitorAlertVOList(result.getData()));
    }

    @GetMapping("/alerts/{alertId}")
    @ResponseBody
    @ApiOperation(value = "告警详情", notes = "")
    public Result<MonitorAlertDetailVO> getMonitorAlertDetail(@PathVariable("alertId") Long alertId) {
        Result<MonitorAlertDetail> result = monitorService.getMonitorAlertDetail(alertId);
        if (null == result || result.failed()) {
            return Result.build(result.getCode(), result.getMessage());
        }
        return Result.buildSucc(MonitorRuleConverter.convert2MonitorAlertDetailVO(result.getData()));
    }

    @PostMapping("/silences")
    @ResponseBody
    @ApiOperation(value = "告警屏蔽创建", notes = "")
    public Result<Boolean> createMonitorSilences(HttpServletRequest request, @RequestBody MonitorSilenceDTO dto) {
        if (!dto.paramLegal()) {
            return Result.buildParamIllegal("参数不合法");
        }
        return monitorService.createSilence(dto, getOperator(request));
    }

    @PutMapping("/silences")
    @ResponseBody
    @ApiOperation(value = "告警屏蔽修改", notes = "")
    public Result<Boolean> modifyMonitorSilences(HttpServletRequest request, @RequestBody MonitorSilenceDTO dto) {
        if (!dto.paramLegal() || null == dto.getId()) {
            return Result.buildParamIllegal("参数不合法");
        }
        return monitorService.createSilence(dto, getOperator(request));
    }

    @DeleteMapping("/silences")
    @ResponseBody
    @ApiOperation(value = "告警屏蔽删除", notes = "")
    public Result<Boolean> releaseMonitorSilences(@RequestParam("monitorId") Long monitorId,
                                                  @RequestParam("silenceId") Long silenceId) {

        return Result.buildSucc(monitorService.releaseSilence(silenceId));
    }

    @GetMapping("/silences")
    @ResponseBody
    @ApiOperation(value = "告警屏蔽列表", notes = "")
    public Result<List<MonitorSilenceVO>> getMonitorSilences(@RequestParam("monitorId") Long monitorId) {
        AppMonitorRulePO appMonitorRulePO = monitorService.getById(monitorId);
        if (null == appMonitorRulePO) {
            return Result.buildFail("获取告警屏蔽列表为空");
        }

        Result<List<Silence>> result = monitorService.getSilences( appMonitorRulePO.getStrategyId());
        if (null == result || result.failed()) {
            return Result.build(result.getCode(), result.getMessage());
        }
        return Result.buildSucc(MonitorRuleConverter.convert2MonitorSilenceVOList(appMonitorRulePO, result.getData()));
    }

    @GetMapping("/silences/{silenceId}")
    @ResponseBody
    @ApiOperation(value = "告警屏蔽详情", notes = "")
    public Result<MonitorSilenceVO> getMonitorSilence(@PathVariable("silenceId") Long silenceId) {
        Silence silence = monitorService.getSilenceById(silenceId);
        if (null == silence){
            return Result.buildFail("获取告警屏蔽详情为空");
        }

        AppMonitorRulePO AppMonitorRulePO = monitorService.getByStrategyId(silence.getStrategyId());
        if (null == AppMonitorRulePO) {
            return Result.buildFail("获取告警屏蔽详情为空");
        }

        return Result.buildSucc(MonitorRuleConverter.convert2MonitorSilenceVO(AppMonitorRulePO, silence));
    }

    @GetMapping("/notify-groups")
    @ResponseBody
    @ApiOperation(value = "告警组列表", notes = "")
    public Result<List<MonitorNotifyGroupVO>> getNotifyGroups() {
        List<NotifyGroup> notifyGroupList = monitorService.getNotifyGroups();
        if (null == notifyGroupList) {
            return Result.buildFail("获取告警组失败");
        }
        return Result.buildSucc(MonitorRuleConverter.convert2MonitorNotifyGroupVOList(notifyGroupList));
    }
}
