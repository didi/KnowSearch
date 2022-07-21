package com.didiglobal.logi.op.manager.domain.component.service;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;

/**
 * @author didi
 * @date 2022-07-12 2:32 下午
 */
public interface ComponentDomainService {
    /**
     * 安装组件
     *
     * @param installComponent
     * @return
     */
    Result<Void> submitInstallComponent(GeneralInstallComponent installComponent);


    /**
     * 扩缩容
     * @param scaleComponent
     * @return
     */
    Result<Void> submitScaleComponent(GeneralScaleComponent scaleComponent);


    /**
     * 创建组件
     *
     * @param component
     * @return
     */
    Result<Integer> createComponent(Component component);
}
