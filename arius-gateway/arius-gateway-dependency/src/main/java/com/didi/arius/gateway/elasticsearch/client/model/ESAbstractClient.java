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

import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectAction;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectRequest;
import com.didi.arius.gateway.elasticsearch.client.gateway.direct.DirectResponse;
import com.didi.arius.gateway.elasticsearch.client.gateway.document.*;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.*;
import com.didi.arius.gateway.elasticsearch.client.request.batch.ESBatchAction;
import com.didi.arius.gateway.elasticsearch.client.request.batch.ESBatchRequest;
import com.didi.arius.gateway.elasticsearch.client.request.batch.ESBatchRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.cat.ESCatAction;
import com.didi.arius.gateway.elasticsearch.client.request.cat.ESCatRequest;
import com.didi.arius.gateway.elasticsearch.client.request.cat.ESCatRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.query.query.ESQueryAction;
import com.didi.arius.gateway.elasticsearch.client.request.query.query.ESQueryRequest;
import com.didi.arius.gateway.elasticsearch.client.request.query.query.ESQueryRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.query.scroll.ESQueryScrollAction;
import com.didi.arius.gateway.elasticsearch.client.request.query.scroll.ESQueryScrollRequest;
import com.didi.arius.gateway.elasticsearch.client.request.query.scroll.ESQueryScrollRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.query.sql.ESSQLAction;
import com.didi.arius.gateway.elasticsearch.client.request.query.sql.ESSQLRequest;
import com.didi.arius.gateway.elasticsearch.client.request.query.sql.ESSQLRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.response.batch.ESBatchResponse;
import com.didi.arius.gateway.elasticsearch.client.response.cat.ESCatResponse;
import com.didi.arius.gateway.elasticsearch.client.response.query.query.ESQueryResponse;
import org.elasticsearch.action.*;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.threadpool.ThreadPool;

public abstract class ESAbstractClient implements Client {

    @Override
    public final ThreadPool threadPool() {
        return null;
    }

    @Override
    public final <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>>
    RequestBuilder prepareExecute(final Action<Request, Response, RequestBuilder> action) {
        return action.newRequestBuilder(this);
    }

    @Override
    public final <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>>
    ActionFuture<Response> execute(Action<Request, Response, RequestBuilder> action, Request request) {
        PlainActionFuture<Response> actionFuture = PlainActionFuture.newFuture();
        execute(action, request, actionFuture);
        return actionFuture;
    }

