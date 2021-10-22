package com.didi.arius.gateway.elasticsearch.client.gateway.search;

import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.utils.XContentParserUtils;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ESMultiSearchResponse extends ESActionResponse implements ToXContent {

    static final class Fields {
        static final String RESPONSES = "responses";
        static final String STATUS = "status";
        static final String ERROR = "error";
    }

    @JSONField(name="responses")
    private List<Item> responses;

    public List<Item> getResponses() {
        return responses;
    }

    public void setResponses(List<Item> responses) {
        this.responses = responses;
    }

    /**
     * A search response item, holding the actual search response, or an error message if it failed.
     */
    public static class Item {
        private ESSearchResponse response;
        private Object exception;

        private int status;

        Item() {

        }

        public Item(ESSearchResponse response, Object exception) {
            this.response = response;
            this.exception = exception;
        }

        /**
         * Is it a failed search?
         */
        public boolean isFailure() {
            return exception != null;
        }

        /**
         * The actual search response, null if its a failure.
         */
        @Nullable
        public ESSearchResponse getResponse() {
            return this.response;
        }

        public Object getFailure() {
            return exception;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    public static ESMultiSearchResponse fromXContent(XContentParser parser) throws IOException {
        XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.nextToken(), parser::getTokenLocation);
        parser.nextToken();

        XContentParserUtils.ensureExpectedToken(Token.FIELD_NAME, parser.currentToken(), parser::getTokenLocation);
        String currentFieldName = parser.currentName();

        ESMultiSearchResponse esMultiSearchResponse = new ESMultiSearchResponse();
        for (Token token = parser.nextToken(); token != Token.END_OBJECT; token = parser.nextToken()) {
            if (token == Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == Token.START_ARRAY) {
                if (Fields.RESPONSES.equals(currentFieldName)) {
                    esMultiSearchResponse.responses = new ArrayList<>();
                    while ((token = parser.nextToken()) != Token.END_ARRAY) {
                        esMultiSearchResponse.responses.add(itemFromXContent(parser));
                    }
                } else {
                    parser.skipChildren();
                }
            } else {
                parser.skipChildren();
            }
        }

        return esMultiSearchResponse;
    }

    private static Item itemFromXContent(XContentParser parser) throws IOException {
        // This parsing logic is a bit tricky here, because the multi search response itself is tricky:
        // 1) The json objects inside the responses array are either a search response or a serialized exception
        // 2) Each response json object gets a status field injected that ElasticsearchException.failureFromXContent(...) does not parse,
        //    but SearchResponse.innerFromXContent(...) parses and then ignores. The status field is not needed to parse
        //    the response item. However in both cases this method does need to parse the 'status' field otherwise the parsing of
        //    the response item in the next json array element will fail due to parsing errors.

        Item item = null;
        String fieldName = null;

        Token token = parser.nextToken();
        assert token == Token.FIELD_NAME;

        outer: for (; token != Token.END_OBJECT; token = parser.nextToken()) {
            switch (token) {
                case FIELD_NAME:
                    fieldName = parser.currentName();
                    if (Fields.ERROR.equals(fieldName)) {
                        token = parser.nextToken();
                        Object err;
                        if (token == Token.START_OBJECT) {
                            err = parser.map();
                        } else {
                            err = parser.objectText();
                        }
                        item = new Item(null, err);
                    } else if (Fields.STATUS.equals(fieldName)) {
                        token = parser.nextToken();
                        item.status = parser.intValue();
                    } else {
                        item = new Item(ESSearchResponse.innerFromXContent(parser), null);
                        if (item.getResponse().getStatus() != null) {
                            item.status = item.getResponse().getStatus();
                        }
                        break outer;
                    }
                    break;
            }
        }
        assert parser.currentToken() == Token.END_OBJECT;
        return item;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.startArray(Fields.RESPONSES);


        for (Item item : responses) {
            builder.startObject();
            if (item.getFailure() == null) {
                item.getResponse().toXContent(builder, params);
            } else {
                builder.field(Fields.ERROR, item.exception);
            }

            if (item.status > 0) {
                builder.field(Fields.STATUS, item.status);
            }

            builder.endObject();
        }
        builder.endArray();
        builder.endObject();

        return builder;
    }

    public RestResponse buildRestResponse(RestChannel channel) {
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
