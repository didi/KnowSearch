package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
* @author weizijun
* @date：2016年8月22日
* 
*/
public class IndexNotPermittedException extends QueryException {
    public IndexNotPermittedException(String msg) {
        super(msg);
    }

    public IndexNotPermittedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public IndexNotPermittedException(StreamInput in) throws IOException{
        super(in);
    }
    
    @Override
    public RestStatus status() {
        return RestStatus.FORBIDDEN;
    }
}
