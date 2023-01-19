package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.infrastructure.db.ComponentHostPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
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
     
     /**
      * 选择通过组件id和宿主和分组名字
      *
      * @param componentId 组件id
      * @param host        宿主
      * @param groupName   分组名字
      * @param isDeleted   被删除
      * @return {@link ComponentHostPO}
      */
     ComponentHostPO selectByComponentIdAndHostAndGroupName(@Param("componentId") int componentId, @Param("host") String host,
                      @Param("groupName") String groupName, @Param("isDeleted") int isDeleted);
     
     /**
      * 更新组件宿主通过组件id和宿主和分组名字
      *
      * @param convertComponentHostDO2PO 转换组件宿主洗po
      */
     void updateComponentHostByComponentIdAndHostAndGroupName(ComponentHostPO convertComponentHostDO2PO);
     
     Long deleteByComponentId(@Param("componentIds")List<Integer> deleteComponentIds);
}