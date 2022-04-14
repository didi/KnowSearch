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

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.unit.TimeValue;

public class ESQueryScrollRequest extends ESActionRequest<ESQueryScrollRequest> {

    private Class clazz;
    private String scrollId;
    private TimeValue scrollTime;

    public ESQueryScrollRequest() {
        // pass
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }


    public ESQueryScrollRequest setScrollId(String scrollId) {
        this.scrollId = scrollId;
        return this;
    }


    public ESQueryScrollRequest clazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public ESQueryScrollRequest scroll(TimeValue keepAlive) {
        this.scrollTime = keepAlive;
        return this;
    }

    public ESQueryScrollRequest scroll(String keepAlive) {
        return scroll( TimeValue.parseTimeValue(keepAlive, null,null));
    }


    @Override
    public RestRequest toRequest() throws Exception {
        if(scrollId == null) {
            throw new Exception("scroll id is null");
        }

        String endPoint = "/_search/scroll";

        JSONObject scrollJson = new JSONObject();
        if(scrollTime!=null) {
            scrollJson.put("scroll", scrollTime.toString());
        }
        scrollJson.put("scroll_id", scrollId);


        RestRequest restRequest = new RestRequest("POST", endPoint, null);
        restRequest.setBody(scrollJson.toJSONString());

        return restRequest;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        return ESQueryResponse.parserResponse(response.getResponseContent(), clazz);
    }
}
