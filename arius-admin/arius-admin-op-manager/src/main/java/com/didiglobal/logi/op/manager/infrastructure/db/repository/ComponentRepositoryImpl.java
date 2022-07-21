package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.ComponentConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.ComponentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
        return componentDao.insert(ComponentConverter.convertComponentDO2PO(component));
    }

    @Override
    public Component getComponentById(int componentId) {
        return ComponentConverter.convertComponentPO2DO(componentDao.findById(componentId));
    }

    @Override
    public void updateContainIds(int componentId, String containIds) {
        componentDao.updateContainIds(componentId, containIds);
    }
}
