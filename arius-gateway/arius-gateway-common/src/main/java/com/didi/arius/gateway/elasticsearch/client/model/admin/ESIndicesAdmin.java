package com.didi.arius.gateway.elasticsearch.client.model.admin;

import com.didi.arius.gateway.elasticsearch.client.request.index.deleteIndex.ESIndicesDeleteIndexAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.deleteIndex.ESIndicesDeleteIndexRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.deleteIndex.ESIndicesDeleteIndexRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.deletetemplate.ESIndicesDeleteTemplateAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.deletetemplate.ESIndicesDeleteTemplateRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.deletetemplate.ESIndicesDeleteTemplateRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.exists.ESIndicesExistsAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.exists.ESIndicesExistsRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.exists.ESIndicesExistsRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.getalias.ESIndicesGetAliasAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.getalias.ESIndicesGetAliasRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.getalias.ESIndicesGetAliasRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.getindex.ESIndicesGetIndexAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.getindex.ESIndicesGetIndexRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.getindex.ESIndicesGetIndexRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.gettemplate.ESIndicesGetTemplateAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.gettemplate.ESIndicesGetTemplateRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.gettemplate.ESIndicesGetTemplateRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.putalias.ESIndicesPutAliasAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.putalias.ESIndicesPutAliasRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.putalias.ESIndicesPutAliasRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.putindex.ESIndicesPutIndexAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.putindex.ESIndicesPutIndexRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.putindex.ESIndicesPutIndexRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.puttemplate.ESIndicesPutTemplateAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.puttemplate.ESIndicesPutTemplateRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.puttemplate.ESIndicesPutTemplateRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.refreshindex.ESIndicesRefreshIndexAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.refreshindex.ESIndicesRefreshIndexRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.refreshindex.ESIndicesRefreshIndexRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.searchshards.ESIndicesSearchShardsAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.searchshards.ESIndicesSearchShardsRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.searchshards.ESIndicesSearchShardsRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.stats.ESIndicesStatsAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.stats.ESIndicesStatsRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.stats.ESIndicesStatsRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.updatesettings.ESIndicesUpdateSettingsAction;
import com.didi.arius.gateway.elasticsearch.client.request.index.updatesettings.ESIndicesUpdateSettingsRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.updatesettings.ESIndicesUpdateSettingsRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.response.indices.deleteindex.ESIndicesDeleteIndexResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.deletetemplate.ESIndicesDeleteTemplateResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.exists.ESIndicesExistsResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.getindex.ESIndicesGetIndexResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.gettemplate.ESIndicesGetTemplateResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.putalias.ESIndicesPutAliasResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.putindex.ESIndicesPutIndexResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.puttemplate.ESIndicesPutTemplateResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.refreshindex.ESIndicesRefreshIndexResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.searchshards.ESIndicesSearchShardsResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.updatesettings.ESIndicesUpdateSettingsResponse;
import org.elasticsearch.action.*;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.threadpool.ThreadPool;


public class ESIndicesAdmin implements ESIndicesAdminClient {
    private final ElasticsearchClient client;

