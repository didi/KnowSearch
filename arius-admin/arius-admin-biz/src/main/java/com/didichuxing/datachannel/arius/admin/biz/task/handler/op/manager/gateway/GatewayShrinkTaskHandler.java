package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayShrinkContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 网关创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@Component("gatewayShrinkTaskHandler")
public class GatewayShrinkTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayShrinkContent content = convertString2Content(param.getExpandData());
				if (Objects.isNull(content.getComponentId())) {
						return Result.buildFail("组建 ID 不能为空");
				}
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentNameById(
						content.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				// 校验 port 的正确性
				if (content.getGroupConfigList().stream().map(GeneralGroupConfigDTO::getFileConfig)
						.noneMatch(this::checkPort)) {
						return Result.buildFail("配置中端口号不可为空");
				}
				return Result.buildSucc();
		}
		
	
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_SHRINK;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return shrink(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final GatewayShrinkContent shrinkContent = JSON.parseObject(expandData,
						GatewayShrinkContent.class);
				final String name = componentService.queryComponentNameById(
						shrinkContent.getComponentId()).getData();
				return String.format("%s【%s】",  operationType().getMessage(),name);
		}
		
		@Override
		protected GatewayShrinkContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, GatewayShrinkContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
			final String                 expandData           = opTask.getExpandData();
				final GatewayShrinkContent gatewayShrinkContent = convertString2Content(expandData);
				//1. 获取组建 id，并从 gatewayCluster 获取名称
				Result<String> nameRes = gatewayClusterManager.getNameByComponentId(
						gatewayShrinkContent.getComponentId());
				if (nameRes.failed()) {
						return Result.buildFrom(nameRes);
				}
				final String clusterName = nameRes.getData();
				// 将缩容的节点获取到
				final List<TupleTwo<String, Integer>> ipAndPortTuples = convertFGeneralGroupConfigDTO2IpAndPortTuple(
						gatewayShrinkContent.getGroupConfigList());
				final List<GatewayNodeHostDTO> nodes = ipAndPortTuples.stream()
						.map(i -> GatewayNodeHostDTO.builder().port(i.v2())
								.clusterName(clusterName)
								.hostName(i.v1())
								.build())
						.distinct()
						.collect(Collectors.toList());
				// 缩容节点写入
				return gatewayClusterManager.shrinkNodesWithECM(nodes, clusterName );
		}

}