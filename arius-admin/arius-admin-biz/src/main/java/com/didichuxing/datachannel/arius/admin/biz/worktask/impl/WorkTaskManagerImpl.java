package com.didichuxing.datachannel.arius.admin.biz.worktask.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskHandler;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.WorkTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskHandleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.WorkTaskPO;
import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.task.WorkTaskDAO;
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
    private WorkTaskDAO          workTaskDao;

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
            WorkTaskPO workTaskPO = ConvertUtil.obj2Obj(task, WorkTaskPO.class);
            boolean succ = workTaskDao.insert(workTaskPO) > 0;
            if (succ) {
                task.setId(workTaskPO.getId());
            }
        } catch (Exception e) {
            LOGGER.error("class=DcdrWorkTaskHandler||method=addTask||taskType={}||businessKey={}||errMsg={}",
                task.getTaskType(), task.getBusinessKey(), e.getStackTrace(), e);
        }
    }

    @Override
    public void updateTask(WorkTask task) {
        workTaskDao.update(ConvertUtil.obj2Obj(task, WorkTaskPO.class));
    }

    @Override
    public Result<WorkTask> getById(Integer id) {
        WorkTaskPO workTaskPO = workTaskDao.getById(id);
        if (workTaskPO == null) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(workTaskPO, WorkTask.class));
    }

    @Override
    public Result<List<WorkTask>> list() {
        List<WorkTaskPO> workTaskPOS = workTaskDao.listAll();
        if (workTaskPOS == null) {
            return Result.buildSucc(Lists.newArrayList());
        }
        return Result.buildSucc(ConvertUtil.list2List(workTaskPOS, WorkTask.class));
    }

    @Override
    public Result<Void> processTask(WorkTaskProcessDTO processDTO) {
        if (AriusObjUtils.isNull(processDTO.getTaskId())) {
            return Result.buildParamIllegal("任务id为空");
        }
        WorkTaskPO taskPO = workTaskDao.getById(processDTO.getTaskId());

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
        WorkTaskPO workTaskPO = workTaskDao.getLatestTask(businessKey, taskType);
        if (workTaskPO == null) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(workTaskPO, WorkTask.class));
    }

    @Override
    public WorkTask getPengingTask(String businessKey, Integer taskType) {
        return ConvertUtil.obj2Obj(workTaskDao.getPengingTask(businessKey, taskType), WorkTask.class);
    }

    @Override
    public List<WorkTask> getPengingTaskByType(Integer taskType) {
        return ConvertUtil.list2List(workTaskDao.getPengingTaskByType(taskType), WorkTask.class);
    }

    @Override
    public List<WorkTask> getSuccessTaskByType(Integer taskType) {
        return ConvertUtil.list2List(workTaskDao.getSuccessTaskByType(taskType), WorkTask.class);
    }
}
