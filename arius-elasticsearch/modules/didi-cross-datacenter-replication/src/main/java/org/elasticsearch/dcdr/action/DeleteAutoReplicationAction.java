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
public class DeleteAutoReplicationAction extends
    ActionType<AcknowledgedResponse> {
    public static final DeleteAutoReplicationAction INSTANCE = new DeleteAutoReplicationAction();
    public static final String NAME = "indices:admin/dcdr/delete_auto_replication";

    private DeleteAutoReplicationAction() {
        super(NAME, AcknowledgedResponse::new);
    }

    public static class Request extends AcknowledgedRequest<DeleteAutoReplicationAction.Request> implements IndicesRequest,
        ToXContentObject {

        private static final ParseField NAME_FIELD = new ParseField("name");

        private static final ObjectParser<DeleteAutoReplicationAction.Request, Void> PARSER = new ObjectParser<>(
            NAME,
            () -> {
                DeleteAutoReplicationAction.Request request = new DeleteAutoReplicationAction.Request();
                return request;
            }
        );

        public static DeleteAutoReplicationAction.Request fromXContent(final XContentParser parser, final String name)
            throws IOException {
            DeleteAutoReplicationAction.Request request = PARSER.parse(parser, null);
            request.setName(name);
            return request;
        }

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Request() {}

        @Override
        public ActionRequestValidationException validate() {
            ActionRequestValidationException e = null;
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
            name = in.readString();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(name);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                builder.field(NAME_FIELD.getPreferredName(), name);
            }
            builder.endObject();
            return builder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DeleteAutoReplicationAction.Request request = (DeleteAutoReplicationAction.Request) o;
            return Objects.equals(name, request.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
