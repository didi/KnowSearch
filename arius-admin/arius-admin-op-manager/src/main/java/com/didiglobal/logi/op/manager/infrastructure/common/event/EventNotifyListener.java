package com.didiglobal.logi.op.manager.infrastructure.common.event;

/**
 * @author didi
 * @date 2022-08-29 17:57
 */
public interface EventNotifyListener<T> {

    /**
     * 获取通知结果
     * @return
     */
    T getResult();

    /**
     * 设置输出结果
     * @param t
     */
    void setValue(T t);

}
