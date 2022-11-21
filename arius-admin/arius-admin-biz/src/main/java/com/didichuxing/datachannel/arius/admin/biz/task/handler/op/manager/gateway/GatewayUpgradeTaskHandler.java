package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayUpgradeContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import org.springframework.stereotype.Component;

/**
 * 网关创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@Component("gatewayUpgradeTaskHandler")
public class GatewayUpgradeTaskHandler extends AbstractGatewayTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayUpgradeContent content = JSON.parseObject(param.getExpandData(),
						GatewayUpgradeContent.class);
				// 校验 componentId 是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<String> result = componentService.queryComponentNameById(
						content.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				
				return Result.buildSucc();
		}
		
		@Override
		protected Result<Void> initParam(OpTask opTask) {
				return Result.buildSucc();
		}
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_UPGRADE;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return upgrade(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				final GatewayUpgradeContent content = convertString2Content(expandData);
				final String name = componentService.queryComponentNameById(content.getComponentId())
				                                    .getData();
				return String.format("%s【%s】", operationType().getMessage(), name);
		}
		
		@Override
		protected GatewayUpgradeContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, GatewayUpgradeContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				//版本号升级
				final String expandData = opTask.getExpandData();
				final GatewayUpgradeContent gatewayUpgradeContent = convertString2Content(expandData);
				// 获取安装包中的版本号
				final Integer packageId = gatewayUpgradeContent.getPackageId();
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Package> packageRes =
						packageService.getPackageById(
								Long.valueOf(packageId));
				if (packageRes.failed()) {
						return Result.buildFrom(packageRes);
				}
				final Integer componentId = gatewayUpgradeContent.getComponentId();
				
				return gatewayClusterManager.updateVersionWithECM(componentId, packageRes.getData().getVersion());
		}
		

}