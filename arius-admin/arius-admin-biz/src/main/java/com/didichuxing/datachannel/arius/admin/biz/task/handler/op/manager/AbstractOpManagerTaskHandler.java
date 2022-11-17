package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager;

import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskHandler;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.task.op.manager.PluginUninstallContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.op.manager.application.ComponentService;
import com.didiglobal.logi.op.manager.application.TaskService;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import com.didiglobal.logi.op.manager.interfaces.assembler.ComponentAssembler;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneraInstallComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralConfigChangeComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralRestartComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralRollbackComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralScaleComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralUpgradeComponentDTO;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抽象类 op-manager 任务处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
public abstract class AbstractOpManagerTaskHandler implements OpTaskHandler {
		
		@Autowired
		protected OpTaskManager        opTaskManager;
		@Autowired
		protected ComponentService     componentService;
		@Autowired
		protected TaskService          taskService;
		@Autowired
		private   OperateRecordService operateRecordService;
		
		
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
				final boolean insert = opTaskManager.insert(opTask);
				if (insert) {
						final OperateRecord operateRecord = recordCurrentOperationTasks(opTask.getExpandData());
						operateRecordService.save(operateRecord);
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
		protected abstract Result<Void> validatedAddTaskParam(OpTask param);
		
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
				final Integer componentId = convertString2Content(expandData);
				final com.didiglobal.logi.op.manager.infrastructure.common.Result<Integer> result =
						componentService.offLineComponent(componentId);
				if (result.failed()) {
						return Result.buildFrom(result);
				}
				return Result.buildSucc(result.getData());
		}
		
}