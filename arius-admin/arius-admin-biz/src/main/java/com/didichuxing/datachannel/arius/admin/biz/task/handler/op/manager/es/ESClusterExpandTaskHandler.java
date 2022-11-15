package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterExpandContent;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayExpandContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import org.springframework.stereotype.Component;


/**
 * es 集群扩容任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterExpandTaskHandler")
public class ESClusterExpandTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterExpandContent content     = convertString2Content(param.getExpandData());
				final Integer              componentId = content.getComponentId();
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentById(
						componentId);
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				
				return Result.buildSucc();
		}
		
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_EXPAND;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return expand(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final ClusterExpandContent gatewayExpandContent = convertString2Content(expandData);
				final String name = componentService.queryComponentById(
						gatewayExpandContent.getComponentId()).getData();
				return String.format("%s【%s】", operationType().getMessage(), name);
		}
		
		@Override
		protected ClusterExpandContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterExpandContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
		
		protected String getName(String expandData) {
				final GatewayExpandContent gatewayExpandContent = JSON.parseObject(expandData,
						GatewayExpandContent.class);
				return componentService.queryComponentById(gatewayExpandContent.getComponentId()).getData();
		}
}