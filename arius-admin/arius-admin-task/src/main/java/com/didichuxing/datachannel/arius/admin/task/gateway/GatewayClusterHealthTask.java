package com.didichuxing.datachannel.arius.admin.task.gateway;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterManager;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.GatewayHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostStatusEnum;
import com.google.common.collect.Lists;

/**
 * 网关群集运行状况任务
 *
 * @author shizeying
 * @date 2022/12/15
 * @since 0.3.2
 */
@Task(name = "GatewayClusterHealthTask", description = "定时同步 gateway 集群健康状态", cron = "0 0/5 * * * ?", autoRegister = true)
public class GatewayClusterHealthTask implements Job {
	private static final ILog LOGGER = LogFactory.getLog(GatewayClusterHealthTask.class);
		@Autowired
		private              GatewayClusterManager     gatewayClusterManager;
		@Autowired
		private              ComponentService          componentService;
		@Autowired
		private              GatewayClusterNodeManager gatewayClusterNodeManager;
		
		@Override
		public TaskResult execute(JobContext jobContext) throws Exception {
				LOGGER.info("class={}||msg=start", this.getClass().getSimpleName());
				final Result<List<GatewayClusterBriefVO>> listResult = gatewayClusterManager.listBriefInfo();
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<List<Component>> result = componentService.listComponentWithAll();
				if (CollectionUtils.isEmpty(listResult.getData())) {
						return TaskResult.buildSuccess();
				}
				
				final List<GatewayClusterDTO> gatewayClusterDTOS = ConvertUtil.list2List(listResult.getData(),
				                                                                         GatewayClusterDTO.class);
				List<GatewayClusterDTO> needUpdateDTOList = Lists.newArrayList();
				final List<GatewayClusterDTO> containsComponentIdDTOLists = gatewayClusterDTOS.stream().filter(
								i -> Objects.nonNull(i.getComponentId()) && i.getComponentId() > 0).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(result.getData())) {
						
						// 对存在组件 ID 的进行健康状态采集，需要通过 ecm 侧进行判断校验
						final Map<Integer, GatewayClusterDTO> componentId2DtoMap = ConvertUtil.list2Map(containsComponentIdDTOLists,
						                                                                                GatewayClusterDTO::getComponentId,
						                                                                                i -> i);
						
						final Map<Integer, Component> id2ComponentMap = ConvertUtil.list2Map(result.getData(), Component::getId,
						                                                                     i -> i);
						
						// 对存在组件 ID 的 gateway 集群进行健康采集判断
						for (Entry<Integer, GatewayClusterDTO> componentId2GatewayClusterEntry : componentId2DtoMap.entrySet()) {
								final Integer componentId = componentId2GatewayClusterEntry.getKey();
								final GatewayClusterDTO gatewayClusterDTO = componentId2GatewayClusterEntry.getValue();
								final Component component = id2ComponentMap.get(componentId);
								if (Objects.isNull(component)) {
										gatewayClusterDTO.setHealth(GatewayHealthEnum.UNKNOWN.getCode());
										needUpdateDTOList.add(gatewayClusterDTO);
										continue;
								}
								if (CollectionUtils.isEmpty(component.getHostList())){
										gatewayClusterDTO.setHealth(GatewayHealthEnum.UNKNOWN.getCode());
										needUpdateDTOList.add(gatewayClusterDTO);
										continue;
								}
								final List<ComponentHost> componentHosts = component.getHostList();
								// 确定 host 是否都处于在线状态
								if (componentHosts.stream().anyMatch(
												i -> HostStatusEnum.find(i.getStatus()).equals(HostStatusEnum.ON_LINE))) {
										gatewayClusterDTO.setHealth(GatewayHealthEnum.GREEN.getCode());
								} else {
										gatewayClusterDTO.setHealth(GatewayHealthEnum.RED.getCode());
								}
							
								needUpdateDTOList.add(gatewayClusterDTO);
						}
				}
				// 对不存在组件 ID 进行健康采集判断
				final List<GatewayClusterDTO> notContainsComponentIdLists = gatewayClusterDTOS.stream().filter(
								i -> Objects.isNull(i.getComponentId()) || i.getComponentId() <= 0).collect(Collectors.toList());
				
				final List<String> clusterNames = notContainsComponentIdLists.stream().map(GatewayClusterDTO::getClusterName)
								.distinct().collect(Collectors.toList());
				final Result<List<GatewayClusterNodeVO>> gatewayNodesRes = gatewayClusterNodeManager.getByClusterNames(
								clusterNames);
				if (CollectionUtils.isNotEmpty(gatewayNodesRes.getData())) {
						
						final Map<String, List<GatewayClusterNodeVO>> clusterName2ListMap = ConvertUtil.list2MapOfList(
										gatewayNodesRes.getData(), GatewayClusterNodeVO::getClusterName, i -> i);
						for (GatewayClusterDTO gatewayClusterDTO : notContainsComponentIdLists) {
								final String clusterName = gatewayClusterDTO.getClusterName();
								final List<GatewayClusterNodeVO> voList = clusterName2ListMap.get(clusterName);
								// 如果是空的，则属于正常集群
								if (CollectionUtils.isEmpty(voList)) {
										gatewayClusterDTO.setHealth(GatewayHealthEnum.GREEN.getCode());
										needUpdateDTOList.add(gatewayClusterDTO);
										continue;
								}
								// 对上报的节点进行校验，判断上报的节点是否再 30 分钟以内，如果都在，则该节点的信息是 green；否则为 red
								final boolean checkDateIsTimeOut = voList.stream().anyMatch(i -> {
										final Date heartbeatTime = i.getHeartbeatTime();
										final Date current = new Date();
										long diff = current.getTime() - heartbeatTime.getTime();
										TimeUnit time = TimeUnit.MINUTES;
										long diffrence = time.convert(diff, TimeUnit.MINUTES);
										return diffrence <= 30;
										
								});
								if (checkDateIsTimeOut) {
										gatewayClusterDTO.setHealth(GatewayHealthEnum.GREEN.getCode());
								} else {
										gatewayClusterDTO.setHealth(GatewayHealthEnum.RED.getCode());
								}
								needUpdateDTOList.add(gatewayClusterDTO);
						}
				}
				for (GatewayClusterDTO gatewayClusterDTO : needUpdateDTOList) {
						gatewayClusterManager.update(gatewayClusterDTO);
				}
				return TaskResult.buildSuccess();
		}
		
}