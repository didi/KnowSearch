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
		
		/**
		 * 创建一个插件
		 *
		 * @param pluginInfoPO PluginInfoPO 对象
		 * @return 一个布尔值。
		 */
		boolean create(PluginInfoPO pluginInfoPO);
		
		/**
		 * 从集群中删除组件
		 *
		 * @param clusterId 集群的 ID。
		 * @param clusterType ES GATEWAY
		 * @param componentId 要删除的组件的 ID。
		 * @return 一个布尔值。
		 */
		boolean delete(Integer clusterId, Integer clusterType, Integer componentId);
		
		/**
		 * 更新插件信息
		 *
		 * @param pluginInfoPO PluginInfoPO 对象
		 * @return 一个布尔值。
		 */
		boolean update(PluginInfoPO pluginInfoPO);
		
		/**
		 *
		 *
		 * @param clusterId 要查询的集群的集群ID。
		 * @param componentId 插件的组件 ID。
		 * @param clusterType 1-k8s，2 个月
		 */
		PluginInfoPO selectByClusterIdAndComponentIdAndClusterType(Integer clusterId, Integer componentId, Integer clusterType);
		
		/**
		 * 获取插件通过id
		 *
		 * @param pluginId 插件id
		 * @return {@link PluginInfoPO}
		 */
		PluginInfoPO getPluginById(Long pluginId);
		
		/**
		 * 查询
		 *
		 * @param pluginInfoPO 插件信息po
		 * @return {@link PluginInfoPO}
		 */
		PluginInfoPO selectByCondition(PluginInfoPO pluginInfoPO);
		
		/**
		 * 删除由ids
		 *
		 * @param pluginIds 插件id
		 * @return {@link Boolean}
		 */
		Boolean deleteByIds(List<Long> pluginIds);
}