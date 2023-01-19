package com.didi.cloud.fastdump.common.event.es;

import com.didi.cloud.fastdump.common.bean.taskcontext.es.ESIndexMoveTaskActionContext;

/**
 * Created by linyunan on 2022/9/6
 */
public class IndexNodeMoveStatsEvent extends BaseMoveStatsEvent {
    private final ESIndexMoveTaskActionContext esIndexMoveTaskContext;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public IndexNodeMoveStatsEvent(Object source, ESIndexMoveTaskActionContext esIndexMoveTaskContext) {
        super(source);
        this.esIndexMoveTaskContext = esIndexMoveTaskContext;
    }

    public ESIndexMoveTaskActionContext getEsIndexMoveTaskContext() {
        return esIndexMoveTaskContext;
    }
}
