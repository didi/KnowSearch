package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginRollbackContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * es 集群回滚任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterPluginRollbackTaskHandler")
public class ESClusterPluginRollbackTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterPluginRollbackContent content = convertString2Content(
						param.getExpandData());
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
				final String name = componentService.queryComponentNameById(componentId)
				                                    .getData();
				return String.format("%s【%s】", operationType().getMessage(), name);
		}
		
		@Override
		protected ClusterPluginRollbackContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterPluginRollbackContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				//TODO 后续考虑下如果端口号变更的情况，那么需要怎么做，这里需要补充更新到节点信息中
				return Result.buildSucc();
		}
	
}