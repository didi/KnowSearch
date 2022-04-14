package com.didi.arius.gateway.common.exception;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
 * @author didi
 * @date 2022-03-23 10:35 上午
 */
public class IndexNotFoundException extends ElasticsearchException {

    public IndexNotFoundException(String msg) {
        super(msg);
    }

    public IndexNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public IndexNotFoundException(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    public RestStatus status() {
        return RestStatus.NOT_FOUND;
    }
}
