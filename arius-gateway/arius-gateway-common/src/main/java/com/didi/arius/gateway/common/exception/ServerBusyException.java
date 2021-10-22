package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

public class ServerBusyException extends QueryException {
    public ServerBusyException(String msg) {
        super(msg);
    }

    public ServerBusyException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ServerBusyException(StreamInput in) throws IOException{
        super(in);
    }

    @Override
    public RestStatus status() {
        return RestStatus.SERVICE_UNAVAILABLE;
    }
}
