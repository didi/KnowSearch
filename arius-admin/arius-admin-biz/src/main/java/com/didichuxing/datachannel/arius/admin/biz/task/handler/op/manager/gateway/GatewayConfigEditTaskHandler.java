package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayConfigContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template.ConfigOperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
				final GatewayConfigContent content     = convertString2Content(expandData);
				final Integer              componentId = content.getComponentId();
				final String name = componentService.queryComponentNameById(componentId).getData();
				final List<String> changeNames = Optional.ofNullable(content.getChangeNames())
						.orElse(Lists.newArrayList());
				String changeName;
				if (changeNames.size() > CHANGE_NAME_SIZE_MAX) {
						changeName = String.format("编辑【%s】等",
								changeNames.stream().limit(CHANGE_NAME_SIZE_MAX).collect(Collectors.joining(",")));
				} else if (CollectionUtils.isNotEmpty(changeNames)) {
						changeName = String.format("编辑【%s】", String.join(",", changeNames));
				} else {
						changeName = "";
				}
				return String.format("%s【%s】%s", operationType().getMessage(), name, changeName);
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				return Result.buildSucc();
		}
		
		@Override
		protected GatewayConfigContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						GatewayConfigContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final GatewayConfigContent content = convertString2Content(
						opTask.getExpandData());
				final Component component = componentService.queryComponentById(content.getComponentId())
						.getData();
				// 有且仅会有一个配置
				final GeneralGroupConfigDTO target = content.getGroupConfigList().get(0);
				final GeneralGroupConfigDTO source = componentService.getComponentConfig(
								content.getComponentId()).getData()
						.stream()
						.filter(i -> i.getGroupName().equals(target.getGroupName()))
						.findFirst()
						.map(i -> ConvertUtil.obj2Obj(i, GeneralGroupConfigDTO.class)).get();
				final Integer id = gatewayClusterManager.getOneByComponentId(
						content.getComponentId()).getData().getId();
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.GATEWAY_CONFIG_EDIT)
						.content(new ConfigOperateRecord(component.getName(), source, target).toString())
						.project(briefVO)
						.bizId(id)
						.userOperation(opTask.getCreator())
						.build();
		}
		
}