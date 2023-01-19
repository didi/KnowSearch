package com.didichuxing.datachannel.arius.admin.task.fastindex;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.FastIndexManager;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didiglobal.knowframework.job.annotation.Task;
import com.didiglobal.knowframework.job.common.TaskResult;
import com.didiglobal.knowframework.job.core.job.Job;
import com.didiglobal.knowframework.job.core.job.JobContext;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Sets;

/**
 * 定时同步数据迁移任务状态
 * @author didi
 */
@Task(name = "RefreshFastIndexTaskStatus", description = "定时同步数据迁移任务状态", cron = "0 0/1 * * * ? *", autoRegister = true)
public class RefreshFastIndexTaskStatus implements Job {
    private static final ILog LOGGER = LogFactory.getLog(RefreshFastIndexTaskStatus.class);

    @Autowired
    private FastIndexManager    fastIndexManager;
    @Autowired
    private OpTaskManager       opTaskManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=RefreshFastIndexTaskStatus||method=execute||msg=start");
        List<OpTask> taskList = opTaskManager.getPendingTaskByType(OpTaskTypeEnum.FAST_INDEX.getType());
        // 获取处于running状态的fastIndex任务列表
        if (AriusObjUtils.isEmptyList(taskList)) {
            LOGGER.info("class=RefreshFastIndexTaskStatus||method=execute||msg=pendingTaskList is empty");
            return TaskResult.buildSuccess();
        }
        Set<Integer> refreshTaskSuccSet = Sets.newConcurrentHashSet();
        List<Integer> pendingTaskIdList = Lists.newArrayList();
        taskList.forEach(opTask -> {
            pendingTaskIdList.add(opTask.getId());
            Result<Void> result = fastIndexManager.refreshTask(opTask.getId());
            if (result.success()) {
                refreshTaskSuccSet.add(opTask.getId());
            }
            LOGGER.info(
                "class=RefreshFastIndexTaskStatus||method=execute||taskId={}||result={}||msg=refreshTask success",
                opTask.getId(), JSON.toJSONString(result));
        });

        LOGGER.info("class=RefreshFastIndexTaskStatus||method=execute||msg=finish");

        return new TaskResult(
            TaskResult.SUCCESS_CODE,
            "refreshTaskSucc:[" + StringUtils.join(refreshTaskSuccSet, ",") + "],failedList["
                                     + pendingTaskIdList.stream().filter(id -> !refreshTaskSuccSet.contains(id))
                                         .map(String::valueOf).collect(Collectors.joining(","))
                                     + "]");
    }
}