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

package org.elasticsearch.action.admin.indices.exists.indices;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.TransportMasterNodeReadAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;

/**
 * Indices exists action.
 */
public class TransportIndicesExistsAction extends TransportMasterNodeReadAction<IndicesExistsRequest, IndicesExistsResponse> {

    @Inject
    public TransportIndicesExistsAction(TransportService transportService, ClusterService clusterService,
                                        ThreadPool threadPool, ActionFilters actionFilters,
                                        IndexNameExpressionResolver indexNameExpressionResolver) {
        super(IndicesExistsAction.NAME, transportService, clusterService, threadPool, actionFilters, IndicesExistsRequest::new,
            indexNameExpressionResolver);
    }

    @Override
    protected String executor() {
        // lightweight in memory check
        return ThreadPool.Names.SAME;
    }

    @Override
    protected IndicesExistsResponse read(StreamInput in) throws IOException {
        return new IndicesExistsResponse(in);
    }

    @Override
    protected ClusterBlockException checkBlock(IndicesExistsRequest request, ClusterState state) {
        //make sure through indices options that the concrete indices call never throws IndexMissingException
        IndicesOptions indicesOptions = IndicesOptions.fromOptions(true, true, request.indicesOptions().expandWildcardsOpen(),
            request.indicesOptions().expandWildcardsClosed());
        return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA_READ,
            indexNameExpressionResolver.concreteIndexNames(state, indicesOptions, request.indices()));
    }

    @Override
    protected void masterOperation(final IndicesExistsRequest request, final ClusterState state,
                                   final ActionListener<IndicesExistsResponse> listener) {
        boolean exists;
        try {
            // Similar as the previous behaviour, but now also aliases and wildcards are supported.
            indexNameExpressionResolver.concreteIndexNames(state, request);
            exists = true;
        } catch (IndexNotFoundException e) {
            exists = false;
        }
        listener.onResponse(new IndicesExistsResponse(exists));
    }
}
