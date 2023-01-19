package org.elasticsearch.dcdr.translog.replica.bulk;

import org.elasticsearch.action.support.WriteResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public final class TranslogSyncResponse extends ReplicationResponse implements WriteResponse {

    private long localCheckpoint;

    public long getLocalCheckpoint() {
        return localCheckpoint;
    }

    public void setLocalCheckpoint(final long localCheckpoint) {
        this.localCheckpoint = localCheckpoint;
    }

    private long maxSeqNo;

    public long getMaxSeqNo() {
        return maxSeqNo;
    }

    public void setMaxSeqNo(final long maxSeqNo) {
        this.maxSeqNo = maxSeqNo;
    }

    public TranslogSyncResponse() {}

    @Override
    public void setForcedRefresh(final boolean forcedRefresh) {}

    public TranslogSyncResponse(final StreamInput in) throws IOException {
        super(in);
        localCheckpoint = in.readZLong();
        maxSeqNo = in.readZLong();
    }

    @Override
    public void writeTo(final StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeZLong(localCheckpoint);
        out.writeZLong(maxSeqNo);
    }

}
