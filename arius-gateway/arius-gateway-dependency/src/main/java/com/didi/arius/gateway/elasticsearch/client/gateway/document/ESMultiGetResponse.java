package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ESMultiGetResponse extends ESActionResponse implements ToXContent {

    private static final ParseField INDEX = new ParseField("_index");
    private static final ParseField TYPE = new ParseField("_type");
    private static final ParseField ID = new ParseField("_id");
    private static final ParseField ERROR = new ParseField("error");
    private static final ParseField DOCS = new ParseField("docs");

    private List<Item> responses;

    public List<Item> getResponses() {
        return responses;
    }

    public void setResponses(List<Item> responses) {
        this.responses = responses;
    }

    public static class Failure implements ToXContent {

        private String index;
        private String type;
        private String id;
        private Map<String, Object> exception;

        Failure() {
        }

        public Failure(String index, String type, String id, Map<String, Object> exception) {
            this.index = index;
            this.type = type;
            this.id = id;
            this.exception = exception;
        }

        /**
         * The index name of the action.
         */
        public String getIndex() {
            return this.index;
        }

        /**
         * The type of the action.
         */
        public String getType() {
            return type;
        }

        /**
         * The id of the action.
         */
        public String getId() {
            return id;
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.field(INDEX.getPreferredName(), index);
            builder.field(TYPE.getPreferredName(), type);
            builder.field(ID.getPreferredName(), id);
            builder.field(ERROR.getPreferredName(), exception);
            builder.endObject();
            return builder;
        }

        public Map<String, Object> getFailure() {
            return exception;
        }
    }

    public static class Item {
        private ESGetResponse response;
        private Failure failure;
        private int status;

        Item() {

        }

        public Item(ESGetResponse response, Failure failure) {
            this.response = response;
            this.failure = failure;
        }

        /**
         * Is it a failed search?
         */
        public boolean isFailure() {
            return failure != null;
        }

        @Nullable
        public ESGetResponse getResponse() {
            return this.response;
        }

        public Failure getFailure() {
            return failure;
        }

        /**
         * The index name of the document.
         */
        public String getIndex() {
            if (failure != null) {
                return failure.getIndex();
            }
            return response.getIndex();
        }

        /**
         * The type of the document.
         */
        public String getType() {
            if (failure != null) {
                return failure.getType();
            }
            return response.getType();
        }

        /**
         * The id of the document.
         */
        public String getId() {
            if (failure != null) {
                return failure.getId();
            }
            return response.getId();
        }
    }

    public static ESMultiGetResponse fromXContent(XContentParser parser) throws IOException {
        String currentFieldName = null;
        List<Item> items = new ArrayList<>();
        for (Token token = parser.nextToken(); token != Token.END_OBJECT; token = parser.nextToken()) {
            switch (token) {
                case FIELD_NAME:
                    currentFieldName = parser.currentName();
                    break;
                case START_ARRAY:
                    if (DOCS.getPreferredName().equals(currentFieldName)) {
                        for (token = parser.nextToken(); token != Token.END_ARRAY; token = parser.nextToken()) {
                            if (token == Token.START_OBJECT) {
                                items.add(parseItem(parser));
                            }
                        }
                    }
                    break;
                default:
                    // If unknown tokens are encounter then these should be ignored, because
                    // this is parsing logic on the client side.
                    break;
            }
        }
        ESMultiGetResponse esMultiGetResponse = new ESMultiGetResponse();
        esMultiGetResponse.responses = items;
        return esMultiGetResponse;
    }

    private static Item parseItem(XContentParser parser) throws IOException {
        String currentFieldName = null;
        String index = null;
        String type = null;
        String id = null;
        Map<String, Object> exception = null;
        ESGetResponse esGetResponse = null;
        for (Token token = parser.nextToken(); token != Token.END_OBJECT; token = parser.nextToken()) {
            switch (token) {
                case FIELD_NAME:
                    currentFieldName = parser.currentName();
                    if (INDEX.getPreferredName().equals(currentFieldName) == false
                            && TYPE.getPreferredName().equals(currentFieldName) == false
                            && ID.getPreferredName().equals(currentFieldName) == false
                            && ERROR.getPreferredName().equals(currentFieldName) == false) {
                        esGetResponse = ESGetResponse.fromXContentEmbedded(parser, index, type, id);
                    }
                    break;
                case VALUE_STRING:
                    if (INDEX.getPreferredName().equals(currentFieldName)) {
                        index = parser.text();
                    } else if (TYPE.getPreferredName().equals(currentFieldName)) {
                        type = parser.text();
                    } else if (ID.getPreferredName().equals(currentFieldName)) {
                        id = parser.text();
                    }
                    break;
                case START_OBJECT:
                    if (ERROR.getPreferredName().equals(currentFieldName)) {
                        exception = parser.map();
                    }
                    break;
                default:
                    // If unknown tokens are encounter then these should be ignored, because
                    // this is parsing logic on the client side.
                    break;
            }
            if (esGetResponse != null) {
                break;
            }
        }

        if (exception != null) {
            return new Item(null, new Failure(index, type, id, exception));
        } else {
            return new Item(esGetResponse, null);
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.startArray(DOCS.getPreferredName());
        for (Item response : responses) {
            if (response.isFailure()) {
                Failure failure = response.getFailure();
                failure.toXContent(builder, params);
            } else {
                ESGetResponse getResponse = response.getResponse();
                getResponse.toXContent(builder, params);
            }
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

    @Override
    public org.elasticsearch.rest.RestResponse buildRestResponse(RestChannel channel) {
        try {
            XContentBuilder builder = channel.newBuilder();
            toXContent(builder, channel.request());
            return new BytesRestResponse(getRestStatus(), builder);
        } catch (IOException e) {
            return new BytesRestResponse(getRestStatus(), XContentType.JSON.restContentType(), toString());
        }
    }

    @Override
    public String toString() {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().prettyPrint();
            toXContent(builder, EMPTY_PARAMS);
            return builder.string();
        } catch (IOException e) {
            return "{ \"error\" : \"" + e.getMessage() + "\"}";
        }
    }
}
