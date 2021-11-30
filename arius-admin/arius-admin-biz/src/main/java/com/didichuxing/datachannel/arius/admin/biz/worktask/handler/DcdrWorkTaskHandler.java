package com.didichuxing.datachannel.arius.admin.biz.worktask.handler;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskHandler;
import com.didichuxing.datachannel.arius.admin.biz.worktask.WorkTaskManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskDCDRProgressEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.task.WorkTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.AbstractTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("dcdrWorkTaskHandler")
public class DcdrWorkTaskHandler implements WorkTaskHandler {

    @Autowired
    private TemplateLogicService templateLogicService;

    @Autowired
    private WorkTaskManager workTaskManager;

    @Override
    public Result<WorkTask> addTask(WorkTask workTask) {
        if (AriusObjUtils.isNull(workTask.getBusinessKey())) {
            return Result.buildParamIllegal("业务id为空");
        }
        if (existUnClosedTask(workTask.getBusinessKey(), workTask.getTaskType())) {
            return Result.buildParamIllegal("该模版存在未完成dcdr任务，不允许再次创建");
        }

        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
            .getLogicTemplateWithPhysicalsById(workTask.getBusinessKey());

        IndexTemplatePhy masterPhy = templateLogicWithPhysical.getMasterPhyTemplate();
        if (masterPhy == null) {
            return Result.buildParamIllegal("物理主模版不存在");
        }
        DCDRTaskDetail taskDetail = ConvertUtil.str2ObjByJson(workTask.getExpandData(), DCDRTaskDetail.class);
        taskDetail.setCreateTime(new Date());
        taskDetail.setMasterPhysicalTemplateName(masterPhy.getName());
        taskDetail.setMasterPhysicalClusterName(masterPhy.getCluster());

        IndexTemplatePhy slavePhy = templateLogicWithPhysical.getSlavePhyTemplate();
        if (slavePhy != null) {
            taskDetail.setSlavePhysicalTemplateName(slavePhy.getName());
            taskDetail.setSlavePhysicalClusterName(slavePhy.getCluster());
        }
        taskDetail.setLogicTemplateId(templateLogicWithPhysical.getId());
        taskDetail.setLogicTemplateName(templateLogicWithPhysical.getName());
        taskDetail.setTaskProgress(WorkTaskDCDRProgressEnum.STEP_1.getProgress());
        taskDetail.setStatus(WorkTaskStatusEnum.SUCCESS.getStatus());

        workTask.setDeleteFlag(false);
        workTask.setStatus(WorkTaskStatusEnum.RUNNING.getStatus());
        workTask.setExpandData(JSON.toJSONString(taskDetail));
        workTask.setTitle(templateLogicWithPhysical.getName() + WorkTaskTypeEnum.TEMPLATE_DCDR.getMessage());
        workTaskManager.insert(workTask);

        return Result.buildSucc(workTask);
    }

    @Override
    public boolean existUnClosedTask(Integer key, Integer type) {
        return workTaskManager.getPengingTask(key, type) != null;
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

        workTaskManager.updateTaskById(updateWorkTask);

        return Result.buildSucc();
    }

    @Override
    public AbstractTaskDetail getTaskDetail(String extensions) {
        return JSON.parseObject(extensions, DCDRTaskDetail.class);
    }

}
