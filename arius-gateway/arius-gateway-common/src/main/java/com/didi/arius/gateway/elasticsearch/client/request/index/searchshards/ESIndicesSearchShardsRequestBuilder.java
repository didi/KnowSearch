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

package com.didi.arius.gateway.elasticsearch.client.request.index.searchshards;

import com.didi.arius.gateway.elasticsearch.client.request.broadcast.ESBroadcastOperationRequestBuilder;
import com.didi.arius.gateway.elasticsearch.client.response.indices.searchshards.ESIndicesSearchShardsResponse;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * A request to get indices level nodestats. Allow to enable different nodestats to be returned.
 * <p>
 * By default, the {@link #setDocs(boolean)}, {@link #setStore(boolean)}, {@link #setIndexing(boolean)}
 * are enabled. Other nodestats can be enabled as well.
 * <p>
 * All the nodestats to be returned can be cleared using {@link #clear()}, at which point, specific
 * nodestats can be enabled.
 */
public class ESIndicesSearchShardsRequestBuilder extends ESBroadcastOperationRequestBuilder<ESIndicesSearchShardsRequest, ESIndicesSearchShardsResponse, ESIndicesSearchShardsRequestBuilder> {

    public ESIndicesSearchShardsRequestBuilder(ElasticsearchClient client, ESIndicesSearchShardsAction action) {
        super(client, action, new ESIndicesSearchShardsRequest());
    }
}
