package com.didi.arius.gateway.elasticsearch.client.gateway.search.response;

import com.didi.arius.gateway.elasticsearch.client.utils.XContentParserUtils;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.Map;

public class FailReason implements ToXContent {
    private static final String TYPE = "type";
    private static final String REASON = "reason";
    private static final String CAUSED_BY = "caused_by";

    private String type;

    private String reason;

    private Map<String, Object> causedBy;

    public FailReason() {
        // pass
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Map<String, Object> getCausedBy() {
        return causedBy;
    }

    public void setCausedBy(Map<String, Object> causedBy) {
        this.causedBy = causedBy;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field(TYPE, type);
        builder.field(REASON, reason);
        builder.field(CAUSED_BY, causedBy);
        return builder;
    }

    public static FailReason fromXContent(XContentParser parser) throws IOException {
        XContentParser.Token token = parser.currentToken();
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, token, parser::getTokenLocation);

        String type = null;
        String reason = null;
        Map<String, Object> causedBy = null;

        String currentFieldName = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if (TYPE.equals(currentFieldName)) {
                    type = parser.text();
                } else if (REASON.equals(currentFieldName)) {
                    reason = parser.text();
                } else {
                    parser.skipChildren();
                }
            } else if (token == XContentParser.Token.START_OBJECT) {
                if (CAUSED_BY.equals(currentFieldName)) {
                    causedBy = parser.map();
                } else {
                    parser.skipChildren();
                }
            }
        }
        FailReason failReason = new FailReason();
        failReason.setReason(reason);
        failReason.setType(type);
        failReason.setCausedBy(causedBy);

        return failReason;
    }
}
