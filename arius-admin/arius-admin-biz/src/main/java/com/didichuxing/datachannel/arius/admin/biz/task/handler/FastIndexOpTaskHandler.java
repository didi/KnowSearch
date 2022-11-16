package com.didichuxing.datachannel.arius.admin.biz.task.handler;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("fastIndexOpTaskHandler")
public class FastIndexOpTaskHandler extends AbstractOpTaskHandler {
    @Override
    public Result<OpTask> addTask(OpTask opTask) {
        if (AriusObjUtils.isNull(opTask.getBusinessKey())) {
            return Result.buildParamIllegal("业务id为空");
        }

        opTask.setCreateTime(new Date());
        opTask.setUpdateTime(new Date());
        opTaskManager.insert(opTask);
        boolean succ = 0 < opTask.getId();
        if (!succ) {
            LOGGER.error(
                "class=DCDRWorkTaskHandler||method=addTask||taskType={}||businessKey={}||errMsg=failed to insert",
                opTask.getTaskType(), opTask.getBusinessKey());
            return Result.buildFail();
        }
        return Result.buildSucc(opTask);
    }

    @Override
    public Result<Void> process(OpTask opTask, Integer step, String status, String expandData) {

        opTask.setExpandData(expandData);
        opTask.setStatus(status);
        opTask.setUpdateTime(new Date());
        opTaskManager.updateTask(opTask);

        return Result.buildSucc();
    }
}