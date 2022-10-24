package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;

/**
 * 容器规格列表 Mapper 接口
 * @author didi
 * @since 2020-08-24
 */
@Repository
public interface ESMachineNormsDAO {

    List<ESMachineNormsPO> listMachineNorms();

    ESMachineNormsPO getById(Long id);

    int insert(ESMachineNormsPO param);
}
