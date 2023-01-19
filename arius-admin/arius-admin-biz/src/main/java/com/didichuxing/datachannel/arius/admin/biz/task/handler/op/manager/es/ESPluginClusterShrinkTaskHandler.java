package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginShrinkContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;

/**
 * es plugin集群缩容任务处理程序
 *
 * @author shizeying
 * @date 2022/12/23
 * @since 0.3.2
 */
@org.springframework.stereotype.Component("esPluginClusterShrinkTaskHandler")
public class ESPluginClusterShrinkTaskHandler  extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterPluginShrinkContent content           = convertString2Content(
						param.getExpandData());
				final Integer                    componentId       = content.getComponentId();
				final Integer                    dependComponentId = content.getDependComponentId();
				
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
				return OpTaskTypeEnum.ES_PLUGIN_CLUSTER_SHRINK;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return shrink(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterPluginShrinkContent content = convertString2Content(expandData);
		
				final String name = componentService.queryComponentNameById(content.getComponentId())
				                                    .getData();
					// 获取依赖安装的主机名称
				final String dependComponentName =
						componentService.queryComponentNameById(content.getDependComponentId())
				                                                   .getData();
				
				return String.format("%s-集群【%s】-插件名称【%s】", operationType().getMessage(),dependComponentName,
						name);
		}
		
		@Override
		protected ClusterPluginShrinkContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterPluginShrinkContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterPluginShrinkContent content = convertString2Content(
						opTask.getExpandData());
				final Integer dependComponentId = content.getDependComponentId();
				final String dependComponentName =
						componentService.queryComponentNameById(dependComponentId)
								.getData();
				final String name = componentService.queryComponentNameById(content.getComponentId())
						.getData();
				final String hosts = content.getGroupConfigList().stream()
						.map(GeneralGroupConfigDTO::getHosts)
						.distinct()
						.collect(Collectors.joining(","));
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_PLUGIN_SHRINK)
						.content(
								String.format("集群：【%s】，插件缩容：【%s】，缩容节点：【%s】",  dependComponentName,
										name,hosts))
						.project(briefVO)
						.userOperation(opTask.getCreator())
						.build();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				return Result.buildSucc();
		}
}