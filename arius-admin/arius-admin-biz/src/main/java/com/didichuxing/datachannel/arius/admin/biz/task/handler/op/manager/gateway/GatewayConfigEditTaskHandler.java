package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayConfigContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 网关创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@org.springframework.stereotype.Component("gatewayConfigEditTaskHandler")
public class GatewayConfigEditTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayConfigContent content = convertString2Content(param.getExpandData());
				if (Objects.isNull(content.getComponentId())) {
						return Result.buildFail("组建 id 不能为空");
				}
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Component> result =
						componentService.queryComponentById(
						content.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
					//对比现有配置和端口，是否发生端口变化，当前不支持端口修改
				final List<GeneralGroupConfigDTO> groupConfigList = content.getGroupConfigList();
				if (CollectionUtils.isEmpty(groupConfigList)){
						return Result.buildFail("配置为空");
				}
				final long count = groupConfigList.stream().map(GeneralGroupConfigDTO::getGroupName)
						.count();
				if (count>1){
						return Result.buildFail("配置组的名称不能是重复的");
				}
				final Map<String, String> groupName2DTOPortMap = ConvertUtil.list2Map(
						groupConfigList, GeneralGroupConfigDTO::getGroupName,
						i -> getHttpPort(i.getFileConfig()));
				final Map<String, String> groupName2PortMap = ConvertUtil.list2Map(
						result.getData().getGroupConfigList(),
						ComponentGroupConfig::getGroupName, i -> getHttpPort(i.getFileConfig()));
				if (!groupName2DTOPortMap.keySet().stream()
						.allMatch(groupName2PortMap::containsKey)) {
						return Result.buildFail("当前修改的配置组不存在，请确认后再进行提交");
				}
				// 校验端口号
				for (Entry<String, String> groupName2PortEntry : groupName2DTOPortMap.entrySet()) {
						final String groupName = groupName2PortEntry.getKey();
						final String port      = groupName2PortEntry.getValue();
						final String oldPort   = groupName2PortMap.get(groupName);
						if (!StringUtils.equals(port, oldPort)) {
								return Result.buildFail("配置变更不支持变更端口号");
						}
				}
				return Result.buildSucc();
		}
		
	
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_CONFIG_EDIT;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return configChange(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final Integer componentId = convertString2Content(expandData).getComponentId();
				final String name = componentService.queryComponentNameById(componentId)
				                                    .getData();
				return String.format("%s【%s】", operationType().getMessage(), name);
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				//TODO 后续考虑下如果端口号变更的情况，那么需要怎么做，这里需要补充更新到节点信息中
				return Result.buildSucc();
		}
		
		@Override
		protected GatewayConfigContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						GatewayConfigContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
		
}