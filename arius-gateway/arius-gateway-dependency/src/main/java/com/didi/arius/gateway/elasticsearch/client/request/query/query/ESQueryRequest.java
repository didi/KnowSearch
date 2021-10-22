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

package com.didi.arius.gateway.elasticsearch.client.request.query.query;

import com.didi.arius.gateway.elasticsearch.client.response.query.query.ESQueryResponse;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionRequest;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;
import com.didi.arius.gateway.elasticsearch.client.model.RestRequest;
import com.didi.arius.gateway.elasticsearch.client.model.RestResponse;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * A request to execute search against one or more indices (or all). Best created using
 * {@link Requests#searchRequest(String...)}.
 * <p>
 * Note, the search {@link #source(SearchSourceBuilder)}
 * is required. The search source is the different search options, including aggregations and such.
 * <p>
 *
 * @see Requests#searchRequest(String...)
 */
public class ESQueryRequest extends ESActionRequest<ESQueryRequest> implements IndicesRequest.Replaceable {
    private SearchType searchType = SearchType.DEFAULT;

    private Class clazz = null;

    private String[] indices;

    private String routing;

    private String preference;

    private BytesReference source;

    private TimeValue scrollTime;

    private String[] types = Strings.EMPTY_ARRAY;


    public ESQueryRequest() {
    }


    public ESQueryRequest(String... indices) {
        indices(indices);
    }

    public ESQueryRequest(String[] indices, byte[] source) {
        indices(indices);
        source(source);
    }

    public ESQueryRequest clazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    @Override
    public ESQueryRequest indices(String... indices) {
        if (indices == null) {
            throw new IllegalArgumentException("indices must not be null");
        } else {
            for (int i = 0; i < indices.length; i++) {
                if (indices[i] == null) {
                    throw new IllegalArgumentException("indices[" + i + "] must not be null");
                }
            }
        }
        this.indices = indices;
        return this;
    }


    @Override
    public ActionRequestValidationException validate() {
        return null;
    }


    @Override
    public String[] indices() {
        return indices;
    }

    @Override
    public IndicesOptions indicesOptions() {
        throw new RuntimeException("not support");
    }



                        /* type */
    public String[] types() {
        return types;
    }

    public ESQueryRequest types(String... types) {
        this.types = types;
        return this;
    }

                        /* preference */
    public ESQueryRequest preference(String preference) {
        this.preference = preference;
        return this;
    }

                        /* routing */
    public String routing() {
        return this.routing;
    }

    public ESQueryRequest routing(String routing) {
        this.routing = routing;
        return this;
    }

    public ESQueryRequest routing(String... routings) {
        this.routing = Strings.arrayToCommaDelimitedString(routings);
        return this;
    }


    /* source */
    public ESQueryRequest source(SearchSourceBuilder sourceBuilder) {
        return source(sourceBuilder.buildAsBytes(Requests.CONTENT_TYPE));
    }

    public ESQueryRequest source(String source) {
        return source(new BytesArray(source));
    }

    public ESQueryRequest source(XContentBuilder builder) {
        return source(builder.bytes());
    }

    public ESQueryRequest source(byte[] source) {
        return source(source, 0, source.length);
    }

    public ESQueryRequest source(byte[] source, int offset, int length) {
        return source(new BytesArray(source, offset, length));
    }

    public ESQueryRequest source(BytesReference source) {
        this.source = source;
        return this;
    }

    public BytesReference source() {
        return source;
    }


                        /* scroll */
    public ESQueryRequest scroll(TimeValue keepAlive) {
        this.scrollTime = keepAlive;
        return this;
    }

    public ESQueryRequest scroll(String keepAlive) {
        return scroll( TimeValue.parseTimeValue(keepAlive, null,null));
    }


    @Override
    public RestRequest toRequest() throws Exception {
        String index = StringUtils.join(indices, ",");
        String type = StringUtils.join(types, ",");

        String endpoint;
        if (type == null || type.length() == 0) {
            endpoint = String.format("/%s/_search", index);
        } else {
            endpoint = String.format("/%s/%s/_search", index, type);
        }

        RestRequest restRequest = new RestRequest("GET", endpoint, source);
        if(scrollTime!=null) {
            restRequest.addParam("scroll", scrollTime.toString());
        }

        if(preference!=null) {
            restRequest.addParam("preference", preference);
        }

        if(routing!=null) {
            restRequest.addParam("routing", routing);
        }

        return restRequest;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        return ESQueryResponse.parserResponse(response.getResponseContent(), clazz);
    }
}
