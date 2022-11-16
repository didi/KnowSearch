package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayShrinkContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import java.util.Objects;
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
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentById(
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
				final String name = componentService.queryComponentById(
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
}