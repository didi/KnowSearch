package org.elasticsearch.dcdr.action;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.elasticsearch.action.*;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.dcdr.translog.primary.DCDRTemplateMetadata;

/**
 * author weizijun
 * date：2019-08-27
 */
public class GetAutoReplicationAction extends
    ActionType<GetAutoReplicationAction.Response> {
    public static final GetAutoReplicationAction INSTANCE = new GetAutoReplicationAction();
    public static final String NAME = "indices:admin/dcdr/get_auto_replication";

    private GetAutoReplicationAction() {
        super(NAME, Response::new);
    }

    public static class Request extends AcknowledgedRequest<GetAutoReplicationAction.Request> implements IndicesRequest, ToXContentObject {

        private static final ParseField NAME_FIELD = new ParseField("name");

        private static final ObjectParser<GetAutoReplicationAction.Request, Void> PARSER = new ObjectParser<>(
            NAME,
            () -> {
                GetAutoReplicationAction.Request request = new GetAutoReplicationAction.Request();
                return request;
            }
        );

        public static GetAutoReplicationAction.Request fromXContent(final XContentParser parser, final String name)
            throws IOException {
            GetAutoReplicationAction.Request request = PARSER.parse(parser, null);
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
            return null;
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
            name = in.readOptionalString();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeOptionalString(name);
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
            GetAutoReplicationAction.Request request = (GetAutoReplicationAction.Request) o;
            return Objects.equals(name, request.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    public static class Response extends ActionResponse implements ToXContentObject {

        private Map<String, DCDRTemplateMetadata> dcdrTemplateMetadatas;

        public Response(Map<String, DCDRTemplateMetadata> dcdrTemplateMetadatas) {
            this.dcdrTemplateMetadatas = dcdrTemplateMetadatas;
        }

        public Map<String, DCDRTemplateMetadata> getDcdrTemplateMetadatas() {
            return dcdrTemplateMetadatas;
        }

        public Response(StreamInput in) throws IOException {
            super(in);
            dcdrTemplateMetadatas = in.readMap(StreamInput::readString, DCDRTemplateMetadata::new);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeMap(dcdrTemplateMetadatas, StreamOutput::writeString, (out1, value) -> value.writeTo(out1));
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                builder.startArray("dcdrs");
                for (Map.Entry<String, DCDRTemplateMetadata> entry : dcdrTemplateMetadatas.entrySet()) {
                    entry.getValue().toXContent(builder, params);
                }
                builder.endArray();
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
            Response response = (Response) o;
            return Objects.equals(dcdrTemplateMetadatas, response.dcdrTemplateMetadatas);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dcdrTemplateMetadatas);
        }
    }
}
