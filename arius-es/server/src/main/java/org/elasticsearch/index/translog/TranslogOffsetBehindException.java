package org.elasticsearch.index.translog;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

/**
 * author weizijun
 * date：2019-08-08
 */
public class TranslogOffsetBehindException extends ElasticsearchException {
    public TranslogOffsetBehindException(String msg, Object... args) {
        super(msg, args);
    }

    public TranslogOffsetBehindException(String message, Throwable cause) {
        super(message, cause);
    }

    public TranslogOffsetBehindException(StreamInput in) throws IOException {
        super(in);
    }
}
