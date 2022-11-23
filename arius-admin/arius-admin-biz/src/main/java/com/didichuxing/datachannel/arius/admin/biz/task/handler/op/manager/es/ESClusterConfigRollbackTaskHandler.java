package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterConfigRollbackContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import java.util.Objects;
import org.springframework.stereotype.Component;

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
				if (Objects.isNull(content.getComponentId())) {
						return Result.buildFail("组建 ID 不能为空");
				}
				
				return Result.buildSucc();
		}
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_CONFIG_ROLLBACK;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return configChange(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final Integer componentId = convertString2Content(expandData).getComponentId();
				final String name = componentService.queryComponentNameById(componentId)
				                                    .getData();
				return String.format("%s【%s】", operationType().getMessage(), name);
		}
		
		@Override
		protected ClusterConfigRollbackContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						ClusterConfigRollbackContent.class);
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