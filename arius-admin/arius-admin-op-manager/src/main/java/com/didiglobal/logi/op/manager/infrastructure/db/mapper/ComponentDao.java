package com.didiglobal.logi.op.manager.infrastructure.db.mapper;

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
     * @return 更新条数
     */
    int updateContainIds(@Param("componentId") int componentId, @Param("containIds") String containIds);

    /**
     * 更新组件（安装包id）
     * @param componentPO
     * @return 更新条数
     */
    int update(ComponentPO componentPO);

    /**
     * 获取所有组件
     * @return
     */
    List<ComponentPO> listAll();

    /**
     * 查询component
     *
     * @param componentPO 组件
     * @return 组件列表
     */
    List<ComponentPO> queryComponent(ComponentPO componentPO);

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

    /**
     * 删除组件
     * @param id
     * @return
     */
    int delete(int id);
    
    /**
     * 按名称查询组件。
     *
     * @param name 要查询的组件的名称。
     * @return 一个 ComponentPO 对象
     */
    ComponentPO queryComponentByName(@Param("name") String name);
    
    /**
     * > 通过id查询组件
     *
     * @param id 要查询的组件的id
     * @return 一个 ComponentPO 对象
     */
    ComponentPO queryComponentById(@Param("id")Integer id);

    /**
     * 通过软件包包ids查询软件是否在使用
     * @param packageIds
     * @return
     */
    List<ComponentPO> getByPackageIds(List<Integer> packageIds);
}