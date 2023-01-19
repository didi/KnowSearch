package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginRollbackContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import java.util.List;
import java.util.Objects;

/**
 * es 集群回滚任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@org.springframework.stereotype.Component("esClusterPluginRollbackTaskHandler")
public class ESClusterPluginRollbackTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterPluginRollbackContent content = convertString2Content(
						param.getExpandData());
				final Integer componentId = content.getComponentId();
				final Result<PluginVO> pluginVOResultRes = pluginManager.getClusterByComponentId(
						componentId, PluginClusterTypeEnum.ES);
				if (Objects.isNull(pluginVOResultRes.getData())) {
						return Result.buildFail("回滚的插件不存在");
				}
				
				return checkInitRollBackParam(content, OpTaskTypeEnum.ES_CLUSTER_PLUG_UPGRADE);
		}
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_PLUG_ROLLBACK;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return rollback(expandData);
		}
		
		@Override
		protected Result<Void> initParam(OpTask opTask) {
				final ClusterPluginRollbackContent content = convertString2Content(opTask.getExpandData());
					final com.didiglobal.logi.op.manager.infrastructure.common.Result<List<ComponentGroupConfig>> componentConfigRes = componentService.getComponentConfig(
						content.getComponentId());
				if (componentConfigRes.failed()) {
						return Result.buildFrom(componentConfigRes);
				}
				opTask.setExpandData(initRollBackParam(content,componentConfigRes.getData(),
						OperationEnum.UPGRADE));
				return Result.buildSucc();
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterPluginRollbackContent content     = convertString2Content(expandData);
				final Integer                componentId = content.getComponentId();
				final Component data = componentService.queryComponentById(
								componentId)
						.getData();
				final String name = data.getName();
				final Integer taskId = content.getTaskId();
				final Integer clusterId = pluginManager.getClusterByComponentId(
						content.getComponentId(), PluginClusterTypeEnum.ES).getData().getClusterId();
				final String cluster = clusterPhyManager.getOneById(clusterId).getData().getCluster();
				return String.format("%s- 集群名称【%s】- 插件名称【%s】回滚任务 ID【%s】",
						operationType().getMessage(), cluster,
						name,
						taskId);
		}
		
		@Override
		protected ClusterPluginRollbackContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterPluginRollbackContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterPluginRollbackContent content = convertString2Content(
						opTask.getExpandData());
				final com.didiglobal.logi.op.manager.domain.component.entity.Component component = componentService.queryComponentById(
								content.getComponentId()).getData();
				final Result<PluginVO> pluginVOResultRes = pluginManager.getClusterByComponentId(
						content.getComponentId(), PluginClusterTypeEnum.ES);
				return new OperateRecord.Builder().operationTypeEnum(
								OperateTypeEnum.PHYSICAL_CLUSTER_PLUGIN_ROLLBACK).content(
								String.format("%s 插件升级回滚：回滚任务 ID：【%s】", component.getName(),
										content.getTaskId())).project(briefVO)
						.bizId(pluginVOResultRes.getData().getClusterId()).userOperation(opTask.getCreator())
						.build();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				//TODO 后续考虑下如果端口号变更的情况，那么需要怎么做，这里需要补充更新到节点信息中
				return Result.buildSucc();
		}
	
}