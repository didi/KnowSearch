package com.didichuxing.datachannel.arius.admin.biz.task.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.TASK;

import com.didichuxing.datachannel.arius.admin.biz.page.TaskPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskHandler;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.OpTaskVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskHandleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.task.OpTaskService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.op.manager.application.TaskService;
import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import com.didiglobal.logi.op.manager.interfaces.assembler.TaskDetailAssembler;
import com.didiglobal.logi.op.manager.interfaces.vo.TaskDetailVO;
import com.didiglobal.logi.security.service.UserService;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2020/12/21
 */
@Service
public class OpTaskManagerImpl implements OpTaskManager {
    private static final ILog    LOGGER = LogFactory.getLog(OpTaskManagerImpl.class);
    private static final String SUCCESS="success";
    @Autowired
    private OpTaskService opTaskService;
    
    @Autowired
    private HandleFactory handleFactory;
    
    @Autowired
    private UserService userService;
    @Autowired
    private TaskService taskService;
    

    @Override
    public Result<OpTask> addTask(OpTaskDTO opTaskDTO, Integer projectId) throws NotFindSubclassException {
        if (AriusObjUtils.isNull(opTaskDTO.getCreator())) {
            return Result.buildParamIllegal("提交人为空");
        }

        OpTaskTypeEnum typeEnum = OpTaskTypeEnum.valueOfType(opTaskDTO.getTaskType());
        if (OpTaskTypeEnum.UNKNOWN.equals(typeEnum)) {
            return Result.buildNotExist("任务类型不存在");
        }

        if (AriusObjUtils.isNull(userService.getUserBriefByUserName(opTaskDTO.getCreator()))) {
            return Result.buildParamIllegal("提交人非法");
        }
        OpTaskHandleEnum taskHandleEnum = OpTaskHandleEnum.valueOfType(opTaskDTO.getTaskType());

        OpTaskHandler handler = (OpTaskHandler) handleFactory.getByHandlerNamePer(taskHandleEnum.getMessage());

        return handler.addTask(ConvertUtil.obj2Obj(opTaskDTO, OpTask.class));
    }

    @Override
    public boolean existUnClosedTask(Integer key, Integer type) throws NotFindSubclassException {
        OpTaskHandleEnum taskHandleEnum = OpTaskHandleEnum.valueOfType(type);

        OpTaskHandler handler = (OpTaskHandler) handleFactory.getByHandlerNamePer(taskHandleEnum.getMessage());
        return handler.existUnClosedTask(String.valueOf(key), type);
    }

    @Override
    public boolean insert(OpTask task) {
        try {
           return opTaskService.insert(task);
        } catch (Exception e) {
            LOGGER.error("class=DCDRWorkTaskHandler||method=addTask||taskType={}||businessKey={}||errMsg={}",
                task.getTaskType(), task.getBusinessKey(), e.getStackTrace(), e);
            return false;
        }
    }

    @Override
    public Boolean updateTask(OpTask task) {
        return opTaskService.update(task);
    }

    @Override
    public Result<OpTask> getById(Integer id) {
        OpTask opTask = opTaskService.getById(id);
        if (opTask == null) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(opTask);
    }

    @Override
    public PaginationResult<OpTaskVO> pageGetTasks(Integer projectId, OpTaskQueryDTO queryDTO) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(TASK.getPageSearchType());
        if (baseHandle instanceof TaskPageSearchHandle) {
            TaskPageSearchHandle handle = (TaskPageSearchHandle) baseHandle;
            return handle.doPage(queryDTO, projectId);
        }
        LOGGER.warn(
                "class=OpTaskManagerImpl||method=pageGetTasks||msg=failed to get the TaskPageSearchHandle");

