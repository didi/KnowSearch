package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.component.repository.ComponentHostRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.ComponentHostPO;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.ComponentConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.ComponentHostDao;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
    public int updateComponentHostStatus(int componentId, String host, String groupName, int status) {
        return componentHostDao.updateStatus(componentId, host, groupName, status);
    }

    @Override
    public List<ComponentHost> listComponentHost() {
        return ComponentConverter.convertComponentHostPO2DOList(componentHostDao.listAll());
    }

    @Override
    public int unInstallComponentHost(int componentId, String host, String groupName, int isDeleted) {
        return componentHostDao.updateDeleteStatus(componentId, host, groupName, isDeleted);
    }

    @Override
    public List<ComponentHost> listHostByComponentId(int componentId) {
        return ComponentConverter.convertComponentHostPO2DOList(componentHostDao.findByComponentId(componentId));
    }
    
    @Override
    public ComponentHost selectByComponentIdAndHostAndGroupName(int componentId, String host,
        String groupName, int isDeleted) {
        return ComponentConverter.convertComponentHostPO2DO(
            componentHostDao.selectByComponentIdAndHostAndGroupName(componentId, host, groupName,
                isDeleted));
    }
    
    @Override
    public void updateComponentHostByComponentIdAndHostAndGroupName(ComponentHost componentHost) {
        componentHostDao.updateComponentHostByComponentIdAndHostAndGroupName(ComponentConverter.convertComponentHostDO2PO(componentHost));
    }
    
    @Override
    public Boolean deleteByComponentIds(List<Integer> deleteComponentIds) {
        if (CollectionUtils.isEmpty(deleteComponentIds)){
            return true;
        }
        final List<ComponentHostPO> componentHostPOS = componentHostDao.listAll();
        final long countDelete = componentHostPOS.stream()
            .filter(i -> deleteComponentIds.contains(i.getComponentId()))
            .count();
        if (countDelete==0){
            return true;
        }
        return countDelete==componentHostDao.deleteByComponentId(deleteComponentIds);
    }
}