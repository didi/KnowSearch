package com.didi.cloud.fastdump.common.event.es;

import com.didi.cloud.fastdump.common.event.BaseEvent;

/**
 * Created by linyunan on 2022/9/6
 */
public abstract class BaseMoveStatsEvent extends BaseEvent {
    public BaseMoveStatsEvent(Object source) {
        super(source);
    }
}
