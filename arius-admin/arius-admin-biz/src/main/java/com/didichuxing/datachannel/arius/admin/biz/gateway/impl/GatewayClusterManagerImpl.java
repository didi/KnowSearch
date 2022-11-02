package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.GATEWAY_CLUSTER;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterManager;
import com.didichuxing.datachannel.arius.admin.biz.page.GatewayClusterPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 网关集群
 *
 * @author shizeying
 * @date 2022/10/31
 * @since 0.3.2
 */
@Component
public class GatewayClusterManagerImpl implements GatewayClusterManager {
	
	@Autowired
	private GatewayClusterService gatewayClusterService;
	@Autowired
	private GatewayNodeService gatewayNodeService;
	@Autowired
	private HandleFactory handleFactory;
	
	@Override
	public Result<List<GatewayClusterBriefVO>> listBriefInfo() {
		return Result.buildSucc(gatewayClusterService.listAll());
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<GatewayClusterVO> join(GatewayClusterJoinDTO param, Integer projectId) {
		Result<Void> result = checkGatewayJoinParam(param, projectId);
		if (result.failed()) {
			return Result.buildFrom(result);
		}
		//1. 设置 clusterName 到节点对象中
		final List<GatewayNodeHostDTO> gatewayNodeHosts = param.getGatewayNodeHosts();
		gatewayNodeHosts.forEach(node -> node.setClusterName(param.getClusterName()));
		//2. 转换 gatewayCluster 对象
		final GatewayClusterDTO gatewayDTO = ConvertUtil.obj2Obj(param, GatewayClusterDTO.class);
		//3. 初始化一些信息
		initGatewayCluster(gatewayDTO);
		final boolean addGatewayCluster = gatewayClusterService.insertOne(gatewayDTO);
		//4. 写入节点信息
		final boolean addGatewayNode = gatewayNodeService.insertBatch(param.getGatewayNodeHosts());
		if (addGatewayCluster && addGatewayNode) {
			final GatewayClusterVO gatewayClusterVO = ConvertUtil.obj2Obj(gatewayDTO,
					GatewayClusterVO.class);
			gatewayClusterVO.setNodes(
					ConvertUtil.list2List(gatewayNodeHosts, GatewayClusterNodeVO.class));
			return Result.buildSucc(gatewayClusterVO);
		}
		
		return Result.buildFail("gateway 集群加入失败, 请联系管理员");
	}
	
	
	@Override
	public PaginationResult<GatewayClusterVO> pageGetCluster(GatewayConditionDTO condition,
			Integer projectId) {
		BaseHandle baseHandle = null;
		try {
			baseHandle = handleFactory.getByHandlerNamePer(GATEWAY_CLUSTER.getPageSearchType());
		} catch (NotFindSubclassException e) {
			return PaginationResult.buildFail("没有找到对应的处理器");
		}
		if (baseHandle instanceof GatewayClusterPageSearchHandle) {
		
			GatewayClusterPageSearchHandle handler = (GatewayClusterPageSearchHandle) baseHandle;
			return handler.doPage(condition, projectId);
		}
		return PaginationResult.buildFail("没有找到对应的处理器");
	}
	
	/**
	 * > 检查网关集群加入的参数
	 *
	 * @param param     GatewayClusterJoinDTO
	 * @param projectId 当前用户的项目 ID
	 * @return Result<Void>
	 */
	private Result<Void> checkGatewayJoinParam(GatewayClusterJoinDTO param, Integer projectId) {
		if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
			return Result.buildFail("非超级项目无权限");
		}
		if (Objects.isNull(param)) {
			return Result.buildFail("传入实体不可为空");
		}
		if (StringUtils.isEmpty(param.getClusterName())) {
			return Result.buildFail("集群名称不可为空");
		}
		if (!gatewayClusterService.checkNameCluster(param.getClusterName())) {
			return Result.buildFail("集群名称已存在，不可创建同名集群");
		}
		if (CollectionUtils.isEmpty(param.getGatewayNodeHosts())) {
			return Result.buildFail("节点信息不可为空");
		}
		if (param.getGatewayNodeHosts().stream().anyMatch(
				node -> StringUtils.isEmpty(node.getHostname()) || StringUtils.isEmpty(node.getPort()))) {
			return Result.buildFail("传入的节点信息地址和端口号不可为空");
		}
		
		return Result.buildSucc();
	}
	
	/**
	 * > 该函数初始化网关集群
	 *
	 * @param gatewayClusterDTO 传递给 initGatewayCluster 方法的 gatewayDTO 对象。
	 */
	private void initGatewayCluster(GatewayClusterDTO gatewayClusterDTO) {
		gatewayClusterDTO.setEcmAccess(Boolean.FALSE);
		gatewayClusterDTO.setHealth(ClusterHealthEnum.UNKNOWN.getCode());
		gatewayClusterDTO.setComponentId(-1);
	}
}