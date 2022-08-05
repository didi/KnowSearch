package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.ComponentGroupConfigPO;
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
}
