package com.didichuxing.datachannel.arius.admin.biz.task.impl;

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
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.task.OpTaskService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.TASK;

/**
 * @author d06679
 * @date 2020/12/21
 */
@Service
public class OpTaskManagerImpl implements OpTaskManager {
    private static final ILog    LOGGER = LogFactory.getLog(OpTaskManagerImpl.class);

    @Autowired
    private OpTaskService opTaskService;

    @Autowired
    private HandleFactory        handleFactory;

    @Autowired
    private UserService          userService;
    

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
    public void insert(OpTask task) {
        try {
            opTaskService.insert(task);
        } catch (Exception e) {
            LOGGER.error("class=DCDRWorkTaskHandler||method=addTask||taskType={}||businessKey={}||errMsg={}",
                task.getTaskType(), task.getBusinessKey(), e.getStackTrace(), e);
        }
    }

    @Override
    public void updateTask(OpTask task) {
        opTaskService.update(task);
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
}