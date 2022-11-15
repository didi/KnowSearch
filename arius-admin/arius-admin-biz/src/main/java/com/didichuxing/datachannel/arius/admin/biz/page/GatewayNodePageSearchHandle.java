package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 网关节点页面搜索处理
 *
 * @author shizeying
 * @date 2022/11/03
 * @since 0.3.2
 */
@Component
public class GatewayNodePageSearchHandle extends
		AbstractPageSearchHandle<GatewayNodeConditionDTO, GatewayClusterNodeVO>{
		
	private static final ILog LOGGER = LogFactory.getLog(GatewayClusterPageSearchHandle.class);
	private static final CharSequence[] CHAR_SEQUENCES = {"*", "?"};
	@Autowired
	private GatewayNodeService gatewayNodeService;
	
	@Override
	protected Result<Boolean> checkCondition(GatewayNodeConditionDTO condition, Integer projectId) {
		String nodeName = condition.getNodeName();
		if (StringUtils.containsAny(nodeName, CHAR_SEQUENCES)) {
			return Result.buildParamIllegal("物理集群名称不允许带类似 *, ? 等通配符查询");
		}
		return Result.buildSucc();
	}
	
	@Override
	protected void initCondition(GatewayNodeConditionDTO condition, Integer projectId) {
	
	}
	
	@Override
	protected PaginationResult<GatewayClusterNodeVO> buildPageData(GatewayNodeConditionDTO condition,
			Integer projectId) {
		List<GatewayClusterNodeVO> pagingGatewayNodeList = ConvertUtil.list2List(
				gatewayNodeService.pageByCondition(
						condition), GatewayClusterNodeVO.class);
		final Long totalHit = gatewayNodeService.countByCondition(condition);
		return PaginationResult.buildSucc(pagingGatewayNodeList, totalHit, condition.getPage(),
				condition.getSize());
	}
}