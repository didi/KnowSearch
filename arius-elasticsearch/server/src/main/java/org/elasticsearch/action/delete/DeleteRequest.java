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

package org.elasticsearch.action.delete;

import static org.elasticsearch.action.ValidateActions.addValidationError;
import static org.elasticsearch.index.seqno.SequenceNumbers.UNASSIGNED_PRIMARY_TERM;
import static org.elasticsearch.index.seqno.SequenceNumbers.UNASSIGNED_SEQ_NO;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.CompositeIndicesRequest;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.replication.ReplicatedWriteRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.shard.ShardId;

/**
 * A request to delete a document from an index based on its type and id. Best created using
 * {@link org.elasticsearch.client.Requests#deleteRequest(String)}.
 * <p>
 * The operation requires the {@link #index()}, {@link #type(String)} and {@link #id(String)} to
 * be set.
 *
 * @see DeleteResponse
 * @see org.elasticsearch.client.Client#delete(DeleteRequest)
 * @see org.elasticsearch.client.Requests#deleteRequest(String)
 */
public class DeleteRequest extends ReplicatedWriteRequest<DeleteRequest>
        implements DocWriteRequest<DeleteRequest>, CompositeIndicesRequest {

    private static final ShardId NO_SHARD_ID = null;

    // Set to null initially so we can know to override in bulk requests that have a default type.
    private String type;
    private String id;
    @Nullable
    private String routing;
    private long version = Versions.MATCH_ANY;
    private VersionType versionType = VersionType.INTERNAL;
    private long ifSeqNo = UNASSIGNED_SEQ_NO;
    private long ifPrimaryTerm = UNASSIGNED_PRIMARY_TERM;

    private BytesReference source;
    private XContentType contentType;

    private String pipeline;
    private IndexRequest indexRequest;

    public DeleteRequest(StreamInput in) throws IOException {
        super(in);
        type = in.readString();
        id = in.readString();
        routing = in.readOptionalString();
        if (in.getVersion().before(Version.V_7_0_0)) {
            in.readOptionalString(); // _parent
        }
        version = in.readLong();
        versionType = VersionType.fromValue(in.readByte());
        if (in.getVersion().onOrAfter(Version.V_6_6_0)) {
            ifSeqNo = in.readZLong();
            ifPrimaryTerm = in.readVLong();
        } else {
            ifSeqNo = UNASSIGNED_SEQ_NO;
            ifPrimaryTerm = UNASSIGNED_PRIMARY_TERM;
        }
    }

    public DeleteRequest() {
        super(NO_SHARD_ID);
    }

    /**
     * Constructs a new delete request against the specified index. The {@link #type(String)} and {@link #id(String)}
     * must be set.
     */
    public DeleteRequest(String index) {
        super(NO_SHARD_ID);
        this.index = index;
    }

    /**
     * Constructs a new delete request against the specified index with the type and id.
     *
     * @param index The index to get the document from
     * @param type  The type of the document
     * @param id    The id of the document
     *
     * @deprecated Types are in the process of being removed. Use {@link #DeleteRequest(String, String)} instead.
     */
    @Deprecated
    public DeleteRequest(String index, String type, String id) {
        super(NO_SHARD_ID);
        this.index = index;
        this.type = type;
        this.id = id;
    }

    /**
     * Constructs a new delete request against the specified index and id.
     *
     * @param index The index to get the document from
     * @param id    The id of the document
     */
    public DeleteRequest(String index, String id) {
        super(NO_SHARD_ID);
        this.index = index;
        this.id = id;
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = super.validate();
        if (Strings.isEmpty(type())) {
            validationException = addValidationError("type is missing", validationException);
        }
        if (Strings.isEmpty(id)) {
            validationException = addValidationError("id is missing", validationException);
        }

        validationException = DocWriteRequest.validateSeqNoBasedCASParams(this, validationException);

        return validationException;
    }

    /**
     * The type of the document to delete.
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
     * Sets the type of the document to delete.
     *
     * @deprecated Types are in the process of being removed.
     */
    @Deprecated
    @Override
    public DeleteRequest type(String type) {
        this.type = type;
        return this;
    }

    /**
     * Set the default type supplied to a bulk
     * request if this individual request's type is null
     * or empty
     *
     * @deprecated Types are in the process of being removed.
     */
    @Deprecated
    @Override
    public DeleteRequest defaultTypeIfNull(String defaultType) {
        if (Strings.isNullOrEmpty(type)) {
            type = defaultType;
        }
        return this;
    }

    /**
     * The id of the document to delete.
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Sets the id of the document to delete.
     */
    public DeleteRequest id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Controls the shard routing of the request. Using this value to hash the shard
     * and not the id.
     */
    @Override
    public DeleteRequest routing(String routing) {
        if (routing != null && routing.length() == 0) {
            this.routing = null;
        } else {
            this.routing = routing;
        }
        return this;
    }

    /**
     * Controls the shard routing of the delete request. Using this value to hash the shard
     * and not the id.
     */
    @Override
    public String routing() {
        return this.routing;
    }

    @Override
    public DeleteRequest version(long version) {
        this.version = version;
        return this;
    }

    @Override
    public long version() {
        return this.version;
    }

    @Override
    public DeleteRequest versionType(VersionType versionType) {
        this.versionType = versionType;
        return this;
    }

    /**
     * If set, only perform this delete request if the document was last modification was assigned this sequence number.
     * If the document last modification was assigned a different sequence number a
     * {@link org.elasticsearch.index.engine.VersionConflictEngineException} will be thrown.
     */
    public long ifSeqNo() {
        return ifSeqNo;
    }

    /**
     * If set, only perform this delete request if the document was last modification was assigned this primary term.
     *
     * If the document last modification was assigned a different term a
     * {@link org.elasticsearch.index.engine.VersionConflictEngineException} will be thrown.
     */
    public long ifPrimaryTerm() {
        return ifPrimaryTerm;
    }

    /**
     * only perform this delete request if the document was last modification was assigned the given
     * sequence number. Must be used in combination with {@link #setIfPrimaryTerm(long)}
     *
     * If the document last modification was assigned a different sequence number a
     * {@link org.elasticsearch.index.engine.VersionConflictEngineException} will be thrown.
     */
    public DeleteRequest setIfSeqNo(long seqNo) {
        if (seqNo < 0 && seqNo != UNASSIGNED_SEQ_NO) {
            throw new IllegalArgumentException("sequence numbers must be non negative. got [" +  seqNo + "].");
        }
        ifSeqNo = seqNo;
        return this;
    }

    /**
     * only perform this delete request if the document was last modification was assigned the given
     * primary term. Must be used in combination with {@link #setIfSeqNo(long)}
     *
     * If the document last modification was assigned a different primary term a
     * {@link org.elasticsearch.index.engine.VersionConflictEngineException} will be thrown.
     */
    public DeleteRequest setIfPrimaryTerm(long term) {
        if (term < 0) {
            throw new IllegalArgumentException("primary term must be non negative. got [" + term + "]");
        }
        ifPrimaryTerm = term;
        return this;
    }

    @Override
    public VersionType versionType() {
        return this.versionType;
    }

    @Override
    public OpType opType() {
        return OpType.DELETE;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        // A 7.x request allows null types but if deserialized in a 6.x node will cause nullpointer exceptions.
        // So we use the type accessor method here to make the type non-null (will default it to "_doc").
        out.writeString(type());
        out.writeString(id);
        out.writeOptionalString(routing());
        if (out.getVersion().before(Version.V_7_0_0)) {
            out.writeOptionalString(null); // _parent
        }
        out.writeLong(version);
        out.writeByte(versionType.getValue());
        if (out.getVersion().onOrAfter(Version.V_6_6_0)) {
            out.writeZLong(ifSeqNo);
            out.writeVLong(ifPrimaryTerm);
        } else if (ifSeqNo != UNASSIGNED_SEQ_NO || ifPrimaryTerm != UNASSIGNED_PRIMARY_TERM) {
            assert false : "setIfMatch [" + ifSeqNo + "], currentDocTem [" + ifPrimaryTerm + "]";
            throw new IllegalStateException(
                "sequence number based compare and write is not supported until all nodes are on version 7.0 or higher. " +
                    "Stream version [" + out.getVersion() + "]");
        }
    }

    @Override
    public String toString() {
        return "delete {[" + index + "][" + type() + "][" + id + "]}";
    }

    /**
     * The source of the document to index, recopied to a new array if it is unsafe.
     */
    public BytesReference source() {
        return source;
    }

    public Map<String, Object> sourceAsMap() {
        return XContentHelper.convertToMap(source, false, contentType).v2();
    }

    /**
     * Index the Map in {@link Requests#INDEX_CONTENT_TYPE} format
     *
     * @param source The map to index
     */
    public DeleteRequest source(Map source) throws ElasticsearchGenerationException {
        return source(source, Requests.INDEX_CONTENT_TYPE);
    }

    /**
     * Index the Map as the provided content type.
     *
     * @param source The map to index
     */
    public DeleteRequest source(Map source, XContentType contentType) throws ElasticsearchGenerationException {
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(contentType);
            builder.map(source);
            return source(builder);
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed to generate [" + source + "]", e);
        }
    }

    /**
     * Sets the document source to index.
     *
     * Note, its preferable to either set it using {@link #source(org.elasticsearch.common.xcontent.XContentBuilder)}
     * or using the {@link #source(byte[], XContentType)}.
     */
    public DeleteRequest source(String source, XContentType xContentType) {
        return source(new BytesArray(source), xContentType);
    }

    /**
     * Sets the content source to index.
     */
    public DeleteRequest source(XContentBuilder sourceBuilder) {
        return source(BytesReference.bytes(sourceBuilder), sourceBuilder.contentType());
    }

    /**
     * Sets the content source to index using the default content type ({@link Requests#INDEX_CONTENT_TYPE})
     * <p>
     * <b>Note: the number of objects passed to this method must be an even
     * number. Also the first argument in each pair (the field name) must have a
     * valid String representation.</b>
     * </p>
     */
    public DeleteRequest source(Object... source) {
        return source(Requests.INDEX_CONTENT_TYPE, source);
    }

    /**
     * Sets the content source to index.
     * <p>
     * <b>Note: the number of objects passed to this method as varargs must be an even
     * number. Also the first argument in each pair (the field name) must have a
     * valid String representation.</b>
     * </p>
     */
    public DeleteRequest source(XContentType xContentType, Object... source) {
        if (source.length % 2 != 0) {
            throw new IllegalArgumentException("The number of object passed must be even but was [" + source.length + "]");
        }
        if (source.length == 2 && source[0] instanceof BytesReference && source[1] instanceof Boolean) {
            throw new IllegalArgumentException("you are using the removed method for source with bytes and unsafe flag, the unsafe flag"
                + " was removed, please just use source(BytesReference)");
        }
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(xContentType);
            builder.startObject();
            for (int i = 0; i < source.length; i++) {
                builder.field(source[i++].toString(), source[i]);
            }
            builder.endObject();
            return source(builder);
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed to generate", e);
        }
    }

    /**
     * Sets the document to index in bytes form.
     */
    public DeleteRequest source(BytesReference source, XContentType xContentType) {
        this.source = Objects.requireNonNull(source);
        this.contentType = Objects.requireNonNull(xContentType);
        return this;
    }

    /**
     * Sets the document to index in bytes form.
     */
    public DeleteRequest source(byte[] source, XContentType xContentType) {
        return source(source, 0, source.length, xContentType);
    }

    /**
     * Sets the document to index in bytes form (assumed to be safe to be used from different
     * threads).
     *
     * @param source The source to index
     * @param offset The offset in the byte array
     * @param length The length of the data
     */
    public DeleteRequest source(byte[] source, int offset, int length, XContentType xContentType) {
        return source(new BytesArray(source, offset, length), xContentType);
    }

    /**
     * The content type. This will be used when generating a document from user provided objects like Maps and when parsing the
     * source at index time
     */
    public XContentType getContentType() {
        return contentType;
    }

    /**
     * Sets the ingest pipeline to be executed before deleteing the document
     */
    public DeleteRequest setPipeline(String pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Returns the ingest pipeline to be executed before deleteing the document
     */
    public String getPipeline() {
        return this.pipeline;
    }

    public IndexRequest getIndexRequest() {
        return indexRequest;
    }

    public void setIndexRequest(IndexRequest indexRequest) {
        this.indexRequest = indexRequest;
    }
}
