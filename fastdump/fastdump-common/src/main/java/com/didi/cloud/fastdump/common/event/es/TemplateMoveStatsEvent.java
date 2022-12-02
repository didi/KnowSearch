package com.didi.cloud.fastdump.common.event.es;

import com.didi.cloud.fastdump.common.bean.taskcontext.es.ESTemplateMoveTaskActionContext;

/**
 * Created by linyunan on 2022/9/6
 */
public class TemplateMoveStatsEvent extends BaseMoveStatsEvent {
    private final ESTemplateMoveTaskActionContext esTemplateMoveTaskActionContext;
    private final String                          submitIndexMoveTaskId;
    private final Integer                         totalIndexNum;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source     the object on which the event initially occurred (never {@code null})
     */
    public TemplateMoveStatsEvent(Object source, ESTemplateMoveTaskActionContext esTemplateMoveTaskActionContext,
                                  String submitIndexMoveTaskId, int totalIndexNum) {
        super(source);
        this.esTemplateMoveTaskActionContext = esTemplateMoveTaskActionContext;
        this.submitIndexMoveTaskId           = submitIndexMoveTaskId;
        this.totalIndexNum                   = totalIndexNum;
    }

    public String getSubmitIndexMoveTaskId() {
        return submitIndexMoveTaskId;
    }

    public ESTemplateMoveTaskActionContext getEsTemplateMoveTaskActionContext() {
        return esTemplateMoveTaskActionContext;
    }

    public Integer getTotalIndexNum() {
        return totalIndexNum;
    }
}
