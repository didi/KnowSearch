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

package com.didi.arius.gateway.elasticsearch.client.request.cluster.nodestats;

import com.didi.arius.gateway.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

import static com.didi.arius.gateway.elasticsearch.client.request.cluster.nodestats.ESClusterNodesStatsRequest.*;

/**
 *
 */
public class ESClusterNodesStatsRequestBuilder extends ActionRequestBuilder<ESClusterNodesStatsRequest, ESClusterNodesStatsResponse, ESClusterNodesStatsRequestBuilder> {

    public ESClusterNodesStatsRequestBuilder(ElasticsearchClient client, ESClusterNodesStatsAction action) {
        super(client, action, new ESClusterNodesStatsRequest());
    }

    public final ESClusterNodesStatsRequestBuilder setNodesIds(String... nodesIds) {
        request.nodesIds(nodesIds);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder all() {
        request.all();
        return this;
    }

    public ESClusterNodesStatsRequestBuilder clear() {
        request.clear();
        return this;
    }

    public ESClusterNodesStatsRequestBuilder level(String level) {
        request.level(level);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder setIndices(boolean indices) {
        request.flag(INDICES, indices);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder setBreaker(boolean breaker) {
        request.flag(BREAKERS, breaker);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder setScript(boolean script) {
        request.flag(SCRIPT, script);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder setOs(boolean os) {
        request.flag(OS, os);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder setProcess(boolean process) {
        request.flag(PROCESS, process);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder setJvm(boolean jvm) {
        request.flag(JVM, jvm);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder setThreadPool(boolean threadPool) {
        request.flag(THREAD_POOL, threadPool);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder setFs(boolean fs) {
        request.flag(FS, fs);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder setTransport(boolean transport) {
        request.flag(TRANSPORT, transport);
        return this;
    }

    public ESClusterNodesStatsRequestBuilder setHttp(boolean http) {
        request.flag(HTTP, http);
        return this;
    }
}
