package com.didichuxing.datachannel.arius.admin.core.service.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterVO;
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
	List<GatewayClusterBriefVO> listAll();
	
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
	List<GatewayClusterVO> pageByCondition(GatewayConditionDTO condition);
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
	GatewayClusterVO getOneById(Integer gatewayClusterId);
	
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
}