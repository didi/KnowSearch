package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginUninstallContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;

/**
 * es 集群插件卸载任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterPluginUninstallTaskHandler")
public class ESClusterPluginUninstallTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterPluginUninstallContent content = convertString2Content(
						param.getExpandData());
				if (Objects.isNull(content.getComponentId()) || Objects.isNull(
						content.getDependComponentId())) {
						return Result.buildFail("组建 ID 和组建依赖 ID 不能为空");
				}
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> dependComponentRes =
						componentService.queryComponentNameById(
								content.getDependComponentId());
				if (dependComponentRes.failed()) {
						return Result.buildFrom(dependComponentRes);
				}
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> componentRes =
						componentService.queryComponentNameById(
								content.getComponentId());
				if (dependComponentRes.failed()) {
						return Result.buildFrom(componentRes);
				}
				
				return Result.buildSucc();
		}
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_PLUG_UNINSTALL;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return uninstall(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterPluginUninstallContent content           = convertString2Content(expandData);
				final Integer                       dependComponentId = content.getDependComponentId();
				// 获取依赖安装的主机名称
				final String dependComponentName = componentService.queryComponentNameById(dependComponentId)
				                                                   .getData();
				// 获取组建名称
				final String name = componentService.queryComponentNameById(content.getComponentId()).getData();
				
				return String.format("%s- 集群名称【%s】- 插件名称【%s】", operationType().getMessage(),
						dependComponentName
						, name);
		}
		
		@Override
		protected ClusterPluginUninstallContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterPluginUninstallContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterPluginUninstallContent content = convertString2Content(
						opTask.getExpandData());
				
				final Integer dependComponentId = content.getDependComponentId();
				final com.didiglobal.logi.op.manager.domain.component.entity.Component component = componentService.queryComponentById(
								dependComponentId)
						.getData();
				final ClusterPhyVO clusterPhyVO = clusterPhyManager.getOneByComponentId(
						dependComponentId).getData();
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_PLUGIN_UNINSTALL)
						.content(String.format("集群：【%s】插件卸载【%s】", clusterPhyVO.getCluster(),
								component.getName()))
						.project(briefVO)
						.bizId(clusterPhyVO.getId())
						.userOperation(opTask.getCreator())
						.build();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				String expandData = opTask.getExpandData();
				ClusterPluginUninstallContent content = convertString2Content(expandData);
				//根据依赖组件ID获取集群ID
				Result<Integer> clusterIdRes = clusterPhyManager.getIdByComponentId(content.getDependComponentId());
				if (clusterIdRes.failed()){
						return Result.buildFrom(clusterIdRes);
				}
				Integer clusterId = clusterIdRes.getData();
				return pluginManager.uninstallWithECM(clusterId, PluginClusterTypeEnum.ES,content.getComponentId());
		}
		
	
}