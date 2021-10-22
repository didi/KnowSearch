package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
* @author weizijun
* @date：2016年8月24日
* 
*/
public class FlowLimitException extends QueryException {
    public FlowLimitException(String msg) {
        super(msg);
    }

    public FlowLimitException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public FlowLimitException(StreamInput in) throws IOException{
        super(in);
    }

    @Override
    public RestStatus status() {
        return RestStatus.SERVICE_UNAVAILABLE;
    }
}
