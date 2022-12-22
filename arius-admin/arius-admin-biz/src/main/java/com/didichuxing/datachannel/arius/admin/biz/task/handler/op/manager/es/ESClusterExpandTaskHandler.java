package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterExpandContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;


/**
 * es 集群扩容任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterExpandTaskHandler")
public class ESClusterExpandTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterExpandContent content     = convertString2Content(param.getExpandData());
				final Integer              componentId = content.getComponentId();
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentNameById(
						componentId);
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				
				return Result.buildSucc();
		}
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_EXPAND;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return expand(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterExpandContent gatewayExpandContent = convertString2Content(expandData);
				final String name = componentService.queryComponentNameById(
						gatewayExpandContent.getComponentId()).getData();
				return String.format("%s【%s】", operationType().getMessage(), name);
		}
		
		@Override
		protected ClusterExpandContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterExpandContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterExpandContent content = convertString2Content(
						opTask.getExpandData());
				final String name = componentService.queryComponentNameById(content.getComponentId())
						.getData();
				final String hosts = content.getGroupConfigList().stream()
						.map(GeneralGroupConfigDTO::getHosts)
						.distinct()
						.collect(Collectors.joining(","));
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_CAPACITY)
						.content(String.format("集群：【%s】，扩容：【%s】", name, hosts))
						.project(briefVO)
						.userOperation(opTask.getCreator())
						.build();
		}
		
		protected String getName(String expandData) {
				final ClusterExpandContent gatewayExpandContent = convertString2Content(expandData);
				return componentService.queryComponentNameById(gatewayExpandContent.getComponentId()).getData();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				String expandData = opTask.getExpandData();
				ClusterExpandContent content = convertString2Content(expandData);
				//1. 获取组建 id，并从 gatewayCluster 获取名称
				Result<String> nameRes = clusterPhyManager.getNameByComponentId(content.getComponentId());
				if (nameRes.failed()) {
						return Result.buildFrom(nameRes);
				}
				final String clusterName = nameRes.getData();
				// 获取对应组建
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<com.didiglobal.logi.op.manager.domain.component.entity.Component> componentResult = componentService.queryComponentById(
								content.getComponentId());
				if (componentResult.failed()) {
						return Result.buildFrom(componentResult);
				}
				
				// 将扩容的节点获取到
				final List<TupleTwo<String, Integer>> ipAndPortTuples = convertFGeneralGroupConfigDTO2IpAndPortTuple(
								content.getGroupConfigList());
				Map<String, Integer> ip2PortMap = ConvertUtil.list2Map(ipAndPortTuples, TupleTwo::v1, TupleTwo::v2);
				final List<ESClusterRoleHostDTO> nodes = componentResult.getData().getHostList().stream()
								
								.filter(i -> ip2PortMap.containsKey(i.getHost())).map(i -> ESClusterRoleHostDTO.builder().hostname(
																i.getHost()).ip(i.getHost()).cluster(clusterName).port(
																String.valueOf(ip2PortMap.get(i.getHost()))).machineSpec(i.getMachineSpec())
												.status(HostStatusEnum.find(i.getStatus()).equals(HostStatusEnum.ON_LINE)
																        ? ESClusterNodeStatusEnum.ONLINE.getCode()
																        : ESClusterNodeStatusEnum.OFFLINE.getCode()).build()).collect(
												Collectors.toList());
				
				return clusterPhyManager.expandNodesWithECM(nodes, clusterName);
		}
		
		
}