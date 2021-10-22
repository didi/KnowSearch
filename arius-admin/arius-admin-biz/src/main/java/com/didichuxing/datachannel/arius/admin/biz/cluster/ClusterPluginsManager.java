package com.didichuxing.datachannel.arius.admin.biz.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESPluginVO;

/**
 * Created by linyunan on 2021-07-08
 */
public interface ClusterPluginsManager {

	List<ESPluginVO> getClusterLogicPlugins(Long clusterId);
}
