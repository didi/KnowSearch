package com.didichuxing.datachannel.arius.admin.biz.plugin;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESResponsePluginInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.common.op.manager.IpPort;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.plugin.PluginCreateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.plugin.PluginInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.op.manager.ComponentGroupConfigWithHostVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.PackageBriefVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginInfoTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusScheduleThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.plugin.PluginInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayClusterService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.application.PackageService;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 插件对应能力
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@org.springframework.stereotype.Component
public class PluginManagerImpl implements PluginManager {
		
		private static final ILog                          LOGGER                   = LogFactory.getLog(
				PluginManagerImpl.class);
		private static final Integer UNBOUNDCOMPONENTID = -1;
		@Autowired
		private              PluginInfoService             pluginInfoService;
		@Autowired
		private              ClusterPhyService             clusterPhyService;
		@Autowired
		private              ESClusterNodeService          esClusterNodeService;
		@Autowired
		private AriusScheduleThreadPool ariusScheduleThreadPool;
		@Autowired
		private GatewayClusterService   gatewayClusterService;
		@Autowired
		private ComponentService        componentService;
		@Autowired
		private PackageService          packageService;
		@Autowired
		private ClusterRoleService      clusterRoleService;
		@Autowired
		private ClusterRoleHostService  clusterRoleHostService;
		/**
		 * 插件列表的缓存:ES 集群维度
		 */
		private static final Cache<String, List<PluginVO>> CLUSTER_PHY_PLUGIN_CACHE =
				CacheBuilder.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(10000).build();
		
		@PostConstruct
		private void init() {
				ariusScheduleThreadPool.submitScheduleAtFixedDelayTask(this::refreshClusterPhyPluginInfo,
						120, 180);
		}
		
		
		@Override
		public Result<List<PluginVO>> listGatewayPluginByGatewayClusterId(Integer gatewayId) {
				List<PluginInfoPO> pluginList = pluginInfoService.listByClusterId(gatewayId,
						PluginClusterTypeEnum.GATEWAY);
				return Result.buildSucc(ConvertUtil.list2List(pluginList, PluginVO.class));
		}
		
		@Override
		public Result<List<PluginVO>> listESPluginByClusterId(Integer clusterPhyId) {
				final ClusterPhy cluster = clusterPhyService.getClusterById(clusterPhyId);
				if (Objects.isNull(cluster)) {
						return Result.buildSucc(Collections.emptyList());
				}
				List<PluginInfoPO> pluginList = pluginInfoService.listByClusterId(clusterPhyId,
						PluginClusterTypeEnum.ES);
				final Integer dependComponentId = clusterPhyService.getComponentIdById(clusterPhyId);
				
				final List<Integer> componentIds = pluginList.stream().map(PluginInfoPO::getComponentId).filter(Objects::nonNull)
								.distinct().collect(Collectors.toList());
				//获取插件状态
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<List<Component>> result = componentService.listComponentWithAll();
					final Map<Integer, Component> id2ComponentAllMap = result.getData().stream().collect(
								Collectors.toMap(Component::getId, i -> i));
				final Map<Integer, Component> id2ComponentMap = result.getData().stream().filter(
								component -> componentIds.contains(component.getId())).collect(
								Collectors.toMap(Component::getId, i -> i));
				// 获取内核的插件列表
				final Result<List<PluginVO>> listResult = listESKernelPluginCache(cluster.getCluster());
				final List<PluginVO>         list       = ConvertUtil.list2List(pluginList, PluginVO.class);
				list.addAll(listResult.getData());
				
				// 设置配置信息
				for (PluginVO pluginVO : list) {
						if (Objects.nonNull(pluginVO.getComponentId()) && pluginVO.getComponentId() > 0) {
							
								final Component component = id2ComponentMap.get(pluginVO.getComponentId());
								if (component == null) {
										// 如果组件为 null，则默认给未知
										pluginVO.setStatus(PluginHealthEnum.UNKNOWN.getCode());
								} else {
										pluginVO.setPackageId(component.getPackageId());
										final List<ComponentHost> hostList = component.getHostList();
										// 确认节点是否存在离线
										if (hostList.stream().anyMatch(i -> i.getStatus() == 1)) {
												pluginVO.setStatus(PluginHealthEnum.RED.getCode());
										}
										pluginVO.setStatus(PluginHealthEnum.GREEN.getCode());
										if (PluginInfoTypeEnum.ENGINE.equals(PluginInfoTypeEnum.find(pluginVO.getPluginType()))) {
												// 获取依赖的组件
												final Component componentDepend = id2ComponentAllMap.get(
																component.getDependConfigComponentId());
												// 获取对应的配置项
												final List<ComponentGroupConfig> groupConfigList = componentDepend.getGroupConfigList();
												// 排序后获取最新的配置项为正在使用的配置项：ecm 规定
												groupConfigList.stream().
														max(Comparator.comparing(i -> Integer.parseInt(i.getVersion()))).map(
																componentGroupConfig -> ConvertUtil.obj2Obj(componentGroupConfig,
																                                            ComponentGroupConfigWithHostVO.class, i -> {
																				final List<ComponentHost> hosts = componentDepend.getHostList()
																						.stream().filter(
																								host -> StringUtils.equals(host.getGroupName(),
																										i.getGroupName())).collect(Collectors.toList());
																				i.setComponentHosts(hosts);
																				i.setPackageId(component.getPackageId());
																				i.setComponentId(pluginVO.getComponentId());
																				i.setDependComponentId(dependComponentId);
																						
																				})).map(
																Collections::singletonList).ifPresent(pluginVO::setComponentGroupConfigs);
												
												
										} else if (PluginInfoTypeEnum.PLATFORM.equals(PluginInfoTypeEnum.find(pluginVO.getPluginType()))) {
												component.getGroupConfigList().stream().max(
																Comparator.comparing(i -> Integer.parseInt(i.getVersion()))).map(
																componentGroupConfig -> ConvertUtil.obj2Obj(componentGroupConfig,
																                                            ComponentGroupConfigWithHostVO.class, i -> {
																				final List<ComponentHost> hosts = component.getHostList()
																						.stream().filter(host ->
																								StringUtils.equals(host.getGroupName(),
																										i.getGroupName()))
																						.collect(Collectors.toList());
																				i.setComponentHosts(hosts);
																				i.setPackageId(component.getPackageId());
																				i.setComponentId(pluginVO.getComponentId());
																				i.setDependComponentId(dependComponentId);
																						
																				})).map(Collections::singletonList).ifPresent(
																pluginVO::setComponentGroupConfigs);
										}
								}
						}
				}
				return Result.buildSucc(list);
		}
		
