package com.didi.arius.gateway.elasticsearch.client.gateway.search;

import com.didi.arius.gateway.elasticsearch.client.gateway.search.response.Shards;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.response.src.Hits;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.utils.XContentParserUtils;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/8/31 下午4:27
 * @Modified By
 */
public class ESSearchResponse extends ESActionResponse implements ToXContent {

    /**
     * 查询耗时
     */
    private Long took;
    /**
     * 是否超时
     */
    private Boolean timeOut;
    /**
     * shard结果
     */
    private Shards shards;
    /**
     * 命中结果
     */
    private Hits hits;

    /**
     * 聚合结果
     */
    private Map<String, Object> aggregations;
    /**
     * 滚动id
     */
    private String scrollId;

    private Map<String, Object> suggest;

    private Map<String, Object> profile;

    private Map<String, Object> clusters;

    private Boolean terminatedEarly;

    private int numReducePhases;

    private Integer status;

    public ESSearchResponse() {
        // pass
    }

    public Long getTook() {
        return took;
    }

    public void setTook(Long took) {
        this.took = took;
    }

    public Boolean getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Boolean timeOut) {
        this.timeOut = timeOut;
    }

    public Shards getShards() {
        return shards;
    }

    public void setShards(Shards shard) {
        this.shards = shard;
    }

    public Hits getHits() {
        return hits;
    }

    public void setHits(Hits hits) {
        this.hits = hits;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public Boolean getTerminatedEarly() {
        return terminatedEarly;
    }

    public void setTerminatedEarly(Boolean terminatedEarly) {
        this.terminatedEarly = terminatedEarly;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Map<String, Object> getAggregations() {
        return aggregations;
    }

    static final class Fields {
        static final String SCROLL_ID = "_scroll_id";
        static final String TOOK = "took";
        static final String TIMED_OUT = "timed_out";
        static final String TERMINATED_EARLY = "terminated_early";
        static final String NUM_REDUCE_PHASES = "num_reduce_phases";
        static final String SUGGEST = "suggest";
        static final String PROFILE = "profile";
        static final String AGGREGATIONS = "aggregations";
        static final String HITS = "hits";
        static final String SHARDS = "_shards";
        static final String CLUSTERS_FIELD = "_clusters";
        static final String STATUS = "status";
        private Fields () {

        }
    }

    @Override
    public String toString() {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().prettyPrint();
            builder.startObject();
            toXContent(builder, EMPTY_PARAMS);
            builder.endObject();
            return builder.string();
        } catch (IOException e) {
            return "{ \"error\" : \"" + e.getMessage() + "\"}";
        }
    }

    public static ESSearchResponse innerFromXContent(XContentParser parser) throws IOException {
        XContentParserUtils.ensureExpectedToken(Token.FIELD_NAME, parser.currentToken(), parser::getTokenLocation);
        String currentFieldName = parser.currentName();

        ESSearchResponse esSearchResponse = new ESSearchResponse();
        for (Token token = parser.nextToken(); token != Token.END_OBJECT; token = parser.nextToken()) {
            if (token == Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                dealValue(parser, currentFieldName, esSearchResponse);
            } else if (token == Token.START_OBJECT) {
                if (Fields.HITS.equals(currentFieldName)) {
                    esSearchResponse.hits = Hits.fromXContent(parser);
                } else if (Fields.AGGREGATIONS.equals(currentFieldName)) {
                    esSearchResponse.aggregations = parser.map();
                } else if (Fields.SUGGEST.equals(currentFieldName)) {
                    esSearchResponse.suggest = parser.map();
                } else if (Fields.PROFILE.equals(currentFieldName)) {
                    esSearchResponse.profile = parser.map();
                } else if (Fields.SHARDS.equals(currentFieldName)) {
                    esSearchResponse.shards = Shards.fromXContent(parser);
                } else if (Fields.CLUSTERS_FIELD.equals(currentFieldName)) {
                    esSearchResponse.clusters = parser.map();
                } else {
                    parser.skipChildren();
                }
            }
        }

        return esSearchResponse;
    }

    private static void dealValue(XContentParser parser, String currentFieldName, ESSearchResponse esSearchResponse) throws IOException {
        if (Fields.SCROLL_ID.equals(currentFieldName)) {
            esSearchResponse.scrollId = parser.text();
        } else if (Fields.TOOK.equals(currentFieldName)) {
            esSearchResponse.took = parser.longValue();
        } else if (Fields.TIMED_OUT.equals(currentFieldName)) {
            esSearchResponse.timeOut = parser.booleanValue();
        } else if (Fields.TERMINATED_EARLY.equals(currentFieldName)) {
            esSearchResponse.terminatedEarly = parser.booleanValue();
        } else if (Fields.NUM_REDUCE_PHASES.equals(currentFieldName)) {
            esSearchResponse.numReducePhases = parser.intValue();
        } else if (Fields.STATUS.equals(currentFieldName)) {
            esSearchResponse.status = parser.intValue();
        } else {
            parser.skipChildren();
        }
    }

    public static ESSearchResponse fromXContent(XContentParser parser) throws IOException {
        XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.nextToken(), parser::getTokenLocation);
        parser.nextToken();
        return innerFromXContent(parser);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        if (scrollId != null) {
            builder.field(Fields.SCROLL_ID, scrollId);
        }

        builder.field(Fields.TOOK, took);
        builder.field(Fields.TIMED_OUT, timeOut);

        if (terminatedEarly != null) {
            builder.field(Fields.TERMINATED_EARLY, terminatedEarly);
        }

        shards.toXContent(builder, params);
        hits.toXContent(builder, params);
        if (aggregations != null && !aggregations.isEmpty()) {
            builder.field(Fields.AGGREGATIONS, aggregations);
        }

        if (suggest != null) {
            builder.field(Fields.SUGGEST, suggest);
        }

        if (profile != null) {
            builder.field(Fields.PROFILE, profile);
        }

        return builder;
    }

    @Override
    public RestResponse buildRestResponse(RestChannel channel) {
        XContentBuilder builder = null;
        try {
            builder = channel.newBuilder();
            builder.startObject();
            toXContent(builder, channel.request());
            builder.endObject();
            return new BytesRestResponse(getRestStatus(), builder);
        } catch (IOException e) {
            return new BytesRestResponse(getRestStatus(), XContentType.JSON.restContentType(), toString());
        } finally {
            if (null != builder) {
                builder.close();
            }
        }
    }
}
