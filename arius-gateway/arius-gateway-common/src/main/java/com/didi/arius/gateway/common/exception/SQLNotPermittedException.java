package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
* @author weizijun
* @date：2016年8月22日
* 
*/
public class SQLNotPermittedException extends QueryException {
    public SQLNotPermittedException(String msg) {
        super(msg);
    }

    public SQLNotPermittedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SQLNotPermittedException(StreamInput in) throws IOException{
        super(in);
    }
    
    @Override
    public RestStatus status() {
        return RestStatus.FORBIDDEN;
    }
}
