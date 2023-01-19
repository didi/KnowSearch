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

package org.elasticsearch.client.slm;

import org.elasticsearch.client.TimedRequest;

import java.util.Objects;

public class DeleteSnapshotLifecyclePolicyRequest extends TimedRequest {
    private final String policyId;

    public DeleteSnapshotLifecyclePolicyRequest(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyId() {
        return this.policyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteSnapshotLifecyclePolicyRequest other = (DeleteSnapshotLifecyclePolicyRequest) o;
        return this.policyId.equals(other.policyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.policyId);
    }
}
