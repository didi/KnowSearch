package com.didichuxing.datachannel.arius.admin.biz.worktask.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskHandler;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.WorkTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskHandleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.AriusWorkTaskPO;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.task.AriusWorkTaskDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * @author d06679
 * @date 2020/12/21
 */
@Service
public class WorkTaskManagerImpl implements WorkTaskManager {
    private static final ILog LOGGER = LogFactory.getLog(WorkTaskManagerImpl.class);

    @Autowired
    private AriusWorkTaskDAO ariusWorkTaskDao;

    @Autowired
    private HandleFactory        handleFactory;

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Override
    public Result<WorkTask> addTask(WorkTaskDTO workTaskDTO) {
        if (AriusObjUtils.isNull(workTaskDTO.getCreator())) {
            return Result.buildParamIllegal("提交人为空");
        }

        WorkTaskTypeEnum typeEnum = WorkTaskTypeEnum.valueOfType(workTaskDTO.getTaskType());
        if (WorkTaskTypeEnum.UNKNOWN.equals(typeEnum)) {
            return Result.buildNotExist("任务类型不存在");
        }

        if (AriusObjUtils.isNull(ariusUserInfoService.getByDomainAccount(workTaskDTO.getCreator()))) {
            return Result.buildParamIllegal("提交人非法");
        }

        WorkTaskHandleEnum taskHandleEnum = WorkTaskHandleEnum.valueOfType(workTaskDTO.getTaskType());

        WorkTaskHandler handler = (WorkTaskHandler) handleFactory.getByHandlerNamePer(taskHandleEnum.getMessage());

        return handler.addTask(ConvertUtil.obj2Obj(workTaskDTO, WorkTask.class));
    }

    @Override
    public boolean existUnClosedTask(Integer key, Integer type) {
        WorkTaskHandleEnum taskHandleEnum = WorkTaskHandleEnum.valueOfType(type);

        WorkTaskHandler handler = (WorkTaskHandler) handleFactory.getByHandlerNamePer(taskHandleEnum.getMessage());
        return handler.existUnClosedTask(String.valueOf(key), type);
    }

    @Override
    public void insert(WorkTask task) {
        try {
            AriusWorkTaskPO ariusWorkTaskPO = ConvertUtil.obj2Obj(task, AriusWorkTaskPO.class);
            boolean succ = ariusWorkTaskDao.insert(ariusWorkTaskPO) > 0;
            if (succ) {
                task.setId(ariusWorkTaskPO.getId());
            }
        } catch (Exception e) {
            LOGGER.error("class=DcdrWorkTaskHandler||method=addTask||taskType={}||businessKey={}||errMsg={}",
                task.getTaskType(), task.getBusinessKey(), e.getStackTrace(), e);
        }
    }

    @Override
    public void updateTask(WorkTask task) {
        ariusWorkTaskDao.update(ConvertUtil.obj2Obj(task, AriusWorkTaskPO.class));
    }

    @Override
    public Result<WorkTask> getById(Integer id) {
        AriusWorkTaskPO ariusWorkTaskPO = ariusWorkTaskDao.getById(id);
        if (ariusWorkTaskPO == null) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(ariusWorkTaskPO, WorkTask.class));
    }

    @Override
    public Result<List<WorkTask>> list() {
        List<AriusWorkTaskPO> ariusWorkTaskPOS = ariusWorkTaskDao.listAll();
        if (ariusWorkTaskPOS == null) {
            return Result.buildSucc(Lists.newArrayList());
        }
        return Result.buildSucc(ConvertUtil.list2List(ariusWorkTaskPOS, WorkTask.class));
    }

    @Override
    public Result<Void> processTask(WorkTaskProcessDTO processDTO) {
        if (AriusObjUtils.isNull(processDTO.getTaskId())) {
            return Result.buildParamIllegal("任务id为空");
        }
        AriusWorkTaskPO taskPO = ariusWorkTaskDao.getById(processDTO.getTaskId());

        WorkTaskTypeEnum typeEnum = WorkTaskTypeEnum.valueOfType(taskPO.getTaskType());
        if (WorkTaskTypeEnum.UNKNOWN.equals(typeEnum)) {
            return Result.buildNotExist("任务类型不存在");
        }

        WorkTaskHandleEnum taskHandleEnum = WorkTaskHandleEnum.valueOfType(taskPO.getTaskType());

        WorkTaskHandler handler = (WorkTaskHandler) handleFactory.getByHandlerNamePer(taskHandleEnum.getMessage());

        return handler.process(ConvertUtil.obj2Obj(taskPO, WorkTask.class), processDTO.getTaskProgress(),
            processDTO.getStatus(), processDTO.getExpandData());
    }

    @Override
    public Result<WorkTask> getLatestTask(String businessKey, Integer taskType) {
        AriusWorkTaskPO ariusWorkTaskPO = ariusWorkTaskDao.getLatestTask(businessKey, taskType);
        if (ariusWorkTaskPO == null) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(ariusWorkTaskPO, WorkTask.class));
    }

    @Override
    public WorkTask getPengingTask(String businessKey, Integer taskType) {
        return ConvertUtil.obj2Obj(ariusWorkTaskDao.getPengingTask(businessKey, taskType), WorkTask.class);
    }

    @Override
    public List<WorkTask> getPengingTaskByType(Integer taskType) {
        return ConvertUtil.list2List(ariusWorkTaskDao.getPengingTaskByType(taskType), WorkTask.class);
    }

    @Override
    public List<WorkTask> getSuccessTaskByType(Integer taskType) {
        return ConvertUtil.list2List(ariusWorkTaskDao.getSuccessTaskByType(taskType), WorkTask.class);
    }
}