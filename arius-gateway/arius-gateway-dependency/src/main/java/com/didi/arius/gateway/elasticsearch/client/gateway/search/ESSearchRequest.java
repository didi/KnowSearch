package com.didi.arius.gateway.elasticsearch.client.gateway.search;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.ParseFieldMatcher;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.netty.handler.codec.http.HttpMethod;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

public class ESSearchRequest extends ESActionRequest<ESSearchRequest> {
    private String[] indices;

    private String[] types = Strings.EMPTY_ARRAY;

    private BytesReference source;

    private BytesReference extraSource;

    private boolean isTemplateRequest;

    private final String endpoint;

    private Map<String, String> params = new HashMap<>();

    public ESSearchRequest() {
        this.endpoint = "/_search";
    }

    public ESSearchRequest(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Sets the indices the search will be executed on.
     */
    public ESSearchRequest indices(String... indices) {
        if (indices == null) {
            throw new IllegalArgumentException("indices must not be null");
        } else {
            for (int i = 0; i < indices.length; i++) {
                if (indices[i] == null) {
                    throw new IllegalArgumentException("indices[" + i + "] must not be null");
                }
            }
        }
        this.indices = indices;
        return this;
    }

    /**
     * The indices
     */
    public String[] indices() {
        return indices;
    }

    /**
     * The document types to execute the search against. Defaults to be executed against
     * all types.
     */
    public String[] types() {
        return types;
    }

    /**
     * The document types to execute the search against. Defaults to be executed against
     * all types.
     */
    public ESSearchRequest types(String... types) {
        this.types = types;
        return this;
    }

    /**
     * The source of the search request.
     */
    public ESSearchRequest source(SearchSourceBuilder sourceBuilder) {
        this.source = sourceBuilder.buildAsBytes(Requests.CONTENT_TYPE);
        return this;
    }

    /**
     * The source of the search request. Consider using either {@link #source(byte[])} or
     * {@link #source(SearchSourceBuilder)}.
     */
    public ESSearchRequest source(String source) {
        this.source = new BytesArray(source);
        return this;
    }

    /**
     * The source of the search request in the form of a map.
     */
    public ESSearchRequest source(Map source) {
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(Requests.CONTENT_TYPE);
            builder.map(source);
            return source(builder);
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed to generate [" + source + "]", e);
        }
    }

    public ESSearchRequest source(XContentBuilder builder) {
        this.source = builder.bytes();
        return this;
    }

    /**
     * The search source to execute.
     */
    public ESSearchRequest source(byte[] source) {
        return source(source, 0, source.length);
    }


    /**
     * The search source to execute.
     */
    public ESSearchRequest source(byte[] source, int offset, int length) {
        return source(new BytesArray(source, offset, length));
    }

    /**
     * The search source to execute.
     */
    public ESSearchRequest source(BytesReference source) {
        this.source = source;
        return this;
    }

    /**
     * The search source to execute.
     */
    public BytesReference source() {
        return source;
    }

    /**
     * Allows to provide additional source that will be used as well.
     */
    public ESSearchRequest extraSource(SearchSourceBuilder sourceBuilder) {
        if (sourceBuilder == null) {
            extraSource = null;
            return this;
        }
        this.extraSource = sourceBuilder.buildAsBytes(Requests.CONTENT_TYPE);
        return this;
    }

