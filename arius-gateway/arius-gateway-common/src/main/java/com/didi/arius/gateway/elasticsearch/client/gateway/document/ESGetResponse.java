package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.utils.XContentParserUtils;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.mapper.internal.SourceFieldMapper;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESGetResponse extends ESActionResponse implements ToXContent {
    /**
     * 索引名称
     */
    private String index;
    /**
     * type名称
     */
    private String type;

    /**
     * 主键id
     */
    private String id;

    /**
     * 版本
     */
    private long version = -1;
    /**
     * 得分
     */
    private boolean exists;
    /**
     * source
     */
    private Map<String, Object> source;

    private Map<String, List<Object>> fields;

    private Map<String, Object> otherFields = new HashMap<>();

    public ESGetResponse() {
        // pass
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

    public Map<String, List<Object>> getFields() {
        return fields;
    }

    public void setFields(Map<String, List<Object>> fields) {
        this.fields = fields;
    }


    /**
     * Does the document exists.
     */
    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public static ESGetResponse fromXContent(XContentParser parser) throws IOException {
        XContentParser.Token token = parser.nextToken();
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, token, parser::getTokenLocation);

        return fromXContentEmbedded(parser);
    }

    public static ESGetResponse fromXContentEmbedded(XContentParser parser) throws IOException {
        XContentParser.Token token = parser.nextToken();
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.FIELD_NAME, token, parser::getTokenLocation);
        return fromXContentEmbedded(parser, null, null, null);
    }

    public static ESGetResponse fromXContentEmbedded(XContentParser parser, String index, String type, String id) throws IOException {
        XContentParser.Token token = parser.currentToken();
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.FIELD_NAME, token, parser::getTokenLocation);

        ESGetResponse esGetResponse = new ESGetResponse();
        esGetResponse.index = index;
        esGetResponse.type = type;
        esGetResponse.id = id;
        String currentFieldName = parser.currentName();
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if (INDEX.equals(currentFieldName)) {
                    esGetResponse.index = parser.text();
                } else if (TYPE.equals(currentFieldName)) {
                    esGetResponse.type = parser.text();
                } else if (ID.equals(currentFieldName)) {
                    esGetResponse.id = parser.text();
                }  else if (VERSION.equals(currentFieldName)) {
                    esGetResponse.version = parser.longValue();
                } else if (FOUND.equals(currentFieldName)) {
                    esGetResponse.exists = parser.booleanValue();
                } else {
                    esGetResponse.otherFields.put(currentFieldName, parser.objectText());
                }
            } else if (token == XContentParser.Token.START_OBJECT) {
                startObjDeal(parser, esGetResponse, currentFieldName);
            } else if (token == XContentParser.Token.START_ARRAY) {
                esGetResponse.otherFields.put(currentFieldName, parser.list());
            }
        }

        return esGetResponse;
    }

    private static void startObjDeal(XContentParser parser, ESGetResponse esGetResponse, String currentFieldName) throws IOException {
        XContentParser.Token token;
        if (SourceFieldMapper.NAME.equals(currentFieldName)) {
            esGetResponse.source = parser.map();
        } else if (FIELDS.equals(currentFieldName)) {
            esGetResponse.fields = new HashMap<>();
            while(parser.nextToken() != XContentParser.Token.END_OBJECT) {
                String key = parser.currentName();
                token = parser.nextToken();
                XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, token, parser::getTokenLocation);
                esGetResponse.fields.put(key, parser.list());
            }
        } else {
            esGetResponse.otherFields.put(currentFieldName, parser.map());
        }
    }

    public Map<String, Object> getSource() {
        return source;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }

    public static final String INDEX = "_index";
    public static final String TYPE = "_type";
    public static final String ID = "_id";
    private static final String VERSION = "_version";
    private static final String FOUND = "found";
    private static final String FIELDS = "fields";
    private static final String SOURCE = "_source";

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(INDEX, index);
        builder.field(TYPE, type);
        builder.field(ID, id);
        if (isExists()) {
            if (version != -1) {
                builder.field(VERSION, version);
            }
            toXContentEmbedded(builder, params);
        } else {
            builder.field(FOUND, false);
        }
        builder.endObject();
        return builder;
    }

    public XContentBuilder toXContentEmbedded(XContentBuilder builder, Params params) throws IOException {
        builder.field(FOUND, exists);

        if (fields != null && !fields.isEmpty()) {
            builder.field(FIELDS, fields);
        }

        if (source != null) {
            builder.field(SOURCE, source);
        }

        for (Map.Entry<String, Object> entry : otherFields.entrySet()) {
            builder.field(entry.getKey(), entry.getValue());
        }

        return builder;
    }

    @Override
    public RestResponse buildRestResponse(RestChannel channel) {
        try {
            XContentBuilder xContentBuilder = channel.newBuilder();
            toXContent(xContentBuilder, channel.request());
            return new BytesRestResponse(getRestStatus(), xContentBuilder);
        } catch (IOException e) {
            return new BytesRestResponse(getRestStatus(), XContentType.JSON.restContentType(), toString());
        }
    }

    @Override
    public String toString() {
        try {
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().prettyPrint();
            toXContent(xContentBuilder, EMPTY_PARAMS);
            return xContentBuilder.string();
        } catch (IOException e) {
            return "{ \"error\" : \"" + e.getMessage() + "\"}";
        }
    }
}
