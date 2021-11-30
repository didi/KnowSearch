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

package com.didi.arius.gateway.elasticsearch.client.request.index.puttemplate;

import com.didi.arius.gateway.elasticsearch.client.model.type.ESVersion;
import com.didi.arius.gateway.elasticsearch.client.response.indices.puttemplate.ESIndicesPutTemplateResponse;
import com.didi.arius.gateway.elasticsearch.client.response.setting.template.TemplateConfig;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESIndicesPutTemplateRequestBuilder extends ActionRequestBuilder<ESIndicesPutTemplateRequest, ESIndicesPutTemplateResponse, ESIndicesPutTemplateRequestBuilder> {

    public ESIndicesPutTemplateRequestBuilder(ElasticsearchClient client, ESIndicesPutTemplateAction action) {
        super(client, action, new ESIndicesPutTemplateRequest());
    }

    public ESIndicesPutTemplateRequestBuilder setVersion(ESVersion version) {
        request.setESVersion(version);
        return this;
    }


    public ESIndicesPutTemplateRequestBuilder setTemplate(String template) {
        request.setTemplate(template);
        return this;
    }

    public ESIndicesPutTemplateRequestBuilder setTemplateConfig(String templateConfig) {
        request.setTemplateConfig(templateConfig);
        return this;
    }

    public ESIndicesPutTemplateRequestBuilder setTemplateConfig(TemplateConfig templateConfig) {
        request.setTemplateConfig(templateConfig);
        return this;
    }
}
