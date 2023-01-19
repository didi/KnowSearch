package org.elasticsearch.dcdr.action;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.ElasticsearchClient;
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
 * date：2019-08-12
 */
public class CreateAutoReplicationAction extends
    ActionType<AcknowledgedResponse> {
    public static final CreateAutoReplicationAction INSTANCE = new CreateAutoReplicationAction();
    public static final String NAME = "indices:admin/dcdr/create_auto_replication";

    private CreateAutoReplicationAction() {
        super(NAME, AcknowledgedResponse::new);
    }

    public static class Request extends AcknowledgedRequest<Request> implements IndicesRequest, ToXContentObject {

        private static final ParseField REPLICA_CLUSTER_FIELD = new ParseField("replica_cluster");
        private static final ParseField TEMPLATE_FIELD = new ParseField("template");
        private static final ParseField NAME_FIELD = new ParseField("name");

        private static final ObjectParser<Request, Void> PARSER = new ObjectParser<>(
            NAME,
            () -> {
                Request request = new Request();
                return request;
            }
        );

        static {
            PARSER.declareString(Request::setReplicaCluster, REPLICA_CLUSTER_FIELD);
            PARSER.declareString(Request::setTemplate, TEMPLATE_FIELD);
        }

        public static Request fromXContent(final XContentParser parser, final String name) throws IOException {
            Request request = PARSER.parse(parser, null);
            request.setName(name);
            return request;
        }

        private String replicaCluster;
        private String template;
        private String name;

        public Request() {}

        @Override
        public ActionRequestValidationException validate() {
            ActionRequestValidationException e = null;
            if (template == null) {
                e = addValidationError(TEMPLATE_FIELD.getPreferredName() + " is missing", e);
            }
            if (replicaCluster == null) {
                e = addValidationError(REPLICA_CLUSTER_FIELD.getPreferredName() + " is missing", e);
            }
            if (name == null) {
                e = addValidationError(NAME_FIELD.getPreferredName() + " is missing", e);
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
            template = in.readString();
            name = in.readString();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(replicaCluster);
            out.writeString(template);
            out.writeString(name);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                builder.field(NAME_FIELD.getPreferredName(), name);
                builder.field(TEMPLATE_FIELD.getPreferredName(), template);
                builder.field(REPLICA_CLUSTER_FIELD.getPreferredName(), replicaCluster);
            }
            builder.endObject();
            return builder;
        }

        public String getReplicaCluster() {
            return replicaCluster;
        }

        public void setReplicaCluster(String replicaCluster) {
            this.replicaCluster = replicaCluster;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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
            return Objects.equals(name, request.name) &&
                Objects.equals(replicaCluster, request.replicaCluster) &&
                Objects.equals(template, request.template);
        }

        @Override
        public int hashCode() {
            return Objects.hash(replicaCluster, template);
        }
    }
}
