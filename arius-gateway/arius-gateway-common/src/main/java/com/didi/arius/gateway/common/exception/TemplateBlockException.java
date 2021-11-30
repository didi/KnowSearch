package com.didi.arius.gateway.common.exception;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
 * @author didi
 * @date 2021-11-11 3:25 下午
 */
public class TemplateBlockException extends ElasticsearchException {

    public TemplateBlockException(String msg) {
        super(msg);
    }

    public TemplateBlockException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TemplateBlockException(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    public RestStatus status() {
        return RestStatus.FORBIDDEN;
    }
}
