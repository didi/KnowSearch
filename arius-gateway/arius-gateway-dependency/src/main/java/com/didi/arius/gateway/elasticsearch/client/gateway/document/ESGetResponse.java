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
                if (_INDEX.equals(currentFieldName)) {
                    esGetResponse.index = parser.text();
                } else if (_TYPE.equals(currentFieldName)) {
                    esGetResponse.type = parser.text();
                } else if (_ID.equals(currentFieldName)) {
                    esGetResponse.id = parser.text();
                }  else if (_VERSION.equals(currentFieldName)) {
                    esGetResponse.version = parser.longValue();
                } else if (FOUND.equals(currentFieldName)) {
                    esGetResponse.exists = parser.booleanValue();
                } else {
                    esGetResponse.otherFields.put(currentFieldName, parser.objectText());
                }
            } else if (token == XContentParser.Token.START_OBJECT) {
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
            } else if (token == XContentParser.Token.START_ARRAY) {
                esGetResponse.otherFields.put(currentFieldName, parser.list());
            }
        }

        return esGetResponse;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }

    public static final String _INDEX = "_index";
    public static final String _TYPE = "_type";
    public static final String _ID = "_id";
    private static final String _VERSION = "_version";
    private static final String FOUND = "found";
    private static final String FIELDS = "fields";
    private static final String SOURCE = "_source";
    private static final String ERROR = "error";

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(_INDEX, index);
        builder.field(_TYPE, type);
        builder.field(_ID, id);
        if (isExists()) {
            if (version != -1) {
                builder.field(_VERSION, version);
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

        if (fields != null && false == fields.isEmpty()) {
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
