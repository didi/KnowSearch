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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.response.cluster.nodesstats.ESClusterNodesStatsResponse;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionRequestValidationException;

import java.util.HashSet;
import java.util.Set;

public class ESClusterNodesStatsRequest extends ESActionRequest<ESClusterNodesStatsRequest> {
    public static final String INDICES = "indices";
    public static final String OS = "os";
    public static final String PROCESS = "process";
    public static final String JVM = "jvm";
    public static final String THREAD_POOL = "thread_pool";
    public static final String FS = "fs";
    public static final String TRANSPORT = "transport";
    public static final String HTTP = "http";
    public static final String BREAKERS = "breakers";
    public static final String SCRIPT = "script";

    private Set<String> flags = new HashSet<>();
    private String[] nodesIds = null;
    private String level = null;


    public final ESClusterNodesStatsRequest  nodesIds(String... nodesIds) {
        this.nodesIds = nodesIds;
        return this;
    }

    public ESClusterNodesStatsRequest level(String level) {
        this.level = level;
        return this;
    }

    public ESClusterNodesStatsRequest all() {
        flags.add(INDICES);
        flags.add(OS);
        flags.add(PROCESS);
        flags.add(JVM);
        flags.add(THREAD_POOL);
        flags.add(FS);
        flags.add(TRANSPORT);
        flags.add(HTTP);
        flags.add(BREAKERS);
        flags.add(SCRIPT);
        return this;
    }

    public ESClusterNodesStatsRequest clear() {
        flags.clear();
        return this;
    }


    public ESClusterNodesStatsRequest flag(String name, boolean isSet) {
        if(isSet) {
            flags.add(name);
        } else {
            flags.remove(name);
        }
        return this;
    }

    public boolean isSet(String name) {
        return flags.contains(name);
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        String endpoint = buildEndPoint();
        RestRequest restRequest = new RestRequest("GET", endpoint, null);

        if(level!=null) {
            restRequest.getParams().put("level", level);
        }

        return restRequest;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        // TODO 新版本ES增加了_nodes字段，和node在fastjson中会冲突, 先去除_nodes处理，后续兼容这种情况
        String respStr = response.getResponseContent();
        JSONObject obj = JSON.parseObject(respStr);
        obj.remove("_nodes");

        return JSON.parseObject(obj.toJSONString(), ESClusterNodesStatsResponse.class);
    }

    private String buildEndPoint() {
        String nodes = null;
        if(nodesIds!=null) {
            nodes = StringUtils.join(nodesIds, ",");
        }
        if(nodes!=null && nodes.trim().length()==0) {
            nodes = null;
        }

        String flagStr = null;
        if(flags.size()<10) {
            flagStr = StringUtils.join(flags, ",");
        }
        if(flagStr!=null && flagStr.trim().length()==0) {
            flagStr = null;
        }


        if(nodes==null) {
           if(flagStr==null) {
               return "_nodes/stats";
           } else {
               return "_nodes/stats/" + flagStr.trim();
           }
        } else {
           if(flagStr==null) {
               return "_nodes/" +nodes.trim()+ "/stats";
           } else {
               return "_nodes/" +nodes.trim()+ "/stats/" + flagStr.trim();
           }
        }
    }
}
