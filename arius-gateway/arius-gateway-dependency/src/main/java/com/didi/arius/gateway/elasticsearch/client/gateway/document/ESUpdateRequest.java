package com.didi.arius.gateway.elasticsearch.client.gateway.document;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.utils.RequestConverters;
import org.apache.http.client.methods.HttpPost;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.search.fetch.source.FetchSourceContext;

public class ESUpdateRequest extends ESBaseReplicationRequest<ESUpdateRequest> {

    private String type;
    private String id;
    @Nullable
    private String routing;

    @Nullable
    private String parent;

    private BytesReference source;

    private String[] fields;
    private FetchSourceContext fetchSourceContext;

    private long version = Versions.MATCH_ANY;
    private VersionType versionType = VersionType.INTERNAL;
    private int retryOnConflict = 0;

    private ESIndexRequest upsertRequest;

    private boolean scriptedUpsert = false;
    private boolean docAsUpsert = false;
    private boolean detectNoop = true;

    /**
     * The type of the indexed document.
     */
    public String type() {
        return type;
    }

    /**
     * Sets the type of the indexed document.
     */
    public ESUpdateRequest type(String type) {
        this.type = type;
        return this;
    }

    /**
     * The id of the indexed document.
     */
    public String id() {
        return id;
    }

    /**
     * Sets the id of the indexed document.
     */
    public ESUpdateRequest id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Controls the shard routing of the request. Using this value to hash the shard
     * and not the id.
     */
    public ESUpdateRequest routing(String routing) {
        if (routing != null && routing.length() == 0) {
            this.routing = null;
        } else {
            this.routing = routing;
        }
        return this;
    }

    /**
     * Controls the shard routing of the request. Using this value to hash the shard
     * and not the id.
     */
    public String routing() {
        return this.routing;
    }

    /**
     * The parent id is used for the upsert request and also implicitely sets the routing if not already set.
     */
    public ESUpdateRequest parent(String parent) {
        this.parent = parent;
        if (routing == null) {
            routing = parent;
        }
        return this;
    }

    public String parent() {
        return parent;
    }

    /**
     * The source of the document to index, recopied to a new array if it is unsafe.
     */
    public BytesReference source() {
        return source;
    }

    public ESUpdateRequest source(String source) {
        this.source = new BytesArray(source);
        return this;
    }

    public ESUpdateRequest source(BytesReference source) {
        this.source = source;
        return this;
    }

    /**
     * Indicate that _source should be returned with every hit, with an
     * "include" and/or "exclude" set which can include simple wildcard
     * elements.
     *
     * @param include
     *            An optional include (optionally wildcarded) pattern to filter
     *            the returned _source
     * @param exclude
     *            An optional exclude (optionally wildcarded) pattern to filter
     *            the returned _source
     */
    public ESUpdateRequest fetchSource(@Nullable String include, @Nullable String exclude) {
        FetchSourceContext context = this.fetchSourceContext == null ? FetchSourceContext.FETCH_SOURCE : this.fetchSourceContext;
        this.fetchSourceContext = new FetchSourceContext(context.fetchSource(), new String[] {include}, new String[]{exclude}, false);
        return this;
    }

    /**
     * Indicate that _source should be returned, with an
     * "include" and/or "exclude" set which can include simple wildcard
     * elements.
     *
     * @param includes
     *            An optional list of include (optionally wildcarded) pattern to
     *            filter the returned _source
     * @param excludes
     *            An optional list of exclude (optionally wildcarded) pattern to
     *            filter the returned _source
     */
    public ESUpdateRequest fetchSource(@Nullable String[] includes, @Nullable String[] excludes) {
        FetchSourceContext context = this.fetchSourceContext == null ? FetchSourceContext.FETCH_SOURCE : this.fetchSourceContext;
        this.fetchSourceContext = new FetchSourceContext(context.fetchSource(), includes, excludes, false);
        return this;
    }

    /**
     * Indicates whether the response should contain the updated _source.
     */
    public ESUpdateRequest fetchSource(boolean fetchSource) {
        FetchSourceContext context = this.fetchSourceContext == null ? FetchSourceContext.FETCH_SOURCE : this.fetchSourceContext;
        this.fetchSourceContext = new FetchSourceContext(fetchSource, context.includes(), context.excludes(), false);
        return this;
    }

    /**
     * Explicitly set the fetch source context for this request
     */
    public ESUpdateRequest fetchSource(FetchSourceContext context) {
        this.fetchSourceContext = context;
        return this;
    }


    /**
     * Get the fields to be returned.
     * @deprecated Use {@link ESUpdateRequest#fetchSource()} instead
     */
    @Deprecated
    public String[] fields() {
        return fields;
    }

    /**
     * Gets the {@link FetchSourceContext} which defines how the _source should
     * be fetched.
     */
    public FetchSourceContext fetchSource() {
        return fetchSourceContext;
    }

    /**
     * Sets the number of retries of a version conflict occurs because the document was updated between
     * getting it and updating it. Defaults to 0.
     */
    public ESUpdateRequest retryOnConflict(int retryOnConflict) {
        this.retryOnConflict = retryOnConflict;
        return this;
    }

    public int retryOnConflict() {
        return this.retryOnConflict;
    }

    public ESUpdateRequest version(long version) {
        this.version = version;
        return this;
    }

    public long version() {
        return this.version;
    }

    public ESUpdateRequest versionType(VersionType versionType) {
        this.versionType = versionType;
        return this;
    }

    public VersionType versionType() {
        return this.versionType;
    }

    public boolean docAsUpsert() {
        return this.docAsUpsert;
    }

    public ESUpdateRequest docAsUpsert(boolean shouldUpsertDoc) {
        this.docAsUpsert = shouldUpsertDoc;
        return this;
    }

    public boolean scriptedUpsert(){
        return this.scriptedUpsert;
    }

    public ESUpdateRequest scriptedUpsert(boolean scriptedUpsert) {
        this.scriptedUpsert = scriptedUpsert;
        return this;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        String endpoint;
        if (type() == null) {
            endpoint = RequestConverters.endpoint(index(), "_update", id());
        } else {
            endpoint = RequestConverters.endpoint(index(), type(), id(), "_update");
        }

        RestRequest request = new RestRequest(HttpPost.METHOD_NAME, endpoint);

        RequestConverters.Params parameters = new RequestConverters.Params(request);
        parameters.withRouting(routing());
        parameters.withParent(parent());
        parameters.withTimeout(timeout());
        parameters.withWaitForActiveShards(getWaitForActiveShards());
        parameters.withDocAsUpsert(docAsUpsert());
        parameters.withFetchSourceContext(fetchSource());
        parameters.withRetryOnConflict(retryOnConflict());
        parameters.withVersion(version());
        parameters.withVersionType(versionType());

        request.setBody(source);
        return request;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        XContentParser parser = JsonXContent.jsonXContent.createParser(response.getResponseContent());
        return ESUpdateResponse.fromXContent(parser);
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }
}
