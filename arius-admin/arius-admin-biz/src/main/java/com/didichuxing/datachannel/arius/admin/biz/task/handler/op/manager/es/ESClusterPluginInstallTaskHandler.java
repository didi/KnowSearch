package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginInstallContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.plugin.PluginCreateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginInfoTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;


/**
 * es 集群插件安装任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@org.springframework.stereotype.Component("esClusterPluginInstallTaskHandler")
public class ESClusterPluginInstallTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterPluginInstallContent content = convertString2Content(
						param.getExpandData());
				if (Objects.isNull(content.getPluginType())) {
						return Result.buildFail("插件类型不可为空");
				}
				if (Objects.isNull(content.getDependComponentId())) {
						return Result.buildFail("组建依赖 ID 不可为空");
				}
				if (Objects.isNull(content.getPackageId())) {
						return Result.buildFail("组建包 ID 不可为空");
				}
				if (!content.getGroupConfigList().stream().map(GeneralGroupConfigDTO::getHosts).allMatch(
						StringUtils::isNotEmpty)){
						return Result.buildFail("hosts列表不可为空");
				}
				
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> stringResult = componentService.queryComponentNameById(
						content.getDependComponentId());
				if (stringResult.failed()) {
						return Result.buildFrom(stringResult);
				}
				
				return Result.buildSucc();
		}
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_PLUG_INSTALL;
		}
		
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.installComponent(
								ComponentAssembler.toInstallComponent(convertString2Content(expandData)));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterPluginInstallContent content           = convertString2Content(expandData);
				final Integer                     dependComponentId = content.getDependComponentId();
				// 获取依赖安装的主机名称
				final String dependComponentName = componentService.queryComponentNameById(dependComponentId)
				                                                   .getData();
				// 获取组建名称
				final String name = content.getName();
				
				return String.format("%s- 集群名称【%s】- 插件名称【%s】", operationType().getMessage(),
						dependComponentName
						, name);
		}
		
		@Override
		protected ClusterPluginInstallContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						ClusterPluginInstallContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterPluginInstallContent content = convertString2Content(
						opTask.getExpandData());
				
				final Integer dependComponentId = content.getDependComponentId();
				final ClusterPhyVO clusterPhyVO = clusterPhyManager.getOneByComponentId(
						dependComponentId).getData();
				
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_PLUGIN_INSTALL)
						.content(String.format("集群：【%s】安装【%s】：【%s】", clusterPhyVO.getCluster(),
								PluginInfoTypeEnum.find(content.getPluginType()).getDesc(), content.getName()))
						.project(briefVO)
						.bizId(clusterPhyVO.getId())
						.userOperation(opTask.getCreator())
						.build();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				String expandData = opTask.getExpandData();
				ClusterPluginInstallContent content = convertString2Content(expandData);
				//1.通过依赖的组件获取es集群ID
				Integer dependComponentId = content.getDependComponentId();
				Result<Integer> clusterIdRes = clusterPhyManager.getIdByComponentId(dependComponentId);
				if (clusterIdRes.failed()) {
						return Result.buildFrom(clusterIdRes);
				}
				//2. 获取安装包中的版本号
				final Integer packageId = content.getPackageId();
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Package> packageRes = packageService.getPackageById(
								Long.valueOf(packageId));
				if (packageRes.failed()) {
						return Result.buildFrom(packageRes);
				}
				//3.根据名称获取componentId
				com.didiglobal.logi.op.manager.infrastructure.common.Result<Component> componentResult = componentService.queryComponentByName(
								content.getName());
				if (componentResult.failed()){
						return Result.buildFrom(componentResult);
				}
				PluginCreateDTO pluginCreateDTO = PluginCreateDTO.builder().name(content.getName())
								
								
								.clusterId(clusterIdRes.getData()).version(packageRes.getData().getVersion())
								.componentId(componentResult.getData().getId())
								
								//TODO 插件类型未知 需要等
								.pluginType(PluginInfoTypeEnum.find(content.getPluginType()).getPluginType())
								.clusterType(PluginClusterTypeEnum.ES.getClusterType()).build();
				return pluginManager.createWithECM(pluginCreateDTO);
		}
		
		
}