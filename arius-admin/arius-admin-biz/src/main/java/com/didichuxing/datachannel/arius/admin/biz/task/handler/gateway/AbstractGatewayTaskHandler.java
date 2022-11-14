package com.didichuxing.datachannel.arius.admin.biz.task.handler.gateway;

import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterManager;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskHandler;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.application.TaskService;
import java.util.Date;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO 注意这里暂时为了完成功能先行这么做，等待后续ES各项操作上线后，需要进行对应的代码优化
 * 抽象类网关任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@NoArgsConstructor
public abstract class AbstractGatewayTaskHandler implements OpTaskHandler {
		
		protected final ILog LOGGER = LogFactory.getLog(this.getClass());
		
		@Autowired
		protected OpTaskManager         opTaskManager;
		@Autowired
		protected ComponentService      componentService;
		@Autowired
		protected GatewayClusterManager gatewayClusterManager;
		@Autowired
		protected TaskService           taskService;
		
		
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
				final Result<Integer> submitTaskToOpManagerGetIdRes =
						submitTaskToOpManagerGetId(opTask.getExpandData());
				if (submitTaskToOpManagerGetIdRes.failed()) {
						return Result.buildFrom(submitTaskToOpManagerGetIdRes);
				}
				
				opTask.setBusinessKey(String.valueOf(submitTaskToOpManagerGetIdRes.getData()));
				opTask.setTitle(getTitle(opTask.getExpandData()));
				opTask.setCreateTime(new Date());
				opTask.setUpdateTime(new Date());
				opTask.setStatus(OpTaskStatusEnum.WAITING.getStatus());
				opTask.setDeleteFlag(false);
				opTaskManager.insert(opTask);
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
				final String businessKey = opTask.getBusinessKey();
				// 进行任务执行
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> executeRes =
						taskService.execute(
								Integer.parseInt(businessKey));
				if (executeRes.failed()) {
						return Result.buildFrom(executeRes);
				}
				opTask.setStatus(status);
				opTask.setUpdateTime(new Date());
				opTaskManager.updateTask(opTask);
				return Result.buildSucc();
		}
		
		
		/**
		 * 此函数验证给定参数并返回 Result<Void>。
		 *
		 * @param param 要验证的参数。
		 * @return Result<Void>
		 */
		abstract Result<Void> validatedAddTaskParam(OpTask param);
		
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
		 * 如果找不到子类，它会抛出异常。
		 *
		 * @param opTask@return Result<Void>
		 */
		abstract Result<Void> initParam(OpTask opTask);
		
		
		/**
		 * 此任务的操作类型。
		 *
		 * @return 操作类型。
		 */
		abstract OpTaskTypeEnum operationType();
		
		
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
		protected  abstract String getTitle(String expandData);
		

		/**
		 * > 此函数将字符串转换为内容对象
		 *
		 * @param expandData 从父级传递给子级的数据。
		 * @return 返回类型与参数的类型相同。
		 */
		protected abstract  <T> T convertString2Content(String expandData);
		
}