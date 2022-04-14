package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;

/**
 * 容器规格列表 服务类
 * @author didi
 * @since 2020-08-24
 */
public interface ESMachineNormsService {

    /**
     * 获取所有的机器规格列表
     * @param
     * @return List<ESMachineNormsPO>
     */
    List<ESMachineNormsPO> listMachineNorms();

    ESMachineNormsPO getById(Long id);

}
