package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.infrastructure.db.ComponentPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    void updateContainIds(@Param("componentId") int componentId, @Param("containIds") String containIds);

    /**
     * 更新组件（安装包id）
     * @param componentPO
     */
    int update(ComponentPO componentPO);

    /**
     * 获取所有组件
     * @return
     */
    List<ComponentPO> listAll();

    /**
     * 根军packageId获取组件
     * @param packageId
     * @return
     */
    List<ComponentPO> getByPackageId(int packageId);

    /**
     * 通过id获取依赖组件
     * @param id
     * @return
     */
    ComponentPO findDependComponentById(int id);
}
