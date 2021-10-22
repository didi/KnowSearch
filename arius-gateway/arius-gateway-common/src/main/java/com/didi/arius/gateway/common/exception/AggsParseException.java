package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.xcontent.XContentLocation;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

public class AggsParseException extends QueryException {

    public AggsParseException(String msg) {
        super(msg);
    }
    
    public AggsParseException(String msg, @Nullable XContentLocation location) {
        super(msg);
    }

    public AggsParseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AggsParseException(StreamInput in) throws IOException{
        super(in);
    }
    
    @Override
    public RestStatus status() {
        return RestStatus.BAD_REQUEST;
    }

}

