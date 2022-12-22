package com.didichuxing.datachannel.arius.admin.biz.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.op.manager.ComponentGroupConfigWithHostVO;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.interfaces.vo.ComponentGroupConfigVO;
import java.util.List;

/**
 * 网关集群配置
 *
 * @author shizeying
 * @date 2022/11/04
 * @since 0.3.2
 */
public interface GatewayClusterConfigManager {
	
	/**
	 * 获取网关集群的配置列表
	 *
	 * @param condition 查询条件，包括配置名称、配置键、配置类型、配置状态。
	 * @param projectId 项目编号
	 * @param gatewayClusterId 网关集群 ID
	 * @return 包含 GatewayConfigVO 对象列表的 PaginationResult 对象。
	 */
	PaginationResult<GatewayConfigVO> pageGetConfig(ConfigConditionDTO condition, Integer projectId, Integer gatewayClusterId);
	
	/**
	 * 获取网关集群的配置
	 *
	 * @param gatewayClusterId 网关集群 ID。
	 * @param configId         要查询的配置的配置ID。
	 * @return GeneralGroupConfigHostVO
	 */
	Result<ComponentGroupConfigVO> getConfigByGatewayId(Integer gatewayClusterId, Integer configId);
		
		/**
		 * 通过网关集群 ID 获取组件组的配置
		 *
		 * @param gatewayClusterId 网关集群 ID。
		 * @return ComponentGroupConfigVO 列表
		 */
		Result<List<ComponentGroupConfigWithHostVO>> getConfigsByGatewayId(Integer gatewayClusterId);
		
		/**
		 * 获取回滚配置通过集群体育id
		 *
		 * @param gatewayClusterId 集群id
		 * @param configId  配置id
		 * @return {@link Result}<{@link List}<{@link ComponentGroupConfig}>>
		 */
		Result<List<ComponentGroupConfig>> getRollbackConfigsByClusterPhyId(Integer gatewayClusterId, Integer configId);
}