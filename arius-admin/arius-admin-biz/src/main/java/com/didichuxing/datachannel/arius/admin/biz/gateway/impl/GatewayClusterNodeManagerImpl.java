package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.GATEWAY_NODE;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.page.GatewayNodePageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import lombok.NoArgsConstructor;
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
	private HandleFactory handleFactory;
	
	@Override
	public PaginationResult<GatewayClusterNodeVO> pageGetNode(GatewayNodeConditionDTO condition,
			Integer projectId) {
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
}