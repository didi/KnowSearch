package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

public class DslRateLimitException extends QueryException {
    public DslRateLimitException(String msg) {
        super(msg);
    }

    public DslRateLimitException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DslRateLimitException(StreamInput in) throws IOException{
        super(in);
    }
}
