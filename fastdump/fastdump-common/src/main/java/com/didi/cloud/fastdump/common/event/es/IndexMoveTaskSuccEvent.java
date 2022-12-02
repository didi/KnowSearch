package com.didi.cloud.fastdump.common.event.es;

import com.didi.cloud.fastdump.common.bean.stats.IndexMoveTaskStats;

/**
 * Created by linyunan on 2022/9/6
 */
public class IndexMoveTaskSuccEvent extends BaseESMoveTaskEvent {
    private final IndexMoveTaskStats indexMoveTaskStats;

    public IndexMoveTaskStats getIndexMoveTaskStats() {
        return indexMoveTaskStats;
    }

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public IndexMoveTaskSuccEvent(Object source, IndexMoveTaskStats indexMoveTaskStats) {
        super(source);
        this.indexMoveTaskStats = indexMoveTaskStats;
    }
}
