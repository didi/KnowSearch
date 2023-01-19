package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.GATEWAY_NODE;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.page.GatewayNodePageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import java.util.List;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 网关集群节点经理实现
 *
 * @author shizeying
 * @date 2022/11/03
 * @since 0.3.2
 */
@Component
@NoArgsConstructor
public class GatewayClusterNodeManagerImpl implements GatewayClusterNodeManager {
	@Autowired
	private GatewayNodeService gatewayNodeService;
	@Autowired
	private GatewayClusterService gatewayClusterService;
	@Autowired
	private HandleFactory handleFactory;
	
	@Override
	public PaginationResult<GatewayClusterNodeVO> pageGetNode(GatewayNodeConditionDTO condition,
			Integer projectId, Integer gatewayClusterId) {
		String clusterName = gatewayClusterService.getClusterNameById(gatewayClusterId);
		if (StringUtils.isBlank(clusterName)) {
			return PaginationResult.buildFail("无法匹配到对应的节点信息");
		}
		condition.setClusterName(clusterName);
		BaseHandle baseHandle;
		try {
			baseHandle = handleFactory.getByHandlerNamePer(GATEWAY_NODE.getPageSearchType());
		} catch (NotFindSubclassException e) {
			return PaginationResult.buildFail("没有找到对应的处理器");
		}
		if (baseHandle instanceof GatewayNodePageSearchHandle) {
			
			GatewayNodePageSearchHandle handler = (GatewayNodePageSearchHandle) baseHandle;
			return handler.doPage(condition, projectId);
		}
		return PaginationResult.buildFail("没有找到对应的处理器");
	}
		
		@Override
		public Result<List<GatewayClusterNodeVO>> getByClusterNames(List<String> clusterNames) {
				List<GatewayClusterNodePO> nodes = gatewayNodeService.listByClusterNames(clusterNames);
				return Result.buildSucc(ConvertUtil.list2List(nodes, GatewayClusterNodeVO.class));
		}
}