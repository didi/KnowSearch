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

package com.didi.arius.gateway.elasticsearch.client.model;

import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.*;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.*;
import com.didi.arius.gateway.elasticsearch.client.model.admin.ESAdminClient;
import com.didi.arius.gateway.elasticsearch.client.request.batch.ESBatchRequest;
import com.didi.arius.gateway.elasticsearch.client.request.batch.ESBatchRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.cat.ESCatRequest;
import com.didi.arius.gateway.elasticsearch.client.request.cat.ESCatRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.query.query.ESQueryRequest;
import com.didi.arius.gateway.elasticsearch.client.request.query.query.ESQueryRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.query.scroll.ESQueryScrollRequest;
import com.didi.arius.gateway.elasticsearch.client.request.query.scroll.ESQueryScrollRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.query.sql.ESSQLRequest;
import com.didi.arius.gateway.elasticsearch.client.request.query.sql.ESSQLRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.response.batch.ESBatchResponse;
import com.didi.arius.gateway.elasticsearch.client.response.cat.ESCatResponse;
import com.didi.arius.gateway.elasticsearch.client.response.query.query.ESQueryResponse;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.lease.Releasable;

/**
 * A client provides a one stop interface for performing actions/operations against the cluster.
 * <p>
 * All operations performed are asynchronous by nature. Each request/operation has two flavors, the first
 * simply returns an {@link ActionFuture}, while the second accepts an
 * {@link ActionListener}.
 * <p>
 * A client can either be retrieved from a {@link org.elasticsearch.node.Node} started, or connected remotely
 * to one or more nodes using {@link org.elasticsearch.client.transport.TransportClient}.
 *
 * @see org.elasticsearch.node.Node#client()
 * @see org.elasticsearch.client.transport.TransportClient
 */
public interface Client extends ElasticsearchClient, Releasable {

    /**
     * The admin client that can be used to perform administrative operations.
     */
    ESAdminClient admin();


    ActionFuture<ESQueryResponse> query(final ESQueryRequest request);

    void query(final ESQueryRequest request, final ActionListener<ESQueryResponse> listener);

    ESQueryRequestBuilder prepareQuery(String... indices);


    ActionFuture<ESQueryResponse> execSQL(final ESSQLRequest request);

    void execSQL(final ESSQLRequest request, final ActionListener<ESQueryResponse> listener);

    ESSQLRequestBuilder prepareSQL(String sql);


    ActionFuture<ESQueryResponse> queryScroll(final ESQueryScrollRequest request);

    void queryScroll(final ESQueryScrollRequest request, final ActionListener<ESQueryResponse> listener);

    ESQueryScrollRequestBuilder prepareQueryScroll(String scrollId);



    public ActionFuture<ESCatResponse> cat(final ESCatRequest request);

    public void cat(final ESCatRequest request, final ActionListener<ESCatResponse> listener);

    public ESCatRequestBuilder prepareCat();




    ActionFuture<ESBatchResponse> batch(final ESBatchRequest request);

    void batch(final ESBatchRequest request, final ActionListener<ESBatchResponse> listener);

    ESBatchRequestBuilder prepareBatch();

    /**
     * direct request where url and post content, reture direct response.
     * <p>
     * The id is optional, if it is not provided, one will be generated automatically.
     *
     * @param request The direct request
     * @return The result future
     */
    ActionFuture<DirectResponse> direct(DirectRequest request);

    /**
     * direct request where url and post content, reture direct response.
     * <p>
     * The id is optional, if it is not provided, one will be generated automatically.
     *
     * @param request  The direct request
     * @param listener A listener to be notified with a result
     */
    void direct(DirectRequest request, ActionListener<DirectResponse> listener);


    ActionFuture<ESSearchResponse> search(ESSearchRequest request);

    void search(ESSearchRequest request, ActionListener<ESSearchResponse> listener);

    ActionFuture<ESClearScrollResponse> clearScroll(ESClearScrollRequest request);

    void clearScroll(ESClearScrollRequest request, ActionListener<ESClearScrollResponse> listener);

    ActionFuture<ESSearchResponse> searchScroll(final ESSearchScrollRequest request);

    void searchScroll(final ESSearchScrollRequest request, final ActionListener<ESSearchResponse> listener);

    ActionFuture<ESMultiSearchResponse> multiSearch(ESMultiSearchRequest request);

    void multiSearch(ESMultiSearchRequest request, ActionListener<ESMultiSearchResponse> listener);

    ActionFuture<ESGetResponse> get(ESGetRequest request);

    void get(ESGetRequest request, ActionListener<ESGetResponse> listener);

    ActionFuture<ESMultiGetResponse> multiGet(ESMultiGetRequest request);

    void multiGet(ESMultiGetRequest request, ActionListener<ESMultiGetResponse> listener);

    ActionFuture<ESIndexResponse> index(ESIndexRequest request);

    void index(ESIndexRequest request, ActionListener<ESIndexResponse> listener);

    ActionFuture<ESUpdateResponse> update(ESUpdateRequest request);

    void update(ESUpdateRequest request, ActionListener<ESUpdateResponse> listener);

    ActionFuture<ESDeleteResponse> delete(ESDeleteRequest request);

    void delete(ESDeleteRequest request, ActionListener<ESDeleteResponse> listener);

}