		@Override
		public Result<List<PluginVO>> listESKernelPluginCache(String clusterPhy) {
				try {
						return Result.buildSucc(CLUSTER_PHY_PLUGIN_CACHE.get(clusterPhy,
								() -> getKernelPlugin(clusterPhy)));
				} catch (Exception ignore) {
						return Result.buildSucc(Collections.emptyList());
				}
		}
		
		@Override
		public Result<Void> createWithECM(PluginCreateDTO pluginCreateDTO) {
				// 无需记录：工单记录即可
				PluginInfoPO pluginInfoPO = ConvertUtil.obj2Obj(pluginCreateDTO, PluginInfoPO.class);
				// 确定插件是否已经被写入
				if (pluginInfoService.selectByCondition(pluginInfoPO) != null) {
						return Result.buildSucc();
				}
				return Result.build(pluginInfoService.create(pluginInfoPO));
		}
		
		@Override
		public Result<Void> uninstallWithECM(Integer clusterId, PluginClusterTypeEnum type, Integer componentId) {
				//无需记录：工单记录即可
				return Result.build(pluginInfoService.delete(clusterId,type.getClusterType(),componentId));
		}
		
		@Override
		public Result<Void> updateVersionWithECM(Integer clusterId, Integer componentId, PluginClusterTypeEnum type,
		                                         String version) {
				// 无需记录：工单记录即可
				// 获取对应的 ID
				PluginInfoPO pluginInfoPO = pluginInfoService.selectByClusterIdAndComponentIdAndClusterType(clusterId,
				                                                                                            componentId,
				                                                                                            type.getClusterType());
				if (Objects.isNull(pluginInfoPO)) {
						return Result.buildFail("未找到指定的插件信息");
				}
				pluginInfoPO.setVersion(version);
				return Result.build(pluginInfoService.update(pluginInfoPO));
		}
		
