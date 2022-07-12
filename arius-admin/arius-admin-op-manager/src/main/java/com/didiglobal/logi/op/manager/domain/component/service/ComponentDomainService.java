package com.didiglobal.logi.op.manager.domain.component.service;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;

/**
 * @author didi
 * @date 2022-07-12 2:32 下午
 */
public interface ComponentDomainService {
    /**
     * 安装组件
     * @param component
     * @return
     */
    Result<Void> submitInstallComponent(Component component);
}
