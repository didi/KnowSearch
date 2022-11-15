package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterPluginInstallContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import java.util.Objects;
import org.springframework.stereotype.Component;


/**
 * es 集群插件安装任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterPluginInstallTaskHandler")
public class ESClusterPluginInstallTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterPluginInstallContent content = convertString2Content(
						param.getExpandData());
				if (Objects.isNull(content.getDependComponentId())) {
						return Result.buildFail("组建依赖 ID 不可为空");
				}
				if (Objects.isNull(content.getPackageId())) {
						return Result.buildFail("组建包 ID 不可为空");
				}
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> stringResult = componentService.queryComponentById(
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
				final String dependComponentName = componentService.queryComponentById(dependComponentId)
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
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
}