		@Override
		public Result<Void> checkClusterCompleteUninstallPlugins(Integer clusterId, PluginClusterTypeEnum type) {
				List<PluginInfoPO> pluginInfoPOS = pluginInfoService.listByClusterId(clusterId, type);
				if (CollectionUtils.isEmpty(pluginInfoPOS)) {
						return Result.buildSucc();
				}
				return Result.buildFail("集群中存在插件未完全卸载，请先卸载集群中存在的插件，才可进行下线集群任务");
		}
		@Override
		public Result<List<PackageBriefVO>> getLowerVersionByGatewayClusterId(Integer gatewayClusterId) {
			Integer componentId = gatewayClusterService.getComponentIdById(gatewayClusterId);
			if(Objects.isNull(componentId) || UNBOUNDCOMPONENTID.compareTo(componentId) == 0) {
				return Result.buildSucc(Lists.newArrayList());
			}
			com.didiglobal.logi.op.manager.infrastructure.common.Result<com.didiglobal.logi.op.manager.domain.component.entity.Component> componentResult = componentService.queryComponentById(componentId);
			if (componentResult.failed()) {
				return Result.buildFrom(componentResult);
			}
			com.didiglobal.logi.op.manager.infrastructure.common.Result<Package> packageByIdResult = packageService.getPackageById((long) componentResult.getData().getPackageId());
			if (packageByIdResult.failed()) {
				return Result.buildFrom(packageByIdResult);
			}
			String currentVersion = packageByIdResult.getData().getVersion();
			List<Package> packageList = packageService.queryPackageByName(packageByIdResult.getData().getName());
			List<Package> listPackage = packageList.stream().filter(aPackage -> ESVersionUtil.compareVersion(aPackage.getVersion(), currentVersion) < 0).collect(Collectors.toList());
			return Result.buildSucc(ConvertUtil.list2List(listPackage, PackageBriefVO.class));
		}
		
		@Override
		public Result<PluginVO> getClusterByComponentId(Integer componentId,
				PluginClusterTypeEnum typeEnum) {
				PluginInfoPO pluginPO = pluginInfoService.getOneByComponentId(componentId, typeEnum);
				return Result.buildSucc(ConvertUtil.obj2Obj(pluginPO, PluginVO.class));
		}
		
		@Override
		public Result<List<ComponentGroupConfigWithHostVO>> getConfigsByPluginId(Long pluginId) {
				final PluginInfoPO pluginById = pluginInfoService.getPluginById(pluginId);
				if (Objects.isNull(pluginById)) {
						return Result.buildSucc(Collections.emptyList());
				}
				final Integer      componentId = pluginById.getComponentId();
				final Integer      clusterId = pluginById.getClusterId();
				final Integer dependComponentId = clusterPhyService.getComponentIdById(clusterId);
				if (Objects.isNull(componentId)
						|| componentId <= 0) {
						return Result.buildSucc(Collections.emptyList());
				}
				
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Component> componentRes = componentService.queryComponentById(
						componentId);
				final Component component = componentRes.getData();
				if (Objects.isNull(component) ) {
						return Result.buildSucc(Collections.emptyList());
				}
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<List<ComponentGroupConfig>> componentConfigRes = componentService.getComponentConfig(
						componentId);
				
				final List<ComponentGroupConfig> componentGroupConfigs = componentConfigRes.getData();
				final List<ComponentGroupConfigWithHostVO> hostVOS = ConvertUtil.list2List(
						componentGroupConfigs, ComponentGroupConfigWithHostVO.class);
				// 获取依赖的配置 ID
				List<ComponentHost> hostList          =component.getHostList();
				if (component.getDependConfigComponentId() != null) {
						hostList =
								componentService.queryComponentHostById(component.getDependConfigComponentId())
										.getData();
				}
				final Map<String, List<ComponentHost>> groupName2HostLists = com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil.list2MapOfList(
						hostList,
						ComponentHost::getGroupName, i -> i);
				 List<ClusterRoleInfo> clusterRoleInfos =
						 clusterRoleService.getAllRoleClusterByClusterId(pluginById.getClusterId());
				 List<ESClusterRoleVO> roleClusters = ConvertUtil.list2List(clusterRoleInfos, ESClusterRoleVO.class);
				List<Long> roleClusterIds = Optional.ofNullable(roleClusters).orElse(Collections.emptyList()).stream()
						.map(ESClusterRoleVO::getId).collect(Collectors.toList());
				Map<Long, List<ClusterRoleHost>> roleIdsMap = clusterRoleHostService.getByRoleClusterIds(
						roleClusterIds);
				final List<ClusterRoleHost> clusterRoleHosts = roleIdsMap.values().stream()
						.filter(Objects::nonNull)
						.flatMap(Collection::stream).collect(Collectors.toList());
				final List<ESClusterRoleHostVO> esClusterRoleHostVOS = ConvertUtil.list2List(
						clusterRoleHosts, ESClusterRoleHostVO.class);
				final Map<String, List<ESClusterRoleHostVO>> ip2VOSMap = ConvertUtil.list2MapOfList(
						esClusterRoleHostVOS, ESClusterRoleHostVO::getIp, i -> i);
				final List<ClusterRoleInfo> allRoleClusterByClusterId = clusterRoleService.getAllRoleClusterByClusterId(
						clusterId);
				final List<ESClusterRoleVO> esClusterRoleVOS = ConvertUtil.list2List(
						allRoleClusterByClusterId, ESClusterRoleVO.class);
				for (ComponentGroupConfigWithHostVO hostVO : hostVOS) {
						final String              groupName      = hostVO.getGroupName();
								//获取IP维度的最小端口号和最大端口号
						final List<IpPort> ipPorts = Lists.newArrayList();
						
						if (PluginInfoTypeEnum.find(pluginById.getPluginType())==PluginInfoTypeEnum.ENGINE){
								ipPorts.addAll(CommonUtils.generalGroupConfig2ESIpPortList(
								ConvertUtil.obj2Obj(hostVO,
										GeneralGroupConfig.class)));
						}else if (PluginInfoTypeEnum.find(pluginById.getPluginType())==PluginInfoTypeEnum.PLATFORM){
								ipPorts.addAll(CommonUtils.generalGroupConfig2GatewayIpPortList(
								ConvertUtil.obj2Obj(hostVO,
										GeneralGroupConfig.class)));
						}
						final Map<String, IpPort> ip2IportMap =ConvertUtil.list2Map(ipPorts, IpPort::getIp);
						final List<ComponentHost> componentHosts = groupName2HostLists.get(groupName);
						hostVO.setComponentHosts(componentHosts);
						hostVO.setComponentId(componentId);
						hostVO.setDependComponentId(dependComponentId);
						hostVO.setPackageId(component.getPackageId());
						if (PluginInfoTypeEnum.ENGINE.equals(PluginInfoTypeEnum.find(pluginById.getPluginType()))){
								final List<ESClusterRoleHostVO> clusterRoleHostVOS = Optional.ofNullable(componentHosts)
										.orElse(Collections.emptyList())
										.stream()
										.filter(i -> ip2VOSMap.containsKey(i.getHost()))
										.map(i -> ip2VOSMap.get(i.getHost()))
										.filter(Objects::nonNull)
										.flatMap(Collection::stream)
										.distinct()
										.filter(i -> ip2IportMap.containsKey(i.getIp()))
										.filter(i -> {
												final IpPort ipPort = ip2IportMap.get(i.getIp());
												//判断是否再此范围内
												final Integer port = Integer.parseInt(i.getPort());
												return port >= ipPort.getMinPort() && port <= ipPort.getMaxPort();
										})
										.collect(Collectors.toList());
								hostVO.setEsClusterRoles(clusterRoleHostVOS);
								//设置有角色信息的
								final Map<Long, List<ESClusterRoleHostVO>> roleId2ListMap = ConvertUtil.list2MapOfList(
										clusterRoleHostVOS, ESClusterRoleHostVO::getRoleClusterId, i -> i);
								esClusterRoleVOS
										//进行节点填充
										.forEach(i ->
												Optional.ofNullable(roleId2ListMap.get(i.getId()))
														.ifPresent(i::setEsClusterRoleHostVO)
										);
								hostVO.setRoleWithNodes(esClusterRoleVOS);
						}
				}
				return Result.buildSucc(hostVOS);
		}
		
