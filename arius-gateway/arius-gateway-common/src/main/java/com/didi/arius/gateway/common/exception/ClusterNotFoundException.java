package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
 * author weizijun
 * date：2019-06-26
 */
public class ClusterNotFoundException extends QueryException {
    public ClusterNotFoundException(String msg) {
        super(msg);
    }

    public ClusterNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ClusterNotFoundException(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    public RestStatus status() {
        return RestStatus.NOT_FOUND;
    }

}
