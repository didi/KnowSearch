package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.ComponentHostPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

     /**
      * 更新状态
      * @param componentId
      * @param host
      * @param groupName
      * @param status
      * @return
      */
     int updateStatus(@Param("componentId") int componentId, @Param("host") String host,
                       @Param("groupName") String groupName, @Param("status") int status);

     /**
      * 获取所有组件host列表
      * @return List<ComponentHostPO> 组件po
      */
     List<ComponentHostPO> listAll();

     /**
      * 更新isDeleted字段
      * @param componentId
      * @param host
      * @param groupName
      * @param isDeleted
      * @return
      */
     int updateDeleteStatus(@Param("componentId") int componentId, @Param("host") String host,
                      @Param("groupName") String groupName, @Param("isDeleted") int isDeleted);

     /**
      * 获取所有组件host列表
      * @param componentId 组件id
      * @return List<ComponentHostPO> 组件po
      */
     List<ComponentHostPO> findByComponentId(@Param("componentId") int componentId);
}
