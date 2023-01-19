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

package org.elasticsearch.action.update;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationRequest;
import org.elasticsearch.action.support.single.instance.InstanceShardOperationRequest;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.action.ValidateActions.addValidationError;
import static org.elasticsearch.index.seqno.SequenceNumbers.UNASSIGNED_PRIMARY_TERM;
import static org.elasticsearch.index.seqno.SequenceNumbers.UNASSIGNED_SEQ_NO;

public class UpdateRequest extends InstanceShardOperationRequest<UpdateRequest>
        implements DocWriteRequest<UpdateRequest>, WriteRequest<UpdateRequest>, ToXContentObject {
    private static ObjectParser<UpdateRequest, Void> PARSER;

    private static final ParseField SCRIPT_FIELD = new ParseField("script");
    private static final ParseField SCRIPTED_UPSERT_FIELD = new ParseField("scripted_upsert");
    private static final ParseField UPSERT_FIELD = new ParseField("upsert");
    private static final ParseField DOC_FIELD = new ParseField("doc");
    private static final ParseField DOC_AS_UPSERT_FIELD = new ParseField("doc_as_upsert");
    private static final ParseField DETECT_NOOP_FIELD = new ParseField("detect_noop");
    private static final ParseField SOURCE_FIELD = new ParseField("_source");
    private static final ParseField IF_SEQ_NO = new ParseField("if_seq_no");
    private static final ParseField IF_PRIMARY_TERM = new ParseField("if_primary_term");

    static {
        PARSER = new ObjectParser<>(UpdateRequest.class.getSimpleName());
        PARSER.declareField((request, script) -> request.script = script,
            (parser, context) -> Script.parse(parser), SCRIPT_FIELD, ObjectParser.ValueType.OBJECT_OR_STRING);
        PARSER.declareBoolean(UpdateRequest::scriptedUpsert, SCRIPTED_UPSERT_FIELD);
        PARSER.declareObject((request, builder) -> request.safeUpsertRequest().source(builder),
            (parser, context) -> {
                XContentBuilder builder = XContentFactory.contentBuilder(parser.contentType());
                builder.copyCurrentStructure(parser);
                return builder;
            }, UPSERT_FIELD);
        PARSER.declareObject((request, builder) -> request.safeDoc().source(builder),
            (parser, context) -> {
                XContentBuilder docBuilder = XContentFactory.contentBuilder(parser.contentType());
                docBuilder.copyCurrentStructure(parser);
                return docBuilder;
            }, DOC_FIELD);
        PARSER.declareBoolean(UpdateRequest::docAsUpsert, DOC_AS_UPSERT_FIELD);
        PARSER.declareBoolean(UpdateRequest::detectNoop, DETECT_NOOP_FIELD);
        PARSER.declareField(UpdateRequest::fetchSource,
            (parser, context) -> FetchSourceContext.fromXContent(parser), SOURCE_FIELD,
            ObjectParser.ValueType.OBJECT_ARRAY_BOOLEAN_OR_STRING);
        PARSER.declareLong(UpdateRequest::setIfSeqNo, IF_SEQ_NO);
        PARSER.declareLong(UpdateRequest::setIfPrimaryTerm, IF_PRIMARY_TERM);
    }

    // Set to null initially so we can know to override in bulk requests that have a default type.
    private String type;
    private String id;
    @Nullable
    private String routing;

    @Nullable
    Script script;

    private FetchSourceContext fetchSourceContext;

    private int retryOnConflict = 0;
    private long ifSeqNo = UNASSIGNED_SEQ_NO;
    private long ifPrimaryTerm = UNASSIGNED_PRIMARY_TERM;


    private RefreshPolicy refreshPolicy = RefreshPolicy.NONE;

    private ActiveShardCount waitForActiveShards = ActiveShardCount.DEFAULT;

    private IndexRequest upsertRequest;

    private boolean scriptedUpsert = false;
    private boolean docAsUpsert = false;
    private boolean detectNoop = true;

    private String pipeline;

    @Nullable
    private IndexRequest doc;

    public UpdateRequest() {}

    public UpdateRequest(StreamInput in) throws IOException {
        super(in);
        waitForActiveShards = ActiveShardCount.readFrom(in);
        type = in.readString();
        id = in.readString();
        routing = in.readOptionalString();
        if (in.getVersion().before(Version.V_7_0_0)) {
            in.readOptionalString(); // _parent
        }
        if (in.readBoolean()) {
            script = new Script(in);
        }
        retryOnConflict = in.readVInt();
        refreshPolicy = RefreshPolicy.readFrom(in);
        if (in.readBoolean()) {
            doc = new IndexRequest(in);
        }
        if (in.getVersion().before(Version.V_7_0_0)) {
            String[] fields = in.readOptionalStringArray();
            if (fields != null) {
                throw new IllegalArgumentException("[fields] is no longer supported");
            }
        }
        fetchSourceContext = in.readOptionalWriteable(FetchSourceContext::new);
        if (in.readBoolean()) {
            upsertRequest = new IndexRequest(in);
        }
        docAsUpsert = in.readBoolean();
        if (in.getVersion().before(Version.V_7_0_0)) {
            long version = in.readLong();
            VersionType versionType = VersionType.readFromStream(in);
            if (version != Versions.MATCH_ANY || versionType != VersionType.INTERNAL) {
                throw new UnsupportedOperationException(
                    "versioned update requests have been removed in 7.0. Use if_seq_no and if_primary_term");
            }
        }
        ifSeqNo = in.readZLong();
        ifPrimaryTerm = in.readVLong();
        detectNoop = in.readBoolean();
        scriptedUpsert = in.readBoolean();
    }

    public UpdateRequest(String index, String id) {
        super(index);
        this.id = id;
    }

    /**
     * @deprecated Types are in the process of being removed. Use {@link #UpdateRequest(String, String)} instead.
     */
    @Deprecated
    public UpdateRequest(String index, String type, String id) {
        super(index);
        this.type = type;
        this.id = id;
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = super.validate();
        if(upsertRequest != null && upsertRequest.version() != Versions.MATCH_ANY) {
            validationException = addValidationError("can't provide version in upsert request", validationException);
        }
        if (Strings.isEmpty(type())) {
            validationException = addValidationError("type is missing", validationException);
        }
        if (Strings.isEmpty(id)) {
            validationException = addValidationError("id is missing", validationException);
        }

        validationException = DocWriteRequest.validateSeqNoBasedCASParams(this, validationException);

        if (ifSeqNo != UNASSIGNED_SEQ_NO) {
            if (retryOnConflict > 0) {
                validationException = addValidationError("compare and write operations can not be retried", validationException);
            }

            if (docAsUpsert) {
                validationException = addValidationError("compare and write operations can not be used with upsert", validationException);
            }
            if (upsertRequest != null) {
                validationException =
                    addValidationError("upsert requests don't support `if_seq_no` and `if_primary_term`", validationException);
            }
        }

        if (script == null && doc == null) {
            validationException = addValidationError("script or doc is missing", validationException);
        }
        if (script != null && doc != null) {
            validationException = addValidationError("can't provide both script and doc", validationException);
        }
        if (doc == null && docAsUpsert) {
            validationException = addValidationError("doc must be specified if doc_as_upsert is enabled", validationException);
        }
        return validationException;
    }

    /**
     * The type of the indexed document.
     *
     * @deprecated Types are in the process of being removed.
     */
    @Deprecated
    @Override
    public String type() {
        if (type == null) {
            return MapperService.SINGLE_MAPPING_NAME;
        }
        return type;
    }

    /**
     * Sets the type of the indexed document.
     *
     * @deprecated Types are in the process of being removed.
     */
    @Deprecated
    public UpdateRequest type(String type) {
        this.type = type;
        return this;
    }

    /**
     * Set the default type supplied to a bulk
     * request if this individual request's type is null
     * or empty
     * @deprecated Types are in the process of being removed.
     */
    @Deprecated
    @Override
    public UpdateRequest defaultTypeIfNull(String defaultType) {
        if (Strings.isNullOrEmpty(type)) {
            type = defaultType;
        }
        return this;
    }

    /**
     * The id of the indexed document.
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Sets the id of the indexed document.
     */
    public UpdateRequest id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Controls the shard routing of the request. Using this value to hash the shard
     * and not the id.
     */
    @Override
    public UpdateRequest routing(String routing) {
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
    @Override
    public String routing() {
        return this.routing;
    }

    public ShardId getShardId() {
        return this.shardId;
    }

    public Script script() {
        return this.script;
    }

    /**
     * The script to execute. Note, make sure not to send different script each times and instead
     * use script params if possible with the same (automatically compiled) script.
     */
    public UpdateRequest script(Script script) {
        this.script = script;
        return this;
    }

    /**
     * @deprecated Use {@link #script()} instead
     */
    @Deprecated
    public String scriptString() {
        return this.script == null ? null : this.script.getIdOrCode();
    }

    /**
     * @deprecated Use {@link #script()} instead
     */
    @Deprecated
    public ScriptType scriptType() {
        return this.script == null ? null : this.script.getType();
    }

    /**
     * @deprecated Use {@link #script()} instead
     */
    @Deprecated
    public Map<String, Object> scriptParams() {
        return this.script == null ? null : this.script.getParams();
    }

    /**
     * The script to execute. Note, make sure not to send different script each
     * times and instead use script params if possible with the same
     * (automatically compiled) script.
     *
     * @deprecated Use {@link #script(Script)} instead
     */
    @Deprecated
    public UpdateRequest script(String script, ScriptType scriptType) {
        updateOrCreateScript(script, scriptType, null, null);
        return this;
    }

    /**
     * The script to execute. Note, make sure not to send different script each
     * times and instead use script params if possible with the same
     * (automatically compiled) script.
     *
     * @deprecated Use {@link #script(Script)} instead
     */
    @Deprecated
    public UpdateRequest script(String script) {
        updateOrCreateScript(script, ScriptType.INLINE, null, null);
        return this;
    }

    /**
     * The language of the script to execute.
     *
     * @deprecated Use {@link #script(Script)} instead
     */
    @Deprecated
    public UpdateRequest scriptLang(String scriptLang) {
        updateOrCreateScript(null, null, scriptLang, null);
        return this;
    }

    /**
     * @deprecated Use {@link #script()} instead
     */
    @Deprecated
    public String scriptLang() {
        return script == null ? null : script.getLang();
    }

    /**
     * Add a script parameter.
     *
     * @deprecated Use {@link #script(Script)} instead
     */
    @Deprecated
    public UpdateRequest addScriptParam(String name, Object value) {
        Script script = script();
        if (script == null) {
            HashMap<String, Object> scriptParams = new HashMap<>();
            scriptParams.put(name, value);
            updateOrCreateScript(null, null, null, scriptParams);
        } else {
            Map<String, Object> scriptParams = script.getParams();
            if (scriptParams == null) {
                scriptParams = new HashMap<>();
                scriptParams.put(name, value);
                updateOrCreateScript(null, null, null, scriptParams);
            } else {
                scriptParams.put(name, value);
            }
        }
        return this;
    }

    /**
     * Sets the script parameters to use with the script.
     *
     * @deprecated Use {@link #script(Script)} instead
     */
    @Deprecated
    public UpdateRequest scriptParams(Map<String, Object> scriptParams) {
        updateOrCreateScript(null, null, null, scriptParams);
        return this;
    }

    private void updateOrCreateScript(String scriptContent, ScriptType type, String lang, Map<String, Object> params) {
        Script script = script();
        if (script == null) {
            script = new Script(type == null ? ScriptType.INLINE : type, lang, scriptContent == null ? "" : scriptContent, params);
        } else {
            String newScriptContent = scriptContent == null ? script.getIdOrCode() : scriptContent;
            ScriptType newScriptType = type == null ? script.getType() : type;
            String newScriptLang = lang == null ? script.getLang() : lang;
            Map<String, Object> newScriptParams = params == null ? script.getParams() : params;
            script = new Script(newScriptType, newScriptLang, newScriptContent, newScriptParams);
        }
        script(script);
    }

    /**
     * The script to execute. Note, make sure not to send different script each
     * times and instead use script params if possible with the same
     * (automatically compiled) script.
     *
     * @deprecated Use {@link #script(Script)} instead
     */
    @Deprecated
    public UpdateRequest script(String script, ScriptType scriptType, @Nullable Map<String, Object> scriptParams) {
        this.script = new Script(scriptType, Script.DEFAULT_SCRIPT_LANG, script, scriptParams);
        return this;
    }

    /**
     * The script to execute. Note, make sure not to send different script each
     * times and instead use script params if possible with the same
     * (automatically compiled) script.
     *
     * @param script
     *            The script to execute
     * @param scriptLang
     *            The script language
     * @param scriptType
     *            The script type
     * @param scriptParams
     *            The script parameters
     *
     * @deprecated Use {@link #script(Script)} instead
     */
    @Deprecated
    public UpdateRequest script(String script, @Nullable String scriptLang, ScriptType scriptType,
            @Nullable Map<String, Object> scriptParams) {
        this.script = new Script(scriptType, scriptLang, script, scriptParams);
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
    public UpdateRequest fetchSource(@Nullable String include, @Nullable String exclude) {
        FetchSourceContext context = this.fetchSourceContext == null ? FetchSourceContext.FETCH_SOURCE : this.fetchSourceContext;
        String[] includes = include == null ? Strings.EMPTY_ARRAY : new String[]{include};
        String[] excludes = exclude == null ? Strings.EMPTY_ARRAY : new String[]{exclude};
        this.fetchSourceContext = new FetchSourceContext(context.fetchSource(), includes, excludes);
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
    public UpdateRequest fetchSource(@Nullable String[] includes, @Nullable String[] excludes) {
        FetchSourceContext context = this.fetchSourceContext == null ? FetchSourceContext.FETCH_SOURCE : this.fetchSourceContext;
        this.fetchSourceContext = new FetchSourceContext(context.fetchSource(), includes, excludes);
        return this;
    }

    /**
     * Indicates whether the response should contain the updated _source.
     */
    public UpdateRequest fetchSource(boolean fetchSource) {
        FetchSourceContext context = this.fetchSourceContext == null ? FetchSourceContext.FETCH_SOURCE : this.fetchSourceContext;
        this.fetchSourceContext = new FetchSourceContext(fetchSource, context.includes(), context.excludes());
        return this;
    }

    /**
     * Explicitly set the fetch source context for this request
     */
    public UpdateRequest fetchSource(FetchSourceContext context) {
        this.fetchSourceContext = context;
        return this;
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
    public UpdateRequest retryOnConflict(int retryOnConflict) {
        this.retryOnConflict = retryOnConflict;
        return this;
    }

    public int retryOnConflict() {
        return this.retryOnConflict;
    }

    @Override
    public UpdateRequest version(long version) {
        throw new UnsupportedOperationException("update requests do not support versioning");
    }

    @Override
    public long version() {
        return Versions.MATCH_ANY;
    }

    @Override
    public UpdateRequest versionType(VersionType versionType) {
        throw new UnsupportedOperationException("update requests do not support versioning");
    }

    @Override
    public VersionType versionType() {
        return VersionType.INTERNAL;
    }

    /**
     * only perform this update request if the document's modification was assigned the given
     * sequence number. Must be used in combination with {@link #setIfPrimaryTerm(long)}
     *
     * If the document last modification was assigned a different sequence number a
     * {@link org.elasticsearch.index.engine.VersionConflictEngineException} will be thrown.
     */
    public UpdateRequest setIfSeqNo(long seqNo) {
        if (seqNo < 0 && seqNo != UNASSIGNED_SEQ_NO) {
            throw new IllegalArgumentException("sequence numbers must be non negative. got [" +  seqNo + "].");
        }
        ifSeqNo = seqNo;
        return this;
    }

    /**
     * only performs this update request if the document's last modification was assigned the given
     * primary term. Must be used in combination with {@link #setIfSeqNo(long)}
     *
     * If the document last modification was assigned a different term a
     * {@link org.elasticsearch.index.engine.VersionConflictEngineException} will be thrown.
     */
    public UpdateRequest setIfPrimaryTerm(long term) {
        if (term < 0) {
            throw new IllegalArgumentException("primary term must be non negative. got [" + term + "]");
        }
        ifPrimaryTerm = term;
        return this;
    }

    /**
     * If set, only perform this update request if the document was last modification was assigned this sequence number.
     * If the document last modification was assigned a different sequence number a
     * {@link org.elasticsearch.index.engine.VersionConflictEngineException} will be thrown.
     */
    public long ifSeqNo() {
        return ifSeqNo;
    }

    /**
     * If set, only perform this update request if the document was last modification was assigned this primary term.
     *
     * If the document last modification was assigned a different term a
     * {@link org.elasticsearch.index.engine.VersionConflictEngineException} will be thrown.
     */
    public long ifPrimaryTerm() {
        return ifPrimaryTerm;
    }

    @Override
    public OpType opType() {
        return OpType.UPDATE;
    }

    @Override
    public UpdateRequest setRefreshPolicy(RefreshPolicy refreshPolicy) {
        this.refreshPolicy = refreshPolicy;
        return this;
    }

    @Override
    public RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    public ActiveShardCount waitForActiveShards() {
        return this.waitForActiveShards;
    }

    /**
     * Sets the number of shard copies that must be active before proceeding with the write.
     * See {@link ReplicationRequest#waitForActiveShards(ActiveShardCount)} for details.
     */
    public UpdateRequest waitForActiveShards(ActiveShardCount waitForActiveShards) {
        this.waitForActiveShards = waitForActiveShards;
        return this;
    }

    /**
     * A shortcut for {@link #waitForActiveShards(ActiveShardCount)} where the numerical
     * shard count is passed in, instead of having to first call {@link ActiveShardCount#from(int)}
     * to get the ActiveShardCount.
     */
    public UpdateRequest waitForActiveShards(final int waitForActiveShards) {
        return waitForActiveShards(ActiveShardCount.from(waitForActiveShards));
    }

    /**
     * Sets the doc to use for updates when a script is not specified.
     */
    public UpdateRequest doc(IndexRequest doc) {
        this.doc = doc;
        return this;
    }

    /**
     * Sets the doc to use for updates when a script is not specified.
     */
    public UpdateRequest doc(XContentBuilder source) {
        safeDoc().source(source);
        return this;
    }

    /**
     * Sets the doc to use for updates when a script is not specified.
     */
    public UpdateRequest doc(Map<String, Object> source) {
        safeDoc().source(source);
        return this;
    }

    /**
     * Sets the doc to use for updates when a script is not specified.
     */
    public UpdateRequest doc(Map<String, Object> source, XContentType contentType) {
        safeDoc().source(source, contentType);
        return this;
    }

    /**
     * Sets the doc to use for updates when a script is not specified.
     */
    public UpdateRequest doc(String source, XContentType xContentType) {
        safeDoc().source(source, xContentType);
        return this;
    }

    /**
     * Sets the doc to use for updates when a script is not specified.
     */
    public UpdateRequest doc(byte[] source, XContentType xContentType) {
        safeDoc().source(source, xContentType);
        return this;
    }

    /**
     * Sets the doc to use for updates when a script is not specified.
     */
    public UpdateRequest doc(byte[] source, int offset, int length, XContentType xContentType) {
        safeDoc().source(source, offset, length, xContentType);
        return this;
    }

    /**
     * Sets the doc to use for updates when a script is not specified, the doc provided
     * is a field and value pairs.
     */
    public UpdateRequest doc(Object... source) {
        safeDoc().source(source);
        return this;
    }

    /**
     * Sets the doc to use for updates when a script is not specified, the doc provided
     * is a field and value pairs.
     */
    public UpdateRequest doc(XContentType xContentType, Object... source) {
        safeDoc().source(xContentType, source);
        return this;
    }

    public IndexRequest doc() {
        return this.doc;
    }

    private IndexRequest safeDoc() {
        if (doc == null) {
            doc = new IndexRequest();
        }
        return doc;
    }

    /**
     * Sets the index request to be used if the document does not exists. Otherwise, a
     * {@link org.elasticsearch.index.engine.DocumentMissingException} is thrown.
     */
    public UpdateRequest upsert(IndexRequest upsertRequest) {
        this.upsertRequest = upsertRequest;
        return this;
    }

    /**
     * Sets the doc source of the update request to be used when the document does not exists.
     */
    public UpdateRequest upsert(XContentBuilder source) {
        safeUpsertRequest().source(source);
        return this;
    }

    /**
     * Sets the doc source of the update request to be used when the document does not exists.
     */
    public UpdateRequest upsert(Map<String, Object> source) {
        safeUpsertRequest().source(source);
        return this;
    }

    /**
     * Sets the doc source of the update request to be used when the document does not exists.
     */
    public UpdateRequest upsert(Map<String, Object> source, XContentType contentType) {
        safeUpsertRequest().source(source, contentType);
        return this;
    }

    /**
     * Sets the doc source of the update request to be used when the document does not exists.
     */
    public UpdateRequest upsert(String source, XContentType xContentType) {
        safeUpsertRequest().source(source, xContentType);
        return this;
    }

    /**
     * Sets the doc source of the update request to be used when the document does not exists.
     */
    public UpdateRequest upsert(byte[] source, XContentType xContentType) {
        safeUpsertRequest().source(source, xContentType);
        return this;
    }

    /**
     * Sets the doc source of the update request to be used when the document does not exists.
     */
    public UpdateRequest upsert(byte[] source, int offset, int length, XContentType xContentType) {
        safeUpsertRequest().source(source, offset, length, xContentType);
        return this;
    }

    /**
     * Sets the doc source of the update request to be used when the document does not exists. The doc
     * includes field and value pairs.
     */
    public UpdateRequest upsert(Object... source) {
        safeUpsertRequest().source(source);
        return this;
    }

    /**
     * Sets the doc source of the update request to be used when the document does not exists. The doc
     * includes field and value pairs.
     */
    public UpdateRequest upsert(XContentType xContentType, Object... source) {
        safeUpsertRequest().source(xContentType, source);
        return this;
    }

    public IndexRequest upsertRequest() {
        return this.upsertRequest;
    }

    private IndexRequest safeUpsertRequest() {
        if (upsertRequest == null) {
            upsertRequest = new IndexRequest();
        }
        return upsertRequest;
    }

    /**
     * Should this update attempt to detect if it is a noop? Defaults to true.
     * @return this for chaining
     */
    public UpdateRequest detectNoop(boolean detectNoop) {
        this.detectNoop = detectNoop;
        return this;
    }

    /**
     * Should this update attempt to detect if it is a noop? Defaults to true.
     */
    public boolean detectNoop() {
        return detectNoop;
    }

    public UpdateRequest fromXContent(XContentParser parser) throws IOException {
        return PARSER.parse(parser, this, null);
    }

    public boolean docAsUpsert() {
        return this.docAsUpsert;
    }

    public UpdateRequest docAsUpsert(boolean shouldUpsertDoc) {
        this.docAsUpsert = shouldUpsertDoc;
        return this;
    }

    public boolean scriptedUpsert(){
        return this.scriptedUpsert;
    }

    public UpdateRequest scriptedUpsert(boolean scriptedUpsert) {
        this.scriptedUpsert = scriptedUpsert;
        return this;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        waitForActiveShards.writeTo(out);
        // A 7.x request allows null types but if deserialized in a 6.x node will cause nullpointer exceptions.
        // So we use the type accessor method here to make the type non-null (will default it to "_doc").
        out.writeString(type());
        out.writeString(id);
        out.writeOptionalString(routing);
        if (out.getVersion().before(Version.V_7_0_0)) {
            out.writeOptionalString(null); // _parent
        }

        boolean hasScript = script != null;
        out.writeBoolean(hasScript);
        if (hasScript) {
            script.writeTo(out);
        }
        out.writeVInt(retryOnConflict);
        refreshPolicy.writeTo(out);
        if (doc == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            // make sure the basics are set
            doc.index(index);
            doc.type(type);
            doc.id(id);
            doc.writeTo(out);
        }
        if (out.getVersion().before(Version.V_7_0_0)) {
            out.writeOptionalStringArray(null);
        }
        out.writeOptionalWriteable(fetchSourceContext);
        if (upsertRequest == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            // make sure the basics are set
            upsertRequest.index(index);
            upsertRequest.type(type);
            upsertRequest.id(id);
            upsertRequest.writeTo(out);
        }
        out.writeBoolean(docAsUpsert);
        if (out.getVersion().before(Version.V_7_0_0)) {
            out.writeLong(Versions.MATCH_ANY);
            out.writeByte(VersionType.INTERNAL.getValue());
        }
        out.writeZLong(ifSeqNo);
        out.writeVLong(ifPrimaryTerm);
        out.writeBoolean(detectNoop);
        out.writeBoolean(scriptedUpsert);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        if (docAsUpsert) {
            builder.field("doc_as_upsert", docAsUpsert);
        }
        if (doc != null) {
            XContentType xContentType = doc.getContentType();
            try (XContentParser parser = XContentHelper.createParser(NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE, doc.source(), xContentType)) {
                builder.field("doc");
                builder.copyCurrentStructure(parser);
            }
        }

        if (ifSeqNo != UNASSIGNED_SEQ_NO) {
            builder.field(IF_SEQ_NO.getPreferredName(), ifSeqNo);
            builder.field(IF_PRIMARY_TERM.getPreferredName(), ifPrimaryTerm);
        }

        if (script != null) {
            builder.field("script", script);
        }
        if (upsertRequest != null) {
            XContentType xContentType = upsertRequest.getContentType();
            try (XContentParser parser = XContentHelper.createParser(NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE, upsertRequest.source(), xContentType)) {
                builder.field("upsert");
                builder.copyCurrentStructure(parser);
            }
        }
        if (scriptedUpsert) {
            builder.field("scripted_upsert", scriptedUpsert);
        }
        if (detectNoop == false) {
            builder.field("detect_noop", detectNoop);
        }
        if (fetchSourceContext != null) {
            builder.field("_source", fetchSourceContext);
        }
        builder.endObject();
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
            .append("update {[").append(index)
            .append("][").append(type())
            .append("][").append(id).append("]");
        res.append(", doc_as_upsert[").append(docAsUpsert).append("]");
        if (doc != null) {
            res.append(", doc[").append(doc).append("]");
        }
        if (script != null) {
            res.append(", script[").append(script).append("]");
        }
        if (upsertRequest != null) {
            res.append(", upsert[").append(upsertRequest).append("]");
        }
        res.append(", scripted_upsert[").append(scriptedUpsert).append("]");
        res.append(", detect_noop[").append(detectNoop).append("]");
        return res.append("}").toString();
    }

    public String getPipeline() {
        return pipeline;
    }

    public UpdateRequest setPipeline(String pipeline) {
        this.pipeline = pipeline;
        return this;
    }
}
