package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import com.didi.arius.gateway.elasticsearch.client.utils.XContentParserUtils;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;

public class ESDeleteResponse extends DocWriteResponse implements ToXContent {
    public static ESDeleteResponse fromXContent(XContentParser parser) throws IOException {
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.nextToken(), parser::getTokenLocation);

        Builder context = new Builder();
        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            parseInnerToXContent(parser, context);
        }
        return context.build();
    }

    public ESDeleteResponse() {
    }

    private ESDeleteResponse(String index, String type, String id, long seqNo, long primaryTerm, long version, Result result, boolean found, boolean created) {
        super(index, type, id, seqNo, primaryTerm, version, result, found, created);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        interToXContent(builder, params);
        if (result == Result.DELETED) {
            builder.field(FOUND, true);
        } else {
            builder.field(FOUND, false);
        }
        builder.endObject();
        return builder;
    }

    public static class Builder extends DocWriteResponse.Builder {

        @Override
        public ESDeleteResponse build() {
            ESDeleteResponse esDeleteResponse = new ESDeleteResponse(index, type, id, seqNo, primaryTerm, version, result, found, created);
            esDeleteResponse.setForcedRefresh(forcedRefresh);
            if (shards != null) {
                esDeleteResponse.setShards(shards);
            }

            if (result == null) {
                if (found = true) {
                    result = Result.DELETED;
                } else {
                    result = Result.NOT_FOUND;
                }

                esDeleteResponse.setResult(result);
            }

            return esDeleteResponse;
        }
    }
}
