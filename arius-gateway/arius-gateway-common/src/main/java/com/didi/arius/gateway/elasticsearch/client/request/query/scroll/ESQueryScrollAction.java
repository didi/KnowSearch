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

package com.didi.arius.gateway.elasticsearch.client.request.query.scroll;

import com.didi.arius.gateway.elasticsearch.client.response.query.query.ESQueryResponse;
import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

/**
 */
public class ESQueryScrollAction extends Action<ESQueryScrollRequest, ESQueryResponse, ESQueryScrollRequestBuilder> {

    public static final ESQueryScrollAction INSTANCE = new ESQueryScrollAction();
    public static final String NAME = "query:scroll";

    private ESQueryScrollAction() {
        super(NAME);
    }

    @Override
    public ESQueryResponse newResponse() {
        return new ESQueryResponse();
    }

    @Override
    public ESQueryScrollRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESQueryScrollRequestBuilder(client, this);
    }
}
