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

package com.didi.arius.gateway.elasticsearch.client.request.cluster.updatesetting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.response.cluster.updatesetting.ESClusterUpdateSettingsResponse;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.unit.TimeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Request for an update cluster settings action
 */
public class ESClusterUpdateSettingsRequest extends ESActionRequest<ESClusterUpdateSettingsRequest> {
    private static final TimeValue DEFAULT_ACK_TIMEOUT =  new TimeValue(30 , TimeUnit.SECONDS);
    private static final String PERSISTENT_STR = "persistent";
    private static final String TRANSIENT_STR = "transient";


    protected TimeValue timeout = DEFAULT_ACK_TIMEOUT;
    private Map<String, String> transients = new HashMap<>();
    private Map<String, String> persistents = new HashMap<>();

    public ESClusterUpdateSettingsRequest() {}

    public ESClusterUpdateSettingsRequest timeout(String timeout) {
        this.timeout = TimeValue.parseTimeValue(timeout, this.timeout, getClass().getSimpleName() + ".timeout");
        return this;
    }

    public ESClusterUpdateSettingsRequest timeout(TimeValue timeout) {
        this.timeout = timeout;
        return this;
    }


    public ESClusterUpdateSettingsRequest addTransient(String key, String value) {
        transients.put(key, value);

        return this;
    }

    public ESClusterUpdateSettingsRequest addPersistent(String key, String value) {
        persistents.put(key, value);
        return this;
    }


    @Override
    public ActionRequestValidationException validate() {
        return null;
    }


    @Override
    public RestRequest toRequest() throws Exception {
        JSONObject obj = new JSONObject();

        addSetting(TRANSIENT_STR, transients, obj);
        addSetting(PERSISTENT_STR, persistents, obj);

        RestRequest rr = new RestRequest("PUT", "/_cluster/settings", null);
        rr.setBody(obj.toJSONString());

        if (timeout != null) {
            rr.addParam("timeout", timeout.toString());
        }
        return rr;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        String respStr = response.getResponseContent();
        return JSON.parseObject(respStr, ESClusterUpdateSettingsResponse.class);
    }

    private void addSetting(String name, Map<String, String> m, JSONObject root) {
        if (m.size() == 0) {
            return;
        }

        JSONObject o = new JSONObject();
        for (String key : m.keySet()) {
            o.put(key, m.get(key));
        }
        root.put(name, o);
    }
}
