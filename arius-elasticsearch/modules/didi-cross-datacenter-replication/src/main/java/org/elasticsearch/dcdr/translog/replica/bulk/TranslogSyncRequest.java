package org.elasticsearch.dcdr.translog.replica.bulk;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.action.support.replication.ReplicatedWriteRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.translog.Translog;

public final class TranslogSyncRequest extends ReplicatedWriteRequest<TranslogSyncRequest> {

    private String historyUUID;
    private List<Translog.Operation> operations;
    private long maxSeqNoOfUpdatesOrDeletes;

    public TranslogSyncRequest(final StreamInput in) throws IOException {
        super(in);
        historyUUID = in.readString();
        maxSeqNoOfUpdatesOrDeletes = in.readZLong();
        operations = in.readList(Translog.Operation::readOperation);
    }

    public TranslogSyncRequest(
        final ShardId shardId,
        final String historyUUID,
        final List<Translog.Operation> operations,
        long maxSeqNoOfUpdatesOrDeletes
    ) {
        super(shardId);
        setRefreshPolicy(RefreshPolicy.NONE);
        this.historyUUID = historyUUID;
        this.operations = operations;
        this.maxSeqNoOfUpdatesOrDeletes = maxSeqNoOfUpdatesOrDeletes;
    }

    public String getHistoryUUID() {
        return historyUUID;
    }

    public List<Translog.Operation> getOperations() {
        return operations;
    }

    public long getMaxSeqNoOfUpdatesOrDeletes() {
        return maxSeqNoOfUpdatesOrDeletes;
    }

    @Override
    public void writeTo(final StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(historyUUID);
        out.writeZLong(maxSeqNoOfUpdatesOrDeletes);
        out.writeVInt(operations.size());
        for (Translog.Operation operation : operations) {
            Translog.Operation.writeOperation(out, operation);
        }
    }

    @Override
    public String toString() {
        return "BulkShardOperationsRequest{" +
            "historyUUID=" + historyUUID +
            ", operations=" + operations.size() +
            ", maxSeqNoUpdates=" + maxSeqNoOfUpdatesOrDeletes +
            ", shardId=" + shardId +
            ", timeout=" + timeout +
            ", index='" + index + '\'' +
            ", waitForActiveShards=" + waitForActiveShards +
            '}';
    }

}
