package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterCreateContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterCreateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterTag;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterCreateSourceEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;


/**
 * es集群创建任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@org.springframework.stereotype.Component("esClusterCreateTaskHandler")
public class ESClusterCreateTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterCreateContent content = convertString2Content(param.getExpandData());
				if (Objects.isNull(content.getClusterType())) {
						return Result.buildFail("ES集群类型不可为空");
				}
				// 校验 port 的正确性
				if (content.getGroupConfigList().stream().map(GeneralGroupConfigDTO::getFileConfig)
						.noneMatch(this::checkPort)) {
						return Result.buildFail("配置中端口号不可为空");
				}
				// 校验创建的名称是否在已经存在
				final Result<Boolean> result = clusterPhyManager.verifyNameUniqueness(
						content.getName());
				if (Boolean.TRUE.equals(result.getData())) {
						return Result.buildFail("ES集群名称已存在，不可创建同名集群");
				}
				final List<GeneralGroupConfigDTO> groupConfigList = content.getGroupConfigList();
				if (CollectionUtils.isEmpty(groupConfigList)) {
						return Result.buildFail("配置为空");
				}
				if (CollectionUtils.isEmpty(content.getDefaultGroupNames())) {
						return Result.buildFail("默认的配置组名称未进行选择");
				}
				final List<String> groupNames = groupConfigList.stream()
						.map(GeneralGroupConfigDTO::getGroupName).distinct().collect(
								Collectors.toList());
				if (!new HashSet<>(groupNames).containsAll(content.getDefaultGroupNames())) {
						return Result.buildFail("默认的配置组名称未匹配到");
				}
				
				return Result.buildSucc();
		}
		
	
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_NEW;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
			
				return install(expandData);
		}
		
		
		@Override
		protected String getTitle(String expandData) {
				return String.format("%s【%s】",
						operationType().getMessage(),convertString2Content(expandData).getName()
						);
		}
		
		@Override
		protected ClusterCreateContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterCreateContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(OpTask opTask) {
				final ProjectBriefVO briefVO = projectService.getProjectBriefByProjectId(
						AuthConstant.SUPER_PROJECT_ID);
				return new OperateRecord.Builder()
						.operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_NEW)
						.content(convertString2Content(opTask.getExpandData()).getName())
						.project(briefVO)
						.userOperation(opTask.getCreator())
						.build();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				String expandData = opTask.getExpandData();
				ClusterCreateContent content = convertString2Content(expandData);
				// 获取对应组建
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Component> componentResult = componentService.queryComponentByName(
								content.getName());
				if (componentResult.failed()) {
						return Result.buildFrom(componentResult);
				}
				// 获取安装包中的版本号
				final Integer packageId = content.getPackageId();
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Package> packageRes = packageService.getPackageById(
								Long.valueOf(packageId));
				if (packageRes.failed()) {
						return Result.buildFrom(packageRes);
				}
				ClusterCreateDTO createDTO = initClusterCreateDTO(content, componentResult.getData(), packageRes.getData());
				return clusterPhyManager.createWithECM(createDTO, opTask.getCreator());
		}
		
		private ClusterCreateDTO initClusterCreateDTO(ClusterCreateContent content, Component component, Package pac) {
				//1.转换为k v
				final Map<String, GeneralGroupConfigDTO> groupName2GroupConfigDTOMap = ConvertUtil.list2Map(
						content.getGroupConfigList(), GeneralGroupConfigDTO::getGroupName);
				//2.获取指定的配置
				List<GeneralGroupConfigDTO> configDTOS = content.getDefaultGroupNames().stream()
						.map(groupName2GroupConfigDTOMap::get).collect(
								Collectors.toList());
				if (CollectionUtils.isEmpty(configDTOS)) {
						configDTOS = Collections.singletonList(content.getGroupConfigList().get(0));
				}
				//3.获取指定配置的ip和端口号
				final List<TupleTwo<String, Integer>> ip2PortTuples = convertFGeneralGroupConfigDTO2IpAndPortTuple(
								configDTOS);
				Map<String, Integer> ip2PortMap = ConvertUtil.list2Map(ip2PortTuples, TupleTwo::v1, TupleTwo::v2);
				//4.转换为master节点
				List<ESClusterRoleHostDTO> nodes = component.getHostList().stream().map(
								i -> ESClusterRoleHostDTO.builder().hostname(i.getHost()).ip(i.getHost()).cluster(content.getName())
												.port(String.valueOf(ip2PortMap.get(i.getHost()))).machineSpec(i.getMachineSpec())
												.status(HostStatusEnum.find(i.getStatus()).equals(HostStatusEnum.ON_LINE)
																        ? ESClusterNodeStatusEnum.ONLINE.getCode()
																        : ESClusterNodeStatusEnum.OFFLINE.getCode())
												//默认都设置为master
												.role(ESClusterNodeRoleEnum.MASTER_NODE.getCode())
												.build()).collect(
								Collectors.toList());
				ClusterTag clusterTag = new ClusterTag();
				clusterTag.setCreateSource(ESClusterCreateSourceEnum.ES_NEW.ordinal());
				return ClusterCreateDTO.builder().type(ESClusterTypeEnum.ES_HOST.getCode()).cluster(content.getName())
								.esVersion(pac.getVersion()).roleClusterHosts(nodes).password(
												String.format("%s:%s", content.getUsername(), content.getPassword())).tags(JSON.toJSONString(clusterTag)).dataCenter(
								content.getDataCenter()).componentId(component.getId())
						.ecmAccess(Boolean.TRUE)
						.proxyAddress(content.getProxyAddress())
						.resourceType(content.getClusterType())
						.platformType(content.getPlatformType()).phyClusterDesc(content.getMemo())
								.build();
		}
		
	
}