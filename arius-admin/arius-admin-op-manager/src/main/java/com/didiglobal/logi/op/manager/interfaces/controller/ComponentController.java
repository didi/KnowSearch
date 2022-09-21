package com.didiglobal.logi.op.manager.interfaces.controller;

import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import com.didiglobal.logi.op.manager.interfaces.assembler.ScriptAssembler;
import com.didiglobal.logi.op.manager.interfaces.dto.ScriptDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.component.ComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.component.ComponentHostReportDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.*;
import com.didiglobal.logi.op.manager.interfaces.vo.ComponentVO;
import com.didiglobal.logi.op.manager.interfaces.vo.GeneralGroupConfigHostVO;
import com.didiglobal.logi.op.manager.interfaces.vo.ScriptVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-12 2:06 下午
 */
@RestController
@Api(value = "安装包中心api")
@RequestMapping(Constants.API_PREFIX_V3 + "/component")
public class ComponentController {

    @Autowired
    private ComponentService componentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageController.class);

    @GetMapping("/list")
    @ApiOperation(value = "")
    public Result<List<Component>> listResult() {
        return componentService.listComponentWithAll();
    }

    @PostMapping("/query")
    @ApiOperation(value = "获取组件详情列表")
    public Result<List<ComponentVO>> listScript(@RequestBody ComponentDTO queryComponentDTO) {
        Result res = componentService.listComponent(ComponentAssembler.toDO(queryComponentDTO));
        if (res.isSuccess()) {
            res.setData(ComponentAssembler.toVOList((List<Component>) res.getData()));
        }
        return res;
    }

    @PostMapping("/install")
    @ApiOperation(value = "")
    public Result<Integer> install(@RequestBody GeneraInstallComponentDTO installComponentDTO) {
        return componentService.installComponent(ComponentAssembler.toInstallComponent(installComponentDTO));
    }

    @PostMapping("/scale/expand")
    @ApiOperation(value = "")
    public Result<Integer> expend(@RequestBody GeneralScaleComponentDTO scaleComponentDTO) {
        scaleComponentDTO.setType(OperationEnum.EXPAND.getType());
        return componentService.scaleComponent(ComponentAssembler.toScaleComponent(scaleComponentDTO));
    }

    @PostMapping("/scale/shrink")
    @ApiOperation(value = "")
    public Result<Integer> shrink(@RequestBody GeneralScaleComponentDTO scaleComponentDTO) {
        scaleComponentDTO.setType(OperationEnum.SHRINK.getType());
        return componentService.scaleComponent(ComponentAssembler.toScaleComponent(scaleComponentDTO));
    }

    @PutMapping("/config")
    @ApiOperation(value = "")
    public Result<Integer> configChange(@RequestBody GeneralConfigChangeComponentDTO changeComponentDTO) {
        return componentService.configChangeComponent(ComponentAssembler.toConfigChangeComponent(changeComponentDTO));
    }

    @PostMapping("/restart")
    @ApiOperation(value = "")
    public Result<Integer> restart(@RequestBody GeneralRestartComponentDTO restartComponentDTO) {
        return componentService.restartComponent(ComponentAssembler.toRestartComponent(restartComponentDTO));
    }


    @PostMapping("/upgrade")
    @ApiOperation(value = "")
    public Result<Integer> upgrade(@RequestBody GeneralUpgradeComponentDTO generalUpgradeComponentDTO) {
        return componentService.upgradeComponent(ComponentAssembler.toUpgradeComponent(generalUpgradeComponentDTO));
    }

    @PostMapping("/rollback")
    @ApiOperation(value = "")
    public Result<Integer> rollback(@RequestBody GeneralRollbackComponentDTO generalRollbackComponentDTO) {
        return componentService.rollbackComponent(ComponentAssembler.toRollbackComponent(generalRollbackComponentDTO));
    }

    @PostMapping("/execute-function")
    @ApiOperation(value = "")
    public Result<Integer> executeFunction(@RequestBody GeneralExecuteComponentFunctionDTO executeComponentFunctionDTO) {
        return componentService.executeFunctionComponent(ComponentAssembler.toExecuteFunctionComponent(executeComponentFunctionDTO));
    }

    @GetMapping("/config")
    @ApiOperation(value = "zeus获取组件分组配置,主要是任务状态获取用")
    public Result<GeneralGroupConfigHostVO> getConfig(@RequestParam(value = "componentId", required = true) Integer componentId,
                                                      @RequestParam(value = "groupName", required = true) String groupName,
                                                      @RequestParam(value = "host", required = true) String host) {
        Result res = componentService.getConfig(componentId, groupName);
        if (res.isSuccess()) {
            ComponentGroupConfig groupConfig = (ComponentGroupConfig) res.getData();
            return ComponentAssembler.toGeneralGroupConfigVO(ConvertUtil.obj2Obj(groupConfig, GeneralGroupConfig.class), host);
        }
        return res;
    }

    @PutMapping("/host/status")
    @ApiOperation(value = "zeus上报状态")
    public Result<Integer> reportHostStatus(@RequestBody ComponentHostReportDTO componentHostReportDTO) {
        return componentService.reportHostStatus(componentHostReportDTO.getComponentId(), componentHostReportDTO.getGroupName(),
                componentHostReportDTO.getHost(), componentHostReportDTO.getStatus());
    }
}
