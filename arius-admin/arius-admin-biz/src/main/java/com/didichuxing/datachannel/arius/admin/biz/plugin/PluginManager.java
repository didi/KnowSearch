package com.didichuxing.datachannel.arius.admin.biz.plugin;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.plugin.PluginCreateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.op.manager.ComponentGroupConfigWithHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.PackageBriefVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import java.util.List;

/**
 * 插件对应能力
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
public interface PluginManager {
		
		/**
		 * 列出网关集群的所有插件
		 *
		 * @param gatewayId 网关集群 ID
		 * @return PluginVO列表
		 */
		Result<List<PluginVO>> listGatewayPluginByGatewayClusterId(Integer gatewayId);
		
		/**
		 * 列出指定ES集群的所有插件
		 *
		 * @param clusterPhyId 集群的物理 ID。
		 * @return 列表<PluginVO>
		 */
		Result<List<PluginVO>> listESPluginByClusterId(Integer clusterPhyId);
		
		
		/**
		 * 列出集群的所有内核插件
		 *
		 * @param clusterPhy 物理集群名称
		 * @return 列表<PluginVO>
		 */
		Result<List<PluginVO>> listESKernelPluginCache(String clusterPhy);
		
		/**
		 * 使用 ECM 创建插件
		 *
		 * @param pluginCreateDTO 包含插件信息的插件 DTO 对象。
		 * @return 一个 PluginCreateResult 对象。
		 */
		Result<Void> createWithECM(PluginCreateDTO pluginCreateDTO);
		
		/**
		 * 卸载指定集群ID、集群类型、组件ID的插件
		 *
		 * @param clusterId 集群 ID。
		 * @param type 要卸载的集群类型。
		 * @param componentId 要卸载的组件的 ID。
		 * @return 卸载的结果。
		 */
		Result<Void> uninstallWithECM(Integer clusterId, PluginClusterTypeEnum type, Integer componentId);
		
	
		/**
		 * 更新插件组件的版本
		 *
		 * @param clusterId 集群编号
		 * @param componentId 要更新的组件的 ID。
		 * @param type 集群类型，即要更新的集群类型。
		 * @param version 要更新的插件版本
		 * @return 返回类型是 Result<Void>，它是操作结果的包装类。
		 */
		Result<Void> updateVersionWithECM(Integer clusterId, Integer componentId, PluginClusterTypeEnum type, String version);
		
	
	
		/**
		 * 检查集群是否完成卸载插件
		 *
		 * @param clusterId 集群编号
		 * @param type 要卸载的集群类型。
		 */
		Result<Void> checkClusterCompleteUninstallPlugins(Integer clusterId, PluginClusterTypeEnum type);
	/**
	 * 通过网关集群id获取之前的版本号
	 *
	 * @param gatewayClusterId 网关集群 ID。
	 * @return 更新前的网关集群版本。
	 */
	Result<List<PackageBriefVO>> getLowerVersionByGatewayClusterId(Integer gatewayClusterId);

		    /**
     * 获取配置通过插件id
     *
     * @param pluginId 插件id
     * @return {@link Result}<{@link List}<{@link ComponentGroupConfigWithHostVO}>>
     */
    Result<List<ComponentGroupConfigWithHostVO>> getConfigsByPluginId(Long pluginId);

		/**
		 * 获取集群通过组件id
		 *
		 * @param componentId 组件id
		 * @param typeEnum          type
		 * @return {@link Result}<{@link PluginVO}>
		 */
		Result<PluginVO> getClusterByComponentId(Integer componentId, PluginClusterTypeEnum typeEnum);
}