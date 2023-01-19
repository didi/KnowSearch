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
 * author weizijun
 * date：2019-08-27
 */
public class DeleteReplicationAction extends
    ActionType<AcknowledgedResponse> {
    public static final DeleteReplicationAction INSTANCE = new DeleteReplicationAction();
    public static final String NAME = "indices:admin/dcdr/delete_replication";

    private DeleteReplicationAction() {
        super(NAME, AcknowledgedResponse::new);
    }

    public static class Request extends AcknowledgedRequest<DeleteReplicationAction.Request> implements IndicesRequest, ToXContentObject {

        private static final ParseField REPLICA_CLUSTER_FIELD = new ParseField("replica_cluster");
        private static final ParseField REPLICA_INDEX_FIELD = new ParseField("replica_index");
        private static final ParseField PRIMARY_INDEX_FIELD = new ParseField("primary_index");

        private static final ObjectParser<DeleteReplicationAction.Request, Void> PARSER = new ObjectParser<>(
            NAME,
            () -> {
                DeleteReplicationAction.Request request = new DeleteReplicationAction.Request();
                return request;
            }
        );

        static {
            PARSER.declareString(DeleteReplicationAction.Request::setReplicaCluster, REPLICA_CLUSTER_FIELD);
            PARSER.declareString(DeleteReplicationAction.Request::setReplicaIndex, REPLICA_INDEX_FIELD);
        }

        public static DeleteReplicationAction.Request fromXContent(final XContentParser parser, final String primaryIndex)
            throws IOException {
            DeleteReplicationAction.Request request = PARSER.parse(parser, null);
            request.setPrimaryIndex(primaryIndex);
            return request;
        }

        private String replicaIndex;
        private String replicaCluster;
        private String primaryIndex;

        public Request() {}

        @Override
        public ActionRequestValidationException validate() {
            ActionRequestValidationException e = null;
            if (replicaIndex == null) {
                e = addValidationError(REPLICA_INDEX_FIELD.getPreferredName() + " is missing", e);
            }
            if (replicaCluster == null) {
                e = addValidationError(REPLICA_CLUSTER_FIELD.getPreferredName() + " is missing", e);
            }
            if (primaryIndex == null) {
                e = addValidationError(PRIMARY_INDEX_FIELD.getPreferredName() + " is missing", e);
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
            replicaCluster = in.readString();
            replicaIndex = in.readString();
            primaryIndex = in.readString();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(replicaCluster);
            out.writeString(replicaIndex);
            out.writeString(primaryIndex);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                builder.field(REPLICA_CLUSTER_FIELD.getPreferredName(), replicaCluster);
                builder.field(REPLICA_INDEX_FIELD.getPreferredName(), replicaIndex);
                builder.field(PRIMARY_INDEX_FIELD.getPreferredName(), primaryIndex);
            }
            builder.endObject();
            return builder;
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

        public String getPrimaryIndex() {
            return primaryIndex;
        }

        public void setPrimaryIndex(String primaryIndex) {
            this.primaryIndex = primaryIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            DeleteReplicationAction.Request request = (DeleteReplicationAction.Request) o;
            return Objects.equals(replicaIndex, request.replicaIndex) &&
                Objects.equals(replicaCluster, request.replicaCluster) &&
                Objects.equals(primaryIndex, request.primaryIndex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(replicaIndex, replicaCluster, primaryIndex);
        }
    }
}
