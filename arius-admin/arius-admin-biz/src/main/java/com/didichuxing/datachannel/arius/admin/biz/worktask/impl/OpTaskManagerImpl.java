package com.didichuxing.datachannel.arius.admin.biz.worktask.impl;

import com.didichuxing.datachannel.arius.admin.biz.worktask.OpTaskHandler;
import com.didichuxing.datachannel.arius.admin.biz.worktask.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.OpTaskPO;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskHandleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.task.OpTaskDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.UserService;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2020/12/21
 */
@Service
public class OpTaskManagerImpl implements OpTaskManager {
    private static final ILog LOGGER = LogFactory.getLog(OpTaskManagerImpl.class);

    @Autowired
    private OpTaskDAO opTaskDao;

    @Autowired
    private HandleFactory        handleFactory;

    @Autowired
    private UserService  userService;

    @Override
    public Result<OpTask> addTask(OpTaskDTO opTaskDTO) throws NotFindSubclassException {
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
            OpTaskPO opTaskPO = ConvertUtil.obj2Obj(task, OpTaskPO.class);
            boolean succ = opTaskDao.insert(opTaskPO) > 0;
            if (succ) {
                task.setId(opTaskPO.getId());
            }
        } catch (Exception e) {
            LOGGER.error("class=DCDRWorkTaskHandler||method=addTask||taskType={}||businessKey={}||errMsg={}",
                task.getTaskType(), task.getBusinessKey(), e.getStackTrace(), e);
        }
    }

    @Override
    public void updateTask(OpTask task) {
        opTaskDao.update(ConvertUtil.obj2Obj(task, OpTaskPO.class));
    }

    @Override
    public Result<OpTask> getById(Integer id) {
        OpTaskPO opTaskPO = opTaskDao.getById(id);
        if (opTaskPO == null) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(opTaskPO, OpTask.class));
    }

    @Override
    public Result<List<OpTask>> list() {
        List<OpTaskPO> opTasks = opTaskDao.listAll();
        if (opTasks == null) {
            return Result.buildSucc(Lists.newArrayList());
        }
        return Result.buildSucc(ConvertUtil.list2List(opTasks, OpTask.class));
    }

    @Override
    public Result<Void> processTask(OpTaskProcessDTO processDTO) throws NotFindSubclassException {
        if (AriusObjUtils.isNull(processDTO.getTaskId())) {
            return Result.buildParamIllegal("任务id为空");
        }
        OpTaskPO taskPO = opTaskDao.getById(processDTO.getTaskId());

        OpTaskTypeEnum typeEnum = OpTaskTypeEnum.valueOfType(taskPO.getTaskType());
        if (OpTaskTypeEnum.UNKNOWN.equals(typeEnum)) {
            return Result.buildNotExist("任务类型不存在");
        }

        OpTaskHandleEnum taskHandleEnum = OpTaskHandleEnum.valueOfType(taskPO.getTaskType());

        OpTaskHandler handler = (OpTaskHandler) handleFactory.getByHandlerNamePer(taskHandleEnum.getMessage());

        return handler.process(ConvertUtil.obj2Obj(taskPO, OpTask.class), processDTO.getTaskProgress(),
            processDTO.getStatus(), processDTO.getExpandData());
    }

    @Override
    public Result<OpTask> getLatestTask(String businessKey, Integer taskType) {
        OpTaskPO opTaskPO = opTaskDao.getLatestTask(businessKey, taskType);
        if (opTaskPO == null) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(opTaskPO, OpTask.class));
    }

    

    @Override
    public List<OpTask> getPendingTaskByType(Integer taskType) {
        return ConvertUtil.list2List(opTaskDao.getPendingTaskByType(taskType), OpTask.class);
    }

    @Override
    public List<OpTask> getSuccessTaskByType(Integer taskType) {
        return ConvertUtil.list2List(opTaskDao.getSuccessTaskByType(taskType), OpTask.class);
    }
}