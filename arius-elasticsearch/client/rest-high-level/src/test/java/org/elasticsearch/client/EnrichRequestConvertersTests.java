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
import org.elasticsearch.client.enrich.DeletePolicyRequest;
import org.elasticsearch.client.enrich.ExecutePolicyRequest;
import org.elasticsearch.client.enrich.GetPolicyRequest;
import org.elasticsearch.client.enrich.PutPolicyRequest;
import org.elasticsearch.client.enrich.PutPolicyRequestTests;
import org.elasticsearch.client.enrich.StatsRequest;
import org.elasticsearch.test.ESTestCase;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class EnrichRequestConvertersTests extends ESTestCase {

    public void testPutPolicy() throws Exception {
        PutPolicyRequest request = PutPolicyRequestTests.createTestInstance();
        Request result = EnrichRequestConverters.putPolicy(request);

        assertThat(result.getMethod(), equalTo(HttpPut.METHOD_NAME));
        assertThat(result.getEndpoint(), equalTo("/_enrich/policy/" + request.getName()));
        assertThat(result.getParameters().size(), equalTo(0));
        RequestConvertersTests.assertToXContentBody(request, result.getEntity());
    }

    public void testDeletePolicy() {
        DeletePolicyRequest request = new DeletePolicyRequest(randomAlphaOfLength(4));
        Request result = EnrichRequestConverters.deletePolicy(request);

        assertThat(result.getMethod(), equalTo(HttpDelete.METHOD_NAME));
        assertThat(result.getEndpoint(), equalTo("/_enrich/policy/" + request.getName()));
        assertThat(result.getParameters().size(), equalTo(0));
        assertThat(result.getEntity(), nullValue());
    }

    public void testGetPolicy() {
        GetPolicyRequest request = new GetPolicyRequest(randomAlphaOfLength(4));
        Request result = EnrichRequestConverters.getPolicy(request);

        assertThat(result.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(result.getEndpoint(), equalTo("/_enrich/policy/" + request.getNames().get(0)));
        assertThat(result.getParameters().size(), equalTo(0));
        assertThat(result.getEntity(), nullValue());

        request = new GetPolicyRequest(randomAlphaOfLength(4), randomAlphaOfLength(4));
        result = EnrichRequestConverters.getPolicy(request);

        assertThat(result.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(result.getEndpoint(), equalTo("/_enrich/policy/" + request.getNames().get(0) + "," + request.getNames().get(1)));
        assertThat(result.getParameters().size(), equalTo(0));
        assertThat(result.getEntity(), nullValue());

        request = new GetPolicyRequest();
        result = EnrichRequestConverters.getPolicy(request);

        assertThat(result.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(result.getEndpoint(), equalTo("/_enrich/policy"));
        assertThat(result.getParameters().size(), equalTo(0));
        assertThat(result.getEntity(), nullValue());
    }

    public void testStats() {
        StatsRequest request = new StatsRequest();
        Request result = EnrichRequestConverters.stats(request);

        assertThat(result.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(result.getEndpoint(), equalTo("/_enrich/_stats"));
        assertThat(result.getParameters().size(), equalTo(0));
        assertThat(result.getEntity(), nullValue());
    }

    public void testExecutePolicy() {
        ExecutePolicyRequest request = new ExecutePolicyRequest(randomAlphaOfLength(4));
        Request result = EnrichRequestConverters.executePolicy(request);

        assertThat(result.getMethod(), equalTo(HttpPost.METHOD_NAME));
        assertThat(result.getEndpoint(), equalTo("/_enrich/policy/" + request.getName() + "/_execute"));
        assertThat(result.getParameters().size(), equalTo(0));
        assertThat(result.getEntity(), nullValue());

        request = new ExecutePolicyRequest(randomAlphaOfLength(4));
        request.setWaitForCompletion(randomBoolean());
        result = EnrichRequestConverters.executePolicy(request);

        assertThat(result.getMethod(), equalTo(HttpPost.METHOD_NAME));
        assertThat(result.getEndpoint(), equalTo("/_enrich/policy/" + request.getName() + "/_execute"));
        assertThat(result.getParameters().size(), equalTo(1));
        assertThat(result.getParameters().get("wait_for_completion"), equalTo(request.getWaitForCompletion().toString()));
        assertThat(result.getEntity(), nullValue());
    }

}
