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

import com.didi.arius.gateway.elasticsearch.client.request.cluster.health.ESClusterHealthRequest;
import com.didi.arius.gateway.elasticsearch.client.request.cluster.health.ESClusterHealthRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.cluster.nodessetting.ESClusterNodesSettingRequest;
import com.didi.arius.gateway.elasticsearch.client.request.cluster.nodessetting.ESClusterNodesSettingRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.cluster.nodestats.ESClusterNodesStatsRequest;
import com.didi.arius.gateway.elasticsearch.client.request.cluster.nodestats.ESClusterNodesStatsRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.request.cluster.updatesetting.ESClusterUpdateSettingsRequest;
import com.didi.arius.gateway.elasticsearch.client.request.cluster.updatesetting.ESClusterUpdateSettingsRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.response.cluster.ESClusterHealthResponse;
import com.didi.arius.gateway.elasticsearch.client.response.cluster.nodessetting.ESClusterNodesSettingResponse;
import com.didi.arius.gateway.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import com.didi.arius.gateway.elasticsearch.client.response.cluster.updatesetting.ESClusterUpdateSettingsResponse;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Administrative actions/operations against indices.
 *
 * @see AdminClient#cluster()
 */
public interface ESClusterAdminClient extends ElasticsearchClient {

    ActionFuture<ESClusterHealthResponse> health(ESClusterHealthRequest request);

    void health(ESClusterHealthRequest request, ActionListener<ESClusterHealthResponse> listener);

    ESClusterHealthRequestBuilder prepareHealth();



    ActionFuture<ESClusterNodesStatsResponse> nodeStats(final ESClusterNodesStatsRequest request);

    void nodeStats(final ESClusterNodesStatsRequest request, final ActionListener<ESClusterNodesStatsResponse> listener);

    ESClusterNodesStatsRequestBuilder prepareNodeStats();



    ActionFuture<ESClusterNodesSettingResponse> nodesSetting(final ESClusterNodesSettingRequest request);

    void nodesSetting(final ESClusterNodesSettingRequest request, final ActionListener<ESClusterNodesSettingResponse> listener);

    ESClusterNodesSettingRequestBuilder prepareNodesSetting();


    ActionFuture<ESClusterUpdateSettingsResponse> updateSetting(final ESClusterUpdateSettingsRequest request);

    void updateSetting(final ESClusterUpdateSettingsRequest request, final ActionListener<ESClusterUpdateSettingsResponse> listener);

    ESClusterUpdateSettingsRequestBuilder prepareUpdateSettings();
}
