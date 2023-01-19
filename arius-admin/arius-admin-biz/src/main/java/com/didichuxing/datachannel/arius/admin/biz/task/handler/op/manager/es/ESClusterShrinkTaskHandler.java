package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterShrinkContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


/**
 * es 集群缩容任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterShrinkTaskHandler")
public class ESClusterShrinkTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterShrinkContent content = convertString2Content(param.getExpandData());
				if (Objects.isNull(content.getComponentId())) {
						return Result.buildFail("组建 ID 不能为空");
				}
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentNameById(
								content.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				// 校验缩容的节点是否被 region 绑定
				Result<String> nameRes = clusterPhyManager.getNameByComponentId(content.getComponentId());
				if (nameRes.failed()) {
						return Result.buildFrom(nameRes);
				}
				final String clusterName = nameRes.getData();
				final List<TupleTwo<String, Integer>> ipAndPortTuples = convertFGeneralGroupConfigDTO2IpAndPortTuple(
								content.getGroupConfigList());
				final List<ESClusterRoleHostDTO> nodes = ipAndPortTuples.stream().map(i -> ESClusterRoleHostDTO.builder().port(
								String.valueOf(i.v2())).cluster(clusterName).hostname(i.v1()).ip(i.v1()).build()).distinct().collect(
								Collectors.toList());
				final Result<Void> voidResult = clusterPhyManager.checkShrinkNodesWhetherToReadWrite(nodes,
						clusterName);
				if (voidResult.failed()) {
						return voidResult;
				}
				return clusterPhyManager.checkShrinkNodesContainsBindRegion(nodes, clusterName);
		}
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_SHRINK;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return shrink(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterShrinkContent content = convertString2Content(expandData);
				final String name = componentService.queryComponentNameById(content.getComponentId())
				                                    .getData();
				final long hostCount = content.getGroupConfigList().stream()
						.map(GeneralGroupConfigDTO::getHosts)
						.flatMap(i -> Arrays.stream(StringUtils.split(i, ",")))
						.distinct()
						.count();

				return String.format("%s:【%s】缩容节点IP个数【%s】", operationType().getMessage(), name,
						hostCount);
		}
		
		@Override
		protected ClusterShrinkContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterShrinkContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterShrinkContent content = convertString2Content(
						opTask.getExpandData());
				final String name = componentService.queryComponentNameById(content.getComponentId())
						.getData();
				final String hosts = content.getGroupConfigList().stream()
						.map(GeneralGroupConfigDTO::getHosts)
						.distinct()
						.collect(Collectors.joining(","));
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_CAPACITY)
						.content(String.format("集群：【%s】，缩容：【%s】", name, hosts))
						.project(briefVO)
						.userOperation(opTask.getCreator())
						.build();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				final String expandData = opTask.getExpandData();
				final ClusterShrinkContent content = convertString2Content(expandData);
				//1. 获取组建 id，并从 gatewayCluster 获取名称
				Result<String> nameRes = clusterPhyManager.getNameByComponentId(content.getComponentId());
				if (nameRes.failed()) {
						return Result.buildFrom(nameRes);
				}
				final String clusterName = nameRes.getData();
				// 将扩容的节点获取到
				final List<TupleTwo<String, Integer>> ipAndPortTuples = convertFGeneralGroupConfigDTO2IpAndPortTuple(
								content.getGroupConfigList());
				final List<ESClusterRoleHostDTO> nodes = ipAndPortTuples.stream().map(i -> ESClusterRoleHostDTO.builder().port(
								String.valueOf(i.v2())).cluster(clusterName).hostname(i.v1()).ip(i.v1()).build())
								.distinct().collect(
								Collectors.toList());
				final Result<Void> voidResult = clusterPhyManager.shrinkNodesUpdateToReadWrite(nodes,
						clusterName);
				if (voidResult.failed()) {
						return voidResult;
				}
				// 缩容节点写入
				return clusterPhyManager.shrinkNodesWithEcm(nodes, clusterName);
		}
	
}