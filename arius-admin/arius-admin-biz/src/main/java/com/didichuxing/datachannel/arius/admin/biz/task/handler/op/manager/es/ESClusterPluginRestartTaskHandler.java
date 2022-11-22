package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginRestartContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * es 集群插件重新启动任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterPluginRestartTaskHandler")
public class ESClusterPluginRestartTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterPluginRestartContent content = convertString2Content(
						param.getExpandData());
				if (Objects.isNull(content.getDependComponentId())) {
						return Result.buildFail("组建依赖 ID 不可为空");
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
				return OpTaskTypeEnum.ES_CLUSTER_PLUG_RESTART;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return restart(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterPluginRestartContent content           = convertString2Content(expandData);
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
		protected ClusterPluginRestartContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterPluginRestartContent.class);
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				//TODO 重启可能根本不需要后置处理，带测试后如果需要再进行补齐
				return Result.buildSucc();
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
		
}