package com.didichuxing.datachannel.arius.admin.biz.plugin;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESResponsePluginInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.plugin.PluginCreateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.plugin.PluginInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginInfoTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusScheduleThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.plugin.PluginInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 插件对应能力
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component
public class PluginManagerImpl implements PluginManager {
		
		private static final ILog                          LOGGER                   = LogFactory.getLog(
				PluginManagerImpl.class);
		@Autowired
		private              PluginInfoService             pluginInfoService;
		@Autowired
		private              ClusterPhyService             clusterPhyService;
		@Autowired
		private              ESClusterNodeService          esClusterNodeService;
		@Autowired
		private              AriusScheduleThreadPool       ariusScheduleThreadPool;
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
				
				List<PluginInfoPO> pluginList = pluginInfoService.listByClusterId(clusterPhyId,
						PluginClusterTypeEnum.ES);
				
				final ClusterPhy cluster = clusterPhyService.getClusterById(clusterPhyId);
				// 获取内核的插件列表
				final Result<List<PluginVO>> listResult = listESKernelPluginCache(cluster.getCluster());
				final List<PluginVO>         list       = ConvertUtil.list2List(pluginList, PluginVO.class);
				list.addAll(listResult.getData());
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