package com.didichuxing.datachannel.arius.admin.common.event.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.ecm.EcmTask;
import org.springframework.context.ApplicationEvent;

/**
 * @author lyn
 * @date 2021-01-26
 */
public class EcmTaskEditEvent extends ApplicationEvent {

    private EcmTask editTask;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public EcmTaskEditEvent(Object source, EcmTask editTask) {
        super(source);
        this.editTask = editTask;
    }

    public EcmTask getEditTask() {
        return editTask;
    }

}