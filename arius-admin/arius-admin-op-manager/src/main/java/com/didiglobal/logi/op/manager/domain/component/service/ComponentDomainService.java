package com.didiglobal.logi.op.manager.domain.component.service;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralBaseOperationComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralConfigChangeComponent;
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
     * 配置变更
     * @param changeComponent
     * @return
     */
    Result<Void> submitConfigChangeComponent(GeneralConfigChangeComponent changeComponent);


    /**
     * 创建组件
     *
     * @param component
     * @return
     */
    Result<Integer> createComponent(Component component);


    /**
     * 通过id获取Component
     * @param id
     * @return
     */
    Result<Component> getComponentById(Integer id);

    /**
     * 组件扩容
     *
     * @param component
     * @return
     */
    Result<Void> expandComponent(Component component);


    /**
     * 组件缩容
     * @param component
     * @return
     */
    Result<Void> shrinkComponent(Component component);

    /**
     * 配置变更
     * @param component
     * @return
     */
    Result<Void> changeComponentConfig(Component component);


    /**
     * 重启组件
     * @param restartComponent
     * @return
     */
    Result<Void> submitRestartComponent(GeneralBaseOperationComponent restartComponent);
}
