package com.didi.cloud.fastdump.common.event.es;

import com.didi.cloud.fastdump.common.event.BaseEvent;

/**
 * Created by linyunan on 2022/9/6
 */
public abstract class BaseESMoveTaskEvent extends BaseEvent {
    public BaseESMoveTaskEvent(Object source) {
        super(source);
    }
}
