package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.ComponentGroupConfigPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author didi
 * @date 2022-07-19 3:20 下午
 */
@Repository
public interface ComponentGroupConfigDao {
    /**
     * 插入分组配置
     * @param groupConfigPO
     * @return
     */
    int insert(ComponentGroupConfigPO groupConfigPO);

    /**
     * 通过id获取配置
     * @param id
     * @return
     */
    ComponentGroupConfigPO getById(int id);

    /**
     * 通过组件id获取配置
     * @param componentId
     * @return
     */
    List<ComponentGroupConfigPO> getByComponentId(int componentId);

    /**
     * 获取所有的组件分组配置
     * @return List<ComponentGroupConfigPO> 分组配置
     */
    List<ComponentGroupConfigPO> listAll();

    /**
     * 更新配置
     * @param groupConfigPO
     * @return
     */
    int update(ComponentGroupConfigPO groupConfigPO);
    
    Long deleteByComponentIds(@Param("componentIds") List<Integer> deleteComponentIds);
}