        return PaginationResult.buildFail("分页获取任务中心信息失败");
    }

    @Override
    public Result<List<OpTask>> list() {
        final List<OpTask> tasks = opTaskService.listAll();
      
        return Result.buildSucc(tasks);
    }
    
    @Override
    public Result<List<OpTask>> getPendingTaskByTypes(List<Integer> taskTypes) {
        return Result.buildSucc(
            ConvertUtil.list2List(opTaskService.getPendingTaskByTypes(taskTypes), OpTask.class));
    }
    
    @Override
    public Result<Void> processTask(OpTaskProcessDTO processDTO) throws NotFindSubclassException {
        if (AriusObjUtils.isNull(processDTO.getTaskId())) {
            return Result.buildParamIllegal("任务id为空");
        }
        OpTask task = opTaskService.getById(processDTO.getTaskId());

        OpTaskTypeEnum typeEnum = OpTaskTypeEnum.valueOfType(task.getTaskType());
        if (OpTaskTypeEnum.UNKNOWN.equals(typeEnum)) {
            return Result.buildNotExist("任务类型不存在");
        }

        OpTaskHandleEnum taskHandleEnum = OpTaskHandleEnum.valueOfType(task.getTaskType());

        OpTaskHandler handler = (OpTaskHandler) handleFactory.getByHandlerNamePer(taskHandleEnum.getMessage());

        return handler.process(task, processDTO.getTaskProgress(),
            processDTO.getStatus(), processDTO.getExpandData());
    }

    @Override
    public Result<OpTask> getLatestTask(String businessKey, Integer taskType) {
        OpTask opTask = opTaskService.getLatestTask(businessKey, taskType);
        if (opTask == null) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(opTask);
    }

    @Override
    public List<OpTask> getPendingTaskByType(Integer taskType) {
       return opTaskService.getPendingTaskByType(taskType);
    }

    @Override
    public List<OpTask> getSuccessTaskByType(Integer taskType) {
       return opTaskService.getSuccessTaskByType(taskType);
    }
    
    @Override
    public Result<Void> execute(Integer id) {
        final OpTask opTask = opTaskService.getById(id);
        Result<Void> result = checkCorrectnessAndStatusTask(opTask);
        if (result.failed()) {
            return result;
        }
        final Integer taskId = Integer.valueOf(opTask.getBusinessKey());
        return Result.buildFromWithData(taskService.execute(taskId));
    }
    
    
    
    @Override
    public Result<Void> operateTask(Integer id, String action) {
        final OpTask opTask = opTaskService.getById(id);
        Result<Void> result = checkCorrectnessAndStatusTask(opTask);
        if (result.failed()){
            return result;
        }
        final Integer taskId = Integer.valueOf(opTask.getBusinessKey());
        final com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> voidResult = taskService.operateTask(
            taskId, action);
        if (voidResult.isSuccess()) {
             Result<Void> processTaskRes = refreshOpTask(
                opTask, taskId);
            if (processTaskRes.failed()) {
                return processTaskRes;
            }
        }
        return Result.buildFromWithData(voidResult);
    }
    
    @Override
    public Result<Void> retryTask(Integer id) {
        final OpTask opTask = opTaskService.getById(id);
        Result<Void> result = checkCorrectnessAndStatusTask(opTask);
        if (result.failed()) {
            return result;
        }
        final Integer taskId = Integer.valueOf(opTask.getBusinessKey());
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> voidResult = taskService.retryTask(taskId);
        if (voidResult.isSuccess()) {
             // 重试后继续执行
            Result<Void> executeRes = execute(id);
            if (executeRes.failed()) {
                return executeRes;
            }
            OpTaskProcessDTO processDTO = ConvertUtil.obj2Obj(opTask, OpTaskProcessDTO.class);
            processDTO.setTaskId(opTask.getId());
            processDTO.setStatus(OpTaskStatusEnum.RUNNING.getStatus());
            try {
                processTask(processDTO);
            } catch (Exception ignore) {}
           
        }
        return Result.buildFromWithData(voidResult);
    }
    
   
    
    @Override
    public Result<Void> operateHost(Integer id, String action, String host, String groupName) {
        final OpTask opTask = opTaskService.getById(id);
        Result<Void> result = checkCorrectnessAndStatusTask(opTask);
        if (result.failed()) {
            return result;
        }
        final Integer taskId = Integer.valueOf(opTask.getBusinessKey());
        final com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> voidResult = taskService.operateHost(
            taskId, action, host, groupName);
        if (voidResult.isSuccess()) {
            Result<Void> processTaskRes = refreshOpTask(
                opTask, taskId);
            if (processTaskRes.failed()) {
                return processTaskRes;
            }
        }
        return Result.buildFromWithData(voidResult);
    }
    
    @Override
    public Result<String> getTaskLog(Integer id, String hostname, String groupName, int type) {
        final OpTask opTask = opTaskService.getById(id);
        Result<Void> result = checkCorrectnessTask(opTask);
        if (result.failed()){
            return Result.buildFrom(result);
        }
        final Integer taskId = Integer.valueOf(opTask.getBusinessKey());
        return Result.buildFromWithData(taskService.getTaskLog(taskId, hostname, type,groupName));
    }
    
    @Override
    public Result<List<TaskDetailVO>> getTaskDetail(Integer id) {
        final OpTask opTask = opTaskService.getById(id);
        Result<Void> result = checkCorrectnessTask(opTask);
        if (result.failed()) {
            return Result.buildFrom(result);
        }
        final Integer taskId = Integer.valueOf(
            opTask.getBusinessKey());
        com.didiglobal.logi.op.manager.infrastructure.common.Result res = taskService.getTaskDetail(
            taskId);
        if (res.failed()) {
            return Result.buildFrom(res);
        }
        return Result.buildSucc(TaskDetailAssembler.toVOList((List<TaskDetail>) res.getData()));
    }
     
     /**
      * > 检查任务是否为 ECM 任务，任务 id 是否正确
      *
      * @param opTask 需要检查的任务对象
      */
     private Result<Void> checkCorrectnessTask(OpTask opTask) {
        if (Objects.isNull(opTask)){
            return Result.buildNotExist("当前任务不存在");
        }
        OpTaskTypeEnum typeEnum = OpTaskTypeEnum.valueOfType(opTask.getTaskType());
        if (!OpTaskTypeEnum.opManagerTask().contains(typeEnum)){
            return Result.buildFail("非ECM任务");
        }
        if (!StringUtils.isNumeric(opTask.getBusinessKey())) {
            return Result.buildNotExist("当前任务id不正确");
        }
        
        return Result.buildSucc();
    }
    
    /**
     * > 检查任务是否正确且未成功执行
     *
     * @param opTask 要执行的任务对象
     * @return 结果<无效>
     */
    private Result<Void> checkCorrectnessAndStatusTask(OpTask opTask) {
        final Result<Void> result = checkCorrectnessTask(opTask);
        if (result.failed()) {
            return result;
        }
        if (StringUtils.equals(SUCCESS, opTask.getStatus())) {
            return Result.buildFail("当前任务执行成功，无需重复执行");
        }
        return Result.buildSucc();
    }
    
    /**
     * > 函数`refreshOpTask`用于更新操作任务的状态
     *
     * @param opTask 需要更新的对象
     * @param taskId 要处理的任务的任务 ID。
     * @return 结果对象。
     */
    private Result<Void> refreshOpTask(OpTask opTask, Integer taskId) {
        // 更新工单中的状态
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Task> taskRes = taskService.getTaskById(
            taskId);
        Integer          status     = taskRes.getData().getStatus();
        OpTaskProcessDTO processDTO = ConvertUtil.obj2Obj(opTask, OpTaskProcessDTO.class);
        processDTO.setTaskId(opTask.getId());
        //3. 填充状态
        processDTO.setStatus(
            OpTaskStatusEnum.valueOfStatusByOpManagerEnum(TaskStatusEnum.find(status)).getStatus());
        try {
            Result<Void> processTaskRes = processTask(processDTO);
            if (processTaskRes.failed()) {
                return processTaskRes;
            }
        } catch (Exception ignore) {
        }
        return Result.buildSucc();
    }
}