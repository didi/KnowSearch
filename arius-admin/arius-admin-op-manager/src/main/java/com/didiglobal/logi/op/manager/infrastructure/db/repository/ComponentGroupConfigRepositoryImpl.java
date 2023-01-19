package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentGroupConfigRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.ComponentGroupConfigPO;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.ComponentConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.ComponentGroupConfigDao;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
    
    @Override
    public boolean deleteByComponentIds(List<Integer> deleteComponentIds) {
        if (CollectionUtils.isEmpty(deleteComponentIds)) {
            return true;
        }
        final long count = configDao.listAll().stream()
            .map(i -> deleteComponentIds.contains(i.getComponentId()))
            .count();
        if (count == 0L) {
            return true;
        }
        return configDao.deleteByComponentIds(deleteComponentIds)==count;
    }
}