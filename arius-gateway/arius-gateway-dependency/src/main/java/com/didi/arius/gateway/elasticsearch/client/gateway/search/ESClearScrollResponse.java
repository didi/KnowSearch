package com.didi.arius.gateway.elasticsearch.client.gateway.search;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.utils.XContentParserUtils;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;

import java.io.IOException;

public class ESClearScrollResponse extends ESActionResponse implements ToXContent {
    private static final ParseField SUCCEEDED = new ParseField("succeeded");
    private static final ParseField NUMFREED = new ParseField("num_freed");

    private boolean succeeded;
    private int numFreed;

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(SUCCEEDED.getPreferredName(), succeeded);
        builder.field(NUMFREED.getPreferredName(), numFreed);
        builder.endObject();
        return builder;
    }

    /**
     * Parse the clear scroll response body into a new {@link ClearScrollResponse} object
     */
    public static ESClearScrollResponse fromXContent(XContentParser parser) throws IOException {
        XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.nextToken(), parser::getTokenLocation);
        String currentFieldName = parser.currentName();

        ESClearScrollResponse esClearScrollResponse = new ESClearScrollResponse();
        for (Token token = parser.nextToken(); token != Token.END_OBJECT; token = parser.nextToken()) {
            if (token == Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if (SUCCEEDED.getPreferredName().equals(currentFieldName)) {
                    esClearScrollResponse.succeeded = parser.booleanValue();
                } else if (NUMFREED.getPreferredName().equals(currentFieldName)) {
                    esClearScrollResponse.numFreed = parser.intValue();
                } else {
                    parser.skipChildren();
                }
            } else if (token == Token.START_OBJECT) {
                parser.skipChildren();
            }
        }

        return  esClearScrollResponse;
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
