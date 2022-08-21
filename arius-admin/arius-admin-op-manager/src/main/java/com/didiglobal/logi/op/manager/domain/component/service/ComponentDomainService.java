package com.didiglobal.logi.op.manager.domain.component.service;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.*;

import java.util.List;
import java.util.Map;

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

    /**
     * 升级组件
     * @param upgradeComponent
     * @return
     */
    Result<Void> submitUpgradeComponent(GeneralUpgradeComponent upgradeComponent);

    /**
     * 执行相应功能的组件爱你
     * @param executeComponentFunction
     * @return
     */
    Result<Void> submitExecuteFunctionComponent(GeneralExecuteComponentFunction executeComponentFunction);

    /**
     * 获取组件配置
     * @param componentId
     * @return
     */
    Result<List<ComponentGroupConfig>>  getComponentConfig(int componentId);

    /**
     * 更新组件
     * @param component
     * @return
     */
    Result<Void> updateComponent(Component component);


    /**
     * 获取所有的component
     * @param 无
     * @return Result<List<Component>>，组件列表
     */
    Result<List<Component>> listComponent();

}
