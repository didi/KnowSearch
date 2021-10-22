package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPluginsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESPluginVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;

/**
 * Created by linyunan on 2021-07-08
 */
@Component
public class ClusterPluginsManagerImpl implements ClusterPluginsManager {
    @Autowired
    private ESPluginService       esPluginService;

    @Autowired
    private ClusterContextManager clusterContextManager;

    @Autowired
    private ESClusterPhyService   esClusterPhyService;

    @Override
    public List<ESPluginVO> getClusterLogicPlugins(Long clusterId) {
		ESClusterLogicContext esClusterLogicContext = clusterContextManager.getESClusterLogicContext(clusterId);
		List<String> associatedClusterPhyNames = esClusterLogicContext.getAssociatedClusterPhyNames();

		if (CollectionUtils.isNotEmpty(associatedClusterPhyNames)) {
			List<Integer> clusterPhyIds = associatedClusterPhyNames
					.stream()
					.map(esClusterPhyService::getClusterByName)
					.map(ESClusterPhy::getId)
					.collect(Collectors.toList());
		}

		return ConvertUtil.list2List(null, ESPluginVO.class);
    }
}
