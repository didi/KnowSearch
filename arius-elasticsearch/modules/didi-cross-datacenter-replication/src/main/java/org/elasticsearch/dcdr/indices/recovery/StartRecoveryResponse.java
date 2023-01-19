package org.elasticsearch.dcdr.indices.recovery;

import java.io.IOException;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.store.Store;
import org.elasticsearch.transport.FutureTransportResponseHandler;
import org.elasticsearch.transport.TransportResponse;
import org.elasticsearch.transport.TransportResponseHandler;

/**
 * author weizijun
 * date：2019-09-05
 */
public class StartRecoveryResponse extends TransportResponse {
    private long recoveryId;
    private ShardId shardId;
    private String targetAllocationId;
    private Store.MetadataSnapshot metadataSnapshot;

    public StartRecoveryResponse(
        final ShardId shardId,
        final String targetAllocationId,
        final Store.MetadataSnapshot metadataSnapshot,
        final long recoveryId
    ) {
        this.recoveryId = recoveryId;
        this.shardId = shardId;
        this.targetAllocationId = targetAllocationId;
        this.metadataSnapshot = metadataSnapshot;
    }

    public StartRecoveryResponse() {}

    public long recoveryId() {
        return this.recoveryId;
    }

    public ShardId shardId() {
        return shardId;
    }

    public String targetAllocationId() {
        return targetAllocationId;
    }

    public Store.MetadataSnapshot metadataSnapshot() {
        return metadataSnapshot;
    }

    static TransportResponseHandler<StartRecoveryResponse> HANDLER =
        new FutureTransportResponseHandler<StartRecoveryResponse>() {
            @Override
            public StartRecoveryResponse read(StreamInput in) throws IOException {
                StartRecoveryResponse response = new StartRecoveryResponse(in);
                return response;
            }
        };

    public StartRecoveryResponse(StreamInput in) throws IOException {
        super(in);
        recoveryId = in.readLong();
        shardId = new ShardId(in);
        targetAllocationId = in.readString();
        metadataSnapshot = new Store.MetadataSnapshot(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeLong(recoveryId);
        shardId.writeTo(out);
        out.writeString(targetAllocationId);
        metadataSnapshot.writeTo(out);
    }
}
