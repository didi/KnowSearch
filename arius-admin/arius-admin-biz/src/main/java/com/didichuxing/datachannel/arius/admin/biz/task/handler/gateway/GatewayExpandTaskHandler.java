package com.didichuxing.datachannel.arius.admin.biz.task.handler.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayExpandContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import org.springframework.stereotype.Component;

/**
 * 网关创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@Component
public class GatewayExpandTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayExpandContent content     = convertString2Content(param.getExpandData());
				final Integer              componentId = content.getComponentId();
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentById(
						componentId);
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				
				return Result.buildSucc();
		}
		
		@Override
		Result<Void> initParam(OpTask opTask) {
				final GatewayExpandContent content = convertString2Content(opTask.getExpandData());
				content.setType(OperationEnum.EXPAND.getType());
				opTask.setExpandData(JSON.toJSONString(content));
				return Result.buildSucc();
		}
		
		@Override
		OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_EXPAND;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				final GatewayExpandContent content = convertString2Content(expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.scaleComponent(
								ComponentAssembler.toScaleComponent(content));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		protected String getName(String expandData) {
				final GatewayExpandContent gatewayExpandContent = JSON.parseObject(expandData,
						GatewayExpandContent.class);
				return componentService.queryComponentById(gatewayExpandContent.getComponentId()).getData();
		}
		
		@Override
		protected String getTitle(String expandData) {
				final GatewayExpandContent gatewayExpandContent = JSON.parseObject(expandData,
						GatewayExpandContent.class);
				final String name = componentService.queryComponentById(
						gatewayExpandContent.getComponentId()).getData();
				return String.format("%s-%s", name, operationType().getMessage());
		}
		
		@Override
		protected GatewayExpandContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						GatewayExpandContent.class);
		}
}