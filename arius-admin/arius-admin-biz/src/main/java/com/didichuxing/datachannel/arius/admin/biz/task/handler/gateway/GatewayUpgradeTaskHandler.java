package com.didichuxing.datachannel.arius.admin.biz.task.handler.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayUpgradeContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
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
public class GatewayUpgradeTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayUpgradeContent content = JSON.parseObject(param.getExpandData(),
						GatewayUpgradeContent.class);
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentById(
						content.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				
				return Result.buildSucc();
		}
		
		@Override
		Result<Void> initParam(OpTask opTask) {
				return Result.buildSucc();
		}
		
		@Override
		OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_UPGRADE;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				final GatewayUpgradeContent content = JSON.parseObject(expandData,
						GatewayUpgradeContent.class);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.upgradeComponent(ComponentAssembler.toUpgradeComponent(content));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		@Override
		protected String getTitle(String expandData) {
				final GatewayUpgradeContent content = convertString2Content(expandData);
				final String name = componentService.queryComponentById(content.getComponentId())
						.getData();
				return String.format("%s-%s", name, operationType().getMessage());
		}
		
		@Override
		protected GatewayUpgradeContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, GatewayUpgradeContent.class);
		}
}