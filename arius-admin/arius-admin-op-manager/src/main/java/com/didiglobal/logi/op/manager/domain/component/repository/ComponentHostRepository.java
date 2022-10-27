package com.didiglobal.logi.op.manager.domain.component.repository;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-12 11:08 上午
 */
public interface ComponentHostRepository {

    /**
     * 保存组件host
     * @param componentHost
     */
    void saveComponentHost(ComponentHost componentHost);

    /**
     * 更新节点状态
     * @param componentId
     * @param host
     * @param groupName
     * @param status
     * @return
     */
    int updateComponentHostStatus(int componentId, String host, String groupName, int status);


    /**
     * 获取所有组件host列表
     * @return List<ComponentHost> 组件列表
     */
    List<ComponentHost> listComponentHost();

    /**
     * 卸载节点
     * @param componentId
     * @param host
     * @param groupName
     * @param isDeleted
     * @return
     */
    int unInstallComponentHost(int componentId, String host, String groupName, int isDeleted);

    /**
     * 获取对应组件host列表
     * @param componentId 组件id
     * @return List<ComponentHost> 组件列表
     */
    List<ComponentHost> listHostByComponentId(int componentId);
}
