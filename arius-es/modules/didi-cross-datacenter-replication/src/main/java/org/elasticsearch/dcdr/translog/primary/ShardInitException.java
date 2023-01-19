package org.elasticsearch.dcdr.translog.primary;

import org.elasticsearch.ElasticsearchException;

/**
 * author weizijun
 * date：2019-10-26
 */
public class ShardInitException extends ElasticsearchException {
    public ShardInitException(String msg, Object... args) {
        super(msg, args);
    }
}
