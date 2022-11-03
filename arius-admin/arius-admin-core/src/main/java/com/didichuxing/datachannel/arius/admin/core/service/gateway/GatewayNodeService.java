package com.didichuxing.datachannel.arius.admin.core.service.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
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
	List<GatewayClusterNodeVO> selectByBatchClusterName(List<String> clusterName);
}