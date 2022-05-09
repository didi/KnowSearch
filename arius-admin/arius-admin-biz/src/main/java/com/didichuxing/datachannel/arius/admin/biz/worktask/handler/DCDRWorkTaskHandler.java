package com.didichuxing.datachannel.arius.admin.biz.worktask.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.worktask.AriusOpTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskHandler;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.AriusOpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskDCDRProgressEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.WorkTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("dcdrWorkTaskHandler")
    public class DCDRWorkTaskHandler implements WorkTaskHandler {
    private static final ILog LOGGER = LogFactory.getLog(DCDRWorkTaskHandler.class);

    @Autowired
    private AriusOpTaskManager ariusOpTaskManager;

    @Override
    public Result<AriusOpTask> addTask(AriusOpTask ariusOpTask) {
        if (AriusObjUtils.isNull(ariusOpTask.getBusinessKey())) {
            return Result.buildParamIllegal("业务id为空");
        }
        if (existUnClosedTask(ariusOpTask.getBusinessKey(), ariusOpTask.getTaskType())) {
            return Result.buildParamIllegal(String.format("模版列表[%s]存在未完成的dcdr模板主从切换任务，不允许再次创建",
                    ariusOpTask.getBusinessKey()));
        }

        ariusOpTask.setCreateTime(new Date());
        ariusOpTask.setUpdateTime(new Date());
        ariusOpTaskManager.insert(ariusOpTask);
        boolean succ = 0 < ariusOpTask.getId();
        if (!succ) {
            LOGGER.error(
                "class=DCDRWorkTaskHandler||method=addTask||taskType={}||businessKey={}||errMsg=failed to insert",
                ariusOpTask.getTaskType(), ariusOpTask.getBusinessKey());
            return Result.buildFail();
        }
        return Result.buildSucc(ariusOpTask);
    }

    @Override
    public boolean existUnClosedTask(String key, Integer type) {
        List<AriusOpTask> pengingTaskList = ariusOpTaskManager.getPendingTaskByType(type);
        if (CollectionUtils.isEmpty(pengingTaskList)) { return false; }

        List<String> businessKeyList = pengingTaskList.stream()
                .map(AriusOpTask::getBusinessKey)
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
    public Result<Void> process(AriusOpTask ariusOpTask, Integer step, String status, String expandData) {
        Result<AriusOpTask> result = ariusOpTaskManager.getById(ariusOpTask.getId());
        if (result.failed()) {
            return Result.buildFrom(result);
        }
        AriusOpTask updateAriusOpTask = result.getData();
        DCDRTaskDetail detail = JSON.parseObject(updateAriusOpTask.getExpandData(), DCDRTaskDetail.class);
        detail.setStatus(status);
        detail.setTaskProgress(step);
        updateAriusOpTask.setExpandData(JSON.toJSONString(detail));
        if (WorkTaskStatusEnum.FAILED.getStatus().equals(status)
            || step.equals(WorkTaskDCDRProgressEnum.STEP_9.getProgress())) {
            updateAriusOpTask.setStatus(status);
        }

        ariusOpTaskManager.updateTask(updateAriusOpTask);

        return Result.buildSucc();
    }

  

}