package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginUpgradeContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * es 集群插件升级任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterPluginUpgradeTaskHandler")
public class ESClusterPluginUpgradeTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterPluginUpgradeContent content = convertString2Content(
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
				return OpTaskTypeEnum.ES_CLUSTER_PLUG_UPGRADE;
		}
		
		@Override
		protected Result<Void> initParam(OpTask opTask) {
				final String                      expandData = opTask.getExpandData();
				final ClusterPluginUpgradeContent content    = convertString2Content(expandData);
				// 获取组建配置信息
				final Integer componentId = content.getComponentId();
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<List<ComponentGroupConfig>> componentConfigRes = componentService.getComponentConfig(
						componentId);
				if (componentConfigRes.failed()) {
						return Result.buildFrom(componentConfigRes);
				}
				final List<ComponentGroupConfig> data = componentConfigRes.getData();
				// 设置配置信息
				content.setGroupConfigList(ConvertUtil.list2List(data, GeneralGroupConfigDTO.class));
				opTask.setExpandData(JSON.toJSONString(content));
				return Result.buildSucc();
		}
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return upgrade(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterPluginUpgradeContent content           = convertString2Content(expandData);
				final Integer                     dependComponentId = content.getDependComponentId();
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
		protected ClusterPluginUpgradeContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterPluginUpgradeContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterPluginUpgradeContent content = convertString2Content(
						opTask.getExpandData());
				
				final Integer dependComponentId = content.getDependComponentId();
				final com.didiglobal.logi.op.manager.domain.component.entity.Component component = componentService.queryComponentById(
								dependComponentId)
						.getData();
				final ClusterPhyVO clusterPhyVO = clusterPhyManager.getOneByComponentId(
						dependComponentId).getData();
				final Integer packageId = component.getPackageId();
				final String beforeVersion = packageService.getPackageById(packageId.longValue()).getData()
						.getVersion();
				final String afterVersion =
						packageService.getPackageById(content.getPackageId().longValue()).getData()
								.getVersion();
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_PLUGIN_UPGRADE)
						.content(String.format("集群：【%s】插件【%s】升级:【%s->%s】", clusterPhyVO.getCluster(),
								component.getName(), beforeVersion, afterVersion))
						.project(briefVO)
						.bizId(clusterPhyVO.getId())
						.userOperation(opTask.getCreator())
						.build();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				String expandData = opTask.getExpandData();
				ClusterPluginUpgradeContent content = convertString2Content(expandData);
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
				return pluginManager.updateVersionWithECM(clusterIdRes.getData(), content.getComponentId(), PluginClusterTypeEnum.ES,
				                                          packageRes.getData().getVersion());
		}
		
		
}