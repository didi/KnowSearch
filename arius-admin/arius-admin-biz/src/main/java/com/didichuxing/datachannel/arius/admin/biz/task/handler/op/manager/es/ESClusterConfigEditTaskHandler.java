package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterConfigContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 网关配置编辑
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterConfigEditTaskHandler")
public class ESClusterConfigEditTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterConfigContent content = convertString2Content(param.getExpandData());
				if (Objects.isNull(content.getComponentId())) {
						return Result.buildFail("组建 id 不能为空");
				}
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
				return OpTaskTypeEnum.ES_CLUSTER_CONFIG_EDIT;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return configChange(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final Integer componentId = convertString2Content(expandData).getComponentId();
				final String name = componentService.queryComponentNameById(componentId).getData();
				return String.format("%s【%s】", operationType().getMessage(), name);
		}
		
		@Override
		protected ClusterConfigContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterConfigContent.class);
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