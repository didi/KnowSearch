package com.didi.cloud.fastdump.common.event.es;

import com.didi.cloud.fastdump.common.bean.stats.TemplateMoveTaskStats;

/**
 * Created by linyunan on 2022/9/6
 */
public class TemplateMoveTaskSuccEvent extends BaseESMoveTaskEvent {
    private final TemplateMoveTaskStats templateMoveTaskStats;

    public TemplateMoveTaskStats getTemplateMoveTaskStats() {
        return templateMoveTaskStats;
    }

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public TemplateMoveTaskSuccEvent(Object source, TemplateMoveTaskStats templateMoveTaskStats) {
        super(source);
        this.templateMoveTaskStats = templateMoveTaskStats;
    }
}
