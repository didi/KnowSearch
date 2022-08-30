package com.didiglobal.logi.op.manager.infrastructure.common.event;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import org.springframework.context.ApplicationEvent;

/**
 * @author didi
 * @date 2022-07-12 3:07 下午
 */
public abstract class DomainEvent<T> extends ApplicationEvent implements EventNotifyListener<T>{
    private String describe;
    public DomainEvent(Object source) {
        super(source);
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;

    }
}
