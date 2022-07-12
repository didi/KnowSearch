package com.didiglobal.logi.op.manager.application;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.domain.packages.service.PackageDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
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

    public Result<Void> installComponent(Component component){
        LOGGER.info("start install component[]", component.getName());
        Result checkRes = component.checkInstallParam();
        if (checkRes.failed()) {
            return checkRes;
        }
        componentDomainService.submitInstallComponent(component);
        return null;
    }
}
