package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginConfigRollbackContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginInfoTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * es-cluster 配置回滚任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterPluginConfigRollbackTaskHandler")
public class ESClusterPluginConfigRollbackTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterPluginConfigRollbackContent content = convertString2Content(
						param.getExpandData());
				if (!PluginInfoTypeEnum.find(content.getPluginType()).equals(PluginInfoTypeEnum.PLATFORM)){
						return Result.buildFail("只有平台插件支持配置变更");
				}
				return  checkInitRollBackParam(content, OpTaskTypeEnum.ES_CLUSTER_PLUG_CONFIG_ROLLBACK);
		}
		
		@Override
		protected Result<Void> initParam(OpTask opTask) {
				final ClusterPluginConfigRollbackContent content = convertString2Content(
						opTask.getExpandData());
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<List<ComponentGroupConfig>> componentConfigRes = componentService.getComponentConfig(
						content.getComponentId());
				if (componentConfigRes.failed()) {
						return Result.buildFrom(componentConfigRes);
				}
				opTask.setExpandData(initRollBackParam(content,componentConfigRes.getData(),
						OperationEnum.CONFIG_CHANGE));
				return Result.buildSucc();
		}
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_PLUG_CONFIG_ROLLBACK;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return rollback(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterPluginConfigRollbackContent content = convertString2Content(
						expandData);
				final Integer componentId = content.getComponentId();
				final String name = componentService.queryComponentNameById(componentId)
				                                    .getData();
				final String clusterName = componentService.queryComponentNameById(
						content.getDependComponentId()).getData();
				return String.format("集群【%s】-%s【%s】",clusterName, operationType().getMessage(), name);
		}
		
		@Override
		protected ClusterPluginConfigRollbackContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						ClusterPluginConfigRollbackContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterPluginConfigRollbackContent content = convertString2Content(
						opTask.getExpandData());
				final com.didiglobal.logi.op.manager.domain.component.entity.Component component = componentService.queryComponentById(
								content.getComponentId())
						.getData();
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_PLUGIN_CONFIG_ROLLBACK)
						.content(
								String.format("%s 配置回滚：回滚任务 ID：【%s】", component.getName(),
										content.getTaskId()))
						.project(briefVO)
						.userOperation(opTask.getCreator())
						.build();
		}
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				return Result.buildSucc();
		}
		
}