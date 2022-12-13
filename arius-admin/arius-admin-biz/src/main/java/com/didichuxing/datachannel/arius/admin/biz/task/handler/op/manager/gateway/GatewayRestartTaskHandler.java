package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayRestartContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.stereotype.Component;

/**
 * 网关创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@Component("gatewayRestartTaskHandler")
public class GatewayRestartTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayRestartContent content = convertString2Content(param.getExpandData());
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentNameById(
						content.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				
				return Result.buildSucc();
		}
		
		@Override
		protected Result<Void> initParam(OpTask opTask) {
				final String                expandData = opTask.getExpandData();
				final GatewayRestartContent content    = convertString2Content(expandData);
				// 获取组建配置信息
				final Integer componentId = content.getComponentId();
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<List<ComponentGroupConfig>> componentConfigRes = componentService.getComponentConfig(
						componentId);
				if (componentConfigRes.failed()) {
						return Result.buildFrom(componentConfigRes);
				}
				final List<ComponentGroupConfig> data = componentConfigRes.getData();
				final Map<String, ComponentGroupConfig> groupName2ComMap = ConvertUtil.list2Map(
						data, ComponentGroupConfig::getGroupName);
				
				final List<GeneralGroupConfigDTO> groupConfigList = content.getGroupConfigList();
				final Map<String, GeneralGroupConfigDTO> groupName2ComDTOMap = ConvertUtil.list2Map(
						groupConfigList, GeneralGroupConfigDTO::getGroupName);
				List<GeneralGroupConfigDTO> generalGroupConfigList = Lists.newArrayList();
				for (Entry<String, GeneralGroupConfigDTO> groupConfigDTOEntry : groupName2ComDTOMap.entrySet()) {
						final String               groupName            = groupConfigDTOEntry.getKey();
						final ComponentGroupConfig componentGroupConfig = groupName2ComMap.get(groupName);
						final GeneralGroupConfigDTO generalGroupConfigDTO = ConvertUtil.obj2Obj(
								componentGroupConfig, GeneralGroupConfigDTO.class);
						// 设置 hosts
						generalGroupConfigDTO.setHosts(groupConfigDTOEntry.getValue().getHosts());
						generalGroupConfigList.add(generalGroupConfigDTO);
				}
				content.setGroupConfigList(generalGroupConfigList);
				opTask.setExpandData(JSON.toJSONString(content));
				return Result.buildSucc();
		}
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_RESTART;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
			
			return restart(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
			final GatewayRestartContent restartContent = convertString2Content(expandData);
			final String name = componentService.queryComponentNameById(restartContent.getComponentId()).getData();
			return String.format("%s【%s】", operationType().getMessage(), name);
		}
		
		@Override
		protected GatewayRestartContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, GatewayRestartContent.class);
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				return Result.buildSucc();
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
		
		protected String getName(String expandData) {
				final GatewayRestartContent restartContent = convertString2Content(expandData);
				return componentService.queryComponentNameById(restartContent.getComponentId()).getData();
		}
	
}