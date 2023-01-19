package com.didiglobal.logi.op.manager.domain.component.repository;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import java.util.List;

/**
 * @author didi
 * @date 2022-07-12 11:09 上午
 */
public interface ComponentGroupConfigRepository {
    /**
     * 保存分组配置
     * @param groupConfig
     * @return
     */
    int saveGroupConfig(ComponentGroupConfig groupConfig);

    /**
     * 更新分组配置
     * @param groupConfig
     * @return
     */
    int updateGroupConfig(ComponentGroupConfig groupConfig);

    /**
     * 根据id获取配置
     * @param groupId
     * @return
     */
    ComponentGroupConfig getConfigById(int groupId);

    /**
     * 通过组件id获取配置
     * @param componentId
     * @return
     */
    List<ComponentGroupConfig> getConfigByComponentId(int componentId);

    /**
     * 获取所有的组件分组配置
     * @return List<ComponentGroupConfig>  组件分组配置
     */
    List<ComponentGroupConfig> listGroupConfig();
    
    boolean deleteByComponentIds(List<Integer> deleteComponentIds);
}