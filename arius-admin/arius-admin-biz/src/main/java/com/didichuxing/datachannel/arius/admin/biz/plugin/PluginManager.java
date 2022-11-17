package com.didichuxing.datachannel.arius.admin.biz.plugin;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
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
}