package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPluginsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClusterPluginsManagerImpl implements ClusterPluginsManager {
    @Autowired
    private ESPluginService       esPluginService;

	@Override
	public Result<Long> batchAddPlugins(ESPluginDTO plugin) {
		return esPluginService.addESPlugin(plugin);
	}

	@Override
	public Result<Long> deletePluginById(Long id, String operator) {
		return esPluginService.deletePluginById(id, operator);
	}

	@Override
	public Result<ESPluginPO> editPluginDesc(ESPluginDTO esPluginDTO, String operator) {
		return esPluginService.updateESPluginDesc(esPluginDTO, operator);
	}
}
