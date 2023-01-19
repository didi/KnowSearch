package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.gateway.GatewayCreateContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.BaseResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayClusterCreateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.gateway.GatewayNodeHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.GatewayHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 网关创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@org.springframework.stereotype.Component("gatewayCreateTaskHandler")
public class GatewayCreateTaskHandler extends AbstractGatewayTaskHandler {
		
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final GatewayCreateContent content = convertString2Content(param.getExpandData());

				// 校验 port 的正确性
				if (content.getGroupConfigList().stream().map(GeneralGroupConfigDTO::getFileConfig)
						.noneMatch(this::checkPort)) {
						return Result.buildFail("配置中端口号不可为空");
				}
				// 校验创建的名称是否在已经存在
				final Result<Boolean> result = gatewayClusterManager.verifyNameUniqueness(
						content.getName());
				if (Boolean.TRUE.equals(result.getData())) {
						return Result.buildFail("集群名称已存在，不可创建同名集群");
				}
				final Optional<Result<Void>> optionalResult = content.getGroupConfigList().stream()
						.map(this::checkGeneralGroupConfigDTO)
						.filter(BaseResult::failed)
						.findFirst();
				return optionalResult.orElseGet(Result::buildSucc);
		}
		
		@Override
		protected Result<Void> initParam(OpTask opTask) {
				return Result.buildSucc();
		}
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.GATEWAY_NEW;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				
				return install(expandData);
		}
		
		
		@Override
		protected String getTitle(String expandData) {
				return String.format("%s【%s】",
						operationType().getMessage(), convertString2Content(expandData).getName());
		}
		
		@Override
		protected GatewayCreateContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData,
						GatewayCreateContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.GATEWAY_CREATE)
						.content(convertString2Content(opTask.getExpandData()).getName())
						.project(briefVO)
						.userOperation(opTask.getCreator())
						.build();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
			final String expandData = opTask.getExpandData();
			final GatewayCreateContent gatewayCreateContent = convertString2Content(expandData);
			// 获取对应组建
			final com.didiglobal.logi.op.manager.infrastructure.common.Result<Component> componentResult = componentService.queryComponentByName(
					gatewayCreateContent.getName());
			if (componentResult.failed()) {
				return Result.buildFrom(componentResult);
			}
			// 获取安装包中的版本号
			final Integer packageId = gatewayCreateContent.getPackageId();
			final com.didiglobal.logi.op.manager.infrastructure.common.Result<Package> packageRes = packageService.getPackageById(
					Long.valueOf(packageId));
			if (packageRes.failed()) {
				return Result.buildFrom(packageRes);
			}
			GatewayClusterCreateDTO gatewayClusterDTO = initGatewayClusterDTO(gatewayCreateContent,
					componentResult.getData(), packageRes.getData());
			return gatewayClusterManager.createWithECM(gatewayClusterDTO, AuthConstant.SUPER_PROJECT_ID, opTask.getCreator());
		}
		
		/**
		 * > 函数用于将网关配置文件中的网关集群配置信息转换为数据库中的网关集群配置信息
		 *
		 * @param gatewayCreateContent 用户传入的参数
		 * @param component            当前正在创建的组件对象。
		 * @param pack                  安装包
		 */
		private GatewayClusterCreateDTO initGatewayClusterDTO(GatewayCreateContent gatewayCreateContent,
				Component component, Package pack) {
				final List<TupleTwo<String, Integer>> ip2PortTuples = convertFGeneralGroupConfigDTO2IpAndPortTuple(gatewayCreateContent.getGroupConfigList());
			
				final List<GatewayNodeHostDTO> nodes = ip2PortTuples.stream()
						.map(i -> GatewayNodeHostDTO.builder().port(i.v2())
								.clusterName(gatewayCreateContent.getName())
								.hostName(i.v1())
								.build()).collect(Collectors.toList());
				return GatewayClusterCreateDTO.builder()
						.clusterName(gatewayCreateContent.getName())
						.health(GatewayHealthEnum.GREEN.getCode())
						.ecmAccess(Boolean.TRUE)
						.memo(gatewayCreateContent.getMemo())
						.componentId(component.getId())
						.version(pack.getVersion())
						.proxyAddress(gatewayCreateContent.getProxyAddress())
						.dataCenter(gatewayCreateContent.getProxyAddress())
						.nodes(nodes)
						.build();
		}
	
}