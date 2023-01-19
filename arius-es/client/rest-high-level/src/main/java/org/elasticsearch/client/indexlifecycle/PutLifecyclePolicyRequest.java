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

package org.elasticsearch.client.indexlifecycle;

import org.elasticsearch.client.TimedRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Objects;

public class PutLifecyclePolicyRequest extends TimedRequest implements ToXContentObject {

    private final LifecyclePolicy policy;

    public PutLifecyclePolicyRequest(LifecyclePolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("policy definition cannot be null");
        }
        if (Strings.isNullOrEmpty(policy.getName())) {
            throw new IllegalArgumentException("policy name must be present");
        }
        this.policy = policy;
    }

    public String getName() {
        return policy.getName();
    }

    public LifecyclePolicy getLifecyclePolicy() {
        return policy;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field("policy", policy);
        builder.endObject();
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PutLifecyclePolicyRequest that = (PutLifecyclePolicyRequest) o;
        return Objects.equals(getLifecyclePolicy(), that.getLifecyclePolicy());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLifecyclePolicy());
    }
}
