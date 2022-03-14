package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPluginsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClusterPluginsManagerImpl implements ClusterPluginsManager {
    @Autowired
    private ESPluginService       esPluginService;

	@Override
	public Result<Long> addPlugins(PluginDTO plugin) {
		return esPluginService.addESPlugin(plugin);
	}

	@Override
	public Result<Long> deletePluginById(Long id, String operator) {
		return esPluginService.deletePluginById(id, operator);
	}

	@Override
	public Result<Void> editPluginDesc(PluginDTO pluginDTO, String operator) {
		return esPluginService.updateESPluginDesc(pluginDTO, operator);
	}
}
