package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

public class ResponseTooLargeException extends QueryException {

    public ResponseTooLargeException(String msg) {
        super(msg);
    }

    public ResponseTooLargeException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ResponseTooLargeException(StreamInput in) throws IOException{
        super(in);
    }

}
