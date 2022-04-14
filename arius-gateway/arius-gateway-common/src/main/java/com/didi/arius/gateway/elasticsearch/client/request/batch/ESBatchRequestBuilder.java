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

package com.didi.arius.gateway.elasticsearch.client.request.batch;

import com.didi.arius.gateway.elasticsearch.client.response.batch.ESBatchResponse;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/* TODO支持异步回调函数 */
public class ESBatchRequestBuilder extends ActionRequestBuilder<ESBatchRequest, ESBatchResponse, ESBatchRequestBuilder> {

    public ESBatchRequestBuilder(ElasticsearchClient client, ESBatchAction action) {
        super(client, action, new ESBatchRequest());
    }

    public ESBatchRequestBuilder addNode(BatchType batchType, String index, String type, String id, String content) {
        request.addNode(batchType, index, type, id, content);
        return this;
    }


    @Override
    public ESBatchRequest request() {
        return request;
    }

    @Override
    protected ESBatchRequest beforeExecute(ESBatchRequest request) {
        return request;
    }
}
