package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;


import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ESPluginDAO {

    List<PluginPO> listAll();

    List<PluginPO> getAllSysDefaultPlugins();

    int insert(PluginPO pluginPO);

    int update(PluginPO param);

    int updateDesc(@Param("id") Long id, @Param("desc") String desc);

    int insertBatch(List<PluginPO> params);

    PluginPO getById(Long id);

    List<PluginPO> getByNameAndVersion(@Param("name") String name,
                                       @Param("version") String version);

    int delete(Long id);

    List<PluginPO> getByNameAndVersionAndPhysicClusterId(@Param("name") String name, @Param("version") String version,
                                                         @Param("physicClusterId") String physicClusterId);

    List<PluginPO> listByPhyClusterId(String phyClusterId);

    List<PluginPO> listByPlugIds(List<Long> plugIds);
}
