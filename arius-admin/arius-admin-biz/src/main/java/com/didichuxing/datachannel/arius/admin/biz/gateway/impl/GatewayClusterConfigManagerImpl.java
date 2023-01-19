package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.GATEWAY_CLUSTER_CONFIG;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterConfigManager;
import com.didichuxing.datachannel.arius.admin.biz.page.GatewayClusterConfigPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.op.manager.IpPort;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.op.manager.ComponentGroupConfigWithGatewayHostVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.component.service.ComponentDomainService;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.vo.ComponentGroupConfigVO;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

;

/**
 * 网关集群配置
 *
 * @author shizeying
 * @date 2022/11/04
 * @since 0.3.2
 */
@org.springframework.stereotype.Component
@NoArgsConstructor
public class GatewayClusterConfigManagerImpl implements GatewayClusterConfigManager {
	@Autowired
	private GatewayClusterService gatewayClusterService;
	@Autowired
	private GatewayNodeService gatewayNodeService;
	@Autowired
	private ComponentService      componentService;
	@Autowired
	private ComponentDomainService componentDomainService;
	@Autowired
	private HandleFactory         handleFactory;
	@Override
	public PaginationResult<GatewayConfigVO> pageGetConfig(ConfigConditionDTO condition,
			Integer projectId, Integer gatewayClusterId) {
		condition.setClusterId(gatewayClusterId);

		BaseHandle baseHandle;
		try {
			baseHandle = handleFactory.getByHandlerNamePer(GATEWAY_CLUSTER_CONFIG.getPageSearchType());
		} catch (NotFindSubclassException e) {
			return PaginationResult.buildFail("没有找到对应的处理器");
		}
		if (baseHandle instanceof GatewayClusterConfigPageSearchHandle) {
		
			GatewayClusterConfigPageSearchHandle handler = (GatewayClusterConfigPageSearchHandle) baseHandle;
			return handler.doPage(condition, projectId);
		}
		return PaginationResult.buildFail("没有找到对应的处理器");
	}
	
	@Override
	public Result<ComponentGroupConfigVO> getConfigByGatewayId(Integer gatewayClusterId,
			Integer configId) {
		final Integer componentIdById = gatewayClusterService.getComponentIdById(gatewayClusterId);
		final Optional<ComponentGroupConfig> componentGroupConfigOptional = Optional.ofNullable(
						componentService.getComponentConfig(
								componentIdById).getData()).orElse(Collections.emptyList())
				.stream().filter(i -> Objects.equals(i.getId(), configId)).findFirst();
		
		
		return componentGroupConfigOptional.map(i-> ConvertUtil.obj2Obj(i,
				ComponentGroupConfigVO.class)).map(Result::buildSucc).orElse(Result.buildSucc());
	}
		
