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

package org.elasticsearch.http.nio;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.http.HttpPipelinedMessage;
import org.elasticsearch.http.HttpResponse;
import org.elasticsearch.rest.RestStatus;

public class NioHttpResponse extends DefaultFullHttpResponse implements HttpResponse, HttpPipelinedMessage {

    private final int sequence;
    private final NioHttpRequest request;

    NioHttpResponse(NioHttpRequest request, RestStatus status, BytesReference content) {
        super(request.nettyRequest().protocolVersion(), HttpResponseStatus.valueOf(status.getStatus()), ByteBufUtils.toByteBuf(content));
        this.sequence = request.sequence();
        this.request = request;
    }

    @Override
    public void addHeader(String name, String value) {
        headers().add(name, value);
    }

    @Override
    public boolean containsHeader(String name) {
        return headers().contains(name);
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    public NioHttpRequest getRequest() {
        return request;
    }
}
