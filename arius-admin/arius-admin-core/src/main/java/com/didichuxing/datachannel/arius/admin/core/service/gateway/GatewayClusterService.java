package com.didichuxing.datachannel.arius.admin.core.service.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import java.util.List;

/**
 * 网关集群service
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
public interface GatewayClusterService {
	
	/**
	 * 它返回所有 GatewayClusterBriefVO 对象的列表。
	 *
	 * @return GatewayClusterBriefVO 对象的列表。
	 */
	List<GatewayClusterPO> listAll();
	
	/**
	 * > 该函数检查集群名称是否有效
	 *
	 * @param clusterName 要创建的集群的名称。
	 * @return 布尔值
	 */
	boolean checkNameCluster(String clusterName);
	
	/**
	 * 新增
	 *
	 * @param gatewayDTO GatewayDTO 对象
	 */
	boolean insertOne(GatewayClusterDTO gatewayDTO);
	
	/**
	 * > 列出所有符合给定条件的网关集群
	 *
	 * @param condition 网关条件DTO
	 * @return List<GatewayClusterVO>
	 */
	List<GatewayClusterPO> pageByCondition(GatewayConditionDTO condition);
	/**
	 * > 计算符合给定条件的记录数
	 *
	 * @param condition 用于过滤数据的条件。
	 * @return 符合条件的行数。
	 */
	Long countByCondition(GatewayConditionDTO condition);
	
	/**
	 * 通过 id 获取一个 GatewayClusterVO
	 *
	 * @param gatewayClusterId 网关集群的 ID。
	 * @return 一个 GatewayClusterVO 对象。
	 */
	GatewayClusterPO getOneById(Integer gatewayClusterId);
	
	/**
	 * 按 id 删除网关集群
	 *
	 * @param gatewayClusterId 要删除的网关集群的id。
	 * @return 一个布尔值。
	 */
	boolean deleteOneById(Integer gatewayClusterId);
	
	/**
	 * 它编辑一个网关集群。
	 *
	 * @param data 要编辑的数据。
	 * @return 布尔值
	 */
	boolean editOne(GatewayClusterDTO data);
	
	/**
	 * 它返回具有给定 id 的集群的名称。
	 *
	 * @param gatewayClusterId 网关集群的 ID。
	 * @return 包含集群名称的字符串。
	 */
	String getClusterNameById(Integer gatewayClusterId);
	
	
	/**
	 * 它返回具有给定 id 的网关集群的组件 id。
	 *
	 * @param gatewayClusterId 网关集群的 ID。
	 * @return 网关集群的组件 ID。
	 */
	Integer getComponentIdById(Integer gatewayClusterId);
		
		
		/**
		 * 按名称获取一个 GatewayClusterPO
		 *
		 * @param name 网关集群的名称。
		 * @return GatewayClusterPO 对象
		 */
		GatewayClusterPO getOneByName(String name);

	/**
	 * 通过gateway集群名称查询gateway
	 * @param gatewayClusterName
	 * @return
	 */
	List<GatewayClusterPO> listByNames(List<String> gatewayClusterName);
		/**
		 * 通过 componentId 获取一个 GatewayClusterPO
		 *
		 * @param componentId 网关集群的组件 ID。
		 * @return GatewayClusterPO 对象
		 */
		GatewayClusterPO getOneByComponentId(Integer componentId);

		/**
		 * 使用给定的 ID 更新组件的版本。
		 *
		 * @param componentId 您要更新的组件的 ID。
		 * @param version 要更新的组件的版本。
		 * @return 一个布尔值。
		 */
		Boolean updateVersion(Integer componentId, String version);
}