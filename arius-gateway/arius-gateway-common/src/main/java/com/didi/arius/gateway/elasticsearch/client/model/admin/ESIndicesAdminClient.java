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

package com.didi.arius.gateway.elasticsearch.client.model.admin;

import com.didi.arius.gateway.elasticsearch.client.request.index.deleteIndex.ESIndicesDeleteIndexRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.deleteIndex.ESIndicesDeleteIndexRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.deletetemplate.ESIndicesDeleteTemplateRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.deletetemplate.ESIndicesDeleteTemplateRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.exists.ESIndicesExistsRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.exists.ESIndicesExistsRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.getalias.ESIndicesGetAliasRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.getalias.ESIndicesGetAliasRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.getindex.ESIndicesGetIndexRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.getindex.ESIndicesGetIndexRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.gettemplate.ESIndicesGetTemplateRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.gettemplate.ESIndicesGetTemplateRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.putalias.ESIndicesPutAliasRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.putalias.ESIndicesPutAliasRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.putindex.ESIndicesPutIndexRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.putindex.ESIndicesPutIndexRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.puttemplate.ESIndicesPutTemplateRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.puttemplate.ESIndicesPutTemplateRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.refreshindex.ESIndicesRefreshIndexRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.refreshindex.ESIndicesRefreshIndexRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.searchshards.ESIndicesSearchShardsRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.searchshards.ESIndicesSearchShardsRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.index.stats.ESIndicesStatsRequest;
import com.didi.arius.gateway.elasticsearch.client.request.index.stats.ESIndicesStatsRequestBuilder;
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
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Administrative actions/operations against indices.
 *
 * @see AdminClient#indices()
 */
public interface ESIndicesAdminClient extends ElasticsearchClient {
    ActionFuture<ESIndicesStatsResponse> stats(ESIndicesStatsRequest request);

    void stats(ESIndicesStatsRequest request, ActionListener<ESIndicesStatsResponse> listener);

    ESIndicesStatsRequestBuilder prepareStats(String... indices);

    ESIndicesStatsRequestBuilder prepareStats();


    ActionFuture<ESIndicesSearchShardsResponse> searchShards(ESIndicesSearchShardsRequest request);

    void searchShards(ESIndicesSearchShardsRequest request, ActionListener<ESIndicesSearchShardsResponse> listener);

    ESIndicesSearchShardsRequestBuilder prepareSearchShards(String... indices);

    ESIndicesSearchShardsRequestBuilder prepareSearchShards();




    ActionFuture<ESIndicesGetAliasResponse> alias(ESIndicesGetAliasRequest request);

    void alias(ESIndicesGetAliasRequest request, ActionListener<ESIndicesGetAliasResponse> listener);

    ESIndicesGetAliasRequestBuilder prepareAlias(String... indices);

    ESIndicesGetAliasRequestBuilder prepareAlias();



    ActionFuture<ESIndicesPutAliasResponse> putAlias(ESIndicesPutAliasRequest request);

    void putAlias(ESIndicesPutAliasRequest request, ActionListener<ESIndicesPutAliasResponse> listener);

    ESIndicesPutAliasRequestBuilder preparePutAlias();






    ActionFuture<ESIndicesGetIndexResponse> getIndex(final ESIndicesGetIndexRequest request);

    void getIndex(ESIndicesGetIndexRequest request, ActionListener<ESIndicesGetIndexResponse> listener);

    ESIndicesGetIndexRequestBuilder prepareGetIndex(String... indices);

    ESIndicesGetIndexRequestBuilder prepareGetIndex();


    ActionFuture<ESIndicesPutIndexResponse> putIndex(final ESIndicesPutIndexRequest request);

    void putIndex(ESIndicesPutIndexRequest request, ActionListener<ESIndicesPutIndexResponse> listener);

    ESIndicesPutIndexRequestBuilder preparePutIndex(String index);


    ActionFuture<ESIndicesDeleteIndexResponse> deleteIndex(final ESIndicesDeleteIndexRequest request);

    void deleteIndex(ESIndicesDeleteIndexRequest request, ActionListener<ESIndicesDeleteIndexResponse> listener);

    ESIndicesDeleteIndexRequestBuilder prepareDeleteIndex(String index);


    ActionFuture<ESIndicesRefreshIndexResponse> refreshIndex(final ESIndicesRefreshIndexRequest request);

    void refreshIndex(ESIndicesRefreshIndexRequest request, ActionListener<ESIndicesRefreshIndexResponse> listener);

    ESIndicesRefreshIndexRequestBuilder prepareRefreshIndex(String index);




    ActionFuture<ESIndicesGetTemplateResponse> getTemplate(final ESIndicesGetTemplateRequest request);

    void getTemplate(ESIndicesGetTemplateRequest request, ActionListener<ESIndicesGetTemplateResponse> listener);

    ESIndicesGetTemplateRequestBuilder prepareGetTemplate(String... templates);

    ESIndicesGetTemplateRequestBuilder prepareGetTemplate();


    public ActionFuture<ESIndicesPutTemplateResponse> putTemplate(final ESIndicesPutTemplateRequest request);

    void putTemplate(ESIndicesPutTemplateRequest request, ActionListener<ESIndicesPutTemplateResponse> listener);

    ESIndicesPutTemplateRequestBuilder preparePutTemplate(String template);


    ActionFuture<ESIndicesDeleteTemplateResponse> deleteTemplate(final ESIndicesDeleteTemplateRequest request);

    void deleteTemplate(ESIndicesDeleteTemplateRequest request, ActionListener<ESIndicesDeleteTemplateResponse> listener);

    ESIndicesDeleteTemplateRequestBuilder prepareDeleteTemplate(String template);





    ActionFuture<ESIndicesUpdateSettingsResponse> updateSettings(final ESIndicesUpdateSettingsRequest request);

    void updateSettings(ESIndicesUpdateSettingsRequest request, ActionListener<ESIndicesUpdateSettingsResponse> listener);

    ESIndicesUpdateSettingsRequestBuilder prepareUpdateSettings(String index);


    public ActionFuture<ESIndicesExistsResponse> exists(final ESIndicesExistsRequest request);

    public void exists(ESIndicesExistsRequest request, ActionListener<ESIndicesExistsResponse> listener);

    public ESIndicesExistsRequestBuilder prepareExists(String index);
}
