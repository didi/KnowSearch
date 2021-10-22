package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
* @author weizijun
* @date：2016年8月22日
* 
*/
public class ServerException extends QueryException {
    public ServerException(String msg) {
        super(msg);
    }

    public ServerException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ServerException(StreamInput in) throws IOException{
        super(in);
    }
    
    @Override
    public RestStatus status() {
        return RestStatus.INTERNAL_SERVER_ERROR;
    }
}
