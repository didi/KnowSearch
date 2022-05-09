package com.didichuxing.datachannel.arius.admin.biz.worktask.impl;

import com.didichuxing.datachannel.arius.admin.biz.worktask.AriusOpTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskHandler;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.AriusOpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.AriusOpTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.AriusOpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.AriusOpTaskPO;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.task.AriusOpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskHandleEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.task.AriusOpTaskDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2020/12/21
 */
@Service
public class AriusOpTaskManagerImpl implements AriusOpTaskManager {
    private static final ILog LOGGER = LogFactory.getLog(AriusOpTaskManagerImpl.class);

    @Autowired
    private AriusOpTaskDAO ariusOpTaskDao;

    @Autowired
    private HandleFactory        handleFactory;

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Override
    public Result<AriusOpTask> addTask(AriusOpTaskDTO ariusOpTaskDTO) {
        if (AriusObjUtils.isNull(ariusOpTaskDTO.getCreator())) {
            return Result.buildParamIllegal("提交人为空");
        }

        AriusOpTaskTypeEnum typeEnum = AriusOpTaskTypeEnum.valueOfType(ariusOpTaskDTO.getTaskType());
        if (AriusOpTaskTypeEnum.UNKNOWN.equals(typeEnum)) {
            return Result.buildNotExist("任务类型不存在");
        }

        if (AriusObjUtils.isNull(ariusUserInfoService.getByDomainAccount(ariusOpTaskDTO.getCreator()))) {
            return Result.buildParamIllegal("提交人非法");
        }

        WorkTaskHandleEnum taskHandleEnum = WorkTaskHandleEnum.valueOfType(ariusOpTaskDTO.getTaskType());

        WorkTaskHandler handler = (WorkTaskHandler) handleFactory.getByHandlerNamePer(taskHandleEnum.getMessage());

        return handler.addTask(ConvertUtil.obj2Obj(ariusOpTaskDTO, AriusOpTask.class));
    }

    @Override
    public boolean existUnClosedTask(Integer key, Integer type) {
        WorkTaskHandleEnum taskHandleEnum = WorkTaskHandleEnum.valueOfType(type);

        WorkTaskHandler handler = (WorkTaskHandler) handleFactory.getByHandlerNamePer(taskHandleEnum.getMessage());
        return handler.existUnClosedTask(String.valueOf(key), type);
    }

    @Override
    public void insert(AriusOpTask task) {
        try {
            AriusOpTaskPO ariusOpTaskPO = ConvertUtil.obj2Obj(task, AriusOpTaskPO.class);
            boolean succ = ariusOpTaskDao.insert(ariusOpTaskPO) > 0;
            if (succ) {
                task.setId(ariusOpTaskPO.getId());
            }
        } catch (Exception e) {
            LOGGER.error("class=DCDRWorkTaskHandler||method=addTask||taskType={}||businessKey={}||errMsg={}",
                task.getTaskType(), task.getBusinessKey(), e.getStackTrace(), e);
        }
    }

    @Override
    public void updateTask(AriusOpTask task) {
        ariusOpTaskDao.update(ConvertUtil.obj2Obj(task, AriusOpTaskPO.class));
    }

    @Override
    public Result<AriusOpTask> getById(Integer id) {
        AriusOpTaskPO ariusOpTaskPO = ariusOpTaskDao.getById(id);
        if (ariusOpTaskPO == null) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(ariusOpTaskPO, AriusOpTask.class));
    }

    @Override
    public Result<List<AriusOpTask>> list() {
        List<AriusOpTaskPO> ariusOpTaskPOS = ariusOpTaskDao.listAll();
        if (ariusOpTaskPOS == null) {
            return Result.buildSucc(Lists.newArrayList());
        }
        return Result.buildSucc(ConvertUtil.list2List(ariusOpTaskPOS, AriusOpTask.class));
    }

    @Override
    public Result<Void> processTask(AriusOpTaskProcessDTO processDTO) {
        if (AriusObjUtils.isNull(processDTO.getTaskId())) {
            return Result.buildParamIllegal("任务id为空");
        }
        AriusOpTaskPO taskPO = ariusOpTaskDao.getById(processDTO.getTaskId());

        AriusOpTaskTypeEnum typeEnum = AriusOpTaskTypeEnum.valueOfType(taskPO.getTaskType());
        if (AriusOpTaskTypeEnum.UNKNOWN.equals(typeEnum)) {
            return Result.buildNotExist("任务类型不存在");
        }

        WorkTaskHandleEnum taskHandleEnum = WorkTaskHandleEnum.valueOfType(taskPO.getTaskType());

        WorkTaskHandler handler = (WorkTaskHandler) handleFactory.getByHandlerNamePer(taskHandleEnum.getMessage());

        return handler.process(ConvertUtil.obj2Obj(taskPO, AriusOpTask.class), processDTO.getTaskProgress(),
            processDTO.getStatus(), processDTO.getExpandData());
    }

    @Override
    public Result<AriusOpTask> getLatestTask(String businessKey, Integer taskType) {
        AriusOpTaskPO ariusOpTaskPO = ariusOpTaskDao.getLatestTask(businessKey, taskType);
        if (ariusOpTaskPO == null) {
            return Result.buildFail(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(ariusOpTaskPO, AriusOpTask.class));
    }

    

    @Override
    public List<AriusOpTask> getPendingTaskByType(Integer taskType) {
        return ConvertUtil.list2List(ariusOpTaskDao.getPendingTaskByType(taskType), AriusOpTask.class);
    }

    @Override
    public List<AriusOpTask> getSuccessTaskByType(Integer taskType) {
        return ConvertUtil.list2List(ariusOpTaskDao.getSuccessTaskByType(taskType), AriusOpTask.class);
    }
}