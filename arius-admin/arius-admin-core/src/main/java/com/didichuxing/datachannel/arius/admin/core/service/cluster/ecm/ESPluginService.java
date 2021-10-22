package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ESPlugin;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;

import java.util.List;

/**
 * ES插件包管理 服务类
 * @author didi
 * @since 2020-08-24
 */
public interface ESPluginService {

    /**
     * 获取所有的plugin列表
     *
     * @param
     * @return list
     */
    List<ESPluginPO> listESPlugin();

    /**
     * 获取默认 & 集群相关联的的plugin列表
     *
     * @param
     * @return list
     */
    List<ESPluginPO> listClusterAndDefaultESPlugin(String phyClusterId);

    /**
     * 创建一个plugin
     *
     * @param esPluginDTO ES插件
     * @return result
     */
    Result<ESPluginPO> addESPlugin(ESPluginDTO esPluginDTO);

    /**
     * 修改 plugin
     *
     * @param esPluginDTO ES插件
     * @return ESPluginPO
     */
    Result<ESPluginPO> updateESPluginDesc(ESPluginDTO esPluginDTO, String operator);

    /**
     * 获取指定id的plugin
     *
     * @param id 插件ID
     * @return ESPluginPO
     */
    ESPluginPO getESPluginById(Long id);

    /**
     * 根据id删除一个plugin
     *
     * @param id 插件ID
     * @return result
     */
    Result<Long> deletePluginById(Long id, String operator);

    /**
     * 根据pluginIds 查询plugin列表
     *
     * @param pluginIds 插件IDs
     * @return result
     */
    List<ESPluginPO> listPluginBelongClus(List<String> pluginIds, String name);

    /**
     * 获取全部系统默认插件
     *
     * @return result
     */
    String getAllSysDefaultPlugins();

    /**
     * 根据集群名称获取关联插件
     *
     * @return result
     */
    List<ESPlugin> getPluginsByClusterName(String clusterName);

    /**
     * 上传多个 plugin
     * @param  param ES插件的集合
     * @return result
     */
    Result addESPlugins(List<ESPluginDTO> param);

    /**
     * 将es插件置为已安装
     *
     * @param id
     * @return
     */
    Result installESPlugin(Long id);

    /**
     * 将es插件置为未安装
     *
     * @param id
     * @return
     */
    Result uninstallESPlugin(Long id);

}
