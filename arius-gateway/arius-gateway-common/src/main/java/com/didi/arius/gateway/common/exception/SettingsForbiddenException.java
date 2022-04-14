package com.didi.arius.gateway.common.exception;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

public class SettingsForbiddenException extends ElasticsearchException {
    public SettingsForbiddenException(Throwable cause) {
        super( cause );
    }

    public SettingsForbiddenException(String msg) {
        super(msg);
    }

    public SettingsForbiddenException(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    public RestStatus status() {
        return RestStatus.FORBIDDEN;
    }
}
