package com.didi.cloud.fastdump.rest.http;

/**
 * Created by linyunan on 2022/8/4
 */
public abstract class BaseRequestAllocator<Request, Channel> {
    abstract void dispatchRequest(Request request, Channel channel);
}
