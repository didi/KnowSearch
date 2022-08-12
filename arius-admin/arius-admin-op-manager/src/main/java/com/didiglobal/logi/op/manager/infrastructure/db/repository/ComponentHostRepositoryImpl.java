package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentHostRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.ComponentConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.ComponentHostDao;
import com.didiglobal.logi.op.manager.interfaces.dto.ComponentHostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-19 3:34 下午
 */
@Repository
public class ComponentHostRepositoryImpl implements ComponentHostRepository {

    @Autowired
    private ComponentHostDao componentHostDao;

    @Override
    public void saveComponentHost(ComponentHost componentHost) {
        componentHostDao.insert(ComponentConverter.convertComponentHostDO2PO(componentHost));
    }

    @Override
    public void updateComponentHostStatus(int componentId, String host, int groupId, int status) {
        componentHostDao.updateStatus(componentId, host, groupId, status);
    }

    @Override
    public List<ComponentHost> listComponentHost() {
        return ComponentConverter.convertComponentHostPO2DOList(componentHostDao.listAll());
    }
}
