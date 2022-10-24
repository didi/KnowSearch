package com.didiglobal.logi.op.manager.domain.component.repository;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-12 11:08 上午
 */
public interface ComponentRepository {
    /**
     * 保存组件
     *
     * @param component
     * @return
     */
    int saveComponent(Component component);

    /**
     * 通过id获取组件
     *
     * @param componentId
     * @return
     */
    Component getComponentById(int componentId);


    /**
     * 更新包含的组件信息
     *
     * @param componentId
     * @param containIds
     * @return 更新条数
     */
    int updateContainIds(int componentId, String containIds);


    /**
     * 更新组件信息
     *
     * @param component
     * @return 更新条数
     */
    int updateComponent(Component component);

    /**
     * 获取所有的组件列表
     *
     * @return
     */
    List<Component> listAllComponent();

    /**
     * 根据条件获取组件list
     * @param component
     * @return List<Component>
     */
    List<Component> queryComponent(Component component);

    /**
     * 根据package获取组件
     *
     * @param packageId
     * @return
     */
    List<Component> getComponentByPackageId(int packageId);

    /**
     * 通过id获取依赖的组件
     *
     * @param id
     * @return
     */
    Component getDependComponentById(int id);

    /**
     * 删除组件
     * @param id
     * @return
     */
    int deleteComponent(int componentId);
}
