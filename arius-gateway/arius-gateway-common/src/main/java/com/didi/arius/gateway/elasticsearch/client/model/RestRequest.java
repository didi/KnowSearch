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

package com.didi.arius.gateway.elasticsearch.client.model;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.jboss.netty.handler.codec.http.HttpMethod;

public final class RestRequest {

    //default buffer limit is 300MB
    static final int DEFAULT_BUFFER_LIMIT = 300 * 1024 * 1024;

    HttpAsyncResponseConsumerFactory DEFAULT_HTTP_RESPONSE_FACTORY = new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(DEFAULT_BUFFER_LIMIT);

    private String method;
    private String endpoint;
    private HttpEntity entity;
    private String dsl;
    private Map<String, String> params = new HashMap<>();
    private List<Header> headers = new ArrayList<>();
    private int socketTimeOut = 0;

    public RestRequest(String method, String endpoint) throws IOException {
        this(method, endpoint, null);
    }

    public RestRequest(String method, String endpoint, BytesReference body) throws IOException {
        this.method = Objects.requireNonNull(method, "method cannot be null");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint cannot be null");

        setBody(body);
    }

    public void setBody(String body) throws IOException {
        BytesReference source = body == null ? null : new BytesArray(body);
        setBody(source);
    }

    public void setBody(BytesReference body) throws IOException {
        if(body != null && body.length() > 0) {
            entity = new ByteArrayEntity(body.toBytes(), 0, body.length(), ContentType.APPLICATION_JSON);
            dsl = XContentHelper.convertToJson(body, false);
        } else {
            dsl =  "";
            entity = new ByteArrayEntity(dsl.getBytes(), 0, dsl.length());
        }
    }

    public void setContent(String content) {
        if (content != null) {
            entity = new ByteArrayEntity(content.getBytes(), 0, content.length(), ContentType.APPLICATION_JSON);
        }
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public String getMethod() {
        return method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void addEndpointPrefix(String prefix) {
        if(StringUtils.isBlank(prefix)) {
            return;
        }

        if(prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }

        if(prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length()-1);
        }

        if(endpoint==null) {
            endpoint = "";
        }

        if(StringUtils.isBlank(endpoint)) {
            this.endpoint = "/" + prefix;
        } else {
            this.endpoint = "/" + prefix + "/" + this.endpoint;
        }
    }

    public HttpEntity getEntity() {
        return entity;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }


    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("method", method);
        obj.put("endpoint", endpoint);
        if(dsl!=null) {
            obj.put("dsl", dsl);
        }
        return obj;
    }

    public Request buildRequest() {
        String uri = endpoint;
        if (StringUtils.isNotBlank(endpoint) && !endpoint.startsWith("/")) {
            uri = "/" + endpoint;
        }
        Request request = new Request(method, uri);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            request.addParameter(entry.getKey(), entry.getValue());
        }


        if(!method.equalsIgnoreCase(HttpMethod.HEAD.getName())) {
            request.setEntity(entity);
        }

        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        if(headers!=null) {
            for (Header header : headers) {
                builder.addHeader(header.getName(), header.getValue());
            }
        }
        builder.setHttpAsyncResponseConsumerFactory(DEFAULT_HTTP_RESPONSE_FACTORY);
        RequestOptions options = builder.build();
        request.setOptions(options);

        return request;
    }

    @Override
    public String toString() {
        return toJSON().toJSONString();
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public void setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }
}