		@Override
		public Result<List<ComponentGroupConfigWithGatewayHostVO>> getConfigsByGatewayId(Integer gatewayClusterId) {
				final GatewayClusterPO clusterPO = gatewayClusterService.getOneById(gatewayClusterId);
				final Integer          componentId = clusterPO.getComponentId();
				if (Objects.isNull(componentId)) {
						return Result.buildSucc(Collections.emptyList());
				}
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Component> componentRes = componentService.queryComponentById(
						componentId);
				if (Objects.isNull(componentRes.getData()) || CollectionUtils.isEmpty(
						componentRes.getData().getGroupConfigList())) {
						return Result.buildSucc(Collections.emptyList());
				}
				final List<Integer> groupConfigIds = componentService.getComponentConfig(
								componentId).getData().stream().map(ComponentGroupConfig::getId)
						.collect(Collectors.toList());
				
				final Map<String, List<ComponentHost>> groupName2HostLists = ConvertUtil.list2MapOfList(
						componentRes.getData().getHostList(),
						ComponentHost::getGroupName, i -> i);
				final List<ComponentGroupConfig> data = componentRes.getData().getGroupConfigList()
						.stream().filter(i->groupConfigIds.contains(i.getId())).collect(Collectors.toList());
				final List<GatewayClusterNodePO> nodePOList = gatewayNodeService.listByClusterName(
						clusterPO.getClusterName());
				final Map<String, List<GatewayClusterNodePO>> hostName2ListMap = ConvertUtil.list2MapOfList(
						nodePOList, GatewayClusterNodePO::getHostName, i -> i);
				final List<ComponentGroupConfigWithGatewayHostVO> hostVOS = ConvertUtil.list2List(data,
						ComponentGroupConfigWithGatewayHostVO.class);
				for (ComponentGroupConfigWithGatewayHostVO hostVO : hostVOS) {
						final String              groupName      = hostVO.getGroupName();
						final List<ComponentHost> componentHosts = groupName2HostLists.get(groupName);
						hostVO.setComponentHosts(componentHosts);
						hostVO.setPackageId(componentRes.getData().getPackageId());
							//获取IP维度的最小端口号和最大端口号
						final List<IpPort> ipPorts = CommonUtils.generalGroupConfig2GatewayIpPortList(
								com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil.obj2Obj(hostVO,
										GeneralGroupConfig.class));
						final Map<String, IpPort> ip2IportMap = ConvertUtil.list2Map(ipPorts, IpPort::getIp);
						final List<GatewayClusterNodePO> poList = Optional.ofNullable(componentHosts)
								.orElse(Collections.emptyList())
								.stream()
								.map(ComponentHost::getHost)
								.filter(hostName2ListMap::containsKey)
								.map(hostName2ListMap::get)
								.flatMap(Collection::stream)
								.filter(i -> ip2IportMap.containsKey(i.getHostName()))
								.filter(i -> {
										final IpPort ipPort = ip2IportMap.get(i.getHostName());
										//判断是否再此范围内
										final Integer port = i.getPort();
										return port >= ipPort.getMinPort() && port <= ipPort.getMaxPort();
								})
								.collect(Collectors.toList());
						hostVO.setNodes(ConvertUtil.list2List(poList, GatewayClusterNodeVO.class));
				}
				
				return Result.buildSucc(hostVOS);
		}
		
		@Override
		public Result<List<ComponentGroupConfig>> getRollbackConfigsByClusterPhyId(
				Integer gatewayClusterId, Integer configId) {
				final Integer componentIdById = gatewayClusterService.getComponentIdById(gatewayClusterId);
				if (Objects.isNull(componentIdById)) {
						return Result.buildSucc(Collections.emptyList());
				}
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<com.didiglobal.logi.op.manager.domain.component.entity.Component> componentRes = componentService.queryComponentById(
						componentIdById);
				if (Objects.isNull(componentRes.getData()) || CollectionUtils.isEmpty(
						componentRes.getData().getGroupConfigList())) {
						return Result.buildSucc(Collections.emptyList());
				}
				// 获取配置列表
				final List<ComponentGroupConfig> groupConfigList = componentRes.getData()
						.getGroupConfigList();
				final Optional<ComponentGroupConfig> groupConfigOptional = groupConfigList.stream()
						.filter(i -> Objects.equals(i.getId(), configId))
						.findFirst();
				if (!groupConfigOptional.isPresent()) {
						return Result.buildSucc(Collections.emptyList());
				}
				final String groupName = groupConfigOptional.get().getGroupName();
				final String version   = groupConfigOptional.get().getVersion();
				if (!StringUtils.isNumeric(version)) {
						return Result.buildSucc(Collections.emptyList());
				}
				final List<ComponentGroupConfig> rollbackConfigs = groupConfigList.stream()
						.filter(i -> StringUtils.equals(groupName, i.getGroupName()))
						.filter(i -> Integer.parseInt(i.getVersion()) < Integer.parseInt(version))
						.sorted(Comparator.comparingInt(this::versionToInt).reversed())
            .limit(5)
						.collect(Collectors.toList());
				return Result.buildSucc(rollbackConfigs);
		}
		private int versionToInt(ComponentGroupConfig t) {
        return Integer.parseInt(t.getVersion());
    }
}