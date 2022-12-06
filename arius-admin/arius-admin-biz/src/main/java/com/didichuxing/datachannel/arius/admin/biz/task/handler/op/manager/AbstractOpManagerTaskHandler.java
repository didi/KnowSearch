package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager;

import static java.util.regex.Pattern.compile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.plugin.PluginManager;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskHandler;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.GeneralRollbackComponentContent;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.OfflineContent;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.PluginUninstallContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.OpTaskPO;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.task.OpTaskService;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.application.PackageService;
import com.didiglobal.logi.op.manager.application.TaskService;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneraInstallComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralConfigChangeComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralRestartComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralRollbackComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralScaleComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralUpgradeComponentDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抽象类 op-manager 任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
public abstract class AbstractOpManagerTaskHandler implements OpTaskHandler {
		
		protected final static String           REASON = "reason";
		protected final static String          PATTERN="有重复未完成任务\\[\\d+\\]||有重名未完成任务\\[\\d+\\]";
		@Autowired
		protected OpTaskService        opTaskService;
		@Autowired
		protected ComponentService     componentService;
		@Autowired
		protected TaskService          taskService;
		@Autowired
		private   OperateRecordService operateRecordService;
		@Autowired
		protected PackageService       packageService;
		@Autowired
		protected PluginManager        pluginManager;
		
		
		@Override
		public Result<OpTask> addTask(OpTask opTask) throws NotFindSubclassException {
				// 参数校验
				final Result<Void> result = validatedAddTaskParam(opTask);
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				// 初始化
				final Result<Void> initParamRes = initParam(opTask);
				if (initParamRes.failed()) {
						return Result.buildFrom(initParamRes);
				}
				// 提交一个任务
				final Result<Integer> submitTaskToOpManagerGetIdRes = submitTaskToOpManagerGetId(opTask.getExpandData());
				if (submitTaskToOpManagerGetIdRes.failed()) {
						if (ResultCode.TASK_REPEAT_ERROR.getCode().equals(submitTaskToOpManagerGetIdRes.getCode())) {
								String title = getTitle(opTask.getExpandData());
								//获取返回结果报错的key
								final Pattern compile = compile(PATTERN);
								final Matcher matcher = compile.matcher(submitTaskToOpManagerGetIdRes.getMessage());
								if (matcher.find()) {
										final String  group = matcher.group();
										final Matcher key   = compile("\\d+").matcher(group);
										if (key.find()) {
												OpTaskPO taskPO = opTaskService.getTaskByBusinessKey(
														key.group(0));
												return Result.build(ResultCode.TASK_REPEAT_ERROR.getCode(),
														String.format("有重名未完成任务 [%s]", taskPO.getId()));
										}
										
								}
							
						}
						return Result.buildFrom(submitTaskToOpManagerGetIdRes);
				}
				
				opTask.setBusinessKey(String.valueOf(submitTaskToOpManagerGetIdRes.getData()));
				opTask.setTitle(getTitle(opTask.getExpandData()));
				opTask.setCreateTime(new Date());
				opTask.setUpdateTime(new Date());
				opTask.setStatus(OpTaskStatusEnum.WAITING.getStatus());
				opTask.setDeleteFlag(false);
				final boolean insert = opTaskService.insert(opTask);
				if (insert) {
						final OperateRecord operateRecord = recordCurrentOperationTasks(opTask.getExpandData());
						//operateRecordService.save(operateRecord);
				}
				
				return Result.buildSucc(opTask);
		}
		
		@Override
		public boolean existUnClosedTask(String key, Integer type) {
				return !taskService.tasksToBeAchieved(Integer.valueOf(key)).getData();
		}
		
