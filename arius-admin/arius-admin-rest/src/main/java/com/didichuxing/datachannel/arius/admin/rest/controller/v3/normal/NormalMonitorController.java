package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import com.didichuxing.datachannel.arius.admin.common.bean.common.BaseResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.NameValue;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.NotifyGroupDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.QueryMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.QueryNotifyGroupDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor.MonitorAlertDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor.MonitorAlertVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor.MonitorRuleDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor.NotifyGroupVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.GlobalParams;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Alert;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.MonitorAlertDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.UserInfo;
import com.didichuxing.datachannel.arius.admin.common.constant.NotifyGroupStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.converter.MonitorRuleConverter;
import com.didichuxing.datachannel.arius.admin.common.exception.NotExistException;
import com.didichuxing.datachannel.arius.admin.core.service.monitor.MonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

@RestController
@RequestMapping(V3_NORMAL + "/monitor")
@Api(tags = "Normal-告警中心相关接口")
public class NormalMonitorController {

    @Autowired
    private MonitorService monitorService;

    @GetMapping("/monitorRule/category")
    @ApiOperation(value = "获取告警对象类型")
    public Result<List<NameValue>> getCategory() {
        return Result.buildSucc(monitorService.findMonitorCategory());
    }

    @GetMapping("/monitorRule/statsType")
    @ApiOperation(value = "获取告警策略的统计类型")
    public Result<List<NameValue>> getStatsType() {
        return Result.buildSucc(monitorService.findMonitorStatsTypes());
    }

    @GetMapping("/monitorRule/operator")
    @ApiOperation(value = "获取告警策略的度量方式")
    public Result<List<NameValue>> getOperator() {
        return Result.buildSucc(monitorService.findMonitorOperators());
    }

    @GetMapping("/monitorRule/{category}/metrics")
    @ApiOperation(value = "获取不同告警对象的指标")
    public Result<List<String>> getMetrics(@PathVariable String category) {
        return Result.buildSucc(monitorService.findMonitorMetrics(category));
    }

    @PostMapping("/monitorRule")
    @ApiOperation(value = "添加告警策略", notes = "")
    public Result<Long> createMonitorRule(@RequestBody AppMonitorRuleDTO dto) {
        if (!dto.paramLegal()) {
            return Result.buildParamIllegal("参数不合法!");
        }
        return monitorService.createMonitorRule(dto);
    }

    @DeleteMapping("/monitorRule/{id}")
    @ApiOperation(value = "删除告警策略", notes = "")
    public Result<Boolean> deleteMonitor(@PathVariable Long id) {
        return monitorService.deleteMonitorRule(id);
    }

    @PutMapping("/monitorRule")
    @ApiOperation(value = "修改告警策略", notes = "")
    public Result<Void> modifyMonitors(@RequestBody AppMonitorRuleDTO dto) {
        if (!dto.paramLegal() || null == dto.getId()) {
            return Result.buildParamIllegal("参数不合法!");
        }
        return monitorService.modifyMonitorRule(dto);
    }

    @PostMapping("/monitorRules")
    @ApiOperation(value = "告警策略列表", notes = "")
    public BaseResult getMonitorRules(@RequestBody QueryMonitorRuleDTO dto) {
        Integer appId = dto.getAppId() != null ? dto.getAppId() : GlobalParams.CURRENT_APPID.get();
        dto.setAppId(appId);
        return monitorService.findMonitorRules(dto);
    }

    @GetMapping("/monitorRule/{id}")
    @ApiOperation(value = "告警策略详情", notes = "")
    public Result<MonitorRuleDetailVO> getMonitorDetail(@PathVariable("id") Long id) {
        return monitorService.getMonitorRuleDetail(id);
    }

    @PostMapping("/monitorRule/switch/{id}")
    @ApiOperation(value = "启用/停用告警策略", notes = "0 启用，1禁用")
    public Result<Boolean> switchMonitorRule(@PathVariable("id") Long id, @RequestParam Integer status) {
        return monitorService.switchMonitorRule(id, status);
    }

    @GetMapping("/alerts")
    @ApiOperation(value = "告警列表", notes = "")
    public Result<List<MonitorAlertVO>> getMonitorAlertHistory(@RequestParam("monitorId") Long monitorId,
                                                               @RequestParam("startTime") Long startTime,
                                                               @RequestParam("endTime") Long endTime) {
        Result<List<Alert>> result = monitorService.getMonitorAlertHistory(monitorId, startTime, endTime);
        if (null == result || result.failed()) {
            return Result.buildFail("告警列表为空");
        }
        return Result.buildSucc(MonitorRuleConverter.convert2MonitorAlertVOList(result.getData()));
    }

    @GetMapping("/alerts/{alertId}")
    @ApiOperation(value = "告警详情", notes = "")
    public Result<MonitorAlertDetailVO> getMonitorAlertDetail(@PathVariable("alertId") Long alertId) {
        Result<MonitorAlertDetail> result = monitorService.getMonitorAlertDetail(alertId);
        if (null == result || result.failed()) {
            return Result.buildFail("告警详情为空");
        }
        return Result.buildSucc(MonitorRuleConverter.convert2MonitorAlertDetailVO(result.getData()));
    }

    //告警组

    @PostMapping("/notifyGroups")
    @ApiOperation(value = "告警组列表")
    public PaginationResult<NotifyGroupVO> findNotifyGroups(@RequestBody QueryNotifyGroupDTO param) {
        return monitorService.findNotifyGroupPage(param);
    }

    @GetMapping("/notifyGroup/{id}")
    @ApiOperation(value = "告警组详情")
    public Result<NotifyGroupVO> getNotifyGroup(@PathVariable Long id) throws NotExistException {
        return Result.buildSucc(monitorService.getNotifyGroupVO(id));
    }

    @PostMapping("/notifyGroup")
    @ApiOperation(value = "添加告警组")
    public Result<Void> saveNotifyGroup(@RequestBody NotifyGroupDTO dto) {
        monitorService.saveNotifyGroup(dto);
        return Result.buildSucc();
    }

    @PutMapping("/notifyGroup")
    @ApiOperation(value = "修改告警组")
    public Result<Void> modifyNotifyGroup(@RequestBody NotifyGroupDTO dto) throws Exception {
        monitorService.modifyNotifyGroup(dto);
        return Result.buildSucc();
    }

    @DeleteMapping("/notifyGroup/{id}")
    @ApiOperation(value = "删除告警组")
    public Result<Void> delNotifyGroup(@PathVariable Long id) throws NotExistException {
        monitorService.removeNotifyGroup(id);
        return Result.buildSucc();
    }

    @PostMapping("/notifyGroup/switch/{id}")
    @ApiOperation(value = "开启/关闭告警组")
    public Result<Void> switchNotifyGroup(@PathVariable Long id, @RequestParam Integer status) throws NotExistException {
        if (!status.equals(NotifyGroupStatusEnum.ENABLE.getValue())
                && !status.equals(NotifyGroupStatusEnum.DISABLE.getValue())) {
            return Result.buildFail("status 参数错误");
        }
        monitorService.switchNotifyGroup(id, status);
        return Result.buildSucc();
    }

    @GetMapping("/notifyGroup/{id}/inuse")
    @ApiOperation(value = "判断告警组是否在使用")
    public Result<List<String>> checkNotifyGroupUsed(@PathVariable Long id) {
        return Result.buildSucc(monitorService.checkNotifyGroupUsed(id));
    }

    @GetMapping("/notifyGroup/users")
    @ApiOperation(value = "从夜莺查找人员信息")
    public Result<List<UserInfo>> getNotifyGroup(@RequestParam String keyword) {
        return Result.buildSucc(monitorService.findN9eUsers(keyword));
    }
}
