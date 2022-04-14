package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
* @author weizijun
* @date：2016年8月28日
* 
*/
public class AccessForbiddenException extends QueryException {

    public AccessForbiddenException(String msg) {
        super(msg);
    }

    public AccessForbiddenException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AccessForbiddenException(StreamInput in) throws IOException{
        super(in);
    }
    
    @Override
    public RestStatus status() {
        return RestStatus.FORBIDDEN;
    }

}
