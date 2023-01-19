package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.op.manager.IpPort;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayConfigVO;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayNodeService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 网关集群配置页面搜索
 *
 * @author shizeying
 * @date 2022/11/04
 * @since 0.3.2
 */
@Component
public class GatewayClusterConfigPageSearchHandle extends
		AbstractPageSearchHandle<ConfigConditionDTO, GatewayConfigVO> {
	
	private static final ILog LOGGER = LogFactory.getLog(GatewayClusterConfigPageSearchHandle.class);
	private static final CharSequence[] CHAR_SEQUENCES = {"*", "?"};
	@Autowired
	private GatewayClusterService gatewayClusterService;
	@Autowired
	private GatewayNodeService gatewayNodeService;
	@Autowired
	private ComponentService   componentService;
	
	@Override
	protected Result<Boolean> checkCondition(ConfigConditionDTO condition, Integer projectId) {
		String configName = condition.getGroupName();
		if (StringUtils.containsAny(configName, CHAR_SEQUENCES)) {
			return Result.buildParamIllegal("物理集群名称不允许带类似 *, ? 等通配符查询");
		}
		
		return Result.buildSucc();
	}
	
	@Override
	protected void initCondition(ConfigConditionDTO condition, Integer projectId) {
	
	}
	
	@Override
	protected PaginationResult<GatewayConfigVO> buildPageData(ConfigConditionDTO condition,
			Integer projectId) {
			final GatewayClusterPO clusterPO   = gatewayClusterService.getOneById(
					condition.getClusterId());
			final Integer          componentId = clusterPO.getComponentId();
			// 如果找不到
			if (Objects.isNull(componentId) || componentId <= 0) {
					return PaginationResult.buildSucc(Collections.emptyList(), 0L, condition.getPage(),
							condition.getSize());
			}
			final com.didiglobal.logi.op.manager.domain.component.entity.Component component = componentService.queryComponentById(
					componentId).getData();
			if (Objects.isNull(component)) {
					return PaginationResult.buildSucc(Collections.emptyList(), 0L, condition.getPage(),
							condition.getSize());
			}
			final List<ComponentGroupConfig> data = component.getGroupConfigList();
			if (CollectionUtils.isEmpty(data)) {
					return PaginationResult.buildSucc(Collections.emptyList(), 0L, condition.getPage(),
							condition.getSize());
			}
			// 获取可以编辑的配置 id
			final List<Integer> ids = componentService.getComponentConfig(componentId).getData()
					.stream().map(ComponentGroupConfig::getId).collect(
							Collectors.toList());
			// 获取 host 列表
			final List<ComponentHost> hostList = component.getHostList();
			final Map<String, List<ComponentHost>> groupName2HostsMaps = ConvertUtil.list2MapOfList(
					hostList,
					ComponentHost::getGroupName, i -> i);
			
			//1. 获取全量的 node 节点信息
			List<GatewayClusterNodePO> gatewayClusterNodeList =
					gatewayNodeService.listByClusterNames(
							Collections.singletonList(clusterPO.getClusterName()));
			final List<GatewayClusterNodeVO> nodes = ConvertUtil.list2List(
					gatewayClusterNodeList, GatewayClusterNodeVO.class);
			
			// 进行对应的转换
			final Map<String, List<GatewayClusterNodeVO>> ip2ListMap = ConvertUtil.list2MapOfList(
					nodes, GatewayClusterNodeVO::getHostName, i -> i);
			// 通过 ComponentId 进行选出对应的对应填充
			final List<GatewayConfigVO> gatewayConfigList = ConvertUtil.list2List(data,
					GatewayConfigVO.class);
			for (GatewayConfigVO gatewayConfigVO : gatewayConfigList) {
					final String              groupName      = gatewayConfigVO.getGroupName();
					//获取IP维度的最小端口号和最大端口号
					final List<IpPort> ipPorts = CommonUtils.generalGroupConfig2GatewayIpPortList(
							ConvertUtil.obj2Obj(gatewayConfigVO,
									GeneralGroupConfig.class));
					final Map<String, IpPort> ip2IportMap = ConvertUtil.list2Map(ipPorts, IpPort::getIp);

					final List<ComponentHost> componentHosts = groupName2HostsMaps.get(groupName);
					final List<GatewayClusterNodeVO> nodesList = Optional.ofNullable(componentHosts)
							.orElse(Collections.emptyList())
							.stream()
							.map(ComponentHost::getHost)
							.map(ip2ListMap::get)
							.filter(Objects::nonNull)
							.flatMap(Collection::stream)
							.filter(
										CommonUtils.distinctByKey(i -> String.format("%s:%s", i.getHostName(), i.getPort())))
							.filter(i->ip2IportMap.containsKey(i.getHostName()))
							.filter(i -> {
									final IpPort ipPort = ip2IportMap.get(i.getHostName());
									//判断是否再此范围内
									final Integer port = i.getPort();
									return port >= ipPort.getMinPort() && port <= ipPort.getMaxPort();
							})
							.collect(Collectors.toList());
					gatewayConfigVO.setNodes(nodesList);
					if (ids.contains(gatewayConfigVO.getId())) {
							gatewayConfigVO.setSupportEditAndRollback(true);
					}
			}
			final List<GatewayConfigVO> sortLists = gatewayConfigList.stream()
					.collect(Collectors.groupingBy(GatewayConfigVO::getGroupName))
					.values()
					.stream()
					.map(gatewayConfigVOS -> gatewayConfigVOS.stream()
							.sorted(Comparator.comparingInt(this::versionToInt).reversed())
							.limit(5)
							.collect(Collectors.toList())
					)
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
			final List<GatewayConfigVO> gatewayConfigPage = sortLists.stream()
					.filter(i -> filterByConfigConditionDTO(i, condition))
					.sorted(Comparator.comparing(this::versionToInt).reversed().thenComparing(GatewayConfigVO::getGroupName))
					.collect(Collectors.toList());
			int total = sortLists.size();
			final List<GatewayConfigVO> records = gatewayConfigPage.stream()
					.skip((condition.getPage() - 1) * condition.getSize()).limit(condition.getSize()).
					collect(Collectors.toList());
			return PaginationResult.buildSucc(records, total, condition.getPage(), condition.getSize());
	}
	
	/**
	 * > 如果条件的配置名称为空，则返回 true。否则，返回条件的配置名称是否包含网关配置的组名
	 *
	 * @param gatewayConfigVO 要过滤的对象
	 * @param condition 包含查询条件的条件对象。
	 * @return GatewayConfigVO 对象的列表。
	 */
	private boolean filterByConfigConditionDTO(GatewayConfigVO gatewayConfigVO, ConfigConditionDTO condition) {
		if (StringUtils.isBlank(condition.getGroupName())) {
			return true;
		}
		return StringUtils.containsAny(condition.getGroupName(),gatewayConfigVO.getGroupName());
	}

		private int versionToInt(GatewayConfigVO t) {
				return Integer.parseInt(t.getVersion());
		}

	/**
	 * > 将逗号分隔的主机列表转换为字符串列表
	 *
	 * @param componentGroupConfig 传入方法的 ComponentGroupConfig 对象。
	 * @return 字符串列表
	 */
	private List<String> hostsConvertHostList(ComponentGroupConfig componentGroupConfig) {
		return Lists.newArrayList(StringUtils.split(",", componentGroupConfig.getHosts()));
	}
	
	
}