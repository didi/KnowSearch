package com.didi.arius.gateway.elasticsearch.client.gateway.search.response.src;

import com.didi.arius.gateway.elasticsearch.client.utils.XContentParserUtils;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/8/31 下午4:32
 * @Modified By
 */
public class Hit implements ToXContent {
    public static class Fields {
        static final String INDEX = "_index";
        static final String SHARD = "_shard";
        static final String NODE = "_node";
        static final String TYPE = "_type";
        static final String ID = "_id";
        static final String VERSION = "_version";
        static final String SCORE = "_score";
        static final String FIELDS = "fields";
        static final String HIGHLIGHT = "highlight";
        static final String SORT = "sort";
        static final String MATCHED_QUERIES = "matched_queries";
        static final String EXPLANATION = "_explanation";
        static final String INNER_HITS = "inner_hits";
        static final String NESTED = "_nested";
        static final String SOURCE = "_source";
    }

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
     * 得分
     */
    private float score;
    /**
     * source
     */
    private Map<String, Object> source;

    private String sourceAsString;

    private Map<String, List<Object>> fields = new HashMap<>();

    private Map<String, Object> otherFields;

    public Hit() {
        // pass
    }

    public String getIndex() {
        return index;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }

    public String sourceAsString() {
        if (source == null) {
            return null;
        }

        if (sourceAsString != null) {
            return sourceAsString;
        }

        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.map(source);
            sourceAsString = builder.string();
            return sourceAsString;
        } catch (IOException e) {
            throw new ElasticsearchParseException("failed to convert source to a json string");
        }
    }

    public Map<String, List<Object>> getFields() {
        return fields;
    }

    public void setFields(Map<String, List<Object>> fields) {
        this.fields = fields;
    }

    public Map<String, Object> getOtherFields() {
        return otherFields;
    }

    public void setOtherFields(Map<String, Object> otherFields) {
        this.otherFields = otherFields;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();

        if (index != null) {
            builder.field(Fields.INDEX, index);
        }

        builder.field(Fields.TYPE, type);
        builder.field(Fields.ID, id);

        if (Float.isNaN(score)) {
            builder.field(Fields.SCORE, 0.0f);
        } else {
            builder.field(Fields.SCORE, score);
        }

        if (source != null) {
            builder.field(Fields.SOURCE, source);
        }

        if (fields != null && fields.size() > 0) {
            builder.field(Fields.FIELDS, fields);
        }

        if (otherFields != null) {
            for (Map.Entry<String, Object> entry : otherFields.entrySet()) {
                builder.field(entry.getKey(), entry.getValue());
            }
        }

        builder.endObject();
        return builder;
    }

    public static Hit fromXContent(XContentParser parser) throws  IOException {
        if (parser.currentToken() != XContentParser.Token.START_OBJECT) {
            parser.nextToken();
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser::getTokenLocation);
        }

        XContentParser.Token token = parser.currentToken();
        String currentFieldName = null;
        Hit hit = new Hit();
        hit.otherFields = new TreeMap<>();
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                dealValue(parser, currentFieldName, hit);
            } else if (token == XContentParser.Token.VALUE_NULL) {
                if (Fields.SCORE.equals(currentFieldName)) {
                    hit.score = 0.0f; // NaN gets rendered as null-field
                } else {
                    hit.otherFields.put(currentFieldName, null);
                }
            } else if (token == XContentParser.Token.START_OBJECT) {
                dealStartObj(parser, currentFieldName, hit);
            } else if (token == XContentParser.Token.START_ARRAY) {
                hit.otherFields.put(currentFieldName, parser.list());
            }
        }

        return hit;
    }

    private static void dealStartObj(XContentParser parser, String currentFieldName, Hit hit) throws IOException {
        XContentParser.Token token;
        if (Fields.SOURCE.equals(currentFieldName)) {
            hit.source = parser.map();
        } else if (Fields.FIELDS.equals(currentFieldName)) {
            hit.fields = new HashMap<>();
            while(parser.nextToken() != XContentParser.Token.END_OBJECT) {
                String key = parser.currentName();
                token = parser.nextToken();
                XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_ARRAY, token, parser::getTokenLocation);
                hit.fields.put(key, parser.list());
            }
        } else {
            hit.otherFields.put(currentFieldName, parser.map());
        }
    }

    private static void dealValue(XContentParser parser, String currentFieldName, Hit hit) throws IOException {
        if (Fields.INDEX.equals(currentFieldName)) {
            hit.index = parser.text();
        } else if (Fields.TYPE.equals(currentFieldName)) {
            hit.type = parser.text();
        } else if (Fields.ID.equals(currentFieldName)) {
            hit.id = parser.text();
        } else if (Fields.SCORE.equals(currentFieldName)) {
            hit.score = parser.floatValue();
        } else {
            hit.otherFields.put(currentFieldName, parser.objectText());
        }
    }

}
