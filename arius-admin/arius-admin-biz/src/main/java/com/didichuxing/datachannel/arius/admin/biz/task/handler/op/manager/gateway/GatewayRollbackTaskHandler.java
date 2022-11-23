package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayRollbackContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import java.util.Objects;
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
				if (Objects.isNull(content.getTaskId()) || Boolean.FALSE.equals(
						taskService.hasTask(content.getTaskId()).getData())) {
						return Result.buildFail("回滚任务 id 必须存在");
				}
				if (Objects.isNull(content.getComponentId())) {
						return Result.buildFail("组建 id 不能为空");
				}
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentNameById(
						content.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				
				return Result.buildSucc();
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

}