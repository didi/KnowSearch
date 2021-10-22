package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

public class DslForbiddenException extends QueryException {

    public DslForbiddenException(String msg) {
        super(msg);
    }

    public DslForbiddenException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DslForbiddenException(StreamInput in) throws IOException{
        super(in);
    }
    
    @Override
    public RestStatus status() {
        return RestStatus.FORBIDDEN;
    }

}