    public ESIndicesAdmin(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> ActionFuture<Response> execute(Action<Request, Response, RequestBuilder> action, Request request) {
        return client.execute(action, request);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(Action<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {
        client.execute(action, request, listener);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> RequestBuilder prepareExecute(Action<Request, Response, RequestBuilder> action) {
        return client.prepareExecute(action);
    }


    @Override
    public ThreadPool threadPool() {
        return client.threadPool();
    }

    @Override
    public ActionFuture<ESIndicesStatsResponse> stats(final ESIndicesStatsRequest request) {
        return execute( ESIndicesStatsAction.INSTANCE, request);
    }

    @Override
    public void stats(final ESIndicesStatsRequest request, final ActionListener<ESIndicesStatsResponse> listener) {
        execute(ESIndicesStatsAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesStatsRequestBuilder prepareStats(String... indices) {
        return new ESIndicesStatsRequestBuilder(this, ESIndicesStatsAction.INSTANCE).setIndices(indices);
    }

    @Override
    public ESIndicesStatsRequestBuilder prepareStats() {
        return new ESIndicesStatsRequestBuilder(this, ESIndicesStatsAction.INSTANCE);
    }




    @Override
    public ActionFuture<ESIndicesSearchShardsResponse> searchShards(ESIndicesSearchShardsRequest request) {
        return execute( ESIndicesSearchShardsAction.INSTANCE, request);
    }

    @Override
    public void searchShards(ESIndicesSearchShardsRequest request, ActionListener<ESIndicesSearchShardsResponse> listener) {
        execute(ESIndicesSearchShardsAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesSearchShardsRequestBuilder prepareSearchShards(String... indices) {
        return new ESIndicesSearchShardsRequestBuilder(this, ESIndicesSearchShardsAction.INSTANCE).setIndices(indices);
    }

    @Override
    public ESIndicesSearchShardsRequestBuilder prepareSearchShards() {
        return new ESIndicesSearchShardsRequestBuilder(this, ESIndicesSearchShardsAction.INSTANCE);
    }





    @Override
    public ActionFuture<ESIndicesGetAliasResponse> alias(ESIndicesGetAliasRequest request) {
        return execute( ESIndicesGetAliasAction.INSTANCE, request);
    }

    @Override
    public void alias(ESIndicesGetAliasRequest request, ActionListener<ESIndicesGetAliasResponse> listener) {
        execute(ESIndicesGetAliasAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesGetAliasRequestBuilder prepareAlias(String... indices) {
        return new ESIndicesGetAliasRequestBuilder(this, ESIndicesGetAliasAction.INSTANCE).setIndices(indices);
    }

    @Override
    public ESIndicesGetAliasRequestBuilder prepareAlias() {
        return new ESIndicesGetAliasRequestBuilder(this, ESIndicesGetAliasAction.INSTANCE);
    }



    @Override
    public ActionFuture<ESIndicesPutAliasResponse> putAlias(ESIndicesPutAliasRequest request) {
        return execute( ESIndicesPutAliasAction.INSTANCE, request);
    }

    @Override
    public void putAlias(ESIndicesPutAliasRequest request, ActionListener<ESIndicesPutAliasResponse> listener) {
        execute(ESIndicesPutAliasAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesPutAliasRequestBuilder preparePutAlias() {
        return new ESIndicesPutAliasRequestBuilder(this, ESIndicesPutAliasAction.INSTANCE);
    }





    @Override
    public ActionFuture<ESIndicesGetIndexResponse> getIndex(final ESIndicesGetIndexRequest request) {
        return execute( ESIndicesGetIndexAction.INSTANCE, request);
    }

    @Override
    public void getIndex(ESIndicesGetIndexRequest request, ActionListener<ESIndicesGetIndexResponse> listener) {
        execute(ESIndicesGetIndexAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesGetIndexRequestBuilder prepareGetIndex(String... indices) {
        return new ESIndicesGetIndexRequestBuilder(this, ESIndicesGetIndexAction.INSTANCE).setIndices(indices);
    }

    @Override
    public ESIndicesGetIndexRequestBuilder prepareGetIndex() {
        return new ESIndicesGetIndexRequestBuilder(this, ESIndicesGetIndexAction.INSTANCE);
    }




    @Override
    public ActionFuture<ESIndicesPutIndexResponse> putIndex(final ESIndicesPutIndexRequest request) {
        return execute( ESIndicesPutIndexAction.INSTANCE, request);
    }

    @Override
    public void putIndex(ESIndicesPutIndexRequest request, ActionListener<ESIndicesPutIndexResponse> listener) {
        execute(ESIndicesPutIndexAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesPutIndexRequestBuilder preparePutIndex(String index) {
        return new ESIndicesPutIndexRequestBuilder(this, ESIndicesPutIndexAction.INSTANCE).setIndex(index);
    }





    @Override
    public ActionFuture<ESIndicesDeleteIndexResponse> deleteIndex(final ESIndicesDeleteIndexRequest request) {
        return execute( ESIndicesDeleteIndexAction.INSTANCE, request);
    }

    @Override
    public void deleteIndex(ESIndicesDeleteIndexRequest request, ActionListener<ESIndicesDeleteIndexResponse> listener) {
        execute(ESIndicesDeleteIndexAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesDeleteIndexRequestBuilder prepareDeleteIndex(String index) {
        return new ESIndicesDeleteIndexRequestBuilder(this, ESIndicesDeleteIndexAction.INSTANCE).setIndex(index);
    }


    @Override
    public ActionFuture<ESIndicesRefreshIndexResponse> refreshIndex(final ESIndicesRefreshIndexRequest request) {
        return execute( ESIndicesRefreshIndexAction.INSTANCE, request);
    }

    @Override
    public void refreshIndex(ESIndicesRefreshIndexRequest request, ActionListener<ESIndicesRefreshIndexResponse> listener) {
        execute(ESIndicesRefreshIndexAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesRefreshIndexRequestBuilder prepareRefreshIndex(String index) {
        return new ESIndicesRefreshIndexRequestBuilder(this, ESIndicesRefreshIndexAction.INSTANCE).setIndex(index);
    }

    @Override
    public ActionFuture<ESIndicesGetTemplateResponse> getTemplate(final ESIndicesGetTemplateRequest request) {
        return execute( ESIndicesGetTemplateAction.INSTANCE, request);
    }

    @Override
    public void getTemplate(ESIndicesGetTemplateRequest request, ActionListener<ESIndicesGetTemplateResponse> listener) {
        execute(ESIndicesGetTemplateAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesGetTemplateRequestBuilder prepareGetTemplate(String... templates) {
        return new ESIndicesGetTemplateRequestBuilder(this, ESIndicesGetTemplateAction.INSTANCE).setTemplate(templates);
    }

    @Override
    public ESIndicesGetTemplateRequestBuilder prepareGetTemplate() {
        return new ESIndicesGetTemplateRequestBuilder(this, ESIndicesGetTemplateAction.INSTANCE);
    }



    @Override
    public ActionFuture<ESIndicesPutTemplateResponse> putTemplate(final ESIndicesPutTemplateRequest request) {
        return execute( ESIndicesPutTemplateAction.INSTANCE, request);
    }

    @Override
    public void putTemplate(ESIndicesPutTemplateRequest request, ActionListener<ESIndicesPutTemplateResponse> listener) {
        execute(ESIndicesPutTemplateAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesPutTemplateRequestBuilder preparePutTemplate(String template) {
        return new ESIndicesPutTemplateRequestBuilder(this, ESIndicesPutTemplateAction.INSTANCE).setTemplate(template);
    }





    @Override
    public ActionFuture<ESIndicesDeleteTemplateResponse> deleteTemplate(final ESIndicesDeleteTemplateRequest request) {
        return execute( ESIndicesDeleteTemplateAction.INSTANCE, request);
    }

    @Override
    public void deleteTemplate(ESIndicesDeleteTemplateRequest request, ActionListener<ESIndicesDeleteTemplateResponse> listener) {
        execute(ESIndicesDeleteTemplateAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesDeleteTemplateRequestBuilder prepareDeleteTemplate(String template) {
        return new ESIndicesDeleteTemplateRequestBuilder(this, ESIndicesDeleteTemplateAction.INSTANCE).setTemplate(template);
    }




    @Override
    public ActionFuture<ESIndicesUpdateSettingsResponse> updateSettings(final ESIndicesUpdateSettingsRequest request) {
        return execute( ESIndicesUpdateSettingsAction.INSTANCE, request);
    }

    @Override
    public void updateSettings(ESIndicesUpdateSettingsRequest request, ActionListener<ESIndicesUpdateSettingsResponse> listener) {
        execute(ESIndicesUpdateSettingsAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesUpdateSettingsRequestBuilder prepareUpdateSettings(String index) {
        return new ESIndicesUpdateSettingsRequestBuilder(this, ESIndicesUpdateSettingsAction.INSTANCE).setIndex(index);
    }


    @Override
    public ActionFuture<ESIndicesExistsResponse> exists(final ESIndicesExistsRequest request) {
        return execute( ESIndicesExistsAction.INSTANCE, request);
    }

    @Override
    public void exists(ESIndicesExistsRequest request, ActionListener<ESIndicesExistsResponse> listener) {
        execute(ESIndicesExistsAction.INSTANCE, request, listener);
    }

    @Override
    public ESIndicesExistsRequestBuilder prepareExists(String index) {
        return new ESIndicesExistsRequestBuilder(this, ESIndicesExistsAction.INSTANCE).setIndex(index);
    }

}
