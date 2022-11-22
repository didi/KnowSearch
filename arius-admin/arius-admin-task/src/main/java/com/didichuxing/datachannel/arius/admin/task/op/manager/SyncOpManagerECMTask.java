package com.didichuxing.datachannel.arius.admin.task.op.manager;

import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import com.didiglobal.logi.op.manager.application.TaskService;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 同步 op ecm-task
 *
 * @author shizeying
 * @date 2022/11/18
 * @since 0.3.2
 */
@Task(name = "SyncOpManagerECMTask", description = "定时同步 ECM 任务创建状态", cron = "0 0/2 * * * ?", autoRegister = true)
public class SyncOpManagerECMTask implements Job {
		
		private static final Logger        LOGGER = LoggerFactory.getLogger(SyncOpManagerECMTask.class);
		@Autowired
		private              OpTaskManager opTaskManager;
		@Autowired
		private              TaskService   taskService;
		
		@Override
		public TaskResult execute(JobContext jobContext) throws Exception {
				LOGGER.info("class={}||msg=start", this.getClass().getSimpleName());
				List<String> errorMessage= Lists.newArrayList();
				//1. 获取全量的定时任务
				final List<Integer> taskTypes = OpTaskTypeEnum.opManagerTask().stream()
						.map(OpTaskTypeEnum::getType).collect(Collectors.toList());
				final Result<List<OpTask>> taskByTypeList = opTaskManager.getPendingTaskByTypes(taskTypes);
				final List<OpTask>         opTasks        = taskByTypeList.getData();
				if (CollectionUtils.isEmpty(opTasks)) {
						return TaskResult.SUCCESS;
				}
				//2. 获取 op-manager-ids 获取任务状态
				final List<Integer> taskIds = opTasks.stream().map(OpTask::getBusinessKey)
						.map(Integer::parseInt).collect(Collectors.toList());
				final List<com.didiglobal.logi.op.manager.domain.task.entity.Task> tasks =
						taskService.getTaskListByIds(
								taskIds).getData();
				final Map<Integer, com.didiglobal.logi.op.manager.domain.task.entity.Task> id2TaskMap = ConvertUtil.list2Map(
						tasks, com.didiglobal.logi.op.manager.domain.task.entity.Task::getId,
						i -> i);
				
				//2. 提取和 op-manager-ecm 任务相关的任务
				for (OpTask opTask : opTasks) {
						
						OpTaskProcessDTO processDTO = ConvertUtil.obj2Obj(opTask, OpTaskProcessDTO.class);
						processDTO.setTaskId(opTask.getId());
						if (!StringUtils.isNumeric(opTask.getBusinessKey())) {
								LOGGER.warn("任务 ID【{}】中 businessKey：【{}】", opTask.getId(), opTask.getBusinessKey());
								//将错误信息写入日志列表
								errorMessage.add(String.format("任务 ID【%s】中 businessKey：【%s】不正确，任务状态回写失败",
										opTask.getId(), opTask.getBusinessKey()));
								continue;
						}
						final com.didiglobal.logi.op.manager.domain.task.entity.Task task = id2TaskMap.get(
								Integer.parseInt(opTask.getBusinessKey()));
						//3.填充状态
						processDTO.setStatus(OpTaskStatusEnum.valueOfStatusByOpManagerEnum(TaskStatusEnum.find(task.getStatus())).getStatus());
						//4.更新状态
						final Result<Void> result = opTaskManager.processTask(processDTO);
						if (result.failed()){
						LOGGER.warn("任务ID【{}】状态更新失败,原因：【{}】", opTask.getId(), result.getMessage());
						errorMessage.add(String.format("任务ID【%s】状态更新失败,原因：【%s】", opTask.getId(),
								result.getMessage()));
						}
				}
				TaskResult taskResult = CollectionUtils.isEmpty(errorMessage) ? TaskResult.SUCCESS :
				                        new TaskResult(TaskResult.FAIL.getCode(),
						                        String.join(",", errorMessage));
				return taskResult;
		}
}