		/******************************************private***********************************************/
		private void refreshClusterPhyPluginInfo() {
				for (String clusterName : clusterPhyService.listClusterNames()) {
						try {
								final List<PluginVO> pluginList = getKernelPlugin(
										clusterName);
								CLUSTER_PHY_PLUGIN_CACHE.put(clusterName, pluginList);
								
						} catch (ESOperateException e) {
								LOGGER.warn("class={}||method=refreshClusterPhyPluginInfo||clusterName={}",
										this.getClass().getSimpleName(), clusterName, e);
						}
				}
		}
		
		/**
		 * > 获取集群的内核plugin信息，并转化为PluginVO对象
		 *
		 * @param clusterName 要查询的集群名称
		 * @return PluginVO 对象列表。
		 */
		private List<PluginVO> getKernelPlugin(String clusterName) throws ESOperateException {
				final List<ESResponsePluginInfo> responsePluginInfos = esClusterNodeService.syncGetPlugins(
								clusterName).stream()
						//		 这里不关系节点名称，所以这里可以将其设置未 NULL，方便流去重复
						.peek(i -> i.setComponent(null)).distinct().collect(Collectors.toList());
				return ConvertUtil.list2List(responsePluginInfos,
						PluginVO.class, initKernelPluginList());
		}
		
		/**
		 * > 该函数用于初始化内核插件列表
		 *
		 * @return 消费者 <PluginVO>
		 */
		private static Consumer<PluginVO> initKernelPluginList() {
				return plu -> {
						plu.setPluginType(PluginInfoTypeEnum.KERNEL.getPluginType());
						plu.setStatus(PluginHealthEnum.GREEN.getCode());
						plu.setComponentId(-1);
						// 内核插件列表无 ID
						plu.setId(-1L);
						
				};
		}
}