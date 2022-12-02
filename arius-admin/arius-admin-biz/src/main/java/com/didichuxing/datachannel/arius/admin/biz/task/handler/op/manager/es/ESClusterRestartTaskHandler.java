package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.es.ClusterRestartContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


/**
 * es 集群重新启动任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@Component("esClusterRestartTaskHandler")
public class ESClusterRestartTaskHandler extends AbstractESTaskHandler {
		
		@Override
		protected Result<Void> validatedAddTaskParam(OpTask param) {
				final ClusterRestartContent content = convertString2Content(param.getExpandData());
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
				final String expandData = opTask.getExpandData();
				final ClusterRestartContent content = convertString2Content(expandData);
				//获取组建配置信息
				final Integer componentId = content.getComponentId();
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<List<ComponentGroupConfig>> componentConfigRes = componentService.getComponentConfig(
						componentId);
				if (componentConfigRes.failed()){
						return Result.buildFrom(componentConfigRes);
				}
				final List<ComponentGroupConfig> data = componentConfigRes.getData();
				//设置配置信息
				final List<GeneralGroupConfigDTO> generalGroupConfigDTOS = ConvertUtil.list2List(data,
						GeneralGroupConfigDTO.class);
				//重启的节点
				final String restartHost = content.getHosts();
				final List<String> restartHostList =
						Lists.newArrayList(StringUtils.split(restartHost, ","));
				if (CollectionUtils.isEmpty(restartHostList)) {
						for (GeneralGroupConfigDTO generalGroupConfigDTO : generalGroupConfigDTOS) {
								final String       hosts    = generalGroupConfigDTO.getHosts();
								final List<String> hostList = Lists.newArrayList(StringUtils.split(hosts, ","));
								final String restartHosts =
										restartHostList.stream().filter(hostList::contains)
												.collect(Collectors.joining(","));
								generalGroupConfigDTO.setHosts(restartHosts);
						}
				}
				content.setGroupConfigList(generalGroupConfigDTOS);
				opTask.setExpandData(JSON.toJSONString(content));
				return Result.buildSucc();
		}
		
		@Override
		protected OpTaskTypeEnum operationType() {
				return OpTaskTypeEnum.ES_CLUSTER_RESTART;
		}
		
		@Override
		protected Result<Integer> submitTaskToOpManagerGetId(String expandData) {
				return restart(expandData);
		}
		
		@Override
		protected String getTitle(String expandData) {
				return String.format("%s【%s】", operationType().getMessage(), getName(expandData));
		}
		
		@Override
		protected ClusterRestartContent convertString2Content(String expandData) {
				return JSON.parseObject(expandData, ClusterRestartContent.class);
		}
		
		@Override
		protected OperateRecord recordCurrentOperationTasks(String expandData) {
				return new OperateRecord();
		}
		
		protected String getName(String expandData) {
				final ClusterRestartContent restartContent = convertString2Content(expandData);
				return componentService.queryComponentNameById(restartContent.getComponentId()).getData();
		}
		
		@Override
		protected Result<Void> afterSuccessTaskExecution(OpTask opTask) {
				//TODO 后续考虑下如果端口号变更的情况，那么需要怎么做，这里需要补充更新到节点信息中
				return Result.buildSucc();
		}
}