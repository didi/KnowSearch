package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

public class QueryDslLengthException extends QueryException {

    public QueryDslLengthException(String msg) {
        super(msg);
    }

    public QueryDslLengthException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public QueryDslLengthException(StreamInput in) throws IOException{
        super(in);
    }

}
