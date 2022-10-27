package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentGroupConfigRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.ComponentGroupConfigPO;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.ComponentConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.ComponentGroupConfigDao;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-19 3:18 下午
 */
@Repository
public class ComponentGroupConfigRepositoryImpl implements ComponentGroupConfigRepository {

    @Autowired
    private ComponentGroupConfigDao configDao;


    @Override
    public int saveGroupConfig(ComponentGroupConfig groupConfig) {
        ComponentGroupConfigPO po = ComponentConverter.convertComponentConfigDO2PO(groupConfig);
        configDao.insert(po);
        return po.getId();
    }

    @Override
    public int updateGroupConfig(ComponentGroupConfig groupConfig) {
        return configDao.update(ComponentConverter.convertComponentConfigDO2PO(groupConfig));
    }

    @Override
    public ComponentGroupConfig getConfigById(int groupId) {
        return ComponentConverter.convertComponentConfigPO2DO(configDao.getById(groupId));
    }

    @Override
    public List<ComponentGroupConfig> getConfigByComponentId(int componentId) {
        return ComponentConverter.convertComponentConfigPO2DOList(configDao.getByComponentId(componentId));
    }

    @Override
    public List<ComponentGroupConfig> listGroupConfig() {
        return ComponentConverter.convertComponentConfigPO2DOList(configDao.listAll());
    }
}
