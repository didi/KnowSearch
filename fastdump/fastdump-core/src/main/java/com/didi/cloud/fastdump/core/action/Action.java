package com.didi.cloud.fastdump.core.action;

/**
 * Created by linyunan on 2022/9/8
 */
public interface Action<T, R> {
    R doAction(T t) throws Exception;
}
