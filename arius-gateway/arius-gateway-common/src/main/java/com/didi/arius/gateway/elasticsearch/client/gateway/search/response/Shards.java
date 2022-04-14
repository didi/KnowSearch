package com.didi.arius.gateway.elasticsearch.client.gateway.search.response;

import com.alibaba.fastjson.JSON;
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
 * @Date: Create on 2018/8/31 下午4:47
 * @Modified By
 */
public class Shards implements ToXContent {

    static final class Fields {
        public static final String _SHARDS = "_shards";
        public static final String TOTAL = "total";
        public static final String SUCCESSFUL = "successful";
        public static final String FAILED = "failed";
        public static final String FAILURES = "failures";
        public static final String SKIPPED = "skipped";
    }

    /**
     * 查询总shard总个数
     */
    private Integer totalShard;

    /**
     * 成功的shard个数
     */
    private Integer successfulShard;

    /**
     * 失败的shard个数
     */
    private Integer failedShard;

    private Integer skippedShard;

    private List<Failure> failures;

    public Shards() {
        // pass
    }

    public Integer getTotalShard() {
        return totalShard;
    }

    public void setTotalShard(Integer totalShard) {
        this.totalShard = totalShard;
    }

    public Integer getSuccessfulShard() {
        return successfulShard;
    }

    public void setSuccessfulShard(Integer successfulShard) {
        this.successfulShard = successfulShard;
    }

    public Integer getFailedShard() {
        return failedShard;
    }

    public void setFailedShard(Integer failedShard) {
        this.failedShard = failedShard;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public List<Failure> getFailures() {
        return failures;
    }

    public void setFailures(List<Failure> failures) {
        this.failures = failures;
    }

    public Integer getSkippedShard() {
        return skippedShard;
    }

    public void setSkippedShard(Integer skippedShard) {
        this.skippedShard = skippedShard;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields._SHARDS);
        builder.field(Fields.TOTAL, totalShard);
        builder.field(Fields.SUCCESSFUL, successfulShard);
        builder.field(Fields.FAILED, failedShard);
        if (failures != null && failures.size() > 0) {
            builder.startArray(Fields.FAILURES);
            //final boolean group = params.paramAsBoolean("group_shard_failures", true); // we group by default
            for (Failure failure : failures) {
                builder.startObject();
                failure.toXContent(builder, params);
                builder.endObject();
            }
            builder.endArray();
        }
        builder.endObject();

        return builder;
    }

    public static Shards fromXContent(XContentParser parser) throws IOException {
        XContentParser.Token token = parser.currentToken();
        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, token, parser::getTokenLocation);

        int total = 0;
        int successful = 0;
        int failed = 0;
        int skipped = 0;
        List<Failure> failuresList = null;
        String currentFieldName = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if (Fields.TOTAL.equals(currentFieldName)) {
                    total = parser.intValue();
                } else if (Fields.SUCCESSFUL.equals(currentFieldName)) {
                    successful = parser.intValue();
                } else if (Fields.FAILED.equals(currentFieldName)){
                    failed = parser.intValue();
                } else if (Fields.SKIPPED.equals(currentFieldName)) {
                    skipped = parser.intValue();
                } else {
                    parser.skipChildren();
                }
            } else if (token == XContentParser.Token.START_ARRAY) {
                failuresList = getFailures(parser, failuresList, currentFieldName);
            } else {
                parser.skipChildren(); // skip potential inner arrays for forward compatibility
            }
        }

        Shards shards = new Shards();
        shards.setTotalShard(total);
        shards.setSuccessfulShard(successful);
        shards.setFailedShard(failed);
        shards.setSkippedShard(skipped);
        shards.setFailures(failuresList);

        return shards;
    }

    private static List<Failure> getFailures(XContentParser parser, List<Failure> failuresList, String currentFieldName) throws IOException {
        XContentParser.Token token;
        if (Fields.FAILURES.equals(currentFieldName)) {
            failuresList = new ArrayList<>();
            while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                failuresList.add(Failure.fromXContent(parser));
            }
        } else {
            parser.skipChildren(); // skip potential inner arrays for forward compatibility
        }
        return failuresList;
    }
}
