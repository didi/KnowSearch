package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;

import java.util.List;

public interface ESPluginService {

    /**
     * 获取所有的plugin列表
     *
     * @param
     * @return list
     */
    List<PluginPO> listESPlugin();

    /**
     * 获取默认 & 集群相关联的的plugin列表
     *
     * @param
     * @return list
     */
    List<PluginPO> listClusterAndDefaultESPlugin(String phyClusterId);

    /**
     * 创建一个plugin
     *
     * @param pluginDTO ES插件
     * @return result
     */
    Result<Long> addESPlugin(PluginDTO pluginDTO);

    /**
     * 修改 plugin （只允许修改描述信息）
     *
     * @param pluginDTO ES插件
     * @return ESPluginPO
     */
    Result<Void> updateESPluginDesc(PluginDTO pluginDTO, String operator);

    /**
     * 获取指定id的plugin
     *
     * @param id 插件ID
     * @return ESPluginPO
     */
    PluginPO getESPluginById(Long id);

    /**
     * 根据id删除一个plugin
     *
     * @param id 插件ID
     * @return result
     */
    Result<Long> deletePluginById(Long id, String operator);

    /**
     * 获取全部系统默认插件
     *
     * @return result
     */
    String getAllSysDefaultPluginIds();

    /**
     * 根据集群名称获取关联插件
     *
     * @return result
     */
    List<Plugin> getPluginsByClusterName(String clusterName);

    /**
     * 上传多个 plugin
     * @param  param ES插件的集合
     * @return result
     */
    Result<String> addESPlugins(List<PluginDTO> param);

}
