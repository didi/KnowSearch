package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;

public interface ClusterPluginsManager {

	/**
	 * 上传插件，涉及ES能力插件和平台能力插件
	 * @param plugin 插件信息
	 * @return result
	 */
	Result<Long> addPlugins(PluginDTO plugin);

	/**
	 * 删除指定的插件
	 * @param id 插件id
	 * @param operator 操作人员
	 * @return result
	 */
	Result<Long> deletePluginById(Long id, String operator);

	/**
	 * 编辑插件的描述信息
	 * @param pluginDTO 插件信息
	 * @param operator 操作人员
	 * @return result
	 */
	Result<Void> editPluginDesc(PluginDTO pluginDTO, String operator);
}
