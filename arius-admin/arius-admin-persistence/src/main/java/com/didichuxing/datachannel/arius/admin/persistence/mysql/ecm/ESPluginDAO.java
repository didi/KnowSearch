package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;


import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ES插件包管理 Mapper 接口
 * @author didi
 * @since 2020-08-24
 */
@Repository
public interface ESPluginDAO {

    List<ESPluginPO> listAll();

    List<ESPluginPO>listPluginBelongClus(@Param("pluginIds")List<String> pluginIds,
                                         @Param("pDefault")String  pDefault);

    String getAllSysDefaultPlugins();

    int insert(ESPluginPO esPluginPO);

    int update(ESPluginPO param);

    int insertBatch(List<ESPluginPO> params);

    ESPluginPO getById(Long id);

    List<ESPluginPO> getByNameAndVersion(@Param("name") String name,
                                         @Param("version") String version);

    int delete(Long id);

    List<ESPluginPO> getByNameAndVersionAndPhysicClusterId(@Param("name") String name, @Param("version") String version,
                                                           @Param("physicClusterId") String physicClusterId);

    List<ESPluginPO> listbyPhyClusterId(String phyClusterId);

    List<ESPluginPO> listByPlugIds(List<Long> plugIds);

    int installESPlugin(Long id);

    int uninstallESPlugin(Long id);
}
