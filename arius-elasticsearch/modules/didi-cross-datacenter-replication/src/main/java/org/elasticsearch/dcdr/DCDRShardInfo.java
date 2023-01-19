package org.elasticsearch.dcdr;

import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.engine.CommitStats;
import org.elasticsearch.index.seqno.SeqNoStats;
import org.elasticsearch.index.shard.ShardId;

import java.io.IOException;

/**
 * author weizijun
 * date：2019-12-11
 */
public class DCDRShardInfo implements Writeable, ToXContentFragment {

    private static final ParseField HISTORY_UUID_FIELD = new ParseField("history_uuid");
    private static final ParseField CHECK_POINT_FIELD = new ParseField("check_point");
    private static final ParseField SHARD_ID_FIELD = new ParseField("shard_id");
    private static final ParseField DISCOVERY_NODE_FIELD = new ParseField("discovery_node");
    private static final ParseField COMMIT_STATS_FIELD = new ParseField("commit_stats");
    private static final ParseField SEQ_NO_STATS_FIELD = new ParseField("seq_no_stats");

    private String historyUUID;

    private long checkPoint;

    private ShardId shardId;

    @Nullable
    private DiscoveryNode discoveryNode;

    @Nullable
    private CommitStats commitStats;
    @Nullable
    private SeqNoStats seqNoStats;

    public DCDRShardInfo(
        DiscoveryNode discoveryNode, String historyUUID, long checkPoint,
        ShardId shardId, CommitStats commitStats, SeqNoStats seqNoStats
    ) {
        this.discoveryNode = discoveryNode;
        this.historyUUID = historyUUID;
        this.checkPoint = checkPoint;
        this.shardId = shardId;
        this.commitStats = commitStats;
        this.seqNoStats = seqNoStats;
    }

    public DCDRShardInfo(StreamInput in) throws IOException {
        readFrom(in);
    }

    public void readFrom(StreamInput in) throws IOException {
        historyUUID = in.readString();
        checkPoint = in.readLong();
        shardId = new ShardId(in);
        discoveryNode = in.readOptionalWriteable(DiscoveryNode::new);
        commitStats = CommitStats.readOptionalCommitStatsFrom(in);
        seqNoStats = in.readOptionalWriteable(SeqNoStats::new);

    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(historyUUID);
        out.writeLong(checkPoint);
        shardId.writeTo(out);
        out.writeOptionalWriteable(discoveryNode);
        out.writeOptionalWriteable(commitStats);
        out.writeOptionalWriteable(seqNoStats);
    }

    public String getHistoryUUID() {
        return historyUUID;
    }

    public long getCheckPoint() {
        return checkPoint;
    }

    public DiscoveryNode getDiscoveryNode() {
        return discoveryNode;
    }

    public ShardId getShardId() {
        return shardId;
    }

    public CommitStats getCommitStats() {
        return commitStats;
    }

    public SeqNoStats getSeqNoStats() {
        return seqNoStats;
    }

    @Override
    public String toString() {
        return "DCDRShardInfo{" +
            "historyUUID='" + historyUUID + '\'' +
            ", checkPoint=" + checkPoint +
            ", discoveryNode=" + discoveryNode +
            ", shardId=" + shardId +
            '}';
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field(HISTORY_UUID_FIELD.getPreferredName(), historyUUID);
        builder.field(CHECK_POINT_FIELD.getPreferredName(), checkPoint);
        builder.field(SHARD_ID_FIELD.getPreferredName(), shardId);

        if (discoveryNode != null) {
            builder.startObject(DISCOVERY_NODE_FIELD.getPreferredName());
            discoveryNode.toXContent(builder, params);
            builder.endObject();
        }

        if (commitStats != null) {
            commitStats.toXContent(builder, params);
        }

        if (seqNoStats != null) {
            seqNoStats.toXContent(builder, params);
        }
        return builder;
    }
}
