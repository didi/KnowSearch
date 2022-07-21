package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.ComponentHostPO;
import org.springframework.stereotype.Repository;

/**
 * @author didi
 * @date 2022-07-19 3:37 下午
 */
@Repository
public interface ComponentHostDao {
     /**
      * 插入数据
      * @param hostPO
      */
     void insert(ComponentHostPO hostPO);
}
