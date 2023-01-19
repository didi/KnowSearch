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

package org.elasticsearch.client;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.indexlifecycle.DeleteLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.ExplainLifecycleRequest;
import org.elasticsearch.client.indexlifecycle.GetLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.LifecycleManagementStatusRequest;
import org.elasticsearch.client.indexlifecycle.LifecyclePolicy;
import org.elasticsearch.client.indexlifecycle.PutLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.RemoveIndexLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.RetryLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.StartILMRequest;
import org.elasticsearch.client.indexlifecycle.StopILMRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.test.ESTestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.client.RequestConvertersTests.randomIndicesNames;
import static org.elasticsearch.client.RequestConvertersTests.setRandomIndicesOptions;
import static org.elasticsearch.client.RequestConvertersTests.setRandomMasterTimeout;
import static org.elasticsearch.client.RequestConvertersTests.setRandomTimeoutTimeValue;
import static org.elasticsearch.client.indexlifecycle.LifecyclePolicyTests.createRandomPolicy;
import static org.hamcrest.CoreMatchers.equalTo;

public class IndexLifecycleRequestConvertersTests extends ESTestCase {

    public void testGetLifecyclePolicy() {
        String[] policies = rarely() ? null : randomIndicesNames(0, 10);
        GetLifecyclePolicyRequest req = new GetLifecyclePolicyRequest(policies);
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(req::setMasterTimeout, TimedRequest.DEFAULT_MASTER_NODE_TIMEOUT, expectedParams);
        setRandomTimeoutTimeValue(req::setTimeout, TimedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request request = IndexLifecycleRequestConverters.getLifecyclePolicy(req);
        assertEquals(request.getMethod(), HttpGet.METHOD_NAME);
        String policiesStr = Strings.arrayToCommaDelimitedString(policies);
        assertEquals(request.getEndpoint(), "/_ilm/policy" + (policiesStr.isEmpty() ? "" : ("/" + policiesStr)));
        assertEquals(request.getParameters(), expectedParams);
    }

    public void testPutLifecyclePolicy() throws Exception {
        String name = randomAlphaOfLengthBetween(2, 20);
        LifecyclePolicy policy = createRandomPolicy(name);
        PutLifecyclePolicyRequest req = new PutLifecyclePolicyRequest(policy);
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(req::setMasterTimeout, TimedRequest.DEFAULT_MASTER_NODE_TIMEOUT, expectedParams);
        setRandomTimeoutTimeValue(req::setTimeout, TimedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request request = IndexLifecycleRequestConverters.putLifecyclePolicy(req);
        assertEquals(HttpPut.METHOD_NAME, request.getMethod());
        assertEquals("/_ilm/policy/" + name, request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
    }

    public void testDeleteLifecycle() {
        String lifecycleName = randomAlphaOfLengthBetween(2,20);
        DeleteLifecyclePolicyRequest req = new DeleteLifecyclePolicyRequest(lifecycleName);
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(req::setMasterTimeout, TimedRequest.DEFAULT_MASTER_NODE_TIMEOUT, expectedParams);
        setRandomTimeoutTimeValue(req::setTimeout, TimedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request request = IndexLifecycleRequestConverters.deleteLifecyclePolicy(req);
        assertEquals(request.getMethod(), HttpDelete.METHOD_NAME);
        assertEquals(request.getEndpoint(), "/_ilm/policy/" + lifecycleName);
        assertEquals(request.getParameters(), expectedParams);
    }

    public void testRemoveIndexLifecyclePolicy() {
        Map<String, String> expectedParams = new HashMap<>();
        String[] indices = randomIndicesNames(0, 10);
        IndicesOptions indicesOptions = setRandomIndicesOptions(IndicesOptions.strictExpandOpen(), expectedParams);
        RemoveIndexLifecyclePolicyRequest req = new RemoveIndexLifecyclePolicyRequest(Arrays.asList(indices), indicesOptions);
        setRandomMasterTimeout(req::setMasterTimeout, TimedRequest.DEFAULT_MASTER_NODE_TIMEOUT, expectedParams);

        Request request = IndexLifecycleRequestConverters.removeIndexLifecyclePolicy(req);
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
        String idxString = Strings.arrayToCommaDelimitedString(indices);
        assertThat(request.getEndpoint(), equalTo("/" + (idxString.isEmpty() ? "" : (idxString + "/")) + "_ilm/remove"));
        assertThat(request.getParameters(), equalTo(expectedParams));
    }

    public void testStartILM() throws Exception {
        StartILMRequest req = new StartILMRequest();
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(req::setMasterTimeout, TimedRequest.DEFAULT_MASTER_NODE_TIMEOUT, expectedParams);
        setRandomTimeoutTimeValue(req::setTimeout, TimedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request request = IndexLifecycleRequestConverters.startILM(req);
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
        assertThat(request.getEndpoint(), equalTo("/_ilm/start"));
        assertThat(request.getParameters(), equalTo(expectedParams));
    }

    public void testStopILM() throws Exception {
        StopILMRequest req = new StopILMRequest();
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(req::setMasterTimeout, TimedRequest.DEFAULT_MASTER_NODE_TIMEOUT, expectedParams);
        setRandomTimeoutTimeValue(req::setTimeout, TimedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request request = IndexLifecycleRequestConverters.stopILM(req);
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
        assertThat(request.getEndpoint(), equalTo("/_ilm/stop"));
        assertThat(request.getParameters(), equalTo(expectedParams));
    }

    public void testLifecycleManagementStatus() throws Exception {
        LifecycleManagementStatusRequest req = new LifecycleManagementStatusRequest();
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(req::setMasterTimeout, TimedRequest.DEFAULT_MASTER_NODE_TIMEOUT, expectedParams);
        setRandomTimeoutTimeValue(req::setTimeout, TimedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request request = IndexLifecycleRequestConverters.lifecycleManagementStatus(req);
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request.getEndpoint(), equalTo("/_ilm/status"));
        assertThat(request.getParameters(), equalTo(expectedParams));
    }

    public void testExplainLifecycle() throws Exception {
        ExplainLifecycleRequest req = new ExplainLifecycleRequest(randomIndicesNames(1, 10));
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(req, expectedParams);
        setRandomIndicesOptions(req::indicesOptions, req::indicesOptions, expectedParams);

        Request request = IndexLifecycleRequestConverters.explainLifecycle(req);
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        String idxString = Strings.arrayToCommaDelimitedString(req.getIndices());
        assertThat(request.getEndpoint(), equalTo("/" + idxString + "/" + "_ilm/explain"));
        assertThat(request.getParameters(), equalTo(expectedParams));
    }

    public void testRetryLifecycle() throws Exception {
        String[] indices = randomIndicesNames(1, 10);
        RetryLifecyclePolicyRequest req = new RetryLifecyclePolicyRequest(indices);
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(req::setMasterTimeout, TimedRequest.DEFAULT_MASTER_NODE_TIMEOUT, expectedParams);
        setRandomTimeoutTimeValue(req::setTimeout, TimedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);
        Request request = IndexLifecycleRequestConverters.retryLifecycle(req);
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
        String idxString = Strings.arrayToCommaDelimitedString(indices);
        assertThat(request.getEndpoint(), equalTo("/" + (idxString.isEmpty() ? "" : (idxString + "/")) + "_ilm/retry"));
        assertThat(request.getParameters(), equalTo(expectedParams));
    }
}
