package com.didiglobal.logi.op.manager.infrastructure.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

/**
 * @author didi
 * @date 2022-07-12 3:07 下午
 */
public class DomainEvent extends ApplicationEvent {
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
