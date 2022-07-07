package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.index;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node.ESBroadcastTimeoutRequest;
import com.didiglobal.logi.elasticsearch.client.model.ESActionResponse;
import com.didiglobal.logi.elasticsearch.client.model.RestRequest;
import com.didiglobal.logi.elasticsearch.client.model.RestResponse;
import com.didiglobal.logi.elasticsearch.client.request.index.stats.IndicesStatsLevel;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class ESIndexStatsRequest extends ESBroadcastTimeoutRequest<ESIndexStatsRequest> {
    public static String COMPLETION = "completion";
    public static String STORE = "store";
    public static String INDEXING = "indexing";
    public static String TRANSLOG = "translog";
    public static String REFRESH = "refresh";
    public static String SUGGEST = "suggest";
    public static String RECOVERY = "recovery";
    public static String WARMER = "warmer";
    public static String SEGMENTS = "segments";
    public static String SEARCH = "search";
    public static String QUERY_CACHE = "query_cache";
    public static String DOCS = "docs";
    public static String FLUSH = "flush";
    public static String FIELDDATA = "fielddata";
    public static String GET = "get";
    public static String MERGE = "merge";
    public static String REQUEST_CACHE = "request_cache";

    private final Set<String> flags = new HashSet<>();
    private IndicesStatsLevel level = null;

    /**
     * Sets all flags to return all nodestats.
     */
    public ESIndexStatsRequest all() {
        flags.add(COMPLETION);
        flags.add(INDEXING);
        flags.add(TRANSLOG);
        flags.add(REFRESH);
        flags.add(SUGGEST);
        flags.add(RECOVERY);
        flags.add(WARMER);
        flags.add(SEGMENTS);
        flags.add(SEARCH);
        flags.add(QUERY_CACHE);
        flags.add(DOCS);
        flags.add(FLUSH);
        flags.add(FIELDDATA);
        flags.add(GET);
        flags.add(MERGE);
        flags.add(REQUEST_CACHE);

        return this;
    }

    /**
     * Clears all nodestats.
     */
    public ESIndexStatsRequest clear() {
        flags.clear();
        return this;
    }

    public ESIndexStatsRequest types(String... types) {
        for (String type : types) {
            flags.add(type);
        }
        return this;
    }


    public ESIndexStatsRequest flag(String type, boolean isSet) {
        if (isSet) {
            flags.add(type);
        } else {
            flags.remove(type);
        }
        return this;
    }

    public ESIndexStatsRequest setLevel(IndicesStatsLevel level) {
        this.level = level;

        return this;
    }

    public boolean isSet(String type) {
        return flags.contains(type);
    }

    @Override
    public RestRequest toRequest() throws Exception {
        String endpoint = buildEndPoint();
        RestRequest rr = new RestRequest("GET", endpoint, null);
        if (level != null) {
            rr.addParam("level", level.getStr());
        }
        return rr;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        String respStr = response.getResponseContent();
        return JSON.parseObject(respStr, ESIndexStatsResponse.class);
    }

    @SuppressWarnings("all")
    private String buildEndPoint() {
        String index = indices != null && indices.length > 0 ? StringUtils.join(indices, ",").trim() : null;
        String type = flags.size() > 0 ? StringUtils.join(flags, ",").trim() : null;
        String indexUrl = null == index ? "/_stats" : String.format("/%s/_stats", index);
        String finalUrl = null == type ? indexUrl : indexUrl + "/" + type;
        return finalUrl;
    }
}
