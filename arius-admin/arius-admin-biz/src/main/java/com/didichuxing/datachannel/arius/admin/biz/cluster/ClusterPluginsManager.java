package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;

public interface ClusterPluginsManager {

	Result<Long> batchAddPlugins(ESPluginDTO plugin);

	Result<Long> deletePluginById(Long id, String operator);

	Result<ESPluginPO> editPluginDesc(ESPluginDTO esPluginDTO, String operator);
}
