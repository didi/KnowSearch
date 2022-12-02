package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayRollbackContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import java.util.List;
import org.springframework.stereotype.Component;

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
				return String.format("%s【%s】",  operationType().getMessage(),name);
		}
		
		@Override
		protected GatewayRollbackContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, GatewayRollbackContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
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