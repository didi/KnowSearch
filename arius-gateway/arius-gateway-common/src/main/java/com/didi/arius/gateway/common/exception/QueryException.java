package com.didi.arius.gateway.common.exception;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

public class QueryException extends ElasticsearchException {

    public QueryException(String msg) {
        super(msg);
    }

    public QueryException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public QueryException(StreamInput in) throws IOException{
        super(in);
    }
    
    @Override
    public RestStatus status() {
        return RestStatus.BAD_REQUEST;
    }
}
