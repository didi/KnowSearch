package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

public class TooManyIndexException extends QueryException {
    public TooManyIndexException(String msg) {
        super(msg);
    }

    public TooManyIndexException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TooManyIndexException(StreamInput in) throws IOException {
        super(in);
    }
}