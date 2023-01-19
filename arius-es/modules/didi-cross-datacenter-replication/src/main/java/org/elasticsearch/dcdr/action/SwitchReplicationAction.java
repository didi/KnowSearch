package org.elasticsearch.dcdr.action;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.Objects;

import static org.elasticsearch.action.ValidateActions.addValidationError;

/**
 * author zhz
 * date：2019-08-27
 */
public class SwitchReplicationAction extends
    ActionType<AcknowledgedResponse> {
    public static final SwitchReplicationAction INSTANCE = new SwitchReplicationAction();
    public static final String NAME = "indices:admin/dcdr/switch_replication";

    private SwitchReplicationAction() {
        super(NAME, AcknowledgedResponse::new);
    }

    public static class Request extends AcknowledgedRequest<Request> implements IndicesRequest, ToXContentObject {

        private static final ParseField REPLICA_CLUSTER_FIELD = new ParseField("replica_cluster");
        private static final ParseField REPLICA_INDEX_FIELD = new ParseField("replica_index");
        private static final ParseField PRIMARY_INDEX_FIELD = new ParseField("primary_index");
        private static final ParseField REPLICATION_STATE_FIELD = new ParseField("replication_state");

        private static final ObjectParser<Request, Void> PARSER = new ObjectParser<>(
            NAME,
            () -> {
                Request request = new Request();
                return request;
            }
        );

        static {
            PARSER.declareString(Request::setReplicaCluster, REPLICA_CLUSTER_FIELD);
            PARSER.declareString(Request::setReplicaIndex, REPLICA_INDEX_FIELD);
        }

        public static Request fromXContent(
            final XContentParser parser,
            final String primaryIndex,
            final Boolean replicationState
        )
            throws IOException {
            SwitchReplicationAction.Request request = PARSER.parse(parser, null);
            request.setPrimaryIndex(primaryIndex);
            request.setReplicationState(replicationState);
            return request;
        }

        private String primaryIndex;
        private String replicaCluster;
        private String replicaIndex;
        private Boolean replicationState;

        public Request() {}

        @Override
        public ActionRequestValidationException validate() {
            ActionRequestValidationException e = null;

            if (primaryIndex == null) {
                e = addValidationError(PRIMARY_INDEX_FIELD.getPreferredName() + " is missing", e);
            }

            if (replicationState == null) {
                e = addValidationError(REPLICATION_STATE_FIELD.getPreferredName() + " is missing", e);
            }

            if (replicaIndex == null) {
                e = addValidationError(REPLICA_INDEX_FIELD.getPreferredName() + " is missing", e);
            }

            if (replicaCluster == null) {
                e = addValidationError(REPLICA_CLUSTER_FIELD.getPreferredName() + " is missing", e);
            }

            return e;
        }

        @Override
        public String[] indices() {
            return new String[0];
        }

        @Override
        public IndicesOptions indicesOptions() {
            return IndicesOptions.strictSingleIndexNoExpandForbidClosed();
        }

        public Request(StreamInput in) throws IOException {
            super(in);
            primaryIndex = in.readString();
            replicaIndex = in.readString();
            replicaCluster = in.readString();
            replicationState = in.readBoolean();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(primaryIndex);
            out.writeString(replicaIndex);
            out.writeString(replicaCluster);
            out.writeBoolean(replicationState);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                builder.field(PRIMARY_INDEX_FIELD.getPreferredName(), primaryIndex);
                builder.field(REPLICA_INDEX_FIELD.getPreferredName(), replicaIndex);
                builder.field(REPLICA_CLUSTER_FIELD.getPreferredName(), replicaCluster);
                builder.field(REPLICATION_STATE_FIELD.getPreferredName(), replicationState);
            }
            builder.endObject();
            return builder;
        }

        public String getPrimaryIndex() {
            return primaryIndex;
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

        public String getReplicaCluster() {
            return replicaCluster;
        }

        public void setReplicaCluster(String replicaCluster) {
            this.replicaCluster = replicaCluster;
        }

        public String getReplicaIndex() {
            return replicaIndex;
        }

        public void setReplicaIndex(String replicaIndex) {
            this.replicaIndex = replicaIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Request request = (Request) o;
            return Objects.equals(primaryIndex, request.primaryIndex) && Objects.equals(replicationState, request.replicationState)
                && Objects.equals(replicaIndex, request.replicaIndex) && Objects.equals(replicaCluster, request.replicaCluster);
        }

        @Override
        public int hashCode() {
            return Objects.hash(primaryIndex, replicaIndex, replicaCluster, replicationState);
        }
    }
}
