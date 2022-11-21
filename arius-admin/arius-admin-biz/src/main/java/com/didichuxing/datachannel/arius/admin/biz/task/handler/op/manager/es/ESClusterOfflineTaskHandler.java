package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.OfflineContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.PluginClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * es 集群升级任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterOfflineTaskHandler")
public class ESClusterOfflineTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final OfflineContent content = convertString2Content(param.getExpandData());
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentNameById(
						content.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				//根据名称获取集群id，然后检验是否存在插件
				Result<ClusterPhy> clusterByName = clusterPhyManager.getClusterByName(content.getName());
				if (clusterByName.failed() || Objects.isNull(clusterByName.getData())){
						return Result.buildFail("未匹配到需要显现的组件");
				}
				//检查集群是否具有资源
				Result<Void> checkIncludeResouceRes=clusterPhyManager.checkCompleteUnbindResources(clusterByName.getData());
				if (checkIncludeResouceRes.failed()){
						return checkIncludeResouceRes;
				}
				//检查集群是否存在插件未卸载干净
				Result<Void> checkClusterUninstallPluginsRes=
								pluginManager.checkClusterCompleteUninstallPlugins(clusterByName.getData().getId(),
								                                                   PluginClusterTypeEnum.ES);
				if (checkClusterUninstallPluginsRes.failed()){
						return checkClusterUninstallPluginsRes;
				}
				
				return Result.buildSucc();
		}
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_OFFLINE;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return offLine(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final OfflineContent content = convertString2Content(expandData);
			
				return String.format("%s【%s】", operationType().getMessage(), content.getName());
		}
		
		@Override
		protected OfflineContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, OfflineContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				final String expandData = opTask.getExpandData();
				final OfflineContent content = convertString2Content(expandData);
				Result<ClusterPhy> clusterByName = clusterPhyManager.getClusterByName(content.getName());
				ClusterPhy clusterPhy = clusterByName.getData();
				return Result.buildFrom(clusterPhyManager.offlineWithECM(clusterPhy.getId(), opTask.getCreator(),
				                                                         AuthConstant.SUPER_PROJECT_ID));
		}
		
}