    public ESSearchRequest extraSource(Map extraSource) {
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(Requests.CONTENT_TYPE);
            builder.map(extraSource);
            return extraSource(builder);
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed to generate [" + extraSource + "]", e);
        }
    }

    public ESSearchRequest extraSource(XContentBuilder builder) {
        this.extraSource = builder.bytes();
        return this;
    }

    /**
     * Allows to provide additional source that will use used as well.
     */
    public ESSearchRequest extraSource(String source) {
        this.extraSource = new BytesArray(source);
        return this;
    }

    /**
     * Allows to provide additional source that will be used as well.
     */
    public ESSearchRequest extraSource(byte[] source) {
        return extraSource(source, 0, source.length);
    }

    /**
     * Allows to provide additional source that will be used as well.
     */
    public ESSearchRequest extraSource(byte[] source, int offset, int length) {
        return extraSource(new BytesArray(source, offset, length));
    }

    /**
     * Allows to provide additional source that will be used as well.
     */
    public ESSearchRequest extraSource(BytesReference source) {
        this.extraSource = source;
        return this;
    }

    /**
     * Additional search source to execute.
     */
    public BytesReference extraSource() {
        return this.extraSource;
    }

    public boolean isTemplateRequest() {
        return isTemplateRequest;
    }

    public void setTemplateRequest(boolean templateRequest) {
        isTemplateRequest = templateRequest;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        String index = StringUtils.join(indices, ",");
        String type = StringUtils.join(types, ",");

        String endpoint;
        if (type == null || type.length() == 0) {
            endpoint = String.format("/%s", index);
        } else {
            endpoint = String.format("/%s/%s", index, type);
        }
        endpoint += this.endpoint;

        if (isTemplateRequest) {
            endpoint += "/template";
        }

        RestRequest restRequest = new RestRequest(HttpMethod.POST.getName(), endpoint);
        restRequest.setBody(source);

        prepareDealSearchType();
        restRequest.setParams(params);
        return restRequest;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        XContentParser parser = JsonXContent.jsonXContent.createParser(response.getResponseStream());
        return ESSearchResponse.fromXContent(parser);
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    /**
     * A comma separated list of routing values to control the shards the search will be executed on.
     */
    public String routing() {
        return this.params.get("routing");
    }

    /**
     * A comma separated list of routing values to control the shards the search will be executed on.
     */
    public ESSearchRequest routing(String routing) {
        putParam("routing", routing);
        return this;
    }

    /**
     * The routing values to control the shards that the search will be executed on.
     */
    public ESSearchRequest routing(String... routings) {
        putParam("routing", Strings.arrayToCommaDelimitedString(routings));
        return this;
    }

    /**
     * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to
     * <tt>_local</tt> to prefer local shards, <tt>_primary</tt> to execute only on primary shards, or
     * a custom value, which guarantees that the same order will be used across different requests.
     */
    public ESSearchRequest preference(String preference) {
        putParam("preference", preference);
        return this;
    }

    public String preference() {
        return params.get("preference");
    }

    /**
     * The tye of search to execute.
     */
    public SearchType searchType() {
        return SearchType.fromString(params.get("search_type"), ParseFieldMatcher.EMPTY);
    }

    /**
     * The search type to execute, defaults to {@link SearchType#DEFAULT}.
     */
    public ESSearchRequest searchType(SearchType searchType) {
        if (searchType.equals(SearchType.COUNT)) {
            return this;
        }

        putParam("search_type", searchType.name().toLowerCase(Locale.ROOT));
        return this;
    }

    /**
     * The a string representation search type to execute, defaults to {@link SearchType#DEFAULT}. Can be
     * one of "dfs_query_then_fetch"/"dfsQueryThenFetch", "dfs_query_and_fetch"/"dfsQueryAndFetch",
     * "query_then_fetch"/"queryThenFetch", and "query_and_fetch"/"queryAndFetch".
     */
    public ESSearchRequest searchType(String searchType) {
        if (searchType.equalsIgnoreCase("count")) {
            return this;
        }

        putParam("search_type", searchType);
        return this;
    }

    private void prepareDealSearchType() {
        String searchType = params.get("search_type");
        if (searchType != null && (searchType.equalsIgnoreCase("count"))) {
            params.remove("search_type");
        }
    }

    public IndicesOptions indicesOptions() {
        return IndicesOptions.fromParameters(
                params.get("expand_wildcards"),
                params.get("ignore_unavailable"),
                params.get("allow_no_indices"),
                IndicesOptions.strictExpandOpenAndForbidClosed());
    }

    public ESSearchRequest indicesOptions(IndicesOptions indicesOptions) {
        putParam("ignore_unavailable", Boolean.toString(indicesOptions.ignoreUnavailable()));
        putParam("allow_no_indices", Boolean.toString(indicesOptions.allowNoIndices()));
        String expandWildcards;
        if (indicesOptions.expandWildcardsOpen() == false && indicesOptions.expandWildcardsClosed() == false) {
            expandWildcards = "none";
        } else {
            StringJoiner joiner = new StringJoiner(",");
            if (indicesOptions.expandWildcardsOpen()) {
                joiner.add("open");
            }
            if (indicesOptions.expandWildcardsClosed()) {
                joiner.add("closed");
            }
            expandWildcards = joiner.toString();
        }
        putParam("expand_wildcards", expandWildcards);
        return this;
    }

    /**
     * Sets if this request should use the request cache or not, assuming that it can (for
     * example, if "now" is used, it will never be cached). By default (not set, or null,
     * will default to the index level setting if request cache is enabled or not).
     */
    public ESSearchRequest requestCache(Boolean requestCache) {
        putParam("request_cache", String.valueOf(requestCache));
        return this;
    }

    public Boolean requestCache() {
        return Boolean.valueOf(params.get("request_cache"));
    }

    public void putParam(String name, String value) {
        if (Strings.hasLength(value)) {
            params.put(name, value);
        }
    }
}
