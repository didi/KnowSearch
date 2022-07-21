package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.ComponentPO;
import org.springframework.stereotype.Repository;

/**
 * @author didi
 * @date 2022-07-19 3:05 下午
 */
@Repository
public interface ComponentDao {
    /**
     * 插入组件
     * @param componentPO
     * @return
     */
    int insert(ComponentPO componentPO);

    /**
     * 通过id获取组件
     * @param id
     * @return
     */
    ComponentPO findById(int id);

    /**
     * 更新依赖组件信息
     * @param componentId
     * @param containIds
     */
    void updateContainIds(int componentId, String containIds);
}
