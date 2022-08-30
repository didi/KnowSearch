package com.didiglobal.logi.op.manager.domain.component.service;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.*;
import io.swagger.models.auth.In;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    Result<Integer> submitInstallComponent(GeneralInstallComponent installComponent);


    /**
     * 扩缩容
     * @param scaleComponent
     * @return
     */
    Result<Integer> submitScaleComponent(GeneralScaleComponent scaleComponent);

    /**
     * 配置变更
     * @param changeComponent
     * @return
     */
    Result<Integer> submitConfigChangeComponent(GeneralConfigChangeComponent changeComponent);


    /**
     * 创建组件
     *
     * @param component
     * @return
     */
    Result<Void> createComponent(Component component);


    /**
     * 通过id获取Component
     * @param id
     * @return
     */
    Result<Component> getComponentById(Integer id);

    /**
     * 组件扩缩容
     * @param component
     * @param groupName2HostNormalStatusMap
     * @param type
     * @return
     */
    Result<Void> scaleComponent(Component component, Map<String, Set<String>> groupName2HostNormalStatusMap, int type);


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
    Result<Integer> submitRestartComponent(GeneralBaseOperationComponent restartComponent);

    /**
     * 升级组件
     * @param upgradeComponent
     * @return
     */
    Result<Integer> submitUpgradeComponent(GeneralUpgradeComponent upgradeComponent);

    /**
     * 执行相应功能的组件爱你D
     * @param executeComponentFunction
     * @return
     */
    Result<Integer> submitExecuteFunctionComponent(GeneralExecuteComponentFunction executeComponentFunction);

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
    Result<Integer>  updateComponent(Component component);


    /**
     * 获取所有的component
     * @param 无
     * @return Result<List<Component>>，组件列表
     */
    Result<List<Component>> listComponentWithAll();

    /**
     * 是否包含对该package依赖的组件
     * @param packageId 安装包id
     * @return Result<Boolean>
     */
    Result<Boolean> hasPackageDependComponent(int packageId);


    /**
     * 根据ComponentId和分组名获取分组信息
     * @param componentId
     * @param groupName
     * @return
     */
    Result<ComponentGroupConfig>  getComponentConfigByGroupName(int componentId, String groupName);

    /**
     * 上报host status状态
     * @param componentId
     * @param groupName
     * @param host
     * @param status
     * @return
     */
    Result<Integer>  reportComponentHostStatus(int componentId, String groupName, String host, int status);

}
