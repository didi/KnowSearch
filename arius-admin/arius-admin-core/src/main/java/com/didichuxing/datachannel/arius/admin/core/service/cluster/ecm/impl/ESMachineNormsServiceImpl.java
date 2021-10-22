package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESMachineNormsService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESMachineNormsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 容器规格列表 服务实现类
 * @author didi
 * @since 2020-08-24
 */
@Service
public class ESMachineNormsServiceImpl implements ESMachineNormsService {

    @Autowired
    private ESMachineNormsDAO machineNormsDAO;

    @Override
    public List<ESMachineNormsPO> listMachineNorms() {
        return machineNormsDAO.listMachineNorms();
    }

    @Override
    public ESMachineNormsPO getById(Long id) {
        return machineNormsDAO.getById(id);
    }
}
