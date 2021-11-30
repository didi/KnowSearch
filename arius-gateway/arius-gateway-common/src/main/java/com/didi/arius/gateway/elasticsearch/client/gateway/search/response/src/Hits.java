package com.didi.arius.gateway.elasticsearch.client.gateway.search.response.src;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.utils.XContentParserUtils;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/8/31 下午4:29
 * @Modified By
 */
public class Hits implements ToXContent {
    static final class Fields {
        private Fields() {

        }
        static final String HITS = "hits";
        static final String TOTAL = "total";
        static final String MAX_SCORE = "max_score";
    }

    /**
     * 结果集
     */
    private List<Hit> hits;

    /**
     * 命中总条数
     */
    private long total;
    /**
     * 最大得分
     */
    private float maxScore;

    public Hits() {
        // pass
    }

    @JSONField(serialize=false)
    public boolean isEmpty() {
        boolean res = false;
        if(hits==null || hits.isEmpty()) {
            res = true;
        }
        return res;
    }

    public List<Hit> getHits() {
        return hits;
    }

    public void setHits(List<Hit> hits) {
        this.hits = hits;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public float getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(float maxScore) {
        this.maxScore = maxScore;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static Hits fromXContent(XContentParser parser) throws IOException {
        if (parser.currentToken() != XContentParser.Token.START_OBJECT) {
            parser.nextToken();
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser::getTokenLocation);
        }
        XContentParser.Token token = parser.currentToken();
        String currentFieldName = null;

        Hits hits = new Hits();
        hits.hits = new ArrayList<>();
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if (Fields.TOTAL.equals(currentFieldName)) {
                    hits.total = parser.longValue();
                } else if (Fields.MAX_SCORE.equals(currentFieldName)) {
                    hits.maxScore = parser.floatValue();
                }
            } else if (token == XContentParser.Token.VALUE_NULL) {
                if (Fields.MAX_SCORE.equals(currentFieldName)) {
                    hits.maxScore = 0.0F; // NaN gets rendered as null-field
                }
            } else if (token == XContentParser.Token.START_ARRAY) {
                dealStartArray(parser, currentFieldName, hits);
            } else if (token == XContentParser.Token.START_OBJECT) {
                parser.skipChildren();
            }
        }

        return hits;
    }

    private static void dealStartArray(XContentParser parser, String currentFieldName, Hits hits) throws IOException {
        XContentParser.Token token;
        if (Fields.HITS.equals(currentFieldName)) {
            while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                hits.hits.add(Hit.fromXContent(parser));
            }
        } else {
            parser.skipChildren();
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.HITS);
        builder.field(Fields.TOTAL, total);
        if (Float.isNaN(maxScore)) {
            builder.field(Fields.MAX_SCORE, 0.0f);
        } else {
            builder.field(Fields.MAX_SCORE, maxScore);
        }

        builder.field(Fields.HITS);
        builder.startArray();

        for (Hit hit : hits) {
            hit.toXContent(builder, params);
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }
}