    /**
     * This is the single execution point of *all* clients.
     * 核心接口
     */
    @Override
    public final <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>>
    void execute(Action<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {
        doExecute(action, request, listener);
    }

    // 子类继承
    protected abstract <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>>
        void doExecute(final Action<Request, Response, RequestBuilder> action, final Request request, ActionListener<Response> listener);


    @Override
    public ActionFuture<ESQueryResponse> query(final ESQueryRequest request) {
        return execute( ESQueryAction.INSTANCE, request);
    }

    @Override
    public void query(final ESQueryRequest request, final ActionListener<ESQueryResponse> listener) {
        execute(ESQueryAction.INSTANCE, request, listener);
    }

    @Override
    public ESQueryRequestBuilder prepareQuery(String... indices) {
        return new ESQueryRequestBuilder(this, ESQueryAction.INSTANCE).setIndices(indices);
    }

    @Override
    public ActionFuture<ESQueryResponse> execSQL(ESSQLRequest request) {
        return execute( ESSQLAction.INSTANCE, request);
    }

    @Override
    public void execSQL(ESSQLRequest request, ActionListener<ESQueryResponse> listener) {
        execute(ESSQLAction.INSTANCE, request, listener);
    }

    @Override
    public ESSQLRequestBuilder prepareSQL(String sql) {
        return new ESSQLRequestBuilder(this, ESSQLAction.INSTANCE).setSQL(sql);
    }

    @Override
    public ActionFuture<ESQueryResponse> queryScroll(final ESQueryScrollRequest request) {
        return execute( ESQueryScrollAction.INSTANCE, request);
    }

    @Override
    public void queryScroll(final ESQueryScrollRequest request, final ActionListener<ESQueryResponse> listener) {
        execute(ESQueryScrollAction.INSTANCE, request, listener);
    }

    @Override
    public ESQueryScrollRequestBuilder prepareQueryScroll(String scrollId) {
        return new ESQueryScrollRequestBuilder(this, ESQueryScrollAction.INSTANCE).setScrollId(scrollId);
    }



    @Override
    public ActionFuture<ESBatchResponse> batch(final ESBatchRequest request) {
        return execute( ESBatchAction.INSTANCE, request);
    }

    @Override
    public void batch(final ESBatchRequest request, final ActionListener<ESBatchResponse> listener) {
        execute(ESBatchAction.INSTANCE, request, listener);
    }

    @Override
    public ESBatchRequestBuilder prepareBatch() {
        return new ESBatchRequestBuilder(this, ESBatchAction.INSTANCE);
    }



    @Override
    public ActionFuture<ESCatResponse> cat(final ESCatRequest request) {
        return execute( ESCatAction.INSTANCE, request);
    }

    @Override
    public void cat(final ESCatRequest request, final ActionListener<ESCatResponse> listener) {
        execute(ESCatAction.INSTANCE, request, listener);
    }

    @Override
    public ESCatRequestBuilder prepareCat() {
        return new ESCatRequestBuilder(this, ESCatAction.INSTANCE);
    }





                        /******** for gateway  ********/
    @Override
    public ActionFuture<DirectResponse> direct(DirectRequest request) {
        return execute( DirectAction.INSTANCE, request);
    }

    @Override
    public void direct(DirectRequest request, ActionListener<DirectResponse> listener) {
        execute(DirectAction.INSTANCE, request, listener);
    }



    @Override
    public ActionFuture<ESSearchResponse> search(ESSearchRequest request) {
        return execute( ESSearchAction.INSTANCE, request);
    }

    @Override
    public void search(ESSearchRequest request, ActionListener<ESSearchResponse> listener) {
        execute(ESSearchAction.INSTANCE, request, listener);
    }



    @Override
    public ActionFuture<ESClearScrollResponse> clearScroll(ESClearScrollRequest request) {
        return execute(ESClearScrollAction.INSTANCE, request);
    }

    @Override
    public void clearScroll(ESClearScrollRequest request, ActionListener<ESClearScrollResponse> listener) {
        execute(ESClearScrollAction.INSTANCE, request, listener);
    }

    @Override
    public ActionFuture<ESSearchResponse> searchScroll(final ESSearchScrollRequest request) {
        return execute(ESSearchScrollAction.INSTANCE, request);
    }

    @Override
    public void searchScroll(final ESSearchScrollRequest request, final ActionListener<ESSearchResponse> listener) {
        execute(ESSearchScrollAction.INSTANCE, request, listener);
    }

    @Override
    public ActionFuture<ESMultiSearchResponse> multiSearch(ESMultiSearchRequest request) {
        return execute(ESMultiSearchAction.INSTANCE, request);
    }

    @Override
    public void multiSearch(ESMultiSearchRequest request, ActionListener<ESMultiSearchResponse> listener) {
        execute(ESMultiSearchAction.INSTANCE, request, listener);
    }

    @Override
    public ActionFuture<ESGetResponse> get(ESGetRequest request) {
        return execute( ESGetAction.INSTANCE, request);
    }

    @Override
    public void get(ESGetRequest request, ActionListener<ESGetResponse> listener) {
        execute(ESGetAction.INSTANCE, request, listener);
    }



    @Override
    public ActionFuture<ESMultiGetResponse> multiGet(ESMultiGetRequest request) {
        return execute(ESMultiGetAction.INSTANCE, request);
    }

    @Override
    public void multiGet(ESMultiGetRequest request, ActionListener<ESMultiGetResponse> listener) {
        execute(ESMultiGetAction.INSTANCE, request, listener);
    }

    @Override
    public ActionFuture<ESIndexResponse> index(ESIndexRequest request) {
        return execute(ESIndexAction.INSTANCE, request);
    }

    @Override
    public void index(ESIndexRequest request, ActionListener<ESIndexResponse> listener){
        execute(ESIndexAction.INSTANCE, request, listener);
    }

    @Override
    public ActionFuture<ESUpdateResponse> update(ESUpdateRequest request) {
        return execute(ESUpdateAction.INSTANCE, request);
    }

    @Override
    public void update(ESUpdateRequest request, ActionListener<ESUpdateResponse> listener){
        execute(ESUpdateAction.INSTANCE, request, listener);
    }

    @Override
    public ActionFuture<ESDeleteResponse> delete(ESDeleteRequest request) {
        return execute(ESDeleteAction.INSTANCE, request);
    }

    @Override
    public void delete(ESDeleteRequest request, ActionListener<ESDeleteResponse> listener){
        execute(ESDeleteAction.INSTANCE, request, listener);
    }

}
