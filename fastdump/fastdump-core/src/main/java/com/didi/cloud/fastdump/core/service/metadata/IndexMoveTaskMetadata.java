package com.didi.cloud.fastdump.core.service.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didi.cloud.fastdump.common.bean.stats.IndexMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.stats.IndexNodeMoveTaskStats;
import com.didi.cloud.fastdump.common.bean.stats.SuccIndexMoveTaskStats;
import com.didi.cloud.fastdump.common.enums.TaskStatusEnum;
import com.didi.cloud.fastdump.common.threadpool.ScheduleThreadPool;
import com.didi.cloud.fastdump.common.utils.ConvertUtil;
import com.didi.cloud.fastdump.core.action.metadata.DeleteIndexMoveStatsAction;
import com.didi.cloud.fastdump.core.action.metadata.GetIndexMoveStatsAction;

/**
 * Created by linyunan on 2022/9/6
 */
@Service
public class IndexMoveTaskMetadata implements TaskStatsService<IndexNodeMoveTaskStats>, TaskNodeDistributedService {
    protected static final Logger                               LOGGER                       = LoggerFactory
        .getLogger(IndexMoveTaskMetadata.class);

    @Autowired
    private GetIndexMoveStatsAction                             getIndexMoveStatsAction;

    @Autowired
    private DeleteIndexMoveStatsAction                          deleteIndexMoveStatsAction;
    private final ScheduleThreadPool                            scheduleThreadPool;
    /**
     * 执行任务所在的节点列表
     */
    private final Map<String/*taskId*/, List<String>>           taskId2NodeIpListMap         = new ConcurrentHashMap<>();

    /**
     * key ——> taskId  value ——> 索引节点级别任务详情
     */
    private final Map<String/*taskId*/, IndexNodeMoveTaskStats> taskId2IndexNodeMoveStatsMap       = new ConcurrentHashMap<>();

    private final Map<String/*taskId*/, SuccIndexMoveTaskStats> taskId2SuccIndexMoveTaskStatsMap   = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        scheduleThreadPool.submitScheduleAtFixedDelayTask(this::refreshStats, 300, 300);
        scheduleThreadPool.submitScheduleAtFixedDelayTask(this::clearSuccStats, 24 * 60 * 1000, 24 * 60 * 1000);

    }

    private synchronized void clearSuccStats() { taskId2SuccIndexMoveTaskStatsMap.clear();}

    private synchronized void refreshStats() {
        List<IndexMoveTaskStats> succIndexMoveTaskStatsList = new ArrayList<>();
        List<String> failedTaskIdList = new ArrayList<>();
        List<String> pauseTaskIdList = new ArrayList<>();
        if (MapUtils.isEmpty(taskId2NodeIpListMap)) {
            // 其他slave节点 taskId2NodeIpListMap 为null 不处理
            return;
        }

        for (Map.Entry<String, List<String>> e : taskId2NodeIpListMap.entrySet()) {
            String taskId = e.getKey();
            if (taskId2SuccIndexMoveTaskStatsMap.containsKey(taskId)) { continue;}

            try {
                // 获取 index move 的聚合状态
                IndexMoveTaskStats indexMoveTaskStats = getIndexMoveStatsAction.doAction(taskId);
                if (null == indexMoveTaskStats) { continue;}

                if (TaskStatusEnum.SUCCESS.getValue().equals(indexMoveTaskStats.getStatus())) {
                    succIndexMoveTaskStatsList.add(indexMoveTaskStats);
                }
                if (TaskStatusEnum.PAUSE.getValue().equals(indexMoveTaskStats.getStatus())) {
                    pauseTaskIdList.add(indexMoveTaskStats.getTaskId());
                }
                if (TaskStatusEnum.FAILED.getValue().equals(indexMoveTaskStats.getStatus())) {
                    failedTaskIdList.add(indexMoveTaskStats.getTaskId());
                }
            } catch (Exception ex) {
                LOGGER.error(
                        "class=IndexMoveTaskMetadata||method=refreshStats||errMsg=failed to get index move task stats||detail={}",
                        ex.getMessage(), ex);
            }
        }

        // 清理暂停任务
        for (String pauseTaskId : pauseTaskIdList) {
            try {
                // 清理其他节点状态
                Boolean deleteFlag = deleteIndexMoveStatsAction.doAction(pauseTaskId);
                if (deleteFlag) {
                    taskId2IndexNodeMoveStatsMap.remove(pauseTaskId);
                    taskId2NodeIpListMap.remove(pauseTaskId);
                }
            } catch (Exception e) {
                LOGGER.error(
                        "class=IndexMoveTaskMetadata||method=refreshStats||taskId={}||errMsg=failed to delete pause task||detail={}",
                        pauseTaskId, e.getMessage(), e);
            }
        }

        // 清理失败任务
        for (String failedTaskId : failedTaskIdList) {
            try {
                // 清理其他节点状态
                Boolean deleteFlag = deleteIndexMoveStatsAction.doAction(failedTaskId);
                if (deleteFlag) {
                    taskId2IndexNodeMoveStatsMap.remove(failedTaskId);
                    taskId2NodeIpListMap.remove(failedTaskId);
                }
            } catch (Exception e) {
                LOGGER.error(
                        "class=IndexMoveTaskMetadata||method=refreshStats||taskId={}||errMsg=failed to delete failed task||detail={}",
                        failedTaskId, e.getMessage(), e);
            }
        }

        // 清理成功任务
        for (IndexMoveTaskStats needClearTaskStats : succIndexMoveTaskStatsList) {
            try {
                String needClearTaskId = needClearTaskStats.getTaskId();
                // 清理其他节点状态
                Boolean deleteFlag = deleteIndexMoveStatsAction.doAction(needClearTaskId);
                if (deleteFlag) {
                    if (!taskId2SuccIndexMoveTaskStatsMap.containsKey(needClearTaskId)) {
                        // 保留成功信息
                        SuccIndexMoveTaskStats succIndexMoveTaskStats =
                                ConvertUtil.obj2ObjByJSON(needClearTaskStats, SuccIndexMoveTaskStats.class);
                        succIndexMoveTaskStats.setSuccTime(System.currentTimeMillis());
                        taskId2SuccIndexMoveTaskStatsMap.put(needClearTaskId, succIndexMoveTaskStats);
                    }

                    taskId2IndexNodeMoveStatsMap.remove(needClearTaskId);
                    taskId2NodeIpListMap.remove(needClearTaskId);
                }
            } catch (Exception e) {
                LOGGER.error(
                    "class=IndexMoveTaskMetadata||method=refreshStats||errMsg=failed to delete task||detail={}",
                    e.getMessage(), e);
            }
        }
    }

    public synchronized void refreshSuccStats(IndexMoveTaskStats indexMoveTaskStats) throws Exception {
        String taskId = indexMoveTaskStats.getTaskId();
        if (taskId2SuccIndexMoveTaskStatsMap.containsKey(taskId)) { return;}

        Boolean deleteFlag = deleteIndexMoveStatsAction.doAction(taskId);
        if (deleteFlag) {
            // 保留成功信息
            SuccIndexMoveTaskStats succIndexMoveTaskStats =
                    ConvertUtil.obj2ObjByJSON(indexMoveTaskStats, SuccIndexMoveTaskStats.class);
            succIndexMoveTaskStats.setSuccTime(System.currentTimeMillis());
            taskId2SuccIndexMoveTaskStatsMap.put(taskId, succIndexMoveTaskStats);
            taskId2NodeIpListMap.remove(taskId);
            taskId2IndexNodeMoveStatsMap.remove(taskId);
        }
    }

    public IndexMoveTaskMetadata(ScheduleThreadPool scheduleThreadPool) {
        this.scheduleThreadPool = scheduleThreadPool;
    }

    @Override
    public boolean putTaskStats(String taskId, IndexNodeMoveTaskStats indexNodeMoveTaskStats) {
        taskId2IndexNodeMoveStatsMap.put(taskId, indexNodeMoveTaskStats);
        return true;
    }

    @Override
    public IndexNodeMoveTaskStats getMoveTaskStats(String taskId) {
        return taskId2IndexNodeMoveStatsMap.get(taskId);
    }

    @Override
    public List<IndexNodeMoveTaskStats> listAllMoveTaskStats() {
        return  null;
    }

    @Override
    public List<String> listAllTaskIds() {
        List<String> succTaskIdList =  new ArrayList<>(taskId2IndexNodeMoveStatsMap.keySet());
        List<String> taskIdList     = new ArrayList<>(taskId2SuccIndexMoveTaskStatsMap.keySet());
        Set<String> finalTaskIdSet = new HashSet<>();
        finalTaskIdSet.addAll(succTaskIdList);
        finalTaskIdSet.addAll(taskIdList);
        return new ArrayList<>(finalTaskIdSet);
    }

    @Override
    public boolean removeTaskStats(String taskId) {
        taskId2IndexNodeMoveStatsMap.remove(taskId);
        return true;
    }

    public SuccIndexMoveTaskStats getSuccTaskStats(String taskId) {
        return taskId2SuccIndexMoveTaskStatsMap.get(taskId);
    }

    public List<SuccIndexMoveTaskStats> listAllSuccTaskStats() {
        return new ArrayList<>(taskId2SuccIndexMoveTaskStatsMap.values());
    }

    @Override
    public boolean isTaskSucc(String taskId) {
        return taskId2SuccIndexMoveTaskStatsMap.containsKey(taskId);
    }

    @Override
    public boolean putTaskIpList(String taskId, List<String> ipList) {
        taskId2NodeIpListMap.put(taskId, ipList);
        return true;
    }

    @Override
    public List<String> getTaskIpList(String taskId) {
        return taskId2NodeIpListMap.get(taskId);
    }

    @Override
    public boolean removeTaskIpList(String taskId) {
        taskId2NodeIpListMap.remove(taskId);
        return true;
    }

    @Override
    public Map<String, List<String>> listAllTaskIpListMap() {
        return taskId2NodeIpListMap;
    }
}
