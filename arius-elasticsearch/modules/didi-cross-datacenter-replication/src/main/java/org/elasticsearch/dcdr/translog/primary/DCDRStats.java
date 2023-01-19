package org.elasticsearch.dcdr.translog.primary;

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * author weizijun
 * date：2019-09-25
 */
public class DCDRStats implements Writeable, ToXContentFragment {

    private static final ParseField PRIMARY_INDEX_FIELD = new ParseField("primary_index");
    private static final ParseField REPLICA_INDEX_FIELD = new ParseField("replica_index");
    private static final ParseField REPLICA_CLUSTER_FIELD = new ParseField("replica_cluster");
    private static final ParseField REPLICATION_STATE_FIELD = new ParseField("replication_state");
    private static final ParseField SHARD_ID_FIELD = new ParseField("shard_id");
    private static final ParseField PRIMARY_GLOBAL_CHECKPOINT_FIELD = new ParseField("primary_global_checkpoint");
    private static final ParseField PRIMARY_MAX_SEQ_NO_FIELD = new ParseField("primary_max_seq_no");
    private static final ParseField REPLICA_GLOBAL_CHECKPOINT_FIELD = new ParseField("replica_global_checkpoint");
    private static final ParseField REPLICA_MAX_SEQ_NO_FIELD = new ParseField("replica_max_seq_no");
    private static final ParseField TIME_SINCE_UPDATE_REPLICA_CHECKPOINT_FIELD = new ParseField("time_since_update_replica_checkpoint");
    private static final ParseField TOTAL_SEND_TIME_MILLIS_FIELD = new ParseField("total_send_time_millis");
    private static final ParseField SUCCESSFUL_SEND_REQUESTS_FIELD = new ParseField("successful_send_requests");
    private static final ParseField FAILED_SEND_REQUESTS_FIELD = new ParseField("failed_send_requests");
    private static final ParseField OPERATIONS_SEND_FIELD = new ParseField("operations_send");
    private static final ParseField BYTES_SEND = new ParseField("bytes_send");
    private static final ParseField TIME_SINCE_LAST_SEND_MILLIS_FIELD = new ParseField("time_since_last_send_millis");

    private static final ParseField COMMIT_TRANSLOG_OFFSET_FIELD = new ParseField("commit_translog_offset");
    private static final ParseField CURRENT_TRANSLOG_OFFSET_FIELD = new ParseField("current_translog_offset");
    private static final ParseField IN_SYNC_TRANSLOG_OFFSET_FIELD = new ParseField("in_sync_translog_offset");
    private static final ParseField IS_SYNCING_FIELD = new ParseField("syncing");
    private static final ParseField IS_RECOVERING_FIELD = new ParseField("recovering");
    private static final ParseField IS_CLOSED_FIELD = new ParseField("closed");
    private static final ParseField AVAILABLE_SEND_BULK_NUMBER_FIELD = new ParseField("available_send_bulk_number");
    private static final ParseField SUCCESS_RECOVER_COUNT_FIELD = new ParseField("success_recover_count");
    private static final ParseField FAILED_RECOVER_COUNT_FIELD = new ParseField("failed_recover_count");
    private static final ParseField RECOVER_TOTAL_TIME_MILLIS_FIELD = new ParseField("recover_total_time_millis");

    /**
     * 主索引名字
     */
    private String primaryIndex;

    /**
     * 从索引名字
     */
    private String replicaIndex;

    /**
     * 从集群
     */
    private String replicaCluster;

    /**
     * 链路状态
     */
    private boolean replicationState;

    /**
     * shardId
     */
    private int shardId;

    /**
     * 向当前节点发送request获取
     */
    private long primaryGlobalCheckpoint;

    /**
     * 向当前节点发送request获取
     */
    private long primaryMaxSeqNo;

    /**
     * 每次send——translog的response中获取
     */
    private long replicaGlobalCheckpoint;

    /**
     * 每次send——translog的response中获取
     */
    private long replicaMaxSeqNo;

    /**
     * 上一次更新checkpoint的时间
     */
    private long timeSinceUpdateReplicaCheckPoint;

    /**
     * send的时候获取
     */
    private long totalSendTimeMillis;

    /**
     * send的时候获取
     */
    private long successfulSendRequests;

    /**
     * send的时候获取
     */
    private long failedSendRequests;

    /**
     * send的时候获取
     */
    private long operationsSends;

    /**
     * send的时候获取
     */
    private long bytesSend;

