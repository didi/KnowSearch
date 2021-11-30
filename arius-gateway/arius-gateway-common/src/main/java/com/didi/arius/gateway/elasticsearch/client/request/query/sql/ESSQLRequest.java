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

package com.didi.arius.gateway.elasticsearch.client.request.query.sql;

import com.didi.arius.gateway.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import org.elasticsearch.action.ActionRequestValidationException;

public class ESSQLRequest extends ESActionRequest<ESSQLRequest> {
    private String sql;

    private Class clazz = null;

    public ESSQLRequest() {
    }


    public ESSQLRequest(String sql) {
        this.sql = sql;
    }


    public ESSQLRequest clazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }


    @Override
    public RestRequest toRequest() throws Exception {
        String endpoint = "/_sql";
        RestRequest restRequest = new RestRequest("POST", endpoint);
        restRequest.setContent(sql);
        return restRequest;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        return ESQueryResponse.parserResponse(response.getResponseContent(), clazz);
    }
}
