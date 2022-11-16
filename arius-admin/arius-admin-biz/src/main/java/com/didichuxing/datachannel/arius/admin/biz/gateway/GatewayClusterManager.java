package com.didichuxing.datachannel.arius.admin.biz.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
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
    Result<String> getBeforeVersionByGatewayClusterId(Integer gatewayClusterId);
		
	
		/**
		 * 验证给定的名称是唯一的。
		 *
		 * @param name 资源的名称。
		 * @return Result<Void>
		 */
		Result<Boolean> verifyNameUniqueness(String name);
}