package com.didiglobal.logi.op.manager.interfaces.controller;

import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import com.didiglobal.logi.op.manager.interfaces.dto.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author didi
 * @date 2022-07-12 2:06 下午
 */
@Controller
@Api(value = "安装包中心api")
@RequestMapping(Constants.API_PREFIX_V3 + "/component")
public class ComponentController {

    @Autowired
    private ComponentService componentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageController.class);

    @PostMapping("/install")
    @ApiOperation(value = "")
    public Result<Void> install(@RequestBody GeneraInstallComponentDTO installComponentDTO) {
        return componentService.installComponent(ComponentAssembler.toInstallComponent(installComponentDTO));
    }

    @PostMapping("/scale/expand")
    @ApiOperation(value = "")
    public Result<Void> expend(@RequestBody GeneralScaleComponentDTO scaleComponentDTO) {
        scaleComponentDTO.setType(OperationEnum.EXPAND.getType());
        return componentService.scaleComponent(ComponentAssembler.toScaleComponent(scaleComponentDTO));
    }

    @PostMapping("/scale/shrink")
    @ApiOperation(value = "")
    public Result<Void> shrink(@RequestBody GeneralScaleComponentDTO scaleComponentDTO) {
        scaleComponentDTO.setType(OperationEnum.SHRINK.getType());
        return componentService.scaleComponent(ComponentAssembler.toScaleComponent(scaleComponentDTO));
    }

    @PutMapping("/config")
    @ApiOperation(value = "")
    public Result<Void> configChange(@RequestBody GeneralConfigChangeComponentDTO changeComponentDTO) {
        return componentService.configChangeComponent(ComponentAssembler.toConfigChangeComponent(changeComponentDTO));
    }

    @PutMapping("/restart")
    @ApiOperation(value = "")
    public Result<Void> restart(@RequestBody GeneralBaseOperationComponentDTO restartOperationComponentDTO) {
        return componentService.restartComponent(ComponentAssembler.toRestartComponent(restartOperationComponentDTO));
    }


    @PutMapping("/upgrade")
    @ApiOperation(value = "")
    public Result<Void> upgrade(@RequestBody GeneralUpgradeComponentDTO generalUpgradeComponentDTO) {
        return componentService.upgradeComponent(ComponentAssembler.toUpgradeComponent(generalUpgradeComponentDTO));
    }

    @PutMapping("/execute-function")
    @ApiOperation(value = "")
    public Result<Void> executeFunction(@RequestBody GeneralExecuteComponentFunctionDTO executeComponentFunctionDTO) {
        return componentService.executeFunctionComponent(ComponentAssembler.toExecuteFunctionComponent(executeComponentFunctionDTO));
    }
}
