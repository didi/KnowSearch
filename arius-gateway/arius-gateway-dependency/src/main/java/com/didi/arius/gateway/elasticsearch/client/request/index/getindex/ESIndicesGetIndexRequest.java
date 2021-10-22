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

package com.didi.arius.gateway.elasticsearch.client.request.index.getindex;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.getindex.ESIndicesGetIndexResponse;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionRequestValidationException;

import java.util.HashSet;
import java.util.Set;

public class ESIndicesGetIndexRequest extends ESActionRequest<ESIndicesGetIndexRequest> {
    private static final String MAPPING_STR = "_mapping";
    private static final String SETTINGS_STR = "_settings";
    private static final String ALIAS_STR = "_alias";

    private String[] indices;
    private Set<String> flags = new HashSet<>();

    public ESIndicesGetIndexRequest setIndices(String... indices) {
        this.indices = indices;
        return this;
    }


    public ESIndicesGetIndexRequest mapping(boolean enable) {
        if (enable) {
            flags.add(MAPPING_STR);
        } else {
            flags.remove(MAPPING_STR);
        }
        return this;
    }


    public ESIndicesGetIndexRequest settings(boolean enable) {
        if (enable) {
            flags.add(SETTINGS_STR);
        } else {
            flags.remove(SETTINGS_STR);
        }
        return this;
    }


    public ESIndicesGetIndexRequest alias(boolean enable) {
        if (enable) {
            flags.add(ALIAS_STR);
        } else {
            flags.remove(ALIAS_STR);
        }
        return this;
    }


    @Override
    public RestRequest toRequest() throws Exception {
        String indicesStr = null;
        if (indices != null) {
            indicesStr = StringUtils.join(indices, ",");
        }
        if (indicesStr != null && indicesStr.length() == 0) {
            indicesStr = null;
        }


        String flagStr = StringUtils.join(flags, ",");
        if (flagStr != null && flagStr.trim().length() == 0) {
            flagStr = null;
        }

        String endPoint;
        if (indicesStr == null) {
            if (flagStr == null) {
                endPoint = "*";
            } else {
                endPoint = "*/" + flagStr.trim();
            }
        } else {
            if (flagStr == null) {
                endPoint = indicesStr.trim();
            } else {
                endPoint = indicesStr.trim() + "/" + flagStr.trim();
            }
        }

        return new RestRequest("GET", endPoint, null);
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        return ESIndicesGetIndexResponse.getResponse(response.getResponseContent());
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }
}