    /**
     * send的时候获取
     */
    private long timeSinceLastSendMillis;

    /**
     * tranlog位点信息
     */
    private String commitOffsetStr;
    private String currentOffsetStr;
    private List<TranslogOffsetGapTuple> inSyncOffset;

    /**
     * dcdr info
     */
    private boolean syncing;
    private boolean recovering;
    private boolean closed;
    private int availableSendBulkNumber;

    /**
     * recover
     */
    private long successRecoverCount;
    private long failedRecoverCount;
    private long recoverTotalTimeMillis;

    public boolean isSyncing() {
        return syncing;
    }

    public void setSyncing(boolean syncing) {
        this.syncing = syncing;
    }

    public boolean isRecovering() {
        return recovering;
    }

    public void setRecovering(boolean recovering) {
        this.recovering = recovering;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public int getAvailableSendBulkNumber() {
        return availableSendBulkNumber;
    }

    public void setAvailableSendBulkNumber(int availableSendBulkNumber) {
        this.availableSendBulkNumber = availableSendBulkNumber;
    }

    public long getSuccessRecoverCount() {
        return successRecoverCount;
    }

    public void setSuccessRecoverCount(long successRecoverCount) {
        this.successRecoverCount = successRecoverCount;
    }

    public long getFailedRecoverCount() {
        return failedRecoverCount;
    }

    public void setFailedRecoverCount(long failedRecoverCount) {
        this.failedRecoverCount = failedRecoverCount;
    }

    public long getRecoverTotalTimeMillis() {
        return recoverTotalTimeMillis;
    }

    public void setRecoverTotalTimeMillis(long recoverTotalTimeMillis) {
        this.recoverTotalTimeMillis = recoverTotalTimeMillis;
    }


    public static class TranslogOffsetGapTuple implements Writeable {

        private String first;
        private String second;

        public TranslogOffsetGapTuple() {}

        public TranslogOffsetGapTuple(String first, String second) {
            this.first = first;
            this.second = second;
        }

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getSecond() {
            return second;
        }

        public void setSecond(String second) {
            this.second = second;
        }

        public void readFrom(StreamInput in) throws IOException {
            this.first = in.readString();
            this.second = in.readString();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeString(first);
            out.writeString(second);
        }

        @Override
        public String toString() {
            return first + "-" + second;
        }
    }

    public DCDRStats() {}

    public DCDRStats(StreamInput in) throws IOException {
        readFrom(in);
    }

    public void readFrom(StreamInput in) throws IOException {
        this.primaryIndex = in.readString();
        this.replicaIndex = in.readString();
        this.replicaCluster = in.readString();
        this.replicationState = in.readBoolean();
        this.shardId = in.readVInt();
        this.primaryGlobalCheckpoint = in.readZLong();
        this.primaryMaxSeqNo = in.readZLong();
        this.replicaGlobalCheckpoint = in.readZLong();
        this.replicaMaxSeqNo = in.readZLong();
        this.timeSinceUpdateReplicaCheckPoint = in.readZLong();
        this.totalSendTimeMillis = in.readVLong();
        this.successfulSendRequests = in.readVLong();
        this.failedSendRequests = in.readVLong();
        this.operationsSends = in.readVLong();
        this.bytesSend = in.readVLong();
        this.timeSinceLastSendMillis = in.readVLong();
        this.commitOffsetStr = in.readString();
        this.currentOffsetStr = in.readString();
        this.inSyncOffset = in.readList(
            inputStream -> {
                TranslogOffsetGapTuple tuple = new TranslogOffsetGapTuple();
                tuple.readFrom(inputStream);
                return tuple;
            }
        );
        syncing = in.readBoolean();
        recovering = in.readBoolean();
        closed = in.readBoolean();
        availableSendBulkNumber = in.readVInt();
        successRecoverCount = in.readZLong();
        failedRecoverCount = in.readZLong();
        recoverTotalTimeMillis = in.readZLong();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(primaryIndex);
        out.writeString(replicaIndex);
        out.writeString(replicaCluster);
        out.writeBoolean(replicationState);
        out.writeVInt(shardId);
        out.writeZLong(primaryGlobalCheckpoint);
        out.writeZLong(primaryMaxSeqNo);
        out.writeZLong(replicaGlobalCheckpoint);
        out.writeZLong(replicaMaxSeqNo);
        out.writeZLong(timeSinceUpdateReplicaCheckPoint);
        out.writeVLong(totalSendTimeMillis);
        out.writeVLong(successfulSendRequests);
        out.writeVLong(failedSendRequests);
        out.writeVLong(operationsSends);
        out.writeVLong(bytesSend);
        out.writeVLong(timeSinceLastSendMillis);
        out.writeString(commitOffsetStr);
        out.writeString(currentOffsetStr);
        out.writeList(inSyncOffset);
        out.writeBoolean(syncing);
        out.writeBoolean(recovering);
        out.writeBoolean(closed);
        out.writeVInt(availableSendBulkNumber);
        out.writeZLong(successRecoverCount);
        out.writeZLong(failedRecoverCount);
        out.writeZLong(recoverTotalTimeMillis);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        {
            toXContentFragment(builder, params);
        }
        builder.endObject();
        return builder;
    }

    public XContentBuilder toXContentFragment(final XContentBuilder builder, final Params params) throws IOException {
        builder.field(PRIMARY_INDEX_FIELD.getPreferredName(), primaryIndex);
        builder.field(REPLICA_INDEX_FIELD.getPreferredName(), replicaIndex);
        builder.field(REPLICA_CLUSTER_FIELD.getPreferredName(), replicaCluster);
        builder.field(REPLICATION_STATE_FIELD.getPreferredName(), replicationState);
        builder.field(SHARD_ID_FIELD.getPreferredName(), shardId);
        builder.field(IS_SYNCING_FIELD.getPreferredName(), syncing);
        builder.field(IS_RECOVERING_FIELD.getPreferredName(), recovering);
        builder.field(IS_CLOSED_FIELD.getPreferredName(), closed);
        builder.field(AVAILABLE_SEND_BULK_NUMBER_FIELD.getPreferredName(), availableSendBulkNumber);
        builder.field(PRIMARY_GLOBAL_CHECKPOINT_FIELD.getPreferredName(), primaryGlobalCheckpoint);
        builder.field(PRIMARY_MAX_SEQ_NO_FIELD.getPreferredName(), primaryMaxSeqNo);
        builder.field(REPLICA_GLOBAL_CHECKPOINT_FIELD.getPreferredName(), replicaGlobalCheckpoint);
        builder.field(REPLICA_MAX_SEQ_NO_FIELD.getPreferredName(), replicaMaxSeqNo);
        builder.field(TIME_SINCE_UPDATE_REPLICA_CHECKPOINT_FIELD.getPreferredName(), timeSinceUpdateReplicaCheckPoint);
        builder.humanReadableField(
            TOTAL_SEND_TIME_MILLIS_FIELD.getPreferredName(),
            "total_send_time",
            new TimeValue(totalSendTimeMillis, TimeUnit.MILLISECONDS)
        );
        builder.field(SUCCESSFUL_SEND_REQUESTS_FIELD.getPreferredName(), successfulSendRequests);
        builder.field(FAILED_SEND_REQUESTS_FIELD.getPreferredName(), failedSendRequests);
        builder.field(OPERATIONS_SEND_FIELD.getPreferredName(), operationsSends);
        builder.humanReadableField(
            BYTES_SEND.getPreferredName(),
            "total_send",
            new ByteSizeValue(bytesSend, ByteSizeUnit.BYTES)
        );
        builder.humanReadableField(
            TIME_SINCE_LAST_SEND_MILLIS_FIELD.getPreferredName(),
            "time_since_last_send",
            new TimeValue(timeSinceLastSendMillis, TimeUnit.MILLISECONDS)
        );
        builder.field(COMMIT_TRANSLOG_OFFSET_FIELD.getPreferredName(), commitOffsetStr);
        builder.field(CURRENT_TRANSLOG_OFFSET_FIELD.getPreferredName(), currentOffsetStr);

        List<String> tuples = new ArrayList<>();
        if (inSyncOffset != null) {
            for (final TranslogOffsetGapTuple tuple : inSyncOffset) {
                tuples.add(tuple.toString());
            }
        }
        builder.array(IN_SYNC_TRANSLOG_OFFSET_FIELD.getPreferredName(), tuples);
        builder.field(SUCCESS_RECOVER_COUNT_FIELD.getPreferredName(), successRecoverCount);
        builder.field(FAILED_RECOVER_COUNT_FIELD.getPreferredName(), failedRecoverCount);
        builder.field(RECOVER_TOTAL_TIME_MILLIS_FIELD.getPreferredName(), recoverTotalTimeMillis);
        return builder;

    }

    public String getPrimaryIndex() {
        return primaryIndex;
    }

    public void setPrimaryIndex(String primaryIndex) {
        this.primaryIndex = primaryIndex;
    }

    public String getReplicaIndex() {
        return replicaIndex;
    }

    public void setReplicaIndex(String replicaIndex) {
        this.replicaIndex = replicaIndex;
    }

    public String getReplicaCluster() {
        return replicaCluster;
    }

    public void setReplicaCluster(String replicaCluster) {
        this.replicaCluster = replicaCluster;
    }

    public Boolean getReplicationState() {
        return replicationState;
    }

    public void setReplicationState(Boolean replicationState) {
        this.replicationState = replicationState;
    }

    public int getShardId() {
        return shardId;
    }

    public void setShardId(int shardId) {
        this.shardId = shardId;
    }

    public long getPrimaryGlobalCheckpoint() {
        return primaryGlobalCheckpoint;
    }

    public void setPrimaryGlobalCheckpoint(long primaryGlobalCheckpoint) {
        this.primaryGlobalCheckpoint = primaryGlobalCheckpoint;
    }

    public long getPrimaryMaxSeqNo() {
        return primaryMaxSeqNo;
    }

    public void setPrimaryMaxSeqNo(long primaryMaxSeqNo) {
        this.primaryMaxSeqNo = primaryMaxSeqNo;
    }

    public long getReplicaGlobalCheckpoint() {
        return replicaGlobalCheckpoint;
    }

    public void setReplicaGlobalCheckpoint(long replicaGlobalCheckpoint) {
        this.replicaGlobalCheckpoint = replicaGlobalCheckpoint;
    }

    public long getReplicaMaxSeqNo() {
        return replicaMaxSeqNo;
    }

    public void setReplicaMaxSeqNo(long replicaMaxSeqNo) {
        this.replicaMaxSeqNo = replicaMaxSeqNo;
    }

    public long getTotalSendTimeMillis() {
        return totalSendTimeMillis;
    }

    public void setTotalSendTimeMillis(long totalSendTimeMillis) {
        this.totalSendTimeMillis = totalSendTimeMillis;
    }

    public long getSuccessfulSendRequests() {
        return successfulSendRequests;
    }

    public void setSuccessfulSendRequests(long successfulSendRequests) {
        this.successfulSendRequests = successfulSendRequests;
    }

    public long getFailedSendRequests() {
        return failedSendRequests;
    }

    public void setFailedSendRequests(long failedSendRequests) {
        this.failedSendRequests = failedSendRequests;
    }

    public long getOperationsSends() {
        return operationsSends;
    }

    public void setOperationsSends(long operationsSends) {
        this.operationsSends = operationsSends;
    }

    public long getBytesSend() {
        return bytesSend;
    }

    public void setBytesSend(long bytesSend) {
        this.bytesSend = bytesSend;
    }

    public long getTimeSinceLastSendMillis() {
        return timeSinceLastSendMillis;
    }

    public void setTimeSinceLastSendMillis(long timeSinceLastSendMillis) {
        this.timeSinceLastSendMillis = timeSinceLastSendMillis;
    }

    public String getCommitOffsetStr() {
        return commitOffsetStr;
    }

    public void setCommitOffsetStr(String commitOffsetStr) {
        this.commitOffsetStr = commitOffsetStr;
    }

    public String getCurrentOffsetStr() {
        return currentOffsetStr;
    }

    public void setCurrentOffsetStr(String currentOffsetStr) {
        this.currentOffsetStr = currentOffsetStr;
    }

    public boolean isReplicationState() {
        return replicationState;
    }

    public void setReplicationState(boolean replicationState) {
        this.replicationState = replicationState;
    }

    public List<TranslogOffsetGapTuple> getInSyncOffset() {
        return inSyncOffset;
    }

    public void setInSyncOffset(List<TranslogOffsetGapTuple> inSyncOffset) {
        this.inSyncOffset = inSyncOffset;
    }

    public long getTimeSinceUpdateReplicaCheckPoint() {
        return timeSinceUpdateReplicaCheckPoint;
    }

    public void setTimeSinceUpdateReplicaCheckPoint(long timeSinceUpdateReplicaCheckPoint) {
        this.timeSinceUpdateReplicaCheckPoint = timeSinceUpdateReplicaCheckPoint;
    }
}
