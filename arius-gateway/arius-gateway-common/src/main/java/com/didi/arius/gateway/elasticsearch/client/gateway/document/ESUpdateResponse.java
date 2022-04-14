package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import com.didi.arius.gateway.elasticsearch.client.utils.XContentParserUtils;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;

public class ESUpdateResponse extends DocWriteResponse implements ToXContent {

    private static final String GET = "get";

    private ESGetResponse esGetResponse;

    public ESUpdateResponse() {}

    private ESUpdateResponse(String index, String type, String id, long seqNo, long primaryTerm, long version, Result result, boolean found, boolean created) {
        super(index, type, id, seqNo, primaryTerm, version, result, found, created);
    }

    public ESGetResponse getEsGetResponse() {
        return esGetResponse;
    }

    public void setEsGetResponse(ESGetResponse esGetResponse) {
        this.esGetResponse = esGetResponse;
    }

    public static ESUpdateResponse fromXContent(XContentParser parser) throws IOException {
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.nextToken(), parser::getTokenLocation);

        Builder context = new Builder();
        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            parseXContentFields(parser, context);
        }
        return context.build();
    }

    /**
     * Parse the current token and update the parsing context appropriately.
     */
    public static void parseXContentFields(XContentParser parser, Builder context) throws IOException {
        XContentParser.Token token = parser.currentToken();
        String currentFieldName = parser.currentName();

        if (GET.equals(currentFieldName)) {
            if (token == XContentParser.Token.START_OBJECT) {
                context.setEsGetResponse(ESGetResponse.fromXContentEmbedded(parser));
            }
        } else {
            parseInnerToXContent(parser, context);
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        interToXContent(builder, params);
        if (esGetResponse != null) {
            builder.startObject(GET);
            esGetResponse.toXContentEmbedded(builder, params);
            builder.endObject();
        }
        builder.endObject();
        return builder;
    }

    public static class Builder extends DocWriteResponse.Builder {

        private ESGetResponse esGetResponse = null;

        @Override
        public ESUpdateResponse build() {
            ESUpdateResponse response = new ESUpdateResponse(index, type, id, seqNo, primaryTerm, version, result, found, created);
            response.setForcedRefresh(forcedRefresh);
            response.setEsGetResponse(esGetResponse);
            if (shards != null) {
                response.setShards(shards);
            }

            if (result == null) {
                response.setResult(Result.UPDATED);
            }
            return response;
        }

        public void setEsGetResponse(ESGetResponse esGetResponse) {
            this.esGetResponse = esGetResponse;
        }
    }
}
