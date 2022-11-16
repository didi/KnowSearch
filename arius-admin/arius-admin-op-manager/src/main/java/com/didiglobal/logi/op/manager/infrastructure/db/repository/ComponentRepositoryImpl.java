package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.ComponentPO;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.ComponentConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.ComponentDao;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-19 3:02 下午
 */
@Repository
public class ComponentRepositoryImpl implements ComponentRepository {

    @Autowired
    private ComponentDao componentDao;

    @Override
    public int saveComponent(Component component) {
        ComponentPO po = ComponentConverter.convertComponentDO2PO(component);
        componentDao.insert(po);
        return po.getId();
    }

    @Override
    public Component getComponentById(int componentId) {
        return ComponentConverter.convertComponentPO2DO(componentDao.findById(componentId));
    }

    @Override
    public int updateContainIds(int componentId, String containIds) {
        return componentDao.updateContainIds(componentId, containIds);
    }

    @Override
    public int updateComponent(Component component) {
        return componentDao.update(ComponentConverter.convertComponentDO2PO(component));
    }

    @Override
    public List<Component> listAllComponent() {
        return ComponentConverter.convertComponentPO2DOList(componentDao.listAll());
    }

    @Override
    public List<Component> queryComponent(Component component) {
        ComponentPO componentPO = ComponentConverter.convertComponentDO2PO(component);
        return ComponentConverter.convertComponentPO2DOList(componentDao.queryComponent(componentPO));
    }

    @Override
    public List<Component> getComponentByPackageId(int packageId) {
        return ComponentConverter.convertComponentPO2DOList(componentDao.getByPackageId(packageId));
    }

    @Override
    public Component getDependComponentById(int id) {
        return ComponentConverter.convertComponentPO2DO(componentDao.findDependComponentById(id));
    }

    @Override
    public int deleteComponent(int componentId) {
        return componentDao.delete(componentId);
    }
    
    @Override
    public Component queryComponentByName(String name) {
        return ComponentConverter.convertComponentPO2DO(componentDao.queryComponentByName(name));
    }

    @Override
    public List<Component> getComponentByPackageIds(List<Integer> packageIds) {
        return ComponentConverter.convertComponentPO2DOList(componentDao.getByPackageIds(packageIds));
    }

    @Override
    public Optional<String> queryComponentById(Integer componentId) {
        return Optional.ofNullable(componentDao.queryComponentById(componentId))
            .map(ComponentPO::getName);
    }
}