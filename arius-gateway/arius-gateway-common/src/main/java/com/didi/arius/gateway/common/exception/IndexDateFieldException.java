package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

public class IndexDateFieldException extends QueryException {
    public IndexDateFieldException(String msg) {
        super(msg);
    }

    public IndexDateFieldException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public IndexDateFieldException(StreamInput in) throws IOException {
        super(in);
    }
}