package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.task;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskLogEnum;
import com.didiglobal.logi.op.manager.interfaces.vo.TaskDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ecm-op-task 控制器
 *
 * @author shizeying
 * @date 2022/11/09
 * @since 0.3.2
 */
@Api(tags = "ECM 任务相关接口 (REST)")
@RestController
@RequestMapping({V3 + "/ecm/op-task"})
@NoArgsConstructor
public class ECMOptaskV3Controller {
		
		@Autowired
		private OpTaskManager opTaskManager;
		
		@PostMapping("/execute/{taskId}")
		@ApiOperation(value = "执行任务")
		public Result<Void> execute(@PathVariable Integer taskId) {
				return opTaskManager.execute(taskId);
		}
		
		@PostMapping("/{action}/{taskId}")
		@ApiOperation(value = "对主任务进行相应的操作")
		public Result<Void> operateTask(@PathVariable String action, @PathVariable Integer taskId) {
				return opTaskManager.operateTask(taskId, action);
		}
		
		@PostMapping("/retry/{taskId}")
		@ApiOperation(value = "重试任务")
		public Result<Void> retryTask(@PathVariable Integer taskId) {
				return opTaskManager.retryTask(taskId);
		}
		
		@PostMapping("/{action}/{taskId}/{host}")
		@ApiOperation(value = "对任务子节点进行相应的操作")
		public Result<Void> operateHost(@PathVariable String action, @PathVariable Integer taskId,
				@PathVariable String host,
				@RequestParam(value = "groupName") String groupName) {
				return opTaskManager.operateHost(taskId, action, host, groupName);
		}
		
		@GetMapping("/log/stdout")
		@ApiOperation(value = "zeus 查看任务执行完成后的标准输出")
		public Result<String> getTaskStdOuts(
				@RequestParam(value = "taskId", required = true) Integer taskId,
				@RequestParam(value = "hostname", required = true) String hostname) {
				return opTaskManager.getTaskLog(taskId, hostname, TaskLogEnum.STDOUT.getType());
		}
		
		@GetMapping("/log/stderr")
		@ApiOperation(value = "zeus 查看任务执行完成后的错误输出")
		public Result<String> getTaskStdErrs(
				@RequestParam(value = "taskId", required = true) Integer taskId,
				@RequestParam(value = "hostname", required = true) String hostname) {
				return opTaskManager.getTaskLog(taskId, hostname, TaskLogEnum.STDERR.getType());
		}
		
		@GetMapping("/detail-info")
		@ApiOperation(value = "通过任务 id 获取任务详情")
		public Result<List<TaskDetailVO>> getTaskDetail(
				@RequestParam(value = "taskId", required = true) Integer taskId) {
			
				return opTaskManager.getTaskDetail(taskId);
		}
		
		
}