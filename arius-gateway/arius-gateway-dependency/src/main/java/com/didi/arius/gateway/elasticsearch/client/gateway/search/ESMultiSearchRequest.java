package com.didi.arius.gateway.elasticsearch.client.gateway.search;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.jboss.netty.handler.codec.http.HttpMethod;

import java.util.*;

import static org.elasticsearch.common.xcontent.support.XContentMapValues.*;

public class ESMultiSearchRequest extends ESActionRequest<ESMultiSearchRequest> {

    private Map<String, String> params = new HashMap<>();

    private List<ESSearchRequest> requests = new ArrayList<>();

    private boolean isTemplateRequest;

    private final String endpoint;

    public ESMultiSearchRequest() {
        this.endpoint = "/_msearch";
    }

    public ESMultiSearchRequest(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Add a search request to execute. Note, the order is important, the search response will be returned in the
     * same order as the search requests.
     */
    public ESMultiSearchRequest add(ESSearchRequest request) {
        requests.add(request);
        return this;
    }

    public ESMultiSearchRequest add(BytesReference data, boolean isTemplateRequest, @Nullable String[] indices, @Nullable String[] types, @Nullable String searchType, @Nullable String routing, IndicesOptions indicesOptions, boolean allowExplicitIndex) throws Exception {
        XContent xContent = XContentFactory.xContent(data);
        int from = 0;
        int length = data.length();
        byte marker = xContent.streamSeparator();
        while (true) {
            int nextMarker = findNextMarker(marker, from, data, length);
            if (nextMarker == -1) {
                break;
            }
            // support first line with \n
            if (nextMarker == 0) {
                from = nextMarker + 1;
                continue;
            }

            ESSearchRequest esSearchRequest = new ESSearchRequest();
            if (indices != null) {
                esSearchRequest.indices(indices);
            }
            if (indicesOptions != null) {
                esSearchRequest.indicesOptions(indicesOptions);
            }
            if (types != null && types.length > 0) {
                esSearchRequest.types(types);
            }
            if (routing != null) {
                esSearchRequest.routing(routing);
            }
            if (searchType != null) {
                esSearchRequest.searchType(searchType);
            }

            IndicesOptions defaultOptions = IndicesOptions.strictExpandOpenAndForbidClosed();


            // now parse the action
            if (nextMarker - from > 0) {
                try (XContentParser parser = xContent.createParser(data.slice(from, nextMarker - from))) {
                    Map<String, Object> source = parser.map();
                    for (Map.Entry<String, Object> entry : source.entrySet()) {
                        Object value = entry.getValue();
                        if ("index".equals(entry.getKey()) || "indices".equals(entry.getKey())) {
                            if (!allowExplicitIndex) {
                                throw new IllegalArgumentException("explicit index in multi percolate is not allowed");
                            }
                            esSearchRequest.indices(nodeStringArrayValue(value));
                        } else if ("type".equals(entry.getKey()) || "types".equals(entry.getKey())) {
                            esSearchRequest.types(nodeStringArrayValue(value));
                        } else if ("search_type".equals(entry.getKey()) || "searchType".equals(entry.getKey())) {
                            esSearchRequest.searchType(nodeStringValue(value, null));
                        } else if ("request_cache".equals(entry.getKey()) || "requestCache".equals(entry.getKey())) {
                            esSearchRequest.requestCache(nodeBooleanValue(value));
                        } else if ("preference".equals(entry.getKey())) {
                            esSearchRequest.preference(nodeStringValue(value, null));
                        } else if ("routing".equals(entry.getKey())) {
                            esSearchRequest.routing(nodeStringValue(value, null));
                        }
                    }
                    defaultOptions = IndicesOptions.fromMap(source, defaultOptions);
                }
            }
            esSearchRequest.indicesOptions(defaultOptions);

            // move pointers
            from = nextMarker + 1;
            // now for the body
            nextMarker = findNextMarker(marker, from, data, length);
            if (nextMarker == -1) {
                break;
            }

            esSearchRequest.source(data.slice(from, nextMarker - from));

            // move pointers
            from = nextMarker + 1;

            add(esSearchRequest);
        }

        return this;
    }

    private int findNextMarker(byte marker, int from, BytesReference data, int length) {
        for (int i = from; i < length; i++) {
            if (data.get(i) == marker) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        String endpoint = this.endpoint;
        if (isTemplateRequest) {
            endpoint += "/template";
        }

        RestRequest restRequest = new RestRequest(HttpMethod.POST.getName(), endpoint);
        restRequest.setParams(params);

        StringBuilder builder = new StringBuilder();
        for (ESSearchRequest esSearchRequest : requests) {
            JSONObject header = new JSONObject();
            if (esSearchRequest.indices() != null && esSearchRequest.indices().length > 0) {
                header.put("index", Strings.arrayToCommaDelimitedString(esSearchRequest.indices()));
            }

            if (esSearchRequest.types() != null && esSearchRequest.types().length > 0) {
                header.put("type", Strings.arrayToCommaDelimitedString(esSearchRequest.types()));
            }

            if (esSearchRequest.getParams().containsKey("search_type")) {
                header.put("search_type", esSearchRequest.getParams().get("search_type"));
            }

            if (esSearchRequest.getParams().containsKey("request_cache")) {
                header.put("request_cache", esSearchRequest.getParams().get("request_cache"));
            }

            if (esSearchRequest.getParams().containsKey("preference")) {
                header.put("preference", esSearchRequest.getParams().get("preference"));
            }

            if (esSearchRequest.getParams().containsKey("routing")) {
                header.put("routing", esSearchRequest.getParams().get("routing"));
            }

            if (esSearchRequest.indicesOptions() != null) {
                header.put("ignore_unavailable", Boolean.toString(esSearchRequest.indicesOptions().ignoreUnavailable()));
                header.put("allow_no_indices", Boolean.toString(esSearchRequest.indicesOptions().allowNoIndices()));
                String expandWildcards;
                if (esSearchRequest.indicesOptions().expandWildcardsOpen() == false && esSearchRequest.indicesOptions().expandWildcardsClosed() == false) {
                    expandWildcards = "none";
                } else {
                    StringJoiner joiner = new StringJoiner(",");
                    if (esSearchRequest.indicesOptions().expandWildcardsOpen()) {
                        joiner.add("open");
                    }
                    if (esSearchRequest.indicesOptions().expandWildcardsClosed()) {
                        joiner.add("closed");
                    }
                    expandWildcards = joiner.toString();
                }
                header.put("expand_wildcards", expandWildcards);
            }

            builder.append(header.toJSONString());
            builder.append("\n");
            builder.append(esSearchRequest.source() == null ? "{}" : esSearchRequest.source().toUtf8());
            builder.append("\n");
        }

        restRequest.setBody(builder.toString());

        return restRequest;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        XContentParser parser = JsonXContent.jsonXContent.createParser(response.getResponseStream());
        return ESMultiSearchResponse.fromXContent(parser);
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    public List<ESSearchRequest> requests() {
        return this.requests;
    }

    public List<ESSearchRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<ESSearchRequest> requests) {
        this.requests = requests;
    }

    public void addRequest(ESSearchRequest request) {
        requests.add(request);
    }

    public boolean isTemplateRequest() {
        return isTemplateRequest;
    }

    public void setTemplateRequest(boolean templateRequest) {
        isTemplateRequest = templateRequest;
    }
}
