package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginExpandContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;

;


/**
 * es 集群插件扩容任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@org.springframework.stereotype.Component("esPluginClusterExpandTaskHandler")
public class ESPluginClusterExpandTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterPluginExpandContent content           = convertString2Content(
						param.getExpandData());
				final Integer                    dependComponentId = content.getDependComponentId();
				final Integer                    componentId       = content.getComponentId();
				
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentNameById(
						componentId);
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Component> componentResultRes = componentService.queryComponentById(
						dependComponentId);
				if (componentResultRes.failed()) {
						return Result.buildFail("依赖组建不存在");
				}
				
				return Result.buildSucc();
		}
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_PLUGIN_CLUSTER_EXPAND;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return expand(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterPluginExpandContent gatewayExpandContent = convertString2Content(expandData);
				final String name = componentService.queryComponentNameById(
						gatewayExpandContent.getComponentId()).getData();
					// 获取依赖安装的主机名称
				final String dependComponentName =
						componentService.queryComponentNameById(gatewayExpandContent.getDependComponentId())
				                                                   .getData();
				return String.format("%s-集群【%s】-插件名称【%s】", operationType().getMessage(),
						dependComponentName, name);
		}
		
		@Override
		protected ClusterPluginExpandContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterPluginExpandContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterPluginExpandContent content = convertString2Content(
						opTask.getExpandData());
				final String name = componentService.queryComponentNameById(content.getComponentId())
						.getData();
						final Integer                    dependComponentId = content.getDependComponentId();
					final String dependComponentName =
							componentService.queryComponentNameById(dependComponentId)
						.getData();
				final String hosts = content.getGroupConfigList().stream()
						.map(GeneralGroupConfigDTO::getHosts)
						.distinct()
						.collect(Collectors.joining(","));
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_PLUGIN_EXPAND)
				.content(String.format("集群：【%s】，插件扩容：【%s】，扩容节点：【%s】", dependComponentName,name, hosts))
						.project(briefVO)
						.userOperation(opTask.getCreator())
						.build();
		}
		
		protected String getName(String expandData) {
				final ClusterPluginExpandContent gatewayExpandContent = convertString2Content(expandData);
				return componentService.queryComponentNameById(gatewayExpandContent.getComponentId()).getData();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				return Result.buildSucc();
		}
}