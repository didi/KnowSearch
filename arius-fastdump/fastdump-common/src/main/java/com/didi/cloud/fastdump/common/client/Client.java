package com.didi.cloud.fastdump.common.client;

/**
 * Created by linyunan on 2022/8/8
 */
public interface Client<T> extends AutoCloseable {
    T getClient();
}
