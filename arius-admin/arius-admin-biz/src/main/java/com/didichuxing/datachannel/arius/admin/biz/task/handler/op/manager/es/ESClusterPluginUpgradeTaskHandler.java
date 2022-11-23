package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginUpgradeContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
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
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
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