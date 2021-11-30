package com.didichuxing.datachannel.arius.admin.task.capaciryplan;

import java.util.Arrays;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.notify.info.rd.ScheduleListTaskFailNotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.info.rd.ScheduleTaskFailNotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.service.NotifyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanArea;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanAreaService;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTask;
import com.didichuxing.datachannel.arius.admin.task.TaskBatch;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * base任务 容量规划base任务
 * @author d06679
 * @date 2019/3/21
 */
public abstract class BaseConcurrentCapacityPlanAreaTask extends BaseConcurrentTask<CapacityPlanArea> {

    private static final ILog         LOGGER         = LogFactory.getLog(BaseConcurrentCapacityPlanAreaTask.class);

    protected static final String     TASK_RETRY_URL = "";

    @Autowired
    protected CapacityPlanAreaService capacityPlanAreaService;

    @Autowired
    protected NotifyService           notifyService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    /**
     * 任务全集
     *
     * @return list
     */
    @Override
    protected List<CapacityPlanArea> getAllItems() {
        return capacityPlanAreaService.listPlaningAreas();
    }

    /**
     * 处理一个批次任务
     *
     * @param taskBatch 批次
     * @return true/false
     */
    @Override
    protected boolean executeByBatch(TaskBatch<CapacityPlanArea> taskBatch) {
        List<CapacityPlanArea> items = taskBatch.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return true;
        }

        boolean succ = true;

        ScheduleListTaskFailNotifyInfo scheduleListTaskFailNotifyInfo = new ScheduleListTaskFailNotifyInfo();

        // 只要有一个集群失败就认为batch失败
        for (CapacityPlanArea area : items) {
            try {
                LOGGER.info("class=BaseConcurrentCapacityPlanAreaTask||method=executeByBatch||executeByArea begin||areaId={}||task={}", area.getResourceId(), getTaskName());
                Result<Void> result = executeByArea(area.getResourceId());
                if (result.success()) {
                    LOGGER.info("class=BaseConcurrentCapacityPlanAreaTask||method=executeByBatch||executeByArea succ||areaId={}||task={}", area.getResourceId(), getTaskName());
                } else {
                    succ = false;
                    LOGGER.warn("class=BaseConcurrentCapacityPlanAreaTask||method=executeByBatch||executeByArea fail||areaId={}||task={}", area.getResourceId(), getTaskName());
                    scheduleListTaskFailNotifyInfo.addScheduleTaskFailMsg(
                            new ScheduleTaskFailNotifyInfo(getTaskName(), getBizStr(area), TASK_RETRY_URL, result.getMessage()));
                }
            } catch (Exception e) {
                succ = false;
                LOGGER.warn("class=BaseConcurrentCapacityPlanAreaTask||method=executeByBatch||executeByArea error||areaId={}||task={}||errMsg={}", area.getResourceId(), getTaskName(),
                    e.getMessage(), e);
                scheduleListTaskFailNotifyInfo.addScheduleTaskFailMsg(
                        new ScheduleTaskFailNotifyInfo(getTaskName(), getBizStr(area), TASK_RETRY_URL, e.getMessage()));
            }
        }

        if(!succ){
            notifyService.send(NotifyTaskTypeEnum.SCHEDULE_TASK_FAILED, scheduleListTaskFailNotifyInfo, Arrays.asList());
        }

        return succ;
    }

    private String getBizStr(CapacityPlanArea area) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(area.getResourceId());
        return "规划AreaId_" + area.getResourceId() + "(" + clusterLogic.getName() + ":" + area.getClusterName() + ")";
    }

    /**
     * 处理一个集群
     * @param areaId 集群名字
     * @return true/false
     */
    protected abstract Result<Void> executeByArea(Long areaId) throws AdminOperateException;
}
