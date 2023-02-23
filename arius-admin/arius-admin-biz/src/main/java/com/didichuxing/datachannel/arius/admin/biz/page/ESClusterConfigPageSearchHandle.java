package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.op.manager.IpPort;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.ConfigConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleVO;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
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
 * es集群配置页面搜索
 *
 * @author shizeying
 * @date 2022/11/04
 * @since 0.3.2
 */
@Component("esClusterConfigPageSearchHandle")
public class ESClusterConfigPageSearchHandle extends
		AbstractPageSearchHandle<ConfigConditionDTO, ClusterPhyConfigVO> {
		
		private static final ILog                   LOGGER         = LogFactory.getLog(
				ESClusterConfigPageSearchHandle.class);
		private static final CharSequence[]         CHAR_SEQUENCES = {"*", "?"};
		@Autowired
		private              ClusterPhyService      clusterPhyService;
		@Autowired
		private              ClusterRoleHostService clusterRoleHostService;
		@Autowired
		private ClusterRoleService clusterRoleService;
		
		@Autowired
		private ComponentService componentService;
		
		
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
		protected PaginationResult<ClusterPhyConfigVO> buildPageData(ConfigConditionDTO condition,
				Integer projectId) {
				final ClusterPhy cluster     = clusterPhyService.getClusterById(condition.getClusterId());
				if (Objects.isNull(cluster)){
						return PaginationResult.buildFail("未匹配到集群");
				}
				Integer          componentId = cluster.getComponentId();
				// 如果找不到
				if (Objects.isNull(componentId)) {
						return PaginationResult.buildSucc(Collections.emptyList(), 0L, condition.getPage(),
								condition.getSize());
				}
				final com.didiglobal.logi.op.manager.domain.component.entity.Component component = componentService.queryComponentById(
						componentId).getData();
				if (Objects.isNull(component)) {
						return PaginationResult.buildSucc(Collections.emptyList(), 0L, condition.getPage(),
								condition.getSize());
				}
				final List<ComponentGroupConfig> data =  component.getGroupConfigList();
				if (CollectionUtils.isEmpty(data)) {
						return PaginationResult.buildSucc(Collections.emptyList(), 0L, condition.getPage(),
								condition.getSize());
				}
				//获取可以编辑的配置id
				final List<Integer> ids = componentService.getComponentConfig(componentId).getData()
						.stream().map(ComponentGroupConfig::getId).collect(
								Collectors.toList());
				// 获取 host 列表
				final List<ComponentHost> hostList = component.getHostList();
				final Map<String, List<ComponentHost>> groupName2HostsMaps = ConvertUtil.list2MapOfList(hostList,
						ComponentHost::getGroupName, i -> i);
				//1. 获取全量的 node 节点信息
				final List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.listNodesByClusters(
						Collections.singletonList(cluster.getCluster()));
				final List<ESClusterRoleHostVO> nodes = ConvertUtil.list2List(
						clusterRoleHosts, ESClusterRoleHostVO.class);
				final Map<String, List<ESClusterRoleHostVO>> ip2ListMap = ConvertUtil.list2MapOfList(
						nodes, ESClusterRoleHostVO::getIp, i -> i);
				// 通过 ComponentId 进行选出对应的对应填充
				final List<ClusterPhyConfigVO> clusterPhyConfigVOS = ConvertUtil.list2List(data,
						ClusterPhyConfigVO.class);
				final List<ClusterRoleInfo> allRoleClusterByClusterId = clusterRoleService.getAllRoleClusterByClusterId(
						condition.getClusterId());
				for (ClusterPhyConfigVO clusterPhyConfigVO : clusterPhyConfigVOS) {
						final String              groupName      = clusterPhyConfigVO.getGroupName();
						final List<ComponentHost> componentHosts = groupName2HostsMaps.get(groupName);
						//获取IP维度的最小端口号和最大端口号
						final List<IpPort> ipPorts = CommonUtils.generalGroupConfig2ESIpPortList(
								ConvertUtil.obj2Obj(clusterPhyConfigVO,
										GeneralGroupConfig.class));
						final Map<String, IpPort> ip2IportMap = ConvertUtil.list2Map(ipPorts, IpPort::getIp);
						final List<ESClusterRoleHostVO> nodesList = Optional.ofNullable(componentHosts)
								.orElse(Collections.emptyList())
								.stream()
								.map(ComponentHost::getHost)
								.map(ip2ListMap::get)
								.filter(Objects::nonNull)
								.flatMap(Collection::stream)
								.filter(
										CommonUtils.distinctByKey(i -> String.format("%s:%s", i.getIp(), i.getPort())))
								.filter(i -> ip2IportMap.containsKey(i.getIp()))
								.filter(i -> {
										final IpPort ipPort = ip2IportMap.get(i.getIp());
										//判断是否再此范围内
										final Integer port = Integer.parseInt(i.getPort());
										return port >= ipPort.getMinPort() && port <= ipPort.getMaxPort();
								})
								.collect(Collectors.toList());
						clusterPhyConfigVO.setNodes(nodesList);
						//设置有角色信息的
						final Map<Long, List<ESClusterRoleHostVO>> roleId2ListMap = ConvertUtil.list2MapOfList(
								nodesList, ESClusterRoleHostVO::getRoleClusterId, i -> i);
						final List<ESClusterRoleVO> roleVOS = ConvertUtil.list2List(
								allRoleClusterByClusterId, ESClusterRoleVO.class);
						roleVOS.forEach(i -> Optional
								.ofNullable(roleId2ListMap.get(i.getId()))
								.ifPresent(i::setEsClusterRoleHostVO));
						clusterPhyConfigVO.setRoleWithNodes(roleVOS);
						
						if (ids.contains(clusterPhyConfigVO.getId())) {
								clusterPhyConfigVO.setSupportEditAndRollback(true);
						}
						
				}
				final List<ClusterPhyConfigVO> sortLists = clusterPhyConfigVOS.stream()
						.collect(Collectors.groupingBy(ClusterPhyConfigVO::getGroupName))
						.values()
						.stream()
						.map(clusterConfigVOS -> clusterConfigVOS.stream()
								.sorted(Comparator.comparingInt(this::versionToInt).reversed())
								.limit(5)
								.collect(Collectors.toList())
						)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());
				final List<ClusterPhyConfigVO> gatewayConfigPage = sortLists.stream()
						.filter(i -> filterByConfigConditionDTO(i, condition))
						.sorted(Comparator.comparing(this::versionToInt).reversed().thenComparing(
								ClusterPhyConfigVO::getGroupName))
						.collect(Collectors.toList());
				int total = sortLists.size();
				final List<ClusterPhyConfigVO> records = gatewayConfigPage.stream()
						.skip((condition.getPage() - 1) * condition.getSize()).limit(condition.getSize()).
						collect(Collectors.toList());
				return PaginationResult.buildSucc(records, total, condition.getPage(), condition.getSize());
		}
		
		private int versionToInt(ClusterPhyConfigVO t) {
				return Integer.parseInt(t.getVersion());
		}

		/**
		 * > 如果条件的配置名称为空，则返回 true。否则，返回条件的配置名称是否包含网关配置的组名
		 *
		 * @param clusterPhyConfigVO 要过滤的对象
		 * @param condition       包含查询条件的条件对象。
		 * @return GatewayConfigVO 对象的列表。
		 */
		private boolean filterByConfigConditionDTO(ClusterPhyConfigVO clusterPhyConfigVO,
				ConfigConditionDTO condition) {
				if (StringUtils.isBlank(condition.getGroupName())) {
						return true;
				}
				return StringUtils.containsAny(condition.getGroupName(), clusterPhyConfigVO.getGroupName());
		}
		

		
		
}