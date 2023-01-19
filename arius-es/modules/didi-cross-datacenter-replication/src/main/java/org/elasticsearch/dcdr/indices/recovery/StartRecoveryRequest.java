package org.elasticsearch.dcdr.indices.recovery;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.transport.TransportRequest;

import java.io.IOException;

/**
 * author weizijun
 * date：2019-09-06
 */
public class StartRecoveryRequest extends TransportRequest {
    private ShardId shardId;

    public StartRecoveryRequest(ShardId shardId) {
        this.shardId = shardId;
    }

    public ShardId getShardId() {
        return shardId;
    }

    public StartRecoveryRequest(StreamInput in) throws IOException {
        super(in);
        shardId = new ShardId(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        shardId.writeTo(out);
    }
}
