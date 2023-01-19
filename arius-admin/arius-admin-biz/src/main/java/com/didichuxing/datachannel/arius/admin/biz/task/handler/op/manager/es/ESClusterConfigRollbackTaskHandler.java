package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterConfigRollbackContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;

/**
 * es-cluster 配置回滚任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterConfigRollbackTaskHandler")
public class ESClusterConfigRollbackTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterConfigRollbackContent content = convertString2Content(
						param.getExpandData());
				return  checkInitRollBackParam(content, OpTaskTypeEnum.ES_CLUSTER_CONFIG_EDIT);
		}
		
		@Override
		protected Result<Void> initParam(OpTask opTask) {
				final ClusterConfigRollbackContent content = convertString2Content(
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
				return OpTaskTypeEnum.ES_CLUSTER_CONFIG_ROLLBACK;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return rollback(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterConfigRollbackContent content = convertString2Content(
						expandData);
				final Integer componentId = content.getComponentId();
				final String name = componentService.queryComponentNameById(componentId)
				                                    .getData();
				final Integer taskId = content.getTaskId();
				return String.format("%s【%s】回滚任务ID【%s】", operationType().getMessage(), name,taskId);
		}
		
		@Override
		protected ClusterConfigRollbackContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						ClusterConfigRollbackContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterConfigRollbackContent content = convertString2Content(
						opTask.getExpandData());
				final com.didiglobal.logi.op.manager.domain.component.entity.Component component = componentService.queryComponentById(
								content.getComponentId())
						.getData();
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_PLUGIN_ROLLBACK)
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