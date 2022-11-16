package com.didichuxing.datachannel.arius.admin.core.service.cluster.plugin;

import com.didichuxing.datachannel.arius.admin.common.bean.po.plugin.PluginInfoPO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import java.util.List;

/**
 * 插件信息service
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
public interface PluginInfoService {
		
		/**
		 * 它返回给定 clusterId 的 PluginInfoPO 对象列表。
		 *
		 * @param clusterId 集群 ID。
		 * @param clusterTypeEnum 集群类型，即插件关联的集群类型。
		 * @return 列表<PluginInfoPO>
		 */
		List<PluginInfoPO> listByClusterId(Integer clusterId, PluginClusterTypeEnum clusterTypeEnum);
}