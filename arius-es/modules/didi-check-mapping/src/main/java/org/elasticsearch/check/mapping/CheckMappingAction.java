package org.elasticsearch.check.mapping;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

import static org.elasticsearch.action.ValidateActions.addValidationError;

/**
 * @author didi
 */
public class CheckMappingAction extends ActionType<AcknowledgedResponse> {

    public static final CheckMappingAction INSTANCE = new CheckMappingAction();
    public static final String NAME = "indices:admin/mapping/check";

    private CheckMappingAction() {
        super(NAME, AcknowledgedResponse::new);
    }

    public static class Request extends AcknowledgedRequest<Request> implements IndicesRequest, ToXContentObject {

        private static final ParseField SOURCE_FIELD = new ParseField("source");

        private static final ObjectParser<Request, Void> PARSER = new ObjectParser<>(
            NAME,
            () -> {
                Request request = new Request();
                return request;
            }
        );

        static {
            PARSER.declareString(Request::setSource, SOURCE_FIELD);
        }

        private String index;

        private String type;

        private String source;

        public Request() {}

        @Override
        public ActionRequestValidationException validate() {
            ActionRequestValidationException e = null;
            if (source == null) {
                e = addValidationError(SOURCE_FIELD.getPreferredName() + " is missing", e);
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
            index = in.readOptionalString();
            type = in.readOptionalString();
            source = in.readString();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeOptionalString(index);
            out.writeOptionalString(type);
            out.writeString(source);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            if (source != null) {
                try (InputStream stream = new BytesArray(source).streamInput()) {
                    builder.rawValue(stream, XContentType.JSON);
                }
            } else {
                builder.startObject().endObject();
            }
            return builder;
        }

        /**
         * The mapping source definition.
         */
        public void source(BytesReference mappingSource, XContentType xContentType) {
            Objects.requireNonNull(xContentType);
            try {
                this.source = XContentHelper.convertToJson(mappingSource, false, false, xContentType);
            } catch (IOException e) {
                throw new UncheckedIOException("failed to convert source to json", e);
            }
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
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
            return Objects.equals(source, request.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source);
        }
    }

}
