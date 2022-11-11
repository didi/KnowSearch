package com.didichuxing.datachannel.arius.admin.biz.task.handler.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayConfigContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 网关创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@Component
public class GatewayConfigTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayConfigContent content = convertString2Content(param.getExpandData());
				if (Objects.isNull(content.getComponentId())){
						return Result.buildFail("组建id不能为空");
				}
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
				return OpTaskTypeEnum.GATEWAY_CONFIG_EDIT;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				final GatewayConfigContent                                                 content             = convertString2Content(
						expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> installComponentRes = componentService.configChangeComponent(
						ComponentAssembler.toConfigChangeComponent(content));
				if (installComponentRes.failed()) {
						return Result.buildFrom(installComponentRes);
				}
				return Result.buildSucc(installComponentRes.getData());
		}
		
		@Override
		protected String getTitle(String expandData) {
				final Integer componentId = convertString2Content(expandData).getComponentId();
				final String name = componentService.queryComponentById(componentId)
						.getData();
				return String.format("%s-%s", name, operationType().getMessage());
		}
		
		@Override
		protected GatewayConfigContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						GatewayConfigContent.class);
		}
}