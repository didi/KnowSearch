package com.didichuxing.datachannel.arius.admin.biz.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterCreateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterVO;
import java.util.List;

/**
 * 网关集群
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
public interface GatewayClusterManager {
    
    /**
     * 列出所有网关集群简单信息
     *
     * @return GatewayClusterBriefVO 对象的列表。
     */
    Result<List<GatewayClusterBriefVO>> listBriefInfo();
    
    /**
     * 加入网关集群
     *
     * @param param     加入集群所需的参数。
     * @param projectId 项目id
     * @return 网关集群VO
     */
    Result<GatewayClusterVO> join(GatewayClusterJoinDTO param, Integer projectId);
    
    /**
     * 按页面获取网关集群列表
     *
     * @param condition 查询的条件，它是一个 GatewayConditionDTO 对象。
     * @param projectId 项目id
     * @return PaginationResult<GatewayClusterVO>
     */
    PaginationResult<GatewayClusterVO> pageGetCluster(GatewayConditionDTO condition,
        Integer projectId) ;
    
    /**
     * 通过id获取一个网关集群
     *
     * @param gatewayClusterId 要查询的网关集群ID。
     * @return 网关集群VO
     */
    Result<GatewayClusterVO> getOneById(Integer gatewayClusterId);
    
    /**
     * 按 id 删除网关集群
     *
     * @param gatewayClusterId 要删除的网关集群的ID。
     * @param projectId
     * @return 一个 CompletableFuture<Void>
     */
    Result<Void> deleteById(Integer gatewayClusterId, Integer projectId);
    
    /**
     * 编辑一个网关集群
     *
     * @param data      要编辑的数据。
     * @param projectId
     * @param operator
     * @return 一个 Result<Void> 对象。
     */
    Result<Void> editOne(GatewayClusterDTO data, Integer projectId, String operator);
    
    
    /**
     * 通过网关集群id获取上一个版本号
     *
     * @param gatewayClusterId 网关集群 ID。
     * @return 更新前的网关集群版本。
     */
    Result<List<Object>> getBeforeVersionByGatewayClusterId(Integer gatewayClusterId);
		
	
		/**
		 * 验证给定的名称是唯一的。
		 *
		 * @param name 资源的名称。
		 * @return Result<Void>
		 */
		Result<Boolean> verifyNameUniqueness(String name);
		
		/**
		 * 创建网关集群。
		 *
		 * @param gatewayCluster GatewayClusterDTO 对象
		 * @param projectId 集群的项目ID
		 * @param operate 操作类型，可以是“创建”或“更新”
		 */
		Result<Void> createWithECM(GatewayClusterCreateDTO gatewayCluster, Integer projectId, String operate);
		
		/**
		 * “给定一个组件 ID，返回组件的名称。”
		 *
		 *
		 * @param componentId 您要获取其名称的组件的组件 ID。
		 * @return 包含字符串的结果对象。
		 */
		Result<String> getNameByComponentId(Integer componentId) ;
		
		/**
		 * 扩展集群中的节点
		 *
		 * @param nodes 要扩展的节点列表。
		 * @return GatewayNodeHostDTO 对象的列表。
		 */
		Result<Void> expandNodesWithECM(List<GatewayNodeHostDTO> nodes);
		
		/**
		 * 通过删除指定节点来收缩集群
		 *
		 * @param nodes 要收缩的节点列表。
		 * @param name
		 * @return GatewayNodeHostDTO 对象的列表。
		 */
		Result<Void> shrinkNodesWithECM(List<GatewayNodeHostDTO> nodes, String name);
		
		/**
		 * “更新一个组件的版本。”
		 *
		 * 注释的第一行是功能的简要说明。第二行是参数列表，第三行是返回值的说明
		 *
		 * @param componentId 要更新的组件的组件 ID。
		 * @param version 要更新的组件的版本。
		 * @return 结果 <Void> 对象。
		 */
		Result<Void> updateVersionWithECM(Integer componentId, String version);
		
		/**
		 * 按名称获取集群
		 *
		 * @param name 要查询的集群名称。
		 * @return GatewayClusterVO 对象
		 */
		Result<GatewayClusterVO> getClusterByName(String name);
		
		/**
		 * 查看解绑资源是否完成
		 *
		 * @param data GatewayClusterVO 对象
		 * @return 返回类型是 CompletableFuture<GatewayClusterVO>
		 */
		Result<Void> checkCompleteUnbindResources(GatewayClusterVO data);
		
		/**
		 * OfflineWithECM() 用于使用 ECM 使项目离线
		 *
		 * @param id 待下线的项目id。
		 * @return 一个 Result 对象，里面有一个 Void 对象。
		 */
		Result<Void> offlineWithECM(Integer id);
}