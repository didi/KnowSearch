package com.didichuxing.datachannel.arius.admin.biz.worktask.handler;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskHandler;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskDCDRProgressEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.AbstractTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("dcdrWorkTaskHandler")
public class DcdrWorkTaskHandler implements WorkTaskHandler {
    private static final ILog LOGGER = LogFactory.getLog(DcdrWorkTaskHandler.class);

    @Autowired
    private WorkTaskManager      workTaskManager;

    @Override
    public Result<WorkTask> addTask(WorkTask workTask) {
        if (AriusObjUtils.isNull(workTask.getBusinessKey())) {
            return Result.buildParamIllegal("业务id为空");
        }
        if (existUnClosedTask(workTask.getBusinessKey(), workTask.getTaskType())) {
            return Result.buildParamIllegal(String.format("模版列表[%s]存在未完成的dcdr模板主从切换任务，不允许再次创建",
                    workTask.getBusinessKey()));
        }

        workTask.setCreateTime(new Date());
        workTask.setUpdateTime(new Date());
        workTaskManager.insert(workTask);
        boolean succ = 0 < workTask.getId();
        if (!succ) {
            LOGGER.error(
                "class=DcdrWorkTaskHandler||method=addTask||taskType={}||businessKey={}||errMsg=failed to insert",
                workTask.getTaskType(), workTask.getBusinessKey());
            return Result.buildFail();
        }
        return Result.buildSucc(workTask);
    }

    @Override
    public boolean existUnClosedTask(String key, Integer type) {
        List<WorkTask> pengingTaskList = workTaskManager.getPengingTaskByType(type);
        if (CollectionUtils.isEmpty(pengingTaskList)) { return false; }

        List<String> businessKeyList = pengingTaskList.stream()
                .map(WorkTask::getBusinessKey)
                .collect(Collectors.toList());

        List<String> templateIdListToCreate = ListUtils.string2StrList(key);
        for (String businessKey : businessKeyList) {
            List<String> templateIdListFromDB = ListUtils.string2StrList(businessKey);
            for (String templateIdFromDB : templateIdListFromDB) {
                if (templateIdListToCreate.contains(templateIdFromDB)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Result<Void> process(WorkTask workTask, Integer step, String status, String expandData) {
        Result<WorkTask> result = workTaskManager.getById(workTask.getId());
        if (result.failed()) {
            return Result.buildFrom(result);
        }
        WorkTask updateWorkTask = result.getData();
        DCDRTaskDetail detail = JSON.parseObject(updateWorkTask.getExpandData(), DCDRTaskDetail.class);
        detail.setStatus(status);
        detail.setTaskProgress(step);
        updateWorkTask.setExpandData(JSON.toJSONString(detail));
        if (WorkTaskStatusEnum.FAILED.getStatus().equals(status)
            || step.equals(WorkTaskDCDRProgressEnum.STEP_9.getProgress())) {
            updateWorkTask.setStatus(status);
        }

        workTaskManager.updateTask(updateWorkTask);

        return Result.buildSucc();
    }

    @Override
    public AbstractTaskDetail getTaskDetail(String extensions) {

        return JSON.parseObject(extensions, DCDRTaskDetail.class);
    }

}
