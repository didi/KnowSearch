package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 网关集群页面搜索处理
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
@Component
public class GatewayClusterPageSearchHandle extends
		AbstractPageSearchHandle<GatewayConditionDTO, GatewayClusterVO> {
	
	private static final ILog LOGGER = LogFactory.getLog(GatewayClusterPageSearchHandle.class);
	private static final CharSequence[] CHAR_SEQUENCES = {"*", "?"};
	@Autowired
	private GatewayClusterService gatewayClusterService;
	@Autowired
	private GatewayNodeService gatewayNodeService;
	
	
	@Override
	protected Result<Boolean> checkCondition(GatewayConditionDTO condition, Integer projectId) {
		Integer status = condition.getHealth();
		if (null != status && !ClusterHealthEnum.isExitByCode(status)) {
			return Result.buildParamIllegal("集群状态类型不存在");
		}
		String clusterName = condition.getClusterName();
		if (StringUtils.containsAny(clusterName, CHAR_SEQUENCES)) {
			return Result.buildParamIllegal("物理集群名称不允许带类似 *, ? 等通配符查询");
		}
		return Result.buildSucc();
	}
	
	@Override
	protected void initCondition(GatewayConditionDTO condition, Integer projectId) {
	
	}
	
	@Override
	protected PaginationResult<GatewayClusterVO> buildPageData(GatewayConditionDTO condition,
			Integer projectId) {
		List<GatewayClusterVO> pagingGatewayClusterList = gatewayClusterService.listByCondition(
				condition);
		final Long totalHit = gatewayClusterService.countByCondition(condition);
		final List<String> clusterNames =
				pagingGatewayClusterList.stream().map(GatewayClusterVO::getClusterName)
						.collect(Collectors.toList());
		List<GatewayClusterNodeVO> nodes =
				gatewayNodeService.selectByBatchClusterName(clusterNames);
		final Map<String, List<GatewayClusterNodeVO>> clusterName2GatewayNodesMap = ConvertUtil.list2MapOfList(
				nodes,
				GatewayClusterNodeVO::getClusterName, i -> i);
		pagingGatewayClusterList.forEach(gatewayClusterVO -> gatewayClusterVO.setNodes(
				clusterName2GatewayNodesMap.get(gatewayClusterVO.getClusterName())));
		return PaginationResult.buildSucc(pagingGatewayClusterList, totalHit, condition.getPage(),
				condition.getSize());
	}
}