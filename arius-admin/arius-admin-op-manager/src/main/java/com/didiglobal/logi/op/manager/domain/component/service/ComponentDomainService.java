package com.didiglobal.logi.op.manager.domain.component.service;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.*;

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
     * @param installComponent 通用安装组件实体
     * @return 任务id
     */
    Result<Integer> submitInstallComponent(GeneralInstallComponent installComponent);

    /**
     * 扩缩容
     *
     * @param scaleComponent 通用扩缩容实体
     * @return 任务id
     */
    Result<Integer> submitScaleComponent(GeneralScaleComponent scaleComponent);

    /**
     * 配置变更
     *
     * @param changeComponent 通用配置变更实体
     * @return 任务id
     */
    Result<Integer> submitConfigChangeComponent(GeneralConfigChangeComponent changeComponent);

    /**
     * 创建组件
     * @param component 组件实体
     * @param groupName2HostNotNormalStatusMap key-分组名，value->正常状态节点列表
     * @return
     */
    Result<Void> createComponent(Component component, Map<String, Set<String>> groupName2HostNotNormalStatusMap);

    /**
     * 通过id获取Component
     *
     * @param id 组件id
     * @return 组件实体
     */
    Result<Component> getComponentById(Integer id);

    /**
     * 组件扩缩容
     *
     * @param component                     组件
     * @param groupName2HostNormalStatusMap key-分组名，value->正常状态节点列表
     * @param type                          类型
     * @return
     */
    Result<Void> scaleComponent(Component component, Map<String, Set<String>> groupName2HostNormalStatusMap, int type);

    /**
     * 配置变更
     *
     * @param component 组件实体
     * @return
     */
    Result<Void> changeComponentConfig(Component component);

    /**
     * 重启组件
     *
     * @param restartComponent 通用重启实体
     * @return 任务id
     */
    Result<Integer> submitRestartComponent(GeneralRestartComponent restartComponent);

    /**
     * 升级组件
     *
     * @param upgradeComponent 通用升级实体
     * @return 任务id
     */
    Result<Integer> submitUpgradeComponent(GeneralUpgradeComponent upgradeComponent);

    /**
     * 回滚组件
     *
     * @param rollbackComponent 通用回滚实体
     * @return 任务id
     */
    Result<Integer> submitRollbackComponent(GeneralRollbackComponent rollbackComponent);

    /**
     * 执行相应功能的组件爱你D
     *
     * @param executeComponentFunction 通用执行功能实体
     * @return 任务id
     */
    Result<Integer> submitExecuteFunctionComponent(GeneralExecuteComponentFunction executeComponentFunction);

    /**
     * 获取组件配置
     *
     * @param componentId 组件id
     * @return 组件配置列表
     */
    Result<List<ComponentGroupConfig>> getComponentConfig(int componentId);

    /**
     * 更新组件
     *
     * @param component 组件
     * @return 更新条数
     */
    Result<Integer> updateComponent(Component component);

    /**
     * 获取所有的component
     *
     * @param
     * @return 组件列表
     */
    Result<List<Component>> listComponentWithAll();

    /**
     * 通过传入的组件参数进行id查询以及名字和描述模糊匹配查询获取组件
     *
     * @param component 组件实体
     * @return 组件列表
     */
    Result<List<Component>> queryComponent(Component component);
    /**
     * 按名称查询组件。
     * @param name 要查询的组件的名称。
     * @return  Result<Component>。
     */
    Result<Component> queryComponentByName(String name);

    /**
     * 是否包含对该package依赖的组件
     *
     * @param packageId 安装包id
     * @return true->安装包有依赖，false->安装包无依赖
     */
    Result<Boolean> hasPackageDependComponent(int packageId);

    /**
     * 根据ComponentId和分组名获取分组信息
     *
     * @param componentId 组件id
     * @param groupName   分组名
     * @return 分组配置
     */
    Result<ComponentGroupConfig> getComponentConfigByGroupName(int componentId, String groupName);

    /**
     * 上报host status状态
     *
     * @param componentId 组件id
     * @param groupName   分组名
     * @param host        节点名
     * @param status      状态
     * @return 更新条数
     */
    Result<Integer> reportComponentHostStatus(int componentId, String groupName, String host, int status);

    /**
     * 下线组件以及包含的组件
     * @param componentId 组件id
     * @return
     */
    Result<Integer> offLine(int componentId);

    /**
     * 卸载组件
     *
     * @param uninstallComponent 卸载组件
     * @return 任务id
     */
    Result<Integer> submitUninstallComponent(GeneralUninstallComponent uninstallComponent);

    /**
     * 给定组件 ID，返回组件的查询。
     *
     * @param componentId 您要查询的组件的 id。
     * @return  Result<String>
     */
    Result<String> queryComponentById(Integer componentId);

    /**
     * 软件包是否依赖
     * @param packageIds
     * @return
     */
    List<Integer> hasPackagesDependComponent(List<Integer> packageIds);
}