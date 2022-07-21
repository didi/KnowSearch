package com.didiglobal.logi.op.manager.interfaces.controller;

import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import com.didiglobal.logi.op.manager.interfaces.dto.ComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.GeneraInstallComponentDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/scale")
    @ApiOperation(value = "")
    public Result<Void> scale(@RequestBody GeneraInstallComponentDTO installComponentDTO) {
        return componentService.installComponent(ComponentAssembler.toInstallComponent(installComponentDTO));
    }
}
