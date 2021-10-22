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

package com.didi.arius.gateway.elasticsearch.client.gateway.search;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.unit.TimeValue;

/**
 * A search request request builder.
 */
public class ESSearchScrollRequestBuilder extends ActionRequestBuilder<ESSearchScrollRequest, ESSearchResponse, ESSearchScrollRequestBuilder> {

    public ESSearchScrollRequestBuilder(ElasticsearchClient client, ESSearchScrollAction action) {
        super(client, action, new ESSearchScrollRequest());
    }


    public ESSearchScrollRequestBuilder setScrollId(String scrollId) {
        request.setScrollId(scrollId);
        return this;
    }

    public ESSearchScrollRequestBuilder setScroll(TimeValue keepAlive) {
        request.scroll(keepAlive);
        return this;
    }

    public ESSearchScrollRequestBuilder setScroll(String keepAlive) {
        request.scroll(keepAlive);
        return this;
    }


    @Override
    public ESSearchScrollRequest request() {
        return request;
    }

    @Override
    protected ESSearchScrollRequest beforeExecute(ESSearchScrollRequest request) {
        return request;
    }
}
