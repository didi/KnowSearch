package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

/**
 * TODO 0.3.2对应能力不能在这里去实现，可能会在0.3.2.x慢慢下线这里的能力，目前存留，是由于旧的ECM需要使用此部分能力
 */
public interface ClusterPluginManager {

    /**
     * 上传插件，涉及ES能力插件和平台能力插件
     *
     * @param plugin    插件信息
     * @param projectId
     * @return result
     */
    Result<Long> addPlugins(PluginDTO plugin, Integer projectId) throws NotFindSubclassException;

    /**
     * 删除指定的插件
     *
     * @param id        插件id
     * @param operator  操作人员
     * @param projectId
     * @return result
     */
    Result<Long> deletePluginById(Long id, String operator, Integer projectId) throws NotFindSubclassException;

    /**
     * 编辑插件的描述信息
     *
     * @param pluginDTO 插件信息
     * @param operator  操作人员
     * @param projectId
     * @return result
     */
    Result<Void> editPluginDesc(PluginDTO pluginDTO, String operator, Integer projectId);
}