		@Override
		public Result<Void> process(OpTask opTask, Integer step, String status, String expandData) {
			final Result<Void> result = validatedProcess(opTask.getExpandData());
			if (result.failed()) {
				return Result.buildFrom(result);
			}
			opTask.setStatus(status);
			opTask.setUpdateTime(new Date());
			// 如果任务的后续处理是失败的，则任务始终无法成功
			if (OpTaskStatusEnum.valueOfStatus(status).equals(OpTaskStatusEnum.SUCCESS)) {
				// 任务完成的后置处理
					final Result<Void> afterSuccessTaskExecutionRes = afterSuccessTaskExecution(opTask);
					String expandDataNew = settingTaskStatus(expandData, afterSuccessTaskExecutionRes);
					if (afterSuccessTaskExecutionRes.failed()) {
							// 如果后置操作失败，那么暂停任务即可
							opTask.setStatus(OpTaskStatusEnum.PAUSE.getStatus());
					}
				opTask.setExpandData(expandDataNew);
			}
			final Boolean aBoolean = opTaskService.update(opTask);
			
			return Result.build(aBoolean);
		}
		
		
		/**
		 * 此函数验证给定参数并返回 Result<Void>。
		 *
		 * @param param 要验证的参数。
		 * @return Result<Void>
		 */
		protected abstract Result<Void> validatedAddTaskParam(OpTask param);
		/**
		 * > 此函数将文件配置转换为 IP 地址和端口的元组列表
		 *
		 * @param dtos 配置文件的文件路径
		 * @return 字符串和整数元组的列表。
		 */
		protected abstract List<TupleTwo<String,Integer>> convertFGeneralGroupConfigDTO2IpAndPortTuple(List<GeneralGroupConfigDTO> dtos);
		
		/**
		 * 此函数接受一个字符串作为参数，并返回一个 Result<Void>。
		 *
		 * @param param 要验证的参数。
		 * @return Result<Void>
		 */
		protected Result<Void> validatedProcess(String param) {
				if (AriusObjUtils.isNull(param)) {
						return Result.buildParamIllegal("提交内容为空");
				}
				return Result.buildSucc();
		}
		/**
		 * > 设置任务状态
		 *
		 * @param expandData 执行任务时传入的数据。
		 * @param afterSuccessTaskExecutionRes 任务执行成功后的任务执行结果。
		 * @return 任务失败的原因。
		 */
		protected String settingTaskStatus(String expandData,
				Result<Void> afterSuccessTaskExecutionRes) {
				final JSONObject jsonObject = JSON.parseObject(expandData);
				if (afterSuccessTaskExecutionRes.failed()) {
						jsonObject.put(REASON, afterSuccessTaskExecutionRes.getData());
				} else {
						jsonObject.remove(REASON);
				}
				return JSON.toJSONString(jsonObject);
		}
		
		
		/**
		 * 如果找不到子类，它会抛出异常。
		 *
		 * @param opTask@return Result<Void>
		 */
		protected Result<Void> initParam(OpTask opTask) {
				return Result.buildSucc();
		}
		
		
		/**
		 * 此任务的操作类型。
		 *
		 * @return 操作类型。
		 */
		protected abstract OpTaskTypeEnum operationType();
		
		
		/**
		 * 该功能用于向 OpManager 交任务
		 *
		 * @param expandData 需要扩展的数据。
		 * @return Result<Integer>
		 */
		protected abstract Result<Integer> submitTaskToOpManagerGetId(String expandData);
		
		
		/**
		 * > 该函数返回一个字符串，该字符串是操作类型的名称与操作的名称连接
		 *
		 * @param expandData 传递给 expand 方法的数据。
		 * @return 操作的标题。
		 */
		protected abstract String getTitle(String expandData);
		
		
		/**
		 * > 此函数将字符串转换为内容对象
		 *
		 * @param expandData 从父级传递给子级的数据。
		 * @return 返回类型与参数的类型相同。
		 */
		protected abstract <T> T convertString2Content(String expandData);
		/**
		 * > 该函数在任务执行成功后调用
		 *  例如：创建：扩容：缩容，下线，删除等任务
		 * @param opTask 已执行的任务对象。
		 * @return Result<Void>
		 */
		protected abstract Result<Void>  afterSuccessTaskExecution(OpTask opTask);
		
		
		/**
		 * > 检查配置文件中是否存在配置端口
		 * <blockquote><pre>
		 * http.port: 8080
		 * http:
		 *      port: 8080
		 * </pre></blockquote>
		 *
		 * @param fileConfig 配置文件内容
		 * @return 布尔值
		 */
		protected abstract boolean checkPort(String fileConfig);
		
		/**
		 * > 记录当前的操作任务
		 *
		 * @param expandData 需要记录的数据，比如用户当前操作，用户当前操作，用户当前操作，用户当前操作，用户当前操作，用户当前操作 user，用户当前的操作，
		 * @return 一个 OperateRecord 对象。
		 */
		protected abstract OperateRecord recordCurrentOperationTasks(String expandData);
		
		
		/**
		 * > 该函数用于安装一个组件
		 *
		 * @param expandData 需要展开的数据，JSON 字符串。
		 * @return 结果对象。
		 */
		protected Result<Integer> install(String expandData) {
				final GeneraInstallComponentDTO dto =
						(GeneraInstallComponentDTO) convertString2Content(expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.installComponent(
								ComponentAssembler.toInstallComponent(dto));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		/**
		 * > 该函数用于扩容一个组件的实例数
		 *
		 * @param expandData 需要扩展的数据。
		 * @return 结果对象。
		 */
		protected Result<Integer> expand(String expandData) {
				final GeneralScaleComponentDTO dto = (GeneralScaleComponentDTO) convertString2Content(
						expandData);
				dto.setType(OperationEnum.EXPAND.getType());
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.scaleComponent(
								ComponentAssembler.toScaleComponent(dto));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		/**
		 * > 该函数用于缩容组件的实例数
		 *
		 * @param expandData 需要扩展的数据。
		 * @return 结果对象。
		 */
		protected Result<Integer> shrink(String expandData) {
				final GeneralScaleComponentDTO dto = convertString2Content(expandData);
				dto.setType(OperationEnum.SHRINK.getType());
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.scaleComponent(
								ComponentAssembler.toScaleComponent(dto));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		/**
		 * > 该函数用于更改组件的配置
		 *
		 * @param expandData 需要处理的数据。
		 * @return 结果对象。
		 */
		protected Result<Integer> configChange(String expandData) {
				final GeneralConfigChangeComponentDTO dto = convertString2Content(expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.configChangeComponent(ComponentAssembler.toConfigChangeComponent(dto));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		/**
		 * > 该函数用于重启一个组件
		 *
		 * @param expandData 需要传递给服务器的数据。
		 * @return 结果对象。
		 */
		protected Result<Integer> restart(String expandData) {
				final GeneralRestartComponentDTO dto = convertString2Content(expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.restartComponent(ComponentAssembler.toRestartComponent(dto));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
	
		/**
		 * > 根据指定参数升级组件
		 *
		 * @param expandData 需要传递给服务器的数据。
		 * @return 结果对象。
		 */
		protected Result<Integer> upgrade(String expandData) {
				final GeneralUpgradeComponentDTO dto = convertString2Content(expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.upgradeComponent(ComponentAssembler.toUpgradeComponent(dto));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		/**
		 * > 该函数用于回滚组件
		 *
		 * @param expandData 需要传递给回滚方法的数据。
		 * @return 结果对象。
		 */
		protected Result<Integer> rollback(String expandData) {
				final GeneralRollbackComponentDTO dto = convertString2Content(expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.rollbackComponent(ComponentAssembler.toRollbackComponent(dto));
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		/**
		 * > 该函数用于卸载一个组件
		 *
		 * @param expandData 需要传递给插件的数据。
		 * @return 卸载组件的结果。
		 */
		protected Result<Integer> uninstall(String expandData) {
				final PluginUninstallContent dto = convertString2Content(expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.uninstallComponent(dto.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		/**
		 * > 该函数用于离线组件
		 *
		 * @param expandData 需要传递给业务逻辑的数据。
		 * @return 结果对象。
		 */
		protected Result<Integer> offLine(String expandData) {
				final OfflineContent content = convertString2Content(expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.offLineComponent(content.getComponentId());
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
		/**
		 * > 该函数用于初始化组件的回滚参数
		 *
		 * @param content 回滚消息的内容，即发送给消费者的消息内容。
		 * @param data 要回滚的数据
		 * @param operationEnum 操作的类型，如增加、删除、修改等。
		 * @return 一个 JSON 字符串。
		 */
		protected <T extends GeneralRollbackComponentContent> String initRollBackParam(T content,
				List<ComponentGroupConfig> data, OperationEnum operationEnum) {
					final OpTask opTask = opTaskService.getById(content.getTaskId());
				final List<GeneralGroupConfigDTO> generalGroupConfigDTOS = ConvertUtil.list2List(data,
						GeneralGroupConfigDTO.class);
			
				// 设置类型
				content.setType(operationEnum.getType());
				content.setTaskId(Integer.parseInt(opTask.getBusinessKey()));
				//进行ip过滤，找出回滚失败的任务节点进行填充
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<List<TaskDetail>> taskDetailRes = taskService.getTaskDetail(
						Integer.parseInt(opTask.getBusinessKey()));
				Map<String,List<String>> groupName2HostsMap= Maps.newHashMap();
				//过滤出指定的host失败列表
				if (taskDetailRes.isSuccess()){
						for (TaskDetail taskDetail : taskDetailRes.getData()) {
								final String groupName = taskDetail.getGroupName();
								final String host      = taskDetail.getHost();
								final Integer status   = taskDetail.getStatus();
								if (TaskStatusEnum.find(status).equals(TaskStatusEnum.FAILED)){
										if (groupName2HostsMap.containsKey(groupName)) {
												groupName2HostsMap.get(groupName).add(host);
										}else {
												groupName2HostsMap.put(groupName, Lists.newArrayList(host));
										}
								}
						}
				}
				//重新设置host,只会回滚失败的任务
				for (GeneralGroupConfigDTO generalGroupConfigDTO : generalGroupConfigDTOS) {
						final List<String> hosts = groupName2HostsMap.get(
								generalGroupConfigDTO.getGroupName());
						if (CollectionUtils.isNotEmpty(hosts)) {
								generalGroupConfigDTO.setHosts(String.join(",", hosts));
						}
				}
					content.setGroupConfigList(generalGroupConfigDTOS);
				return JSON.toJSONString(content);
		}
		
		
		protected <T extends GeneralRollbackComponentContent> Result<Void> checkInitRollBackParam(
				T content,OpTaskTypeEnum opTaskTypeEnum) {
				if (Objects.isNull(content.getComponentId())) {
						return Result.buildFail("组建 ID 不能为空");
				}
				if (Objects.isNull(content.getTaskId())) {
						return Result.buildFail("回滚任务 ID 不能为空");
				}
				// 校验 taskID 是否在 op-task 表中
				final OpTask opTask = opTaskService.getById(content.getTaskId());
				if (Objects.isNull(opTask)) {
						return Result.buildFail("taskID 不存在，无法执行回滚任务");
				}
				// 判断 task 任务是否为集群升级
				final Integer taskType = opTask.getTaskType();
				if (!Objects.equals(OpTaskTypeEnum.valueOfType(taskType),
						opTaskTypeEnum)) {
						switch (opTaskTypeEnum) {
								case ES_CLUSTER_UPGRADE:
								case ES_CLUSTER_PLUG_UPGRADE:
								case GATEWAY_UPGRADE:
										return Result.buildFail("只支持升级类型的回滚任务");
								case ES_CLUSTER_CONFIG_EDIT:
								case GATEWAY_CONFIG_EDIT:
										return Result.buildFail("只支持配置变更的回滚任务");
								default:
										return Result.buildFail("当前操作类型不支持回滚任务");
						}
				}
				// 判断 task 任务是否存在
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Task> taskRes = taskService.getTaskById(
						Integer.parseInt(opTask.getBusinessKey()));
				if (taskRes.failed()) {
						return Result.buildFrom(taskRes);
				}
				return Result.buildSucc();
		}
		
}