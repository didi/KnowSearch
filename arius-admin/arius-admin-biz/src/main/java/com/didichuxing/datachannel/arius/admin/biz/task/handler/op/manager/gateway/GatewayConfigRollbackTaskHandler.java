package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayConfigRollbackContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 网关配置回滚任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("gatewayConfigRollbackTaskHandler")
public class GatewayConfigRollbackTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayConfigRollbackContent content = convertString2Content(
						param.getExpandData());
				if (Objects.isNull(content.getComponentId())) {
						return Result.buildFail("组建 ID 不能为空");
				}
				return Result.buildSucc();
		}
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_CONFIG_ROLLBACK;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return configChange(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final Integer componentId = convertString2Content(expandData).getComponentId();
				final String name = componentService.queryComponentById(componentId)
				                                    .getData();
				return String.format("%s【%s】", operationType().getMessage(), name);
		}
		
		@Override
		protected GatewayConfigRollbackContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, GatewayConfigRollbackContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
}