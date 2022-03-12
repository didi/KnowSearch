package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;


import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ESPluginDAO {

    List<ESPluginPO> listAll();

    List<ESPluginPO> getAllSysDefaultPlugins();

    int insert(ESPluginPO esPluginPO);

    int update(ESPluginPO param);

    int updateDesc(@Param("id") Long id, @Param("desc") String desc);

    int insertBatch(List<ESPluginPO> params);

    ESPluginPO getById(Long id);

    List<ESPluginPO> getByNameAndVersion(@Param("name") String name,
                                         @Param("version") String version);

    int delete(Long id);

    List<ESPluginPO> getByNameAndVersionAndPhysicClusterId(@Param("name") String name, @Param("version") String version,
                                                           @Param("physicClusterId") String physicClusterId);

    List<ESPluginPO> listByPhyClusterId(String phyClusterId);

    List<ESPluginPO> listByPlugIds(List<Long> plugIds);
}
