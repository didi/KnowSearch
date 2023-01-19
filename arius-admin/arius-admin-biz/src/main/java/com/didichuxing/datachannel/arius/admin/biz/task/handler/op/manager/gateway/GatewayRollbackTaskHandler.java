package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayRollbackContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;

/**
 * 网关创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@Component("gatewayRollbackTaskHandler")
public class GatewayRollbackTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayRollbackContent content = convertString2Content(param.getExpandData());
				return checkInitRollBackParam(content, OpTaskTypeEnum.GATEWAY_UPGRADE);
		}
		
	
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_ROLLBACK;
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				//TODO 注意这里如果回滚的是IP/PORT/扩缩容 那么需要进行对应的变更
				return Result.buildSucc();
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
			
				return rollback(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final GatewayRollbackContent content = convertString2Content(expandData);
				final String name = componentService.queryComponentNameById(content.getComponentId())
						.getData();
				final Integer taskId = content.getTaskId();
				return String.format("%s【%s】回滚任务ID【%s】",  operationType().getMessage(), name, taskId);
		}
		
		@Override
		protected GatewayRollbackContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, GatewayRollbackContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final GatewayRollbackContent content = convertString2Content(
						opTask.getExpandData());
				final String name = componentService.queryComponentNameById(content.getComponentId())
						.getData();
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.GATEWAY_ROLLBACK)
						.content(name)
						.project(briefVO)
						.userOperation(opTask.getCreator())
						.build();
		}
		
		@Override
		protected Result<Void> initParam(OpTask opTask) {
				final GatewayRollbackContent content = convertString2Content(opTask.getExpandData());
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<List<ComponentGroupConfig>> componentConfigRes = componentService.getComponentConfig(
						content.getComponentId());
				if (componentConfigRes.failed()) {
						return Result.buildFrom(componentConfigRes);
				}
				opTask.setExpandData(
						initRollBackParam(content, componentConfigRes.getData(), OperationEnum.UPGRADE));
				return Result.buildSucc();
		}
}