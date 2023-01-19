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
import org.elasticsearch.dcdr.translog.primary.DCDRIndexMetadata;

/**
 * author weizijun
 * date：2019-08-27
 */
public class GetReplicationAction extends
    ActionType<GetReplicationAction.Response> {
    public static final GetReplicationAction INSTANCE = new GetReplicationAction();
    public static final String NAME = "indices:admin/dcdr/get_replication";

    private GetReplicationAction() {
        super(NAME, Response::new );
    }

    public static class Request extends AcknowledgedRequest<GetReplicationAction.Request> implements IndicesRequest, ToXContentObject {

        private static final ParseField NAME_FIELD = new ParseField("primaryIndex");

        private static final ObjectParser<GetReplicationAction.Request, Void> PARSER = new ObjectParser<>(
            NAME,
            () -> {
                GetReplicationAction.Request request = new GetReplicationAction.Request();
                return request;
            }
        );

        public static GetReplicationAction.Request fromXContent(final XContentParser parser, final String primaryIndex)
            throws IOException {
            GetReplicationAction.Request request = PARSER.parse(parser, null);
            request.setPrimaryIndex(primaryIndex);
            return request;
        }

        private String primaryIndex;

        public String getPrimaryIndex() {
            return primaryIndex;
        }

        public void setPrimaryIndex(String primaryIndex) {
            this.primaryIndex = primaryIndex;
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
            primaryIndex = in.readOptionalString();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeOptionalString(primaryIndex);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                builder.field(NAME_FIELD.getPreferredName(), primaryIndex);
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
            GetReplicationAction.Request request = (GetReplicationAction.Request) o;
            return Objects.equals(primaryIndex, request.primaryIndex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(primaryIndex);
        }
    }

    public static class Response extends ActionResponse implements ToXContentObject {

        private Map<String, DCDRIndexMetadata> dcdrIndexMetadatas;

        public Response(Map<String, DCDRIndexMetadata> dcdrIndexMetadatas) {
            this.dcdrIndexMetadatas = dcdrIndexMetadatas;
        }

        public Map<String, DCDRIndexMetadata> getDcdrIndexMetadatas() {
            return dcdrIndexMetadatas;
        }

        public Response(StreamInput in) throws IOException {
            super(in);
            dcdrIndexMetadatas = in.readMap(StreamInput::readString, DCDRIndexMetadata::new);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeMap(dcdrIndexMetadatas, StreamOutput::writeString, (out1, value) -> value.writeTo(out1));
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                builder.startArray("dcdrs");
                for (Map.Entry<String, DCDRIndexMetadata> entry : dcdrIndexMetadatas.entrySet()) {
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
            return Objects.equals(dcdrIndexMetadatas, response.dcdrIndexMetadatas);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dcdrIndexMetadatas);
        }
    }
}
