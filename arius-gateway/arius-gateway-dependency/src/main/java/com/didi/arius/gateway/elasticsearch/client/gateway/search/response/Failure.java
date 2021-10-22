package com.didi.arius.gateway.elasticsearch.client.gateway.search.response;

import com.didi.arius.gateway.elasticsearch.client.utils.XContentParserUtils;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;

public class Failure implements ToXContent {
    private static final String REASON_FIELD = "reason";
    private static final String NODE_FIELD = "node";
    private static final String INDEX_FIELD = "index";
    private static final String SHARD_FIELD = "shard";

    private int shard;

    private String index;

    private String node;

    private FailReason reason;

    public int getShard() {
        return shard;
    }

    public void setShard(int shard) {
        this.shard = shard;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public FailReason getReason() {
        return reason;
    }

    public void setReason(FailReason reason) {
        this.reason = reason;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field("shard", shard);
        builder.field("index", index);
        if (node != null) {
            builder.field("node", node);
        }

        if (reason != null) {
            builder.field("reason");
            builder.startObject();
            reason.toXContent(builder, params);
            builder.endObject();
        }
        return builder;
    }

    public static Failure fromXContent(XContentParser parser) throws IOException {
        XContentParser.Token token;
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser::getTokenLocation);
        String currentFieldName = null;
        int shardId = -1;
        String indexName = null;
        String nodeId = null;
        FailReason failReason = null;
        while((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if (SHARD_FIELD.equals(currentFieldName)) {
                    shardId  = parser.intValue();
                } else if (INDEX_FIELD.equals(currentFieldName)) {
                    indexName  = parser.text();
                } else if (NODE_FIELD.equals(currentFieldName)) {
                    nodeId  = parser.text();
                } else {
                    parser.skipChildren();
                }
            } else if (token == XContentParser.Token.START_OBJECT) {
                if (REASON_FIELD.equals(currentFieldName)) {
                    failReason = FailReason.fromXContent(parser);
                } else {
                    parser.skipChildren();
                }
            } else {
                parser.skipChildren();
            }
        }

        Failure failure = new Failure();
        failure.setShard(shardId);
        failure.setIndex(indexName);
        failure.setReason(failReason);
        failure.setNode(nodeId);

        return failure;
    }


}
