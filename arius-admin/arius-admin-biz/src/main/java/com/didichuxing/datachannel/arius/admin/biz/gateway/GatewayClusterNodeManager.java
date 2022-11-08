package com.didichuxing.datachannel.arius.admin.biz.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;

/**
 * 网关集群节点管理器
 *
 * @author shizeying
 * @date 2022/11/03
 * @since 0.3.2
 */
public interface GatewayClusterNodeManager {
	
	/**
	 * 按条件获取网关集群节点列表
	 *
	 * @param condition 查询的条件，它是一个 GatewayNodeConditionDTO 对象。
	 * @param projectId
	 * @param gatewayClusterId
	 * @return 网关集群节点VO
	 */
	PaginationResult<GatewayClusterNodeVO> pageGetNode(GatewayNodeConditionDTO condition,
			Integer projectId, Integer gatewayClusterId);
}