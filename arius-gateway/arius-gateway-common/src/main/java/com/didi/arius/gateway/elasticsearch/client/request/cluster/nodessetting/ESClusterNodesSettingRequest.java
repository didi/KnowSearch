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

package com.didi.arius.gateway.elasticsearch.client.request.cluster.nodessetting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.response.cluster.nodessetting.ESClusterNodesSettingResponse;
import org.elasticsearch.action.ActionRequestValidationException;


public class ESClusterNodesSettingRequest extends ESActionRequest<ESClusterNodesSettingRequest> {
    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        return new RestRequest("GET", "_nodes/settings", null);
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        // TODO 新版本ES增加了_nodes字段，和node在fastjson中会冲突, 先去除_nodes处理，后续兼容这种情况
        String respStr = response.getResponseContent();
        JSONObject obj = JSON.parseObject(respStr);
        obj.remove("_nodes");

        return JSON.parseObject(obj.toJSONString(), ESClusterNodesSettingResponse.class);
    }
}
