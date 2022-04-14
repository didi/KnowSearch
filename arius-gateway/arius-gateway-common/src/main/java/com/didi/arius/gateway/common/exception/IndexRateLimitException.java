package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

public class IndexRateLimitException extends QueryException {
    public IndexRateLimitException(String msg) {
        super(msg);
    }

    public IndexRateLimitException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public IndexRateLimitException(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    public RestStatus status() {
        return RestStatus.SERVICE_UNAVAILABLE;
    }
}