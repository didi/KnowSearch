package org.elasticsearch.dcdr.translog.primary;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import org.elasticsearch.cluster.AbstractDiffable;
import org.elasticsearch.cluster.Diffable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

/**
 * author weizijun
 * date：2019-08-09
 */
public class DCDRIndexMetadata extends AbstractDiffable<DCDRIndexMetadata> implements ToXContentObject, Diffable<DCDRIndexMetadata> {

    private static final ParseField PRIMARY_INDEX_FIELD = new ParseField("primary_index");
    private static final ParseField REPLICA_INDEX_FIELD = new ParseField("replica_index");
    private static final ParseField REPLICA_CLUSTER_FIELD = new ParseField("replica_cluster");
    private static final ParseField REPLICATION_STATE_FIELD = new ParseField("replication_state");

    @SuppressWarnings("unchecked")
    public static final ConstructingObjectParser<DCDRIndexMetadata, String> PARSER = new ConstructingObjectParser<>(
        "replica_info",
        a -> new DCDRIndexMetadata((String) a[0], (String) a[1], (String) a[2], (Boolean) a[3])
    );

    static {
        PARSER.declareString(ConstructingObjectParser.constructorArg(), PRIMARY_INDEX_FIELD);
        PARSER.declareString(ConstructingObjectParser.constructorArg(), REPLICA_INDEX_FIELD);
        PARSER.declareString(ConstructingObjectParser.constructorArg(), REPLICA_CLUSTER_FIELD);
        PARSER.declareBoolean(ConstructingObjectParser.constructorArg(), REPLICATION_STATE_FIELD);
    }

    public static DCDRIndexMetadata parse(XContentParser parser, String name) {
        return PARSER.apply(parser, name);
    }

    private String primaryIndex;
    private String replicaIndex;
    private String replicaCluster;
    private Boolean replicationState;

    public DCDRIndexMetadata(StreamInput in) throws IOException {
        this.primaryIndex = in.readString();
        this.replicaIndex = in.readString();
        this.replicaCluster = in.readString();
        this.replicationState = in.readBoolean();
    }

    public DCDRIndexMetadata(String primaryIndex, String replicaIndex, String replicaCluster, Boolean replicationState) {
        this.primaryIndex = primaryIndex;
        this.replicaIndex = replicaIndex;
        this.replicaCluster = replicaCluster;
        this.replicationState = replicationState;
    }

    public String getPrimaryIndex() {
        return primaryIndex;
    }

    public String getReplicaIndex() {
        return replicaIndex;
    }

    public String getReplicaCluster() {
        return replicaCluster;
    }

    public void setReplicaIndex(String replicaIndex) {
        this.replicaIndex = replicaIndex;
    }

    public void setReplicaCluster(String replicaCluster) {
        this.replicaCluster = replicaCluster;
    }

    public void setPrimaryIndex(String primaryIndex) {
        this.primaryIndex = primaryIndex;
    }

    public Boolean getReplicationState() {
        return replicationState;
    }

    public void setReplicationState(Boolean replicationState) {
        this.replicationState = replicationState;
    }

    public static String name(String primaryIndex, String replicaIndex, String replicaCluster) {
        return String.format(Locale.US, "%s/%s(%s)", primaryIndex, replicaIndex, replicaCluster);
    }

    public String name() {
        return DCDRIndexMetadata.name(primaryIndex, replicaIndex, replicaCluster);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DCDRIndexMetadata that = (DCDRIndexMetadata) o;
        return Objects.equals(replicaIndex, that.replicaIndex) &&
            Objects.equals(replicaCluster, that.replicaCluster) &&
            Objects.equals(primaryIndex, that.primaryIndex) &&
            Objects.equals(replicationState, that.replicationState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(replicaIndex, replicaCluster, primaryIndex, replicationState);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(primaryIndex);
        out.writeString(replicaIndex);
        out.writeString(replicaCluster);
        out.writeBoolean(replicationState);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(PRIMARY_INDEX_FIELD.getPreferredName(), primaryIndex);
        builder.field(REPLICA_INDEX_FIELD.getPreferredName(), replicaIndex);
        builder.field(REPLICA_CLUSTER_FIELD.getPreferredName(), replicaCluster);
        builder.field(REPLICATION_STATE_FIELD.getPreferredName(), replicationState);
        builder.endObject();
        return builder;
    }

    @Override
    public String toString() {
        return "{" +
            "pi='" + primaryIndex + '\'' +
            ", ri='" + replicaIndex + '\'' +
            ", rc='" + replicaCluster + '\'' +
            ", rs='" + replicationState + '\'' +
            '}';
    }
}
