package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayCreateContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 网关创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@Component("gatewayCreateTaskHandler")
public class GatewayCreateTaskHandler extends AbstractGatewayTaskHandler {
		
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayCreateContent content = convertString2Content(param.getExpandData());
				if (StringUtils.isBlank(content.getClusterType())) {
						return Result.buildFail("集群类型不可为空");
				}
				// 校验 port 的正确性
				if (content.getGroupConfigList().stream().map(GeneralGroupConfigDTO::getFileConfig)
						.noneMatch(this::checkPort)) {
						return Result.buildFail("配置中端口号不可为空");
				}
				// 校验创建的名称是否在已经存在
				final Result<Boolean> result = gatewayClusterManager.verifyNameUniqueness(
						content.getName());
				if (Boolean.TRUE.equals(result.getData())) {
						return Result.buildFail("集群名称已存在，不可创建同名集群");
				}
				
				return Result.buildSucc();
		}
		
		@Override
		protected Result<Void> initParam(OpTask opTask) {
				return Result.buildSucc();
		}
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_NEW;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				
				return install(expandData);
		}
		
		
		@Override
		protected String getTitle(String expandData) {
				return String.format("%s【%s】",
						operationType().getMessage(), convertString2Content(expandData).getName());
		}
		
		@Override
		protected GatewayCreateContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						GatewayCreateContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
}