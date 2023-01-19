package org.elasticsearch.search;

import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

public class SearchTimeoutException extends SearchException {

    public SearchTimeoutException(StreamInput in) throws IOException {
        super(in);
    }

    public SearchTimeoutException(long startSearchTick, long timeoutInInMillis) {
        super(null, String.format("search timeout, startTick %d, timeout value %d ms",
            startSearchTick, timeoutInInMillis), null);
    }

    public SearchTimeoutException(String msg) {
        super(null, msg, null);
    }

}
