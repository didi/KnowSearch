package com.didi.cloud.fastdump.core.service.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didi.cloud.fastdump.common.bean.stats.SuccTemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;
import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;
import com.didi.cloud.fastdump.common.threadpool.ScheduleThreadPool;
import com.didi.cloud.fastdump.common.utils.ConvertUtil;
import com.didi.cloud.fastdump.core.action.metadata.GetTemplateMoveStatsAction;

/**
 * Created by linyunan on 2022/9/13
 */
@Service
@Deprecated
public class TemplateMoveTaskMetadata implements TaskStatsService<TemplateMoveTaskStats> {
    protected static final Logger                                  LOGGER                              = LoggerFactory
        .getLogger(TemplateMoveTaskMetadata.class);

    @Autowired
    private ScheduleThreadPool                                     scheduleThreadPool;

    @Autowired
    private GetTemplateMoveStatsAction                             getTemplateMoveStatsAction;
    /**
     * key ——> taskId  value ——> 模板节点级别任务详情
     */
    private final Map<String/*taskId*/, TemplateMoveTaskStats>     taskId2TemplateMoveTaskStatsMap     = new ConcurrentHashMap<>();

    private final Map<String/*taskId*/, SuccTemplateMoveTaskStats> taskId2SuccTemplateMoveTaskStatsMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        scheduleThreadPool.submitScheduleAtFixedDelayTask(this::refreshSuccStats, 300, 300);
    }

    private synchronized void refreshSuccStats() {
        // 清理成功迁移任务的状态
        List<TemplateMoveTaskStats> needClearTaskId = new ArrayList<>();
        if (MapUtils.isEmpty(taskId2TemplateMoveTaskStatsMap)) {
            // 其他slave节点 taskId2TemplateMoveTaskStatsMap 为null 不处理
            return;
        }

        for (Map.Entry<String, TemplateMoveTaskStats> e : taskId2TemplateMoveTaskStatsMap.entrySet()) {
            String templateTaskId = e.getKey();
            try {
                TemplateMoveTaskStats templateMoveTaskStats = getTemplateMoveStatsAction.doAction(templateTaskId);
                if (null == templateMoveTaskStats) { continue;}

                if(TaskStatusEnum.SUCCESS.getValue().equals(templateMoveTaskStats.getStatus())) {
                    needClearTaskId.add(templateMoveTaskStats);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        if (CollectionUtils.isEmpty(needClearTaskId)) { return;}

        for (TemplateMoveTaskStats clearTask : needClearTaskId) {
            SuccTemplateMoveTaskStats succTemplateMoveTaskStats = ConvertUtil.obj2ObjByJSON(clearTask, SuccTemplateMoveTaskStats.class);
            succTemplateMoveTaskStats.setSuccTime(System.currentTimeMillis());
            taskId2SuccTemplateMoveTaskStatsMap.put(clearTask.getTaskId(), succTemplateMoveTaskStats);

            taskId2TemplateMoveTaskStatsMap.remove(clearTask.getTaskId());
        }
    }

    public synchronized void refreshSuccStats(TemplateMoveTaskStats templateMoveTaskStats) throws Exception {
        String taskId = templateMoveTaskStats.getTaskId();
        if (taskId2SuccTemplateMoveTaskStatsMap.containsKey(taskId)) { return;}

        SuccTemplateMoveTaskStats succTemplateMoveTaskStats = ConvertUtil.obj2ObjByJSON(
                templateMoveTaskStats, SuccTemplateMoveTaskStats.class);
        succTemplateMoveTaskStats.setSuccTime(System.currentTimeMillis());
        taskId2SuccTemplateMoveTaskStatsMap.put(templateMoveTaskStats.getTaskId(), succTemplateMoveTaskStats);

        taskId2TemplateMoveTaskStatsMap.remove(templateMoveTaskStats.getTaskId());
    }

    @Override
    public boolean putTaskStats(String taskId, TemplateMoveTaskStats moveTaskStats) {
        taskId2TemplateMoveTaskStatsMap.put(taskId, moveTaskStats);
        return true;
    }

    @Override
    public TemplateMoveTaskStats getMoveTaskStats(String taskId) {
        return taskId2TemplateMoveTaskStatsMap.get(taskId);
    }

    @Override
    public List<TemplateMoveTaskStats> listAllMoveTaskStats() {
        return null;
    }

    @Override
    public List<String> listAllTaskIds() {
        List<String> succTaskIdList =  new ArrayList<>(taskId2SuccTemplateMoveTaskStatsMap.keySet());
        List<String> taskIdList     =  new ArrayList<>(taskId2TemplateMoveTaskStatsMap.keySet());
        Set<String>  finalTaskIdSet =  new HashSet<>();
        finalTaskIdSet.addAll(succTaskIdList);
        finalTaskIdSet.addAll(taskIdList);
        return new ArrayList<>(finalTaskIdSet);
    }

    @Override
    public boolean removeTaskStats(String taskId) {
        taskId2TemplateMoveTaskStatsMap.remove(taskId);
        return true;
    }

    @Override
    public boolean isTaskSucc(String taskId) {
        return taskId2SuccTemplateMoveTaskStatsMap.containsKey(taskId);
    }

    public SuccTemplateMoveTaskStats getSuccTaskStats(String taskId) {
        return taskId2SuccTemplateMoveTaskStatsMap.get(taskId);
    }
}
