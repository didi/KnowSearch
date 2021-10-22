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

package com.didi.arius.gateway.elasticsearch.client.request.index.gettemplate;

import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import com.didi.arius.gateway.elasticsearch.client.response.indices.gettemplate.ESIndicesGetTemplateResponse;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionRequestValidationException;

public class ESIndicesGetTemplateRequest extends ESActionRequest<ESIndicesGetTemplateRequest> {
    private String[] templates;

    public ESIndicesGetTemplateRequest setTemplates(String... tempaltes) {
        this.templates = tempaltes;
        return this;
    }


    @Override
    public RestRequest toRequest() throws Exception {
        String tempalteStr = null;
        if(templates!=null) {
            tempalteStr = StringUtils.join(templates, ",");
        }
        if(tempalteStr!=null && tempalteStr.length()==0) {
            tempalteStr= null;
        }

        String endPoint;
        if(tempalteStr==null) {
            endPoint = "/_template";
        } else {
            endPoint = "/_template/" + tempalteStr.trim();
        }

        return new RestRequest("GET", endPoint, null);
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        return ESIndicesGetTemplateResponse.getResponse(response.getResponseContent());
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }
}
