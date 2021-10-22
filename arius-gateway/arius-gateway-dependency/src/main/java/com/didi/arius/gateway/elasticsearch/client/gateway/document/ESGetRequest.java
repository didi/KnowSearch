/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.utils.RequestConverters;
import org.apache.http.client.methods.HttpGet;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.search.fetch.source.FetchSourceContext;

public class ESGetRequest extends ESActionRequest<ESGetRequest> {

    private String index;
    private String type;
    private String id;
    private String routing;
    private String preference;

    private String[] fields;

    private FetchSourceContext fetchSourceContext;

    private String refresh;

    Boolean realtime;

    private VersionType versionType = VersionType.INTERNAL;
    private long version = Versions.MATCH_ANY;
    private boolean ignoreErrorsOnGeneratedFields;

    private String parent;

    private String[] storedFields;

    public ESGetRequest() {
        type = "_all";
    }

    /**
     * Copy constructor that creates a new get request that is a copy of the one provided as an argument.
     * The new request will inherit though headers and context from the original request that caused it.
     */

    public ESGetRequest(ESGetRequest getRequest) {
        this.index = getRequest.index;
        this.type = getRequest.type;
        this.id = getRequest.id;
        this.routing = getRequest.routing;
        this.preference = getRequest.preference;
        this.fields = getRequest.fields;
        this.fetchSourceContext = getRequest.fetchSourceContext;
        this.refresh = getRequest.refresh;
        this.realtime = getRequest.realtime;
        this.version = getRequest.version;
        this.versionType = getRequest.versionType;
        this.ignoreErrorsOnGeneratedFields = getRequest.ignoreErrorsOnGeneratedFields;
    }

    /**
     * Constructs a new get request against the specified index. The {@link #type(String)} and {@link #id(String)}
     * must be set.
     */
    public ESGetRequest(String index) {
        this.index = index;
        this.type = "_all";
    }

    /**
     * Constructs a new get request against the specified index with the type and id.
     *
     * @param index The index to get the document from
     * @param type  The type of the document
     * @param id    The id of the document
     */
    public ESGetRequest(String index, String type, String id) {
        this.index = index;
        this.type = type;
        this.id = id;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        RestRequest request = new RestRequest(HttpGet.METHOD_NAME, RequestConverters.endpoint(this.index(), this.type(), this.id()));

        RequestConverters.Params parameters = new RequestConverters.Params(request);
        parameters.withPreference(this.preference());
        parameters.withRouting(this.routing());
        parameters.withParent(this.parent());
        parameters.withRefresh(this.refresh());
        parameters.withRealtime(this.realtime());
        parameters.withStoredFields(this.storedFields());
        parameters.withVersion(this.version());
        parameters.withVersionType(this.versionType());
        parameters.withFetchSourceContext(this.fetchSourceContext());
        parameters.withFields(this.fields());

        return request;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        XContentParser parser = JsonXContent.jsonXContent.createParser(response.getResponseStream());
        return ESGetResponse.fromXContent(parser);
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    public String index() {
        return index;
    }

    public ESGetRequest index(String index) {
        this.index = index;
        return this;
    }

    /**
     * Sets the type of the document to fetch.
     */
    public ESGetRequest type(@Nullable String type) {
        if (type == null) {
            type = "_all";
        }
        this.type = type;
        return this;
    }

    /**
     * Sets the id of the document to fetch.
     */
    public ESGetRequest id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Controls the shard routing of the request. Using this value to hash the shard
     * and not the id.
     */
    public ESGetRequest routing(String routing) {
        this.routing = routing;
        return this;
    }

    /**
     * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to
     * <tt>_local</tt> to prefer local shards, <tt>_primary</tt> to execute only on primary shards, or
     * a custom value, which guarantees that the same order will be used across different requests.
     */
    public ESGetRequest preference(String preference) {
        this.preference = preference;
        return this;
    }

    public String type() {
        return type;
    }

    public String id() {
        return id;
    }

    public String routing() {
        return this.routing;
    }

    public String preference() {
        return this.preference;
    }

    /**
     * Allows setting the {@link FetchSourceContext} for this request, controlling if and how _source should be returned.
     */
    public ESGetRequest fetchSourceContext(FetchSourceContext context) {
        this.fetchSourceContext = context;
        return this;
    }

    public FetchSourceContext fetchSourceContext() {
        return fetchSourceContext;
    }

    /**
     * Explicitly specify the fields that will be returned. By default, the <tt>_source</tt>
     * field will be returned.
     */
    public ESGetRequest fields(String... fields) {
        this.fields = fields;
        return this;
    }

    /**
     * Explicitly specify the fields that will be returned. By default, the <tt>_source</tt>
     * field will be returned.
     */
    public String[] fields() {
        return this.fields;
    }

    public ESGetRequest refresh(String refresh) {
        this.refresh = refresh;
        return this;
    }

    public String refresh() {
        return this.refresh;
    }

    public boolean realtime() {
        return this.realtime == null ? true : this.realtime;
    }

    public ESGetRequest realtime(Boolean realtime) {
        this.realtime = realtime;
        return this;
    }

    /**
     * Sets the version, which will cause the get operation to only be performed if a matching
     * version exists and no changes happened on the doc since then.
     */
    public long version() {
        return version;
    }

    public ESGetRequest version(long version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the versioning type. Defaults to {@link VersionType#INTERNAL}.
     */
    public ESGetRequest versionType(VersionType versionType) {
        this.versionType = versionType;
        return this;
    }

    public ESGetRequest ignoreErrorsOnGeneratedFields(boolean ignoreErrorsOnGeneratedFields) {
        this.ignoreErrorsOnGeneratedFields = ignoreErrorsOnGeneratedFields;
        return this;
    }

    public VersionType versionType() {
        return this.versionType;
    }

    public boolean ignoreErrorsOnGeneratedFields() {
        return ignoreErrorsOnGeneratedFields;
    }

    /**
     * @return The parent for this request.
     */
    public String parent() {
        return parent;
    }

    /**
     * Sets the parent id of this document.
     */
    public ESGetRequest parent(String parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Explicitly specify the stored fields that will be returned. By default, the {@code _source}
     * field will be returned.
     */
    public ESGetRequest storedFields(String... fields) {
        this.storedFields = fields;
        return this;
    }

    /**
     * Explicitly specify the stored fields that will be returned. By default, the {@code _source}
     * field will be returned.
     */
    public String[] storedFields() {
        return this.storedFields;
    }

    @Override
    public String toString() {
        return "get [" + index + "][" + type + "][" + id + "]: routing [" + routing + "]";
    }

}
