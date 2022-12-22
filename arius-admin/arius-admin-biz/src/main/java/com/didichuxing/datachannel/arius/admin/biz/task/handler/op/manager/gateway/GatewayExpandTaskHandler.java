package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayExpandContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 网关扩容任务处理程序
 *
 * @author shizeying
 * @date 2022/11/18
 * @since 0.3.2
 */
@Component("gatewayExpandTaskHandler")
public class GatewayExpandTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayExpandContent content     = convertString2Content(param.getExpandData());
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
				return OpTaskTypeEnum.GATEWAY_EXPAND;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				
				return expand(expandData);
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
			// 扩容就是增加节点
			final String expandData = opTask.getExpandData();
			final GatewayExpandContent content = convertString2Content(expandData);
			//1. 获取组建 id，并从 gatewayCluster 获取名称
			Result<String> nameRes = gatewayClusterManager.getNameByComponentId(content.getComponentId());
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
				final List<String> hosts = ipAndPortTuples.stream().map(TupleTwo::v1).distinct()
						.collect(Collectors.toList());
				final Map<String, String> host2MachineSpecMap = content.getGroupConfigList()
						.stream()
						.map(GeneralGroupConfigDTO::getMachineSpec)
						.filter(StringUtils::isNoneBlank)
						.map(JSON::parseObject)
						.map(i -> hosts.stream().map(host -> Tuples.of(host, i.getString(host)))
								.collect(Collectors.toList()))
						.flatMap(Collection::stream)
						.collect(Collectors.toMap(TupleTwo::v1, TupleTwo::v2));
				final List<GatewayNodeHostDTO> nodes = ipAndPortTuples.stream()
						
						.map(i -> GatewayNodeHostDTO.builder().hostName(
										i.v1()).clusterName(i.v1()).port(i.v2)
								.machineSpec(host2MachineSpecMap.getOrDefault(i.v1, null)).build())
						.collect(Collectors.toList());
			
			// 扩容节点写入
			return gatewayClusterManager.expandNodesWithECM(nodes);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final GatewayExpandContent gatewayExpandContent = JSON.parseObject(expandData,
						GatewayExpandContent.class);
				final String name = componentService.queryComponentNameById(
						gatewayExpandContent.getComponentId()).getData();
				return String.format("%s【%s】",  operationType().getMessage(),name);
		}
		
		@Override
		protected GatewayExpandContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						GatewayExpandContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final GatewayExpandContent content = convertString2Content(
						opTask.getExpandData());
				final String name = componentService.queryComponentNameById(content.getComponentId())
						.getData();
				final String hosts = content.getGroupConfigList().stream()
						.map(GeneralGroupConfigDTO::getHosts)
						.distinct()
						.collect(Collectors.joining(","));
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.GATEWAY_EXPAND)
						.content(String.format("集群：【%s】，扩容：【%s】", name, hosts))
						.project(briefVO)
						.userOperation(opTask.getCreator())
						.build();
		}
		
		protected String getName(String expandData) {
				final GatewayExpandContent gatewayExpandContent = JSON.parseObject(expandData,
						GatewayExpandContent.class);
				return componentService.queryComponentNameById(gatewayExpandContent.getComponentId()).getData();
		}
		
}