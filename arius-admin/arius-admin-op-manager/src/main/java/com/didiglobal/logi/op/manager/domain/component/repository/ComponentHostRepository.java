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
     * @param groupId
     * @param status
     */
    void updateComponentHostStatus(int componentId, String host, int groupId, int status);


    /**
     * 获取所有组件列表
     * @return List<ComponentHost> 组件列表
     */
    List<ComponentHost> listComponentHost();
}
