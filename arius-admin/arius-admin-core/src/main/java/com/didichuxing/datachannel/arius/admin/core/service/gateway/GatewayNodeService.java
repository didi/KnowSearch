package com.didichuxing.datachannel.arius.admin.core.service.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import java.util.List;

/**
 * 网关节点service
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
public interface  GatewayNodeService {
	
	
	/**
	 * 插入网关节点主机列表
	 *
	 * @param gatewayNodeHosts 要插入的 gatewayNodeHosts 列表。
	 * @return boolean
	 */
	boolean insertBatch(List<GatewayNodeHostDTO> gatewayNodeHosts);
	
	/**
	 * 通过批量集群名称获取节点列表
	 *
	 * @param clusterName 集群名称
	 * @return 列表<GatewayClusterNodeVO>
	 */
	List<GatewayClusterNodePO> selectByBatchClusterName(List<String> clusterName);
	
	/**
	 * 列出集群中的所有节点
	 *
	 * @param clusterName 集群的名称。
	 * @return GatewayClusterNodeVO 对象列表
	 */
	List<GatewayClusterNodePO> listByClusterName(String clusterName);
	
	/**
	 * 删除具有给定名称的所有集群
	 *
	 * @param clusterName 要删除的集群的名称。
	 * @return 一个布尔值。
	 */
	boolean deleteByClusterName(String clusterName);
	
	/**
	 * > 按条件查询网关集群节点列表
	 *
	 * @param condition 条件对象与查询方法中的条件对象相同。
	 * @return GatewayClusterNodePO 对象的列表。
	 */
	List<GatewayClusterNodePO> pageByCondition(GatewayNodeConditionDTO condition);
	
	/**
	 * > 按指定条件统计GatewayNode记录数
	 *
	 * @param condition 查询的条件。
	 * @return 沿着
	 */
	Long countByCondition(GatewayNodeConditionDTO condition);
	
	/**
	 * 它返回具有指定主机的 GatewayClusterNodePO 对象列表。
	 *
	 * @param hosts 网关集群节点的主机名。
	 * @return 列表<GatewayClusterNodePO>
	 */
	List<GatewayClusterNodePO> listByHosts(List<String> hosts);
	
	/**
	 * 删除一批网关节点
	 *
	 * @param ids 要删除的ID。
	 * @return 一个布尔值。
	 */
	Boolean deleteBatch(List<Integer> ids);
		
		/**
		 * 按群集名称列出
		 *
		 * @param clusterNames 群集名称
		 * @return {@link List}<{@link GatewayClusterNodePO}>
		 */
		List<GatewayClusterNodePO> listByClusterNames(List<String> clusterNames);
}