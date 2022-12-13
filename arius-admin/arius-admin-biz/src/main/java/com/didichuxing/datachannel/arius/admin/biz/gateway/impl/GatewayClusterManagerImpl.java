package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.GATEWAY_CLUSTER;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterManager;
import com.didichuxing.datachannel.arius.admin.biz.page.GatewayClusterPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterCreateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPhyPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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
		private GatewayNodeService    gatewayNodeService;
		@Autowired
		private HandleFactory         handleFactory;
		
		@Autowired
		private ComponentService     componentService;
		@Autowired
		private OperateRecordService operateRecordService;
		@Autowired
		private ClusterPhyService    clusterPhyService;
	
	@Override
	public Result<List<GatewayClusterBriefVO>> listBriefInfo() {
		return Result.buildSucc(ConvertUtil.list2List(gatewayClusterService.listAll(), GatewayClusterBriefVO.class));
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
		initJoinGatewayCluster(gatewayDTO);
		final boolean addGatewayCluster = gatewayClusterService.insertOne(gatewayDTO);
		//4. 写入节点信息
		final boolean addGatewayNode = gatewayNodeService.insertBatch(param.getGatewayNodeHosts());
		if (addGatewayCluster && addGatewayNode) {
			final GatewayClusterVO gatewayClusterVO = ConvertUtil.obj2Obj(gatewayDTO,
					GatewayClusterVO.class);
			gatewayClusterVO.setNodes(
					ConvertUtil.list2List(gatewayNodeHosts, GatewayClusterNodeVO.class));
			String content = String.format("gateway 集群 %s 接入成功", gatewayClusterVO.getClusterName());
			//operateRecordService.saveOperateRecordWithManualTrigger(content,operator,projectId,
			//		data.getId(), OperateTypeEnum.TEMPLATE_SERVICE);
			return Result.buildSucc(gatewayClusterVO);
		}
		
		return Result.buildFail("gateway 集群加入失败, 请联系管理员");
	}
		
		@Override
		@Transactional(rollbackFor = Exception.class)
		public Result<Void> createWithECM(GatewayClusterCreateDTO gatewayCluster, Integer projectId,
		                                  String operate) {
				Result<Void> result = checkGatewayCreateParam(gatewayCluster);
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				//2. 转换 gatewayCluster 对象
				final GatewayClusterDTO gatewayDTO = ConvertUtil.obj2Obj(gatewayCluster,
						GatewayClusterDTO.class);
				final boolean addGatewayCluster = gatewayClusterService.insertOne(gatewayDTO);
				//3. 写入节点信息
				final boolean addGatewayNode = gatewayNodeService.insertBatch(gatewayCluster.getNodes());
				//无需写入到操作记录，工单操作中生成操作记录即可
				return Result.build(addGatewayCluster && addGatewayNode);
		}
		
		@Override
		public Result<String> getNameByComponentId(Integer componentId) {
				GatewayClusterPO gatewayClusterPO = gatewayClusterService.getOneByComponentId(componentId);
				return Optional.ofNullable(gatewayClusterPO).map(GatewayClusterPO::getClusterName)
						.map(Result::buildSucc).orElse(
								Result.buildFail(String.format("组建 ID【%s】绑定未匹配到对应的集群", componentId)));
		}
		
		@Override
		public Result<Void> expandNodesWithECM(List<GatewayNodeHostDTO> nodes) {
				// 无需写入到操作记录，工单操作中生成操作记录即可
				// 校验节点是否已经写入
				final List<GatewayClusterNodePO> gatewayClusterNodePOS = gatewayNodeService.listByClusterName(
						nodes.get(0).getClusterName());
				final Map<String, GatewayNodeHostDTO> expandKey2DTOMap = ConvertUtil.list2Map(
						nodes, i -> String.format("%s-%s-%s", i.getClusterName(),
								i.getHostName(), i.getPort()));
				
				final List<String> key = gatewayClusterNodePOS.stream()
						.map(i -> String.format("%s-%s-%s", i.getClusterName(),
								i.getHostName(), i.getPort()))
						.collect(Collectors.toList());
				final List<String> keyExpand = nodes.stream()
						.map(i -> String.format("%s-%s-%s", i.getClusterName(),
								i.getHostName(), i.getPort()))
						.collect(Collectors.toList());
				
				if (new HashSet<>(key).containsAll(keyExpand)) {
						return Result.buildSucc();
				}
				List<GatewayNodeHostDTO> expands = Lists.newArrayList();
				// 过滤出未呗写入的节点
				for (Entry<String, GatewayNodeHostDTO> keyGatewayNodeHostDTOEntry :
						expandKey2DTOMap.entrySet()) {
						if (!key.contains(keyGatewayNodeHostDTOEntry.getKey())) {
								expands.add(keyGatewayNodeHostDTOEntry.getValue());
						}
				}
				return Result.build(gatewayNodeService.insertBatch(expands));
		}
		
		@Override
		public Result<Void> shrinkNodesWithECM(List<GatewayNodeHostDTO> nodes, String name) {
				final List<GatewayClusterNodePO> nodePOS = gatewayNodeService.listByClusterName(
						name);
				//转换未map
				final Map<String, Integer> string2IdMaps = nodePOS.stream()
						.map(i -> Tuples.of(i.getId(), String.format("hostname:[%s];clusterName:[%s];port:[%s]",
								i.getHostName(), i.getClusterName(), i.getPort())))
						.collect(Collectors.toMap(TupleTwo::v2, TupleTwo::v1));
				//转换未相同的信息
				final List<Integer> ids = nodes.stream()
						.map(i -> String.format("hostname:[%s];clusterName:[%s];port:[%s]",
								i.getHostName(), i.getClusterName(), i.getPort()))
						.filter(string2IdMaps::containsKey).map(string2IdMaps::get)
						.collect(Collectors.toList());
				// 无需写入到操作记录，工单操作中生成操作记录即可
				return Result.build(gatewayNodeService.deleteBatch(ids));
		}
		
		@Override
		public Result<Void> updateVersionWithECM(Integer componentId, String version) {
				if (Objects.isNull(gatewayClusterService.getOneByComponentId(componentId))) {
						return Result.buildFail("版本更新失败, 无法匹配到具有组件 ID【%s】的集群信息");
				}
				// 无需写入到操作记录，工单操作中生成操作记录即可
				return Result.build(gatewayClusterService.updateVersion(componentId, version));
		}
		
		@Override
		public Result<GatewayClusterVO> getClusterByName(String name) {
				GatewayClusterPO clusterPO = gatewayClusterService.getOneByName(name);
				if (Objects.isNull(clusterPO)){
						return Result.buildFail("未找到gateway节点信息");
				}
				
				return Result.buildSucc(ConvertUtil.obj2Obj(clusterPO,GatewayClusterVO.class));
		}
		
		@Override
		public Result<Void> checkCompleteUnbindResources(GatewayClusterVO data) {
				GatewayClusterPO clusterPO = gatewayClusterService.getOneByName(data.getClusterName());
				if (Objects.isNull(clusterPO)){
						return Result.buildFail("未找到gateway节点信息");
				}
				Integer gatewayId = clusterPO.getId();
				List<ClusterPhyPO> clusterPhyList = clusterPhyService.listClusterByGatewayId(gatewayId);
				if (CollectionUtils.isNotEmpty(clusterPhyList)){
						return Result.buildFail(String.format("当前gateway集群绑定了【%s】个ES集群，请先解绑对应的ES集群",clusterPhyList.size()));
				}
				return Result.buildSucc();
		}
		
		@Override
		public Result<Void> offlineWithECM(Integer id) {
				GatewayClusterPO clusterPO = gatewayClusterService.getOneById(id);
				if (Objects.isNull(clusterPO)){
						return Result.buildSucc();
				}
				Result<Void> result = checkCompleteUnbindResources(ConvertUtil.obj2Obj(clusterPO, GatewayClusterVO.class));
				if (result.failed()){
						return result;
				}
				return Result.build(gatewayClusterService.deleteOneById(id));
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
	
	@Override
	public Result<GatewayClusterVO> getOneById(Integer gatewayClusterId) {
		final GatewayClusterVO gatewayClusterVO = ConvertUtil.obj2Obj(gatewayClusterService.getOneById(gatewayClusterId),
						GatewayClusterVO.class);
		if (Objects.isNull(gatewayClusterId)) {
			return Result.buildFail(String.format("id:%s 不存在", gatewayClusterId));
		}
		final String clusterName = gatewayClusterVO.getClusterName();
		// 获取 node 列表
		final List<GatewayClusterNodeVO> clusterNodeVOList = ConvertUtil.list2List(
				gatewayNodeService.listByClusterName(
						clusterName), GatewayClusterNodeVO.class);
		gatewayClusterVO.setNodes(clusterNodeVOList);
		
		return Result.buildSucc(gatewayClusterVO);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<Void> deleteById(Integer gatewayClusterId, Integer projectId) {
			Result<Void> result = checkDeleteById(gatewayClusterId, projectId);
			if (result.failed()) {
					return result;
			}
			final GatewayClusterPO gatewayCluster = gatewayClusterService.getOneById(gatewayClusterId);
			if (Objects.isNull(gatewayCluster)) {
					return Result.buildSucc();
			}
			
			if (Objects.nonNull(gatewayCluster.getComponentId()) || gatewayCluster.getComponentId() > 0) {
					// 下线 op
					com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> componentRes = componentService.offLineComponent(
									gatewayCluster.getComponentId());
					if (componentRes.failed()) {
							// 这里显示回滚处理特殊异常场景
							TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
							return Result.buildFrom(componentRes);
					}
			}
			// 先下线 node
			boolean deleteByClusterName = gatewayNodeService.deleteByClusterName(gatewayCluster.getClusterName());
			final boolean deleteOneById = gatewayClusterService.deleteOneById(gatewayClusterId);
			if (!deleteByClusterName || !deleteOneById) {
					// 这里显示回滚处理特殊异常场景
					TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
					return Result.buildFail("下线失败，请联系管理员");
			}
			//TODO 后续补齐操作记录
			String content = String.format("gateway 集群 %s 下线成功", gatewayCluster.getClusterName());
			//operateRecordService.saveOperateRecordWithManualTrigger(content,operator,projectId,
			//		data.getId(), OperateTypeEnum.TEMPLATE_SERVICE);
			return Result.buildSuccWithMsg("下线 gateway 集群成功");
	}
	
	@Override
	public Result<Void> editOne(GatewayClusterDTO data, Integer projectId, String operator) {
		final Result<Void> voidResult = checkEditData(data, projectId);
		if (voidResult.failed()) {
			return voidResult;
		}
		final GatewayClusterPO gatewayCluster =gatewayClusterService.getOneById(data.getId());
		boolean edit = gatewayClusterService.editOne(data);
		if (edit) {
			//TODO 后续补齐操作记录
			String content = String.format("编辑 gateway 集群 %s 的备注:【%s】->【%s】",
					gatewayCluster.getClusterName(),gatewayCluster.getMemo(),data.getMemo());
			//operateRecordService.saveOperateRecordWithManualTrigger(content,operator,projectId,
			//		data.getId(), OperateTypeEnum.TEMPLATE_SERVICE);
		}
		return Result.build(edit);
	}
	
		@Override
		public Result<Boolean> verifyNameUniqueness(String name) {
				return Result.build(gatewayClusterService.checkNameCluster(name) ||
						Objects.nonNull(componentService.queryComponentByName(name)));
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
			return Result.buildFail(AuthConstant.PROJECT_WITHOUT_PERMISSION);
		}
		if (Objects.isNull(param)) {
			return Result.buildFail("传入实体不可为空");
		}
		if (StringUtils.isEmpty(param.getClusterName())) {
			return Result.buildFail("集群名称不可为空");
		}
		final Result<Boolean> result = verifyNameUniqueness(param.getClusterName());
			//这里不能存在op侧相同的名称或者admin侧相同的名称
		if (Boolean.TRUE.equals(result.getData())) {
			return Result.buildFail("集群名称已存在，不可创建同名集群");
		}
		if (CollectionUtils.isEmpty(param.getGatewayNodeHosts())) {
			return Result.buildFail("节点信息不可为空");
		}
		if (param.getGatewayNodeHosts().stream().anyMatch(
				node -> StringUtils.isEmpty(node.getHostName()) || Objects.isNull(node.getPort()))) {
			return Result.buildFail("传入的节点信息地址和端口号不可为空");
		}
		
		return Result.buildSucc();
	}
		
		/**
		 * > 检查网关集群创建的参数
		 *
		 * @param data 用户传入的数据。
		 * @return 结果<无效>
		 */
		private Result<Void> checkGatewayCreateParam(GatewayClusterCreateDTO data) {
				if (Objects.isNull(data)) {
						return Result.buildFail("传入实体不可为空");
				}
				if (StringUtils.isEmpty(data.getClusterName())) {
						return Result.buildFail("集群名称不可为空");
				}
				final Result<Boolean> result = verifyNameUniqueness(data.getClusterName());
				// 这里不能存在 op 侧相同的名称或者 admin 侧相同的名称
				if (Boolean.TRUE.equals(result.getData())) {
						return Result.buildFail("集群名称已存在，不可创建同名集群");
				}
				if (CollectionUtils.isEmpty(data.getNodes())) {
						return Result.buildFail("节点信息不可为空");
				}
				if (data.getNodes().stream().anyMatch(
						node -> StringUtils.isEmpty(node.getHostName()) || Objects.isNull(node.getPort()))) {
						return Result.buildFail("传入的节点信息地址和端口号不可为空");
				}
				return Result.buildSucc();
		}
	
	/**
	 * > 该函数初始化网关集群
	 *
	 * @param gatewayClusterDTO 传递给 initGatewayCluster 方法的 gatewayDTO 对象。
	 */
	private void initJoinGatewayCluster(GatewayClusterDTO gatewayClusterDTO) {
		gatewayClusterDTO.setEcmAccess(Boolean.FALSE);
		gatewayClusterDTO.setHealth(ClusterHealthEnum.UNKNOWN.getCode());
		gatewayClusterDTO.setComponentId(-1);
	}
	/**
	 * > 该功能检查网关集群的数据
	 *
	 * @param data      要检查的数据。
	 * @param projectId 项目ID
	 */
	private Result<Void> checkEditData(GatewayClusterDTO data, Integer projectId) {
		if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
			return Result.buildFail(AuthConstant.PROJECT_WITHOUT_PERMISSION);
		}
		if (Objects.isNull(data)) {
			return Result.buildFail("传入的实体不能为 NULL");
		}
		if (Objects.isNull(data.getId())) {
			return Result.buildFail("传入的编辑 ID 不能为 NULL");
		}
		if (StringUtils.isNotBlank(data.getClusterName())) {
			return Result.buildFail("不可编辑集群名称");
		}
		if (Objects.nonNull(data.getEcmAccess())) {
			return Result.buildFail("不可编辑 ECM 接入的参数");
		}
		if (Objects.nonNull(data.getHealth())) {
			return Result.buildFail("不可编辑集群健康状态");
		}
		if (Objects.nonNull(data.getComponentId())) {
			return Result.buildFail("不可编辑组建 ID");
		}
		if (Objects.nonNull(data.getVersion())) {
			return Result.buildFail("不可编辑版本号");
		}
		return Result.buildSucc();
		
	}
	/**
	 * > 检查用户是否有权通过id删除网关集群
	 *
	 * @param gatewayClusterId 要删除的网关集群的id。
	 * @param projectId 项目ID
	 * @return 一个 Result<Void> 对象。
	 */
	private Result<Void> checkDeleteById(Integer gatewayClusterId, Integer projectId) {
		if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
			return Result.buildFail(AuthConstant.PROJECT_WITHOUT_PERMISSION);
		}
		//TODO 校验网关是否被物理集群绑定
		//TODO 校验网关是否是 admin 绑定的网关
		return Result.buildSucc();
	}
}