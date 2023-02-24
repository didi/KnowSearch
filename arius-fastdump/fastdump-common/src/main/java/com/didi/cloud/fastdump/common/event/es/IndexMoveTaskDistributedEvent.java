package com.didi.cloud.fastdump.common.event.es;

import java.util.List;

/**
 * Created by linyunan on 2022/9/6
 */
public class IndexMoveTaskDistributedEvent extends BaseESMoveTaskEvent {
    private final String       taskId;
    /**
     * 任务执行节点ids列表
     */
    private final List<String> ipList;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public IndexMoveTaskDistributedEvent(Object source, String taskId, List<String> ipList) {
        super(source);
        this.taskId = taskId;
        this.ipList = ipList;
    }

    public String getTaskId() {
        return taskId;
    }

    public List<String> getIpList() {
        return ipList;
    }
}
