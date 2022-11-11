package com.didichuxing.datachannel.arius.admin.biz.task.handler.gateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson.JSONValidator.Type;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayCreateContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
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
@Component
public class GatewayCreateTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		Result<Void> validatedAddTaskParam(OpTask param) {
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
		Result<Void> initParam(OpTask opTask) {
				return Result.buildSucc();
		}
		
		@Override
		OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_NEW;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				final GatewayCreateContent content = convertString2Content(expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.installComponent(
								ComponentAssembler.toInstallComponent(content));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		
		@Override
		protected String getTitle(String expandData) {
				return String.format("%s-%s", convertString2Content(expandData).getName(),
						operationType().getMessage());
		}
		
		@Override
		protected GatewayCreateContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						GatewayCreateContent.class);
		}
		
		/**
		 * > 检查配置文件中是否存在配置端口
		 * <blockquote><pre>
		 * http.port: 8080
		 * http:
		 *      port: 8080
		 * </pre></blockquote>
		 *
		 * @param fileConfig 配置文件内容
		 * @return 布尔值
		 */
		private boolean checkPort(String fileConfig) {
				final JSONValidator from = JSONValidator.from(fileConfig);
				// 如果传入的 json 是不正确的
				if (!(from.validate() && from.getType().equals(Type.Object))) {
						return true;
				}
				JSONObject jsonObject = JSON.parseObject(fileConfig);
				return jsonObject.values().stream().map(String::valueOf)
						.noneMatch(i -> i.matches("http.port:\\s*\\d*") ||
								i.matches("http:\\\\n\\s*port:\\s*\\d*"));
		}
		
		
}