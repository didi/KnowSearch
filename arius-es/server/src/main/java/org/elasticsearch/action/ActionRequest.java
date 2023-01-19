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

package org.elasticsearch.action;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.monitor.request.RequestTracker;
import org.elasticsearch.transport.TransportRequest;

import java.io.IOException;
import java.util.*;

public abstract class ActionRequest extends TransportRequest {

    public ActionRequest() {
        super();
        // this does not set the listenerThreaded API, if needed, its up to the caller to set it
        // since most times, we actually want it to not be threaded...
        // this.listenerThreaded = request.listenerThreaded();
    }

    public ActionRequest(StreamInput in) throws IOException {
        super(in);
    }

    public ActionRequest(Map<String, List<String>> headers) {
        RequestTracker.registerHeader(headers, this);
    }

    public abstract ActionRequestValidationException validate();

    /**
     * Should this task store its result after it has finished?
     */
    public boolean getShouldStoreResult() {
        return false;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
    }

    protected Map<String, Object> headers;

    @SuppressWarnings("unchecked")
    public final void putHeader(String key, Object value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public final <V> V getHeader(String key) {
        return headers != null ? (V) headers.get(key) : null;
    }

    public final boolean hasHeader(String key) {
        return headers != null && headers.containsKey(key);
    }

    public Set<String> getHeaders() {
        return headers != null ? headers.keySet() : Collections.<String>emptySet();
    }

}
