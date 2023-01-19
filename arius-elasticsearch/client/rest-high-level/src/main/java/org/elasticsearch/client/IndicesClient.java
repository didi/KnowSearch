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

package org.elasticsearch.client;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.admin.indices.flush.SyncedFlushRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryRequest;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.core.ShardsAcknowledgedResponse;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.client.indices.CloseIndexRequest;
import org.elasticsearch.client.indices.CloseIndexResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.DeleteAliasRequest;
import org.elasticsearch.client.indices.FreezeIndexRequest;
import org.elasticsearch.client.indices.GetFieldMappingsRequest;
import org.elasticsearch.client.indices.GetFieldMappingsResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.client.indices.GetIndexTemplatesRequest;
import org.elasticsearch.client.indices.GetIndexTemplatesResponse;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.IndexTemplatesExistRequest;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.client.indices.ReloadAnalyzersRequest;
import org.elasticsearch.client.indices.ReloadAnalyzersResponse;
import org.elasticsearch.client.indices.ResizeRequest;
import org.elasticsearch.client.indices.ResizeResponse;
import org.elasticsearch.client.indices.UnfreezeIndexRequest;
import org.elasticsearch.client.indices.rollover.RolloverRequest;
import org.elasticsearch.client.indices.rollover.RolloverResponse;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.Collections;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 * A wrapper for the {@link RestHighLevelClient} that provides methods for accessing the Indices API.
 * <p>
 * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices.html">Indices API on elastic.co</a>
 */
public final class IndicesClient {
    private final RestHighLevelClient restHighLevelClient;

    IndicesClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * Deletes an index using the Delete Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-delete-index.html">
     * Delete Index API on elastic.co</a>
     * @param deleteIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public AcknowledgedResponse delete(DeleteIndexRequest deleteIndexRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(deleteIndexRequest, IndicesRequestConverters::deleteIndex, options,
            AcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously deletes an index using the Delete Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-delete-index.html">
     * Delete Index API on elastic.co</a>
     * @param deleteIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable deleteAsync(DeleteIndexRequest deleteIndexRequest, RequestOptions options,
                                   ActionListener<AcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(deleteIndexRequest,
            IndicesRequestConverters::deleteIndex, options,
            AcknowledgedResponse::fromXContent, listener, emptySet());
    }

    /**
     * Creates an index using the Create Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-create-index.html">
     * Create Index API on elastic.co</a>
     * @param createIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public CreateIndexResponse create(CreateIndexRequest createIndexRequest,
                                      RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(createIndexRequest, IndicesRequestConverters::createIndex, options,
            CreateIndexResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously creates an index using the Create Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-create-index.html">
     * Create Index API on elastic.co</a>
     * @param createIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable createAsync(CreateIndexRequest createIndexRequest,
                                   RequestOptions options,
                                   ActionListener<CreateIndexResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(createIndexRequest, IndicesRequestConverters::createIndex, options,
            CreateIndexResponse::fromXContent, listener, emptySet());
    }

    /**
     * Creates an index using the Create Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-create-index.html">
     * Create Index API on elastic.co</a>
     * @param createIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     *
     * @deprecated This method uses an old request object which still refers to types, a deprecated feature. The
     * method {@link #create(CreateIndexRequest, RequestOptions)} should be used instead, which accepts a new
     * request object.
     */
    @Deprecated
    public org.elasticsearch.action.admin.indices.create.CreateIndexResponse create(
            org.elasticsearch.action.admin.indices.create.CreateIndexRequest createIndexRequest,
            RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(createIndexRequest,
            IndicesRequestConverters::createIndex, options,
            org.elasticsearch.action.admin.indices.create.CreateIndexResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously creates an index using the Create Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-create-index.html">
     * Create Index API on elastic.co</a>
     * @param createIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     *
     * @deprecated This method uses an old request object which still refers to types, a deprecated feature. The
     * method {@link #createAsync(CreateIndexRequest, RequestOptions, ActionListener)} should be used instead,
     * which accepts a new request object.
     * @return cancellable that may be used to cancel the request
     */
    @Deprecated
    public Cancellable createAsync(org.elasticsearch.action.admin.indices.create.CreateIndexRequest createIndexRequest,
                                   RequestOptions options,
                                   ActionListener<org.elasticsearch.action.admin.indices.create.CreateIndexResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(createIndexRequest,
            IndicesRequestConverters::createIndex, options,
            org.elasticsearch.action.admin.indices.create.CreateIndexResponse::fromXContent, listener, emptySet());
    }

    /**
     * Updates the mappings on an index using the Put Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html">
     * Put Mapping API on elastic.co</a>
     * @param putMappingRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public AcknowledgedResponse putMapping(PutMappingRequest putMappingRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(putMappingRequest, IndicesRequestConverters::putMapping, options,
            AcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously updates the mappings on an index using the Put Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html">
     * Put Mapping API on elastic.co</a>
     * @param putMappingRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable putMappingAsync(PutMappingRequest putMappingRequest, RequestOptions options,
                                       ActionListener<AcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(putMappingRequest, IndicesRequestConverters::putMapping, options,
            AcknowledgedResponse::fromXContent, listener, emptySet());
    }

    /**
     * Updates the mappings on an index using the Put Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html">
     * Put Mapping API on elastic.co</a>
     * @param putMappingRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     *
     * @deprecated This method uses an old request object which still refers to types, a deprecated feature. The method
     * {@link #putMapping(PutMappingRequest, RequestOptions)} should be used instead, which accepts a new request object.
     */
    @Deprecated
    public AcknowledgedResponse putMapping(org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest putMappingRequest,
                                           RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(putMappingRequest, IndicesRequestConverters::putMapping, options,
            AcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously updates the mappings on an index using the Put Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html">
     * Put Mapping API on elastic.co</a>
     * @param putMappingRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     *
     * @deprecated This method uses an old request object which still refers to types, a deprecated feature. The
     * method {@link #putMappingAsync(PutMappingRequest, RequestOptions, ActionListener)} should be used instead,
     * which accepts a new request object.
     * @return cancellable that may be used to cancel the request
     */
    @Deprecated
    public Cancellable putMappingAsync(org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest putMappingRequest,
                                       RequestOptions options,
                                       ActionListener<AcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(putMappingRequest, IndicesRequestConverters::putMapping, options,
            AcknowledgedResponse::fromXContent, listener, emptySet());
    }

    /**
     * Retrieves the mappings on an index or indices using the Get Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-mapping.html">
     * Get Mapping API on elastic.co</a>
     * @param getMappingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public GetMappingsResponse getMapping(GetMappingsRequest getMappingsRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(getMappingsRequest,
            IndicesRequestConverters::getMappings,
            options,
            GetMappingsResponse::fromXContent,
            emptySet());
    }

    /**
     * Asynchronously retrieves the mappings on an index on indices using the Get Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-mapping.html">
     * Get Mapping API on elastic.co</a>
     * @param getMappingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable getMappingAsync(GetMappingsRequest getMappingsRequest, RequestOptions options,
                                       ActionListener<GetMappingsResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(getMappingsRequest,
            IndicesRequestConverters::getMappings,
            options,
            GetMappingsResponse::fromXContent,
            listener,
            emptySet());
    }

    /**
     * Retrieves the mappings on an index or indices using the Get Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-mapping.html">
     * Get Mapping API on elastic.co</a>
     * @param getMappingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     *
     * @deprecated This method uses old request and response objects which still refer to types, a deprecated
     * feature. The method {@link #getMapping(GetMappingsRequest, RequestOptions)} should be used instead, which
     * accepts a new request object.
     */
    @Deprecated
    public org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse getMapping(
            org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest getMappingsRequest,
            RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(getMappingsRequest,
            IndicesRequestConverters::getMappings,
            options,
            org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse::fromXContent,
            emptySet());
    }

    /**
     * Asynchronously retrieves the mappings on an index on indices using the Get Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-mapping.html">
     * Get Mapping API on elastic.co</a>
     * @param getMappingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     *
     * @deprecated This method uses old request and response objects which still refer to types, a deprecated feature.
     * The method {@link #getMapping(GetMappingsRequest, RequestOptions)} should be used instead, which accepts a new
     * request object.
     * @return cancellable that may be used to cancel the request
     */
    @Deprecated
    public Cancellable getMappingAsync(org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest getMappingsRequest,
                                       RequestOptions options,
                                       ActionListener<org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(getMappingsRequest,
            IndicesRequestConverters::getMappings,
            options,
            org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse::fromXContent,
            listener,
            emptySet());
    }

    /**
     * Retrieves the field mappings on an index or indices using the Get Field Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-field-mapping.html">
     * Get Field Mapping API on elastic.co</a>
     * @param getFieldMappingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     *
     * @deprecated This method uses old request and response objects which still refer to types, a deprecated feature.
     * The method {@link #getFieldMapping(GetFieldMappingsRequest, RequestOptions)} should be used instead, which
     * accepts a new request object.
     */
    @Deprecated
    public org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse getFieldMapping(
            org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest getFieldMappingsRequest,
            RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(getFieldMappingsRequest, IndicesRequestConverters::getFieldMapping,
            options, org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously retrieves the field mappings on an index on indices using the Get Field Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-field-mapping.html">
     * Get Field Mapping API on elastic.co</a>
     * @param getFieldMappingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     *
     * @deprecated This method uses old request and response objects which still refer to types, a deprecated feature.
     * The method {@link #getFieldMappingAsync(GetFieldMappingsRequest, RequestOptions, ActionListener)} should be
     * used instead, which accepts a new request object.
     * @return cancellable that may be used to cancel the request
     */
    @Deprecated
    public Cancellable getFieldMappingAsync(
        org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest getFieldMappingsRequest,
        RequestOptions options,
        ActionListener<org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(getFieldMappingsRequest,
            IndicesRequestConverters::getFieldMapping, options,
            org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse::fromXContent,
            listener, emptySet());
    }

    /**
     * Retrieves the field mappings on an index or indices using the Get Field Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-field-mapping.html">
     * Get Field Mapping API on elastic.co</a>
     * @param getFieldMappingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public GetFieldMappingsResponse getFieldMapping(GetFieldMappingsRequest getFieldMappingsRequest,
            RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(getFieldMappingsRequest, IndicesRequestConverters::getFieldMapping,
            options, GetFieldMappingsResponse::fromXContent, emptySet()
        );
    }

    /**
     * Asynchronously retrieves the field mappings on an index or indices using the Get Field Mapping API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-field-mapping.html">
     * Get Field Mapping API on elastic.co</a>
     * @param getFieldMappingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable getFieldMappingAsync(GetFieldMappingsRequest getFieldMappingsRequest,
                                            RequestOptions options, ActionListener<GetFieldMappingsResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(
            getFieldMappingsRequest, IndicesRequestConverters::getFieldMapping, options,
            GetFieldMappingsResponse::fromXContent, listener, emptySet());
    }

    /**
     * Updates aliases using the Index Aliases API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html">
     * Index Aliases API on elastic.co</a>
     * @param indicesAliasesRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public AcknowledgedResponse updateAliases(IndicesAliasesRequest indicesAliasesRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(indicesAliasesRequest, IndicesRequestConverters::updateAliases, options,
            AcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously updates aliases using the Index Aliases API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html">
     * Index Aliases API on elastic.co</a>
     * @param indicesAliasesRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable updateAliasesAsync(IndicesAliasesRequest indicesAliasesRequest, RequestOptions options,
                                          ActionListener<AcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(indicesAliasesRequest,
            IndicesRequestConverters::updateAliases, options,
            AcknowledgedResponse::fromXContent, listener, emptySet());
    }

    /**
     * Opens an index using the Open Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-open-close.html">
     * Open Index API on elastic.co</a>
     * @param openIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public OpenIndexResponse open(OpenIndexRequest openIndexRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(openIndexRequest, IndicesRequestConverters::openIndex, options,
                OpenIndexResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously opens an index using the Open Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-open-close.html">
     * Open Index API on elastic.co</a>
     * @param openIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable openAsync(OpenIndexRequest openIndexRequest, RequestOptions options, ActionListener<OpenIndexResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(openIndexRequest, IndicesRequestConverters::openIndex, options,
                OpenIndexResponse::fromXContent, listener, emptySet());
    }

    /**
     * Closes an index using the Close Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-open-close.html">
     * Close Index API on elastic.co</a>
     * @param closeIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public CloseIndexResponse close(CloseIndexRequest closeIndexRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(closeIndexRequest, IndicesRequestConverters::closeIndex, options,
            CloseIndexResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously closes an index using the Close Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-open-close.html">
     * Close Index API on elastic.co</a>
     * @param closeIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable closeAsync(CloseIndexRequest closeIndexRequest, RequestOptions options,
                                  ActionListener<CloseIndexResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(closeIndexRequest,
            IndicesRequestConverters::closeIndex, options,
            CloseIndexResponse::fromXContent, listener, emptySet());
    }


    /**
     * Checks if one or more aliases exist using the Aliases Exist API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html">
     * Indices Aliases API on elastic.co</a>
     * @param getAliasesRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request
     */
    public boolean existsAlias(GetAliasesRequest getAliasesRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequest(getAliasesRequest, IndicesRequestConverters::existsAlias, options,
                RestHighLevelClient::convertExistsResponse, emptySet());
    }

    /**
     * Asynchronously checks if one or more aliases exist using the Aliases Exist API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html">
     * Indices Aliases API on elastic.co</a>
     * @param getAliasesRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable existsAliasAsync(GetAliasesRequest getAliasesRequest, RequestOptions options, ActionListener<Boolean> listener) {
        return restHighLevelClient.performRequestAsync(getAliasesRequest, IndicesRequestConverters::existsAlias, options,
                RestHighLevelClient::convertExistsResponse, listener, emptySet());
    }

    /**
     * Refresh one or more indices using the Refresh API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-refresh.html"> Refresh API on elastic.co</a>
     * @param refreshRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public RefreshResponse refresh(RefreshRequest refreshRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(refreshRequest, IndicesRequestConverters::refresh, options,
                RefreshResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously refresh one or more indices using the Refresh API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-refresh.html"> Refresh API on elastic.co</a>
     * @param refreshRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable refreshAsync(RefreshRequest refreshRequest, RequestOptions options, ActionListener<RefreshResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(refreshRequest, IndicesRequestConverters::refresh, options,
                RefreshResponse::fromXContent, listener, emptySet());
    }

    /**
     * Flush one or more indices using the Flush API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-flush.html"> Flush API on elastic.co</a>
     * @param flushRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public FlushResponse flush(FlushRequest flushRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(flushRequest, IndicesRequestConverters::flush, options,
                FlushResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously flush one or more indices using the Flush API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-flush.html"> Flush API on elastic.co</a>
     * @param flushRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable flushAsync(FlushRequest flushRequest, RequestOptions options, ActionListener<FlushResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(flushRequest, IndicesRequestConverters::flush, options,
                FlushResponse::fromXContent, listener, emptySet());
    }

    /**
     * Initiate a synced flush manually using the synced flush API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/master/indices-synced-flush-api.html">
     *     Synced flush API on elastic.co</a>
     * @param syncedFlushRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     * @deprecated synced flush is deprecated and will be removed in 8.0.
     * Use {@link #flush(FlushRequest, RequestOptions)} instead.
     */
    @Deprecated
    public SyncedFlushResponse flushSynced(SyncedFlushRequest syncedFlushRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(syncedFlushRequest, IndicesRequestConverters::flushSynced, options,
                SyncedFlushResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously initiate a synced flush manually using the synced flush API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/master/indices-synced-flush-api.html">
     *     Synced flush API on elastic.co</a>
     * @param syncedFlushRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     * @deprecated synced flush is deprecated and will be removed in 8.0.
     * Use {@link #flushAsync(FlushRequest, RequestOptions, ActionListener)} instead.
     */
    @Deprecated
    public Cancellable flushSyncedAsync(SyncedFlushRequest syncedFlushRequest, RequestOptions options,
                                        ActionListener<SyncedFlushResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(syncedFlushRequest, IndicesRequestConverters::flushSynced, options,
                SyncedFlushResponse::fromXContent, listener, emptySet());
    }

    /**
     * Retrieve the settings of one or more indices.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-settings.html">
     * Indices Get Settings API on elastic.co</a>
     * @param getSettingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public GetSettingsResponse getSettings(GetSettingsRequest getSettingsRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(getSettingsRequest, IndicesRequestConverters::getSettings, options,
            GetSettingsResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously retrieve the settings of one or more indices.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-settings.html">
     * Indices Get Settings API on elastic.co</a>
     * @param getSettingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable getSettingsAsync(GetSettingsRequest getSettingsRequest, RequestOptions options,
                                        ActionListener<GetSettingsResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(getSettingsRequest, IndicesRequestConverters::getSettings, options,
            GetSettingsResponse::fromXContent, listener, emptySet());
    }

    /**
     * Retrieve information about one or more indexes
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-index.html">
     * Indices Get Index API on elastic.co</a>
     * @param getIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public GetIndexResponse get(GetIndexRequest getIndexRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(getIndexRequest, IndicesRequestConverters::getIndex, options,
            GetIndexResponse::fromXContent, emptySet());
    }

    /**
     * Retrieve information about one or more indexes
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-index.html">
     * Indices Get Index API on elastic.co</a>
     * @param getIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable getAsync(GetIndexRequest getIndexRequest, RequestOptions options,
                                ActionListener<GetIndexResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(getIndexRequest, IndicesRequestConverters::getIndex, options,
            GetIndexResponse::fromXContent, listener, emptySet());
    }

    /**
     * Retrieve information about one or more indexes
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-index.html">
     * Indices Get Index API on elastic.co</a>
     * @param getIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     * @deprecated This method uses an old request object which still refers to types, a deprecated feature. The method
     * {@link #get(GetIndexRequest, RequestOptions)} should be used instead, which accepts a new request object.
     */
    @Deprecated
    public org.elasticsearch.action.admin.indices.get.GetIndexResponse get(
            org.elasticsearch.action.admin.indices.get.GetIndexRequest getIndexRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(getIndexRequest, IndicesRequestConverters::getIndex, options,
                org.elasticsearch.action.admin.indices.get.GetIndexResponse::fromXContent, emptySet());
    }

    /**
     * Retrieve information about one or more indexes
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-index.html">
     * Indices Get Index API on elastic.co</a>
     * @param getIndexRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @deprecated This method uses an old request object which still refers to types, a deprecated feature. The method
     * {@link #getAsync(GetIndexRequest, RequestOptions, ActionListener)} should be used instead, which accepts a new request object.
     * @return cancellable that may be used to cancel the request
     */
    @Deprecated
    public Cancellable getAsync(org.elasticsearch.action.admin.indices.get.GetIndexRequest getIndexRequest, RequestOptions options,
                                ActionListener<org.elasticsearch.action.admin.indices.get.GetIndexResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(getIndexRequest, IndicesRequestConverters::getIndex, options,
                org.elasticsearch.action.admin.indices.get.GetIndexResponse::fromXContent, listener, emptySet());
    }

    /**
     * Force merge one or more indices using the Force Merge API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-forcemerge.html">
     * Force Merge API on elastic.co</a>
     * @param forceMergeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     * @deprecated use {@link #forcemerge(ForceMergeRequest, RequestOptions)} instead
     */
    @Deprecated
    public ForceMergeResponse forceMerge(ForceMergeRequest forceMergeRequest, RequestOptions options) throws IOException {
        return forcemerge(forceMergeRequest, options);
    }

    /**
     * Force merge one or more indices using the Force Merge API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-forcemerge.html">
     * Force Merge API on elastic.co</a>
     * @param forceMergeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public ForceMergeResponse forcemerge(ForceMergeRequest forceMergeRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(forceMergeRequest, IndicesRequestConverters::forceMerge, options,
                ForceMergeResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously force merge one or more indices using the Force Merge API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-forcemerge.html">
     * Force Merge API on elastic.co</a>
     * @param forceMergeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @deprecated use {@link #forcemergeAsync(ForceMergeRequest, RequestOptions, ActionListener)} instead
     * @return cancellable that may be used to cancel the request
     */
    @Deprecated
    public Cancellable forceMergeAsync(ForceMergeRequest forceMergeRequest, RequestOptions options,
                                       ActionListener<ForceMergeResponse> listener) {
        return forcemergeAsync(forceMergeRequest, options, listener);
    }

    /**
     * Asynchronously force merge one or more indices using the Force Merge API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-forcemerge.html">
     * Force Merge API on elastic.co</a>
     * @param forceMergeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable forcemergeAsync(ForceMergeRequest forceMergeRequest, RequestOptions options,
                                       ActionListener<ForceMergeResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(forceMergeRequest,
                IndicesRequestConverters::forceMerge, options,
                ForceMergeResponse::fromXContent, listener, emptySet());
    }

    /**
     * Clears the cache of one or more indices using the Clear Cache API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-clearcache.html">
     * Clear Cache API on elastic.co</a>
     * @param clearIndicesCacheRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public ClearIndicesCacheResponse clearCache(ClearIndicesCacheRequest clearIndicesCacheRequest,
                                                RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(clearIndicesCacheRequest, IndicesRequestConverters::clearCache, options,
                ClearIndicesCacheResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously clears the cache of one or more indices using the Clear Cache API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-clearcache.html">
     * Clear Cache API on elastic.co</a>
     * @param clearIndicesCacheRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable clearCacheAsync(ClearIndicesCacheRequest clearIndicesCacheRequest, RequestOptions options,
                                       ActionListener<ClearIndicesCacheResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(clearIndicesCacheRequest,
                IndicesRequestConverters::clearCache, options,
                ClearIndicesCacheResponse::fromXContent, listener, emptySet());
    }

    /**
     * Checks if the index (indices) exists or not.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-exists.html">
     * Indices Exists API on elastic.co</a>
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request
     */
    public boolean exists(GetIndexRequest request, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequest(
            request,
            IndicesRequestConverters::indicesExist,
            options,
            RestHighLevelClient::convertExistsResponse,
            Collections.emptySet()
        );
    }

    /**
     * Asynchronously checks if the index (indices) exists or not.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-exists.html">
     * Indices Exists API on elastic.co</a>
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable existsAsync(GetIndexRequest request, RequestOptions options, ActionListener<Boolean> listener) {
        return restHighLevelClient.performRequestAsync(
                request,
                IndicesRequestConverters::indicesExist,
                options,
                RestHighLevelClient::convertExistsResponse,
                listener,
                Collections.emptySet()
        );
    }

    /**
     * Checks if the index (indices) exists or not.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-exists.html">
     * Indices Exists API on elastic.co</a>
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request
     * @deprecated This method uses an old request object which still refers to types, a deprecated feature. The method
     * {@link #exists(GetIndexRequest, RequestOptions)} should be used instead, which accepts a new request object.
     */
    @Deprecated
    public boolean exists(org.elasticsearch.action.admin.indices.get.GetIndexRequest request, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequest(
            request,
            IndicesRequestConverters::indicesExist,
            options,
            RestHighLevelClient::convertExistsResponse,
            Collections.emptySet()
        );
    }

    /**
     * Asynchronously checks if the index (indices) exists or not.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-exists.html">
     * Indices Exists API on elastic.co</a>
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @deprecated This method uses an old request object which still refers to types, a deprecated feature. The method
     * {@link #existsAsync(GetIndexRequest, RequestOptions, ActionListener)} should be used instead, which accepts a new request object.
     * @return cancellable that may be used to cancel the request
     */
    @Deprecated
    public Cancellable existsAsync(org.elasticsearch.action.admin.indices.get.GetIndexRequest request, RequestOptions options,
                                   ActionListener<Boolean> listener) {
        return restHighLevelClient.performRequestAsync(
                request,
                IndicesRequestConverters::indicesExist,
                options,
                RestHighLevelClient::convertExistsResponse,
                listener,
                Collections.emptySet()
        );
    }

    /**
     * Shrinks an index using the Shrink Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-shrink-index.html">
     * Shrink Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public ResizeResponse shrink(ResizeRequest resizeRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(resizeRequest, IndicesRequestConverters::shrink, options,
                ResizeResponse::fromXContent, emptySet());
    }

    /**
     * Shrinks an index using the Shrink Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-shrink-index.html">
     * Shrink Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     * @deprecated use {@link #shrink(ResizeRequest, RequestOptions)}
     */
    @Deprecated
    public org.elasticsearch.action.admin.indices.shrink.ResizeResponse shrink(
        org.elasticsearch.action.admin.indices.shrink.ResizeRequest resizeRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(resizeRequest, IndicesRequestConverters::shrink, options,
            org.elasticsearch.action.admin.indices.shrink.ResizeResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously shrinks an index using the Shrink index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-shrink-index.html">
     * Shrink Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable shrinkAsync(ResizeRequest resizeRequest, RequestOptions options, ActionListener<ResizeResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(resizeRequest, IndicesRequestConverters::shrink, options,
                ResizeResponse::fromXContent, listener, emptySet());
    }

    /**
     * Asynchronously shrinks an index using the Shrink index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-shrink-index.html">
     * Shrink Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     * @deprecated use {@link #shrinkAsync(ResizeRequest, RequestOptions, ActionListener)}
     */
    @Deprecated
    public Cancellable shrinkAsync(org.elasticsearch.action.admin.indices.shrink.ResizeRequest resizeRequest, RequestOptions options,
                                   ActionListener<org.elasticsearch.action.admin.indices.shrink.ResizeResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(resizeRequest, IndicesRequestConverters::shrink, options,
            org.elasticsearch.action.admin.indices.shrink.ResizeResponse::fromXContent, listener, emptySet());
    }

    /**
     * Splits an index using the Split Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-split-index.html">
     * Split Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public ResizeResponse split(ResizeRequest resizeRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(resizeRequest, IndicesRequestConverters::split, options,
                ResizeResponse::fromXContent, emptySet());
    }

    /**
     * Splits an index using the Split Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-split-index.html">
     * Split Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     * @deprecated use {@link #split(ResizeRequest, RequestOptions)}
     */
    @Deprecated
    public org.elasticsearch.action.admin.indices.shrink.ResizeResponse split(
        org.elasticsearch.action.admin.indices.shrink.ResizeRequest resizeRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(resizeRequest, IndicesRequestConverters::split, options,
            org.elasticsearch.action.admin.indices.shrink.ResizeResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously splits an index using the Split Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-split-index.html">
     * Split Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable splitAsync(ResizeRequest resizeRequest, RequestOptions options, ActionListener<ResizeResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(resizeRequest, IndicesRequestConverters::split, options,
                ResizeResponse::fromXContent, listener, emptySet());
    }

    /**
     * Asynchronously splits an index using the Split Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-split-index.html">
     * Split Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     * @deprecated use {@link #splitAsync(ResizeRequest, RequestOptions, ActionListener)}
     */
    @Deprecated
    public Cancellable splitAsync(org.elasticsearch.action.admin.indices.shrink.ResizeRequest resizeRequest, RequestOptions options,
                                  ActionListener<org.elasticsearch.action.admin.indices.shrink.ResizeResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(resizeRequest, IndicesRequestConverters::split, options,
            org.elasticsearch.action.admin.indices.shrink.ResizeResponse::fromXContent, listener, emptySet());
    }

    /**
     * Clones an index using the Clone Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-clone-index.html">
     * Clone Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public ResizeResponse clone(ResizeRequest resizeRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(resizeRequest, IndicesRequestConverters::clone, options,
            ResizeResponse::fromXContent, emptySet());
    }

    /**
     * Clones an index using the Clone Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-clone-index.html">
     * Clone Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     * @deprecated use {@link #clone(ResizeRequest, RequestOptions)}
     */
    @Deprecated
    public org.elasticsearch.action.admin.indices.shrink.ResizeResponse clone(
        org.elasticsearch.action.admin.indices.shrink.ResizeRequest resizeRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(resizeRequest, IndicesRequestConverters::clone, options,
            org.elasticsearch.action.admin.indices.shrink.ResizeResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously clones an index using the Clone Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-clone-index.html">
     * Clone Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable cloneAsync(ResizeRequest resizeRequest, RequestOptions options, ActionListener<ResizeResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(resizeRequest, IndicesRequestConverters::clone, options,
            ResizeResponse::fromXContent, listener, emptySet());
    }

    /**
     * Asynchronously clones an index using the Clone Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-clone-index.html">
     * Clone Index API on elastic.co</a>
     * @param resizeRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     * @deprecated use {@link #cloneAsync(ResizeRequest, RequestOptions, ActionListener)}
     */
    @Deprecated
    public Cancellable cloneAsync(org.elasticsearch.action.admin.indices.shrink.ResizeRequest resizeRequest, RequestOptions options,
                                  ActionListener<org.elasticsearch.action.admin.indices.shrink.ResizeResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(resizeRequest, IndicesRequestConverters::clone, options,
            org.elasticsearch.action.admin.indices.shrink.ResizeResponse::fromXContent, listener, emptySet());
    }

    /**
     * Rolls over an index using the Rollover Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-rollover-index.html">
     * Rollover Index API on elastic.co</a>
     * @param rolloverRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public RolloverResponse rollover(RolloverRequest rolloverRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(rolloverRequest, IndicesRequestConverters::rollover, options,
                RolloverResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously rolls over an index using the Rollover Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-rollover-index.html">
     * Rollover Index API on elastic.co</a>
     * @param rolloverRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable rolloverAsync(RolloverRequest rolloverRequest, RequestOptions options, ActionListener<RolloverResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(rolloverRequest, IndicesRequestConverters::rollover, options,
                RolloverResponse::fromXContent, listener, emptySet());
    }


    /**
     * Rolls over an index using the Rollover Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-rollover-index.html">
     * Rollover Index API on elastic.co</a>
     * @param rolloverRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     *
     * @deprecated This method uses deprecated request and response objects.
     * The method {@link #rollover(RolloverRequest, RequestOptions)} should be used instead, which accepts a new request object.
     */
    @Deprecated
    public org.elasticsearch.action.admin.indices.rollover.RolloverResponse rollover(
            org.elasticsearch.action.admin.indices.rollover.RolloverRequest rolloverRequest,
            RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(rolloverRequest, IndicesRequestConverters::rollover, options,
            org.elasticsearch.action.admin.indices.rollover.RolloverResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously rolls over an index using the Rollover Index API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-rollover-index.html">
     * Rollover Index API on elastic.co</a>
     * @param rolloverRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     *
     * @deprecated This method uses deprecated request and response objects.
     * The method {@link #rolloverAsync(RolloverRequest, RequestOptions, ActionListener)} should be used instead, which
     * accepts a new request object.
     * @return cancellable that may be used to cancel the request
     */
    @Deprecated
    public Cancellable rolloverAsync(org.elasticsearch.action.admin.indices.rollover.RolloverRequest rolloverRequest,
            RequestOptions options,
            ActionListener<org.elasticsearch.action.admin.indices.rollover.RolloverResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(rolloverRequest, IndicesRequestConverters::rollover, options,
            org.elasticsearch.action.admin.indices.rollover.RolloverResponse::fromXContent, listener, emptySet());
    }

    /**
     * Gets one or more aliases using the Get Index Aliases API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html"> Indices Aliases API on
     * elastic.co</a>
     * @param getAliasesRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public GetAliasesResponse getAlias(GetAliasesRequest getAliasesRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(getAliasesRequest, IndicesRequestConverters::getAlias, options,
                GetAliasesResponse::fromXContent, singleton(RestStatus.NOT_FOUND.getStatus()));
    }

    /**
     * Asynchronously gets one or more aliases using the Get Index Aliases API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html"> Indices Aliases API on
     * elastic.co</a>
     * @param getAliasesRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable getAliasAsync(GetAliasesRequest getAliasesRequest, RequestOptions options,
                                     ActionListener<GetAliasesResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(getAliasesRequest,
            IndicesRequestConverters::getAlias, options,
            GetAliasesResponse::fromXContent, listener, singleton(RestStatus.NOT_FOUND.getStatus()));
    }

    /**
     * Updates specific index level settings using the Update Indices Settings API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-update-settings.html"> Update Indices Settings
     * API on elastic.co</a>
     * @param updateSettingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public AcknowledgedResponse putSettings(UpdateSettingsRequest updateSettingsRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(updateSettingsRequest, IndicesRequestConverters::indexPutSettings, options,
            AcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously updates specific index level settings using the Update Indices Settings API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-update-settings.html"> Update Indices Settings
     * API on elastic.co</a>
     * @param updateSettingsRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable putSettingsAsync(UpdateSettingsRequest updateSettingsRequest, RequestOptions options,
                                        ActionListener<AcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(updateSettingsRequest,
            IndicesRequestConverters::indexPutSettings, options,
            AcknowledgedResponse::fromXContent, listener, emptySet());
    }


    /**
     * Puts an index template using the Index Templates API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html"> Index Templates API
     * on elastic.co</a>
     * @param putIndexTemplateRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     * @deprecated This old form of request allows types in mappings. Use {@link #putTemplate(PutIndexTemplateRequest, RequestOptions)}
     * instead which introduces a new request object without types.
     */
    @Deprecated
    public AcknowledgedResponse putTemplate(
            org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest putIndexTemplateRequest,
            RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(putIndexTemplateRequest, IndicesRequestConverters::putTemplate, options,
            AcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously puts an index template using the Index Templates API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html"> Index Templates API
     * on elastic.co</a>
     * @param putIndexTemplateRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @deprecated This old form of request allows types in mappings.
     * Use {@link #putTemplateAsync(PutIndexTemplateRequest, RequestOptions, ActionListener)}
     * instead which introduces a new request object without types.
     * @return cancellable that may be used to cancel the request
     */
    @Deprecated
    public Cancellable putTemplateAsync(
                org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest putIndexTemplateRequest,
                RequestOptions options, ActionListener<AcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(putIndexTemplateRequest,
            IndicesRequestConverters::putTemplate, options,
            AcknowledgedResponse::fromXContent, listener, emptySet());
    }


    /**
     * Puts an index template using the Index Templates API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html"> Index Templates API
     * on elastic.co</a>
     * @param putIndexTemplateRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public AcknowledgedResponse putTemplate(
            PutIndexTemplateRequest putIndexTemplateRequest,
            RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(putIndexTemplateRequest, IndicesRequestConverters::putTemplate, options,
            AcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously puts an index template using the Index Templates API.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html"> Index Templates API
     * on elastic.co</a>
     * @param putIndexTemplateRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable putTemplateAsync(PutIndexTemplateRequest putIndexTemplateRequest,
                                        RequestOptions options, ActionListener<AcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(putIndexTemplateRequest,
            IndicesRequestConverters::putTemplate, options,
            AcknowledgedResponse::fromXContent, listener, emptySet());
    }

    /**
     * Validate a potentially expensive query without executing it.
     * <p>
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-validate.html"> Validate Query API
     * on elastic.co</a>
     * @param validateQueryRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public ValidateQueryResponse validateQuery(ValidateQueryRequest validateQueryRequest,
                                               RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(validateQueryRequest,
            IndicesRequestConverters::validateQuery, options,
            ValidateQueryResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously validate a potentially expensive query without executing it.
     * <p>
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-validate.html"> Validate Query API
     * on elastic.co</a>
     * @param validateQueryRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable validateQueryAsync(ValidateQueryRequest validateQueryRequest, RequestOptions options,
                                          ActionListener<ValidateQueryResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(validateQueryRequest,
            IndicesRequestConverters::validateQuery, options,
            ValidateQueryResponse::fromXContent, listener, emptySet());
    }

    /**
     * Gets index templates using the Index Templates API. The mappings will be returned in a legacy deprecated format, where the
     * mapping definition is nested under the type name.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html"> Index Templates API
     * on elastic.co</a>
     * @param getIndexTemplatesRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     * @deprecated This method uses an old response object which still refers to types, a deprecated feature. Use
     * {@link #getIndexTemplate(GetIndexTemplatesRequest, RequestOptions)} instead which returns a new response object
     */
    @Deprecated
    public org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse getTemplate(
            GetIndexTemplatesRequest getIndexTemplatesRequest, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(getIndexTemplatesRequest,
            IndicesRequestConverters::getTemplatesWithDocumentTypes,
            options,
            org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse::fromXContent, emptySet());
    }

    /**
     * Gets index templates using the Index Templates API
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html"> Index Templates API
     * on elastic.co</a>
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param getIndexTemplatesRequest the request
     * @return the response
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public GetIndexTemplatesResponse getIndexTemplate(GetIndexTemplatesRequest getIndexTemplatesRequest,
                  RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(getIndexTemplatesRequest,
            IndicesRequestConverters::getTemplates,
            options, GetIndexTemplatesResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously gets index templates using the Index Templates API. The mappings will be returned in a legacy deprecated format,
     * where the mapping definition is nested under the type name.
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html"> Index Templates API
     * on elastic.co</a>
     * @param getIndexTemplatesRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @deprecated This method uses an old response object which still refers to types, a deprecated feature. Use
     * {@link #getIndexTemplateAsync(GetIndexTemplatesRequest, RequestOptions, ActionListener)} instead which returns a new response object
     * @return cancellable that may be used to cancel the request
     */
    @Deprecated
    public Cancellable getTemplateAsync(GetIndexTemplatesRequest getIndexTemplatesRequest, RequestOptions options,
            ActionListener<org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(getIndexTemplatesRequest,
            IndicesRequestConverters::getTemplatesWithDocumentTypes,
            options,
            org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse::fromXContent,
            listener, emptySet());
    }

    /**
     * Asynchronously gets index templates using the Index Templates API
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html"> Index Templates API
     * on elastic.co</a>
     * @param getIndexTemplatesRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable getIndexTemplateAsync(GetIndexTemplatesRequest getIndexTemplatesRequest, RequestOptions options,
                                             ActionListener<GetIndexTemplatesResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(getIndexTemplatesRequest,
            IndicesRequestConverters::getTemplates,
            options, GetIndexTemplatesResponse::fromXContent, listener, emptySet());
    }

    /**
     * Uses the Index Templates API to determine if index templates exist
     *
     * @param indexTemplatesRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @return true if any index templates in the request exist, false otherwise
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public boolean existsTemplate(IndexTemplatesExistRequest indexTemplatesRequest,
                                  RequestOptions options) throws IOException {
        return restHighLevelClient.performRequest(indexTemplatesRequest,
            IndicesRequestConverters::templatesExist, options,
            RestHighLevelClient::convertExistsResponse, emptySet());
    }

    /**
     * Uses the Index Templates API to determine if index templates exist
     * @param indexTemplatesExistRequest the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion. The listener will be called with the value {@code true}
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable existsTemplateAsync(IndexTemplatesExistRequest indexTemplatesExistRequest,
                                           RequestOptions options,
                                           ActionListener<Boolean> listener) {

        return restHighLevelClient.performRequestAsync(indexTemplatesExistRequest,
            IndicesRequestConverters::templatesExist, options,
            RestHighLevelClient::convertExistsResponse, listener, emptySet());
    }

    /**
     * Calls the analyze API
     *
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-analyze.html">Analyze API on elastic.co</a>
     *
     * @param request   the request
     * @param options   the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     */
    public AnalyzeResponse analyze(AnalyzeRequest request, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(request, IndicesRequestConverters::analyze, options,
            AnalyzeResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously calls the analyze API
     *
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-analyze.html">Analyze API on elastic.co</a>
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable analyzeAsync(AnalyzeRequest request, RequestOptions options,
                                    ActionListener<AnalyzeResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(request, IndicesRequestConverters::analyze, options,
            AnalyzeResponse::fromXContent, listener, emptySet());
    }

    /**
     * Synchronously calls the _freeze API
     *
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     */
    public ShardsAcknowledgedResponse freeze(FreezeIndexRequest request, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(request, IndicesRequestConverters::freezeIndex, options,
            ShardsAcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously calls the _freeze API
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable freezeAsync(FreezeIndexRequest request, RequestOptions options,
                                   ActionListener<ShardsAcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(request, IndicesRequestConverters::freezeIndex, options,
            ShardsAcknowledgedResponse::fromXContent, listener, emptySet());
    }

    /**
     * Synchronously calls the _unfreeze API
     *
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     */
    public ShardsAcknowledgedResponse unfreeze(UnfreezeIndexRequest request, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(request, IndicesRequestConverters::unfreezeIndex, options,
            ShardsAcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously calls the _unfreeze API
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable unfreezeAsync(UnfreezeIndexRequest request, RequestOptions options,
                                     ActionListener<ShardsAcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(request,
            IndicesRequestConverters::unfreezeIndex, options,
            ShardsAcknowledgedResponse::fromXContent, listener, emptySet());
    }

    /**
     * Delete an index template using the Index Templates API
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html"> Index Templates API
     * on elastic.co</a>
     *
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public AcknowledgedResponse deleteTemplate(DeleteIndexTemplateRequest request, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(request, IndicesRequestConverters::deleteTemplate,
            options, AcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously delete an index template using the Index Templates API
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html"> Index Templates API
     * on elastic.co</a>
     * @param request  the request
     * @param options  the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable deleteTemplateAsync(DeleteIndexTemplateRequest request, RequestOptions options,
                                           ActionListener<AcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(request, IndicesRequestConverters::deleteTemplate,
            options, AcknowledgedResponse::fromXContent, listener, emptySet());
    }

    /**
     * Synchronously calls the _reload_search_analyzers API
     *
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     */
    public ReloadAnalyzersResponse reloadAnalyzers(ReloadAnalyzersRequest request, RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(request, IndicesRequestConverters::reloadAnalyzers, options,
                ReloadAnalyzersResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously calls the _reload_search_analyzers API
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable reloadAnalyzersAsync(ReloadAnalyzersRequest request, RequestOptions options,
                                            ActionListener<ReloadAnalyzersResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(request, IndicesRequestConverters::reloadAnalyzers, options,
                ReloadAnalyzersResponse::fromXContent, listener, emptySet());
    }

    /**
     * Synchronously calls the delete alias api
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     */
    public org.elasticsearch.client.core.AcknowledgedResponse deleteAlias(DeleteAliasRequest request,
                                                                          RequestOptions options) throws IOException {
        return restHighLevelClient.performRequestAndParseEntity(request, IndicesRequestConverters::deleteAlias, options,
            org.elasticsearch.client.core.AcknowledgedResponse::fromXContent, emptySet());
    }

    /**
     * Asynchronously calls the delete alias api
     * @param request the request
     * @param options the request options (e.g. headers), use {@link RequestOptions#DEFAULT} if nothing needs to be customized
     * @param listener the listener to be notified upon request completion
     * @return cancellable that may be used to cancel the request
     */
    public Cancellable deleteAliasAsync(DeleteAliasRequest request, RequestOptions options,
                                                ActionListener<org.elasticsearch.client.core.AcknowledgedResponse> listener) {
        return restHighLevelClient.performRequestAsyncAndParseEntity(request, IndicesRequestConverters::deleteAlias, options,
            org.elasticsearch.client.core.AcknowledgedResponse::fromXContent, listener, emptySet());
    }
}
