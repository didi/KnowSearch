package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterUpgradeContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import java.util.List;
import org.springframework.stereotype.Component;


/**
 * es 集群升级任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterUpgradeTaskHandler")
public class ESClusterUpgradeTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterUpgradeContent content = convertString2Content(param.getExpandData());
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentNameById(
						content.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				
				return Result.buildSucc();
		}
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_UPGRADE;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return upgrade(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterUpgradeContent content = convertString2Content(expandData);
				final String name = componentService.queryComponentNameById(content.getComponentId())
				                                    .getData();
				return String.format("%s【%s】", operationType().getMessage(), name);
		}
		
		@Override
		protected ClusterUpgradeContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterUpgradeContent.class);
		}
		
		@Override
		protected Result<Void> initParam(OpTask opTask) {
				final String expandData = opTask.getExpandData();
				final ClusterUpgradeContent content = convertString2Content(expandData);
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
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				final ClusterUpgradeContent content = convertString2Content(
						opTask.getExpandData());
				final com.didiglobal.logi.op.manager.domain.component.entity.Component component = componentService.queryComponentById(
						content.getComponentId()).getData();
				final Integer packageId = component.getPackageId();
				final String beforeVersion = packageService.getPackageById(packageId.longValue()).getData()
						.getVersion();
				final String afterVersion =
						packageService.getPackageById(content.getPackageId().longValue()).getData()
								.getVersion();
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_UPGRADE)
						.content(String.format("集群：【%s】，升级：【%s->%s】", component.getName(), beforeVersion,
								afterVersion))
						.project(briefVO)
						.userOperation(opTask.getCreator())
						.build();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				// 版本号升级
				final String expandData = opTask.getExpandData();
				final ClusterUpgradeContent content = convertString2Content(expandData);
				// 获取安装包中的版本号
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Package> packageRes = packageService.getPackageById(
								Long.valueOf(content.getPackageId()));
				if (packageRes.failed()) {
						return Result.buildFrom(packageRes);
				}
				final Integer componentId = content.getComponentId();
				
				return clusterPhyManager.updateVersionWithECM(componentId, packageRes.getData().getVersion());
		}

}