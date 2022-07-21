package com.didiglobal.logi.op.manager.application;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author didi
 * @date 2022-07-12 2:31 下午
 */
@org.springframework.stereotype.Component
public class ComponentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentService.class);

    @Autowired
    private ComponentDomainService componentDomainService;

    public Result<Void> installComponent(GeneralInstallComponent installComponent){
        LOGGER.info("start install component[{}]", installComponent.getName());
        Result checkRes = installComponent.checkInstallParam();
        if (checkRes.failed()) {
            return checkRes;
        }
        componentDomainService.submitInstallComponent(installComponent);
        return Result.success();
    }

    public Result<Void> scaleComponent(GeneralScaleComponent scaleComponent){
        LOGGER.info("start scale component[{}]", scaleComponent);
        Result checkRes = scaleComponent.checkScaleParam();
        if (checkRes.failed()) {
            return checkRes;
        }
        componentDomainService.submitScaleComponent(scaleComponent);
        return Result.success();
    }
}
