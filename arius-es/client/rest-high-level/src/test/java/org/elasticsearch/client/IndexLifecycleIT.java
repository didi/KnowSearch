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

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.client.core.AcknowledgedResponse;
import org.elasticsearch.client.indexlifecycle.AllocateAction;
import org.elasticsearch.client.indexlifecycle.DeleteAction;
import org.elasticsearch.client.indexlifecycle.DeleteLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.ExplainLifecycleRequest;
import org.elasticsearch.client.indexlifecycle.ExplainLifecycleResponse;
import org.elasticsearch.client.indexlifecycle.ForceMergeAction;
import org.elasticsearch.client.indexlifecycle.GetLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.GetLifecyclePolicyResponse;
import org.elasticsearch.client.indexlifecycle.IndexLifecycleExplainResponse;
import org.elasticsearch.client.indexlifecycle.LifecycleAction;
import org.elasticsearch.client.indexlifecycle.LifecycleManagementStatusRequest;
import org.elasticsearch.client.indexlifecycle.LifecycleManagementStatusResponse;
import org.elasticsearch.client.indexlifecycle.LifecyclePolicy;
import org.elasticsearch.client.indexlifecycle.LifecyclePolicyMetadata;
import org.elasticsearch.client.indexlifecycle.OperationMode;
import org.elasticsearch.client.indexlifecycle.Phase;
import org.elasticsearch.client.indexlifecycle.PhaseExecutionInfo;
import org.elasticsearch.client.indexlifecycle.PutLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.RemoveIndexLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.RemoveIndexLifecyclePolicyResponse;
import org.elasticsearch.client.indexlifecycle.RetryLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.RolloverAction;
import org.elasticsearch.client.indexlifecycle.ShrinkAction;
import org.elasticsearch.client.indexlifecycle.StartILMRequest;
import org.elasticsearch.client.indexlifecycle.StopILMRequest;
import org.elasticsearch.client.indexlifecycle.UnfollowAction;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.client.indexlifecycle.LifecyclePolicyTests.createRandomPolicy;
import static org.elasticsearch.test.ClientAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class IndexLifecycleIT extends ESRestHighLevelClientTestCase {

    public void testRemoveIndexLifecyclePolicy() throws Exception {
        String policyName = randomAlphaOfLength(10);
        LifecyclePolicy policy = createRandomPolicy(policyName);
        PutLifecyclePolicyRequest putRequest = new PutLifecyclePolicyRequest(policy);
        assertAcked(execute(putRequest, highLevelClient().indexLifecycle()::putLifecyclePolicy,
                highLevelClient().indexLifecycle()::putLifecyclePolicyAsync));

        createIndex("foo", Settings.builder().put("index.lifecycle.name", policyName).build());
        createIndex("baz", Settings.builder().put("index.lifecycle.name", policyName).build());
        createIndex("rbh", Settings.builder().put("index.lifecycle.name", policyName).build());

        GetSettingsRequest getSettingsRequest = new GetSettingsRequest().indices("foo", "baz", "rbh");
        GetSettingsResponse settingsResponse = highLevelClient().indices().getSettings(getSettingsRequest, RequestOptions.DEFAULT);
        assertThat(settingsResponse.getSetting("foo", "index.lifecycle.name"), equalTo(policyName));
        assertThat(settingsResponse.getSetting("baz", "index.lifecycle.name"), equalTo(policyName));
        assertThat(settingsResponse.getSetting("rbh", "index.lifecycle.name"), equalTo(policyName));

        List<String> indices = new ArrayList<>();
        indices.add("foo");
        indices.add("rbh");
        RemoveIndexLifecyclePolicyRequest removeReq = new RemoveIndexLifecyclePolicyRequest(indices);
        RemoveIndexLifecyclePolicyResponse removeResp = execute(removeReq, highLevelClient().indexLifecycle()::removeIndexLifecyclePolicy,
                highLevelClient().indexLifecycle()::removeIndexLifecyclePolicyAsync);
        assertThat(removeResp.hasFailures(), is(false));
        assertThat(removeResp.getFailedIndexes().isEmpty(), is(true));

        getSettingsRequest = new GetSettingsRequest().indices("foo", "baz", "rbh");
        settingsResponse = highLevelClient().indices().getSettings(getSettingsRequest, RequestOptions.DEFAULT);
        assertNull(settingsResponse.getSetting("foo", "index.lifecycle.name"));
        assertThat(settingsResponse.getSetting("baz", "index.lifecycle.name"), equalTo(policyName));
        assertNull(settingsResponse.getSetting("rbh", "index.lifecycle.name"));
    }

    public void testStartStopILM() throws Exception {
        String policyName = randomAlphaOfLength(10);
        LifecyclePolicy policy = createRandomPolicy(policyName);
        PutLifecyclePolicyRequest putRequest = new PutLifecyclePolicyRequest(policy);
        assertAcked(execute(putRequest, highLevelClient().indexLifecycle()::putLifecyclePolicy,
            highLevelClient().indexLifecycle()::putLifecyclePolicyAsync));

        createIndex("foo", Settings.builder().put("index.lifecycle.name", "bar").build());
        createIndex("baz", Settings.builder().put("index.lifecycle.name", "eggplant").build());
        createIndex("squash", Settings.EMPTY);

        LifecycleManagementStatusRequest statusRequest = new LifecycleManagementStatusRequest();
        LifecycleManagementStatusResponse statusResponse = execute(
            statusRequest,
            highLevelClient().indexLifecycle()::lifecycleManagementStatus,
            highLevelClient().indexLifecycle()::lifecycleManagementStatusAsync);
        assertEquals(statusResponse.getOperationMode(), OperationMode.RUNNING);

        StopILMRequest stopReq = new StopILMRequest();
        AcknowledgedResponse stopResponse = execute(stopReq, highLevelClient().indexLifecycle()::stopILM,
                highLevelClient().indexLifecycle()::stopILMAsync);
        assertTrue(stopResponse.isAcknowledged());


        statusResponse = execute(statusRequest, highLevelClient().indexLifecycle()::lifecycleManagementStatus,
            highLevelClient().indexLifecycle()::lifecycleManagementStatusAsync);
        assertThat(statusResponse.getOperationMode(),
                Matchers.anyOf(equalTo(OperationMode.STOPPING),
                    equalTo(OperationMode.STOPPED)));

        StartILMRequest startReq = new StartILMRequest();
        AcknowledgedResponse startResponse = execute(startReq, highLevelClient().indexLifecycle()::startILM,
                highLevelClient().indexLifecycle()::startILMAsync);
        assertTrue(startResponse.isAcknowledged());

        statusResponse = execute(statusRequest, highLevelClient().indexLifecycle()::lifecycleManagementStatus,
            highLevelClient().indexLifecycle()::lifecycleManagementStatusAsync);
        assertEquals(statusResponse.getOperationMode(), OperationMode.RUNNING);
    }

    public void testExplainLifecycle() throws Exception {
        Map<String, Phase> lifecyclePhases = new HashMap<>();
        Map<String, LifecycleAction> hotActions = new HashMap<>();
        hotActions.put(RolloverAction.NAME, new RolloverAction(null, TimeValue.timeValueHours(50 * 24), null));
        Phase hotPhase = new Phase("hot", randomFrom(TimeValue.ZERO, null), hotActions);
        lifecyclePhases.put("hot", hotPhase);

        Map<String, LifecycleAction> warmActions = new HashMap<>();
        warmActions.put(UnfollowAction.NAME, new UnfollowAction());
        warmActions.put(AllocateAction.NAME, new AllocateAction(null, null, null, Collections.singletonMap("_name", "node-1")));
        warmActions.put(ShrinkAction.NAME, new ShrinkAction(1));
        warmActions.put(ForceMergeAction.NAME, new ForceMergeAction(1000));
        lifecyclePhases.put("warm", new Phase("warm", TimeValue.timeValueSeconds(1000), warmActions));

        Map<String, LifecycleAction> coldActions = new HashMap<>();
        coldActions.put(UnfollowAction.NAME, new UnfollowAction());
        coldActions.put(AllocateAction.NAME, new AllocateAction(0, null, null, null));
        lifecyclePhases.put("cold", new Phase("cold", TimeValue.timeValueSeconds(2000), coldActions));

        Map<String, LifecycleAction> deleteActions = Collections.singletonMap(DeleteAction.NAME, new DeleteAction());
        lifecyclePhases.put("delete", new Phase("delete", TimeValue.timeValueSeconds(3000), deleteActions));

        LifecyclePolicy policy = new LifecyclePolicy(randomAlphaOfLength(10), lifecyclePhases);
        PutLifecyclePolicyRequest putRequest = new PutLifecyclePolicyRequest(policy);
        AcknowledgedResponse putResponse = execute(putRequest, highLevelClient().indexLifecycle()::putLifecyclePolicy,
            highLevelClient().indexLifecycle()::putLifecyclePolicyAsync);
        assertTrue(putResponse.isAcknowledged());
        GetLifecyclePolicyRequest getRequest = new GetLifecyclePolicyRequest(policy.getName());
        GetLifecyclePolicyResponse getResponse = execute(getRequest, highLevelClient().indexLifecycle()::getLifecyclePolicy,
            highLevelClient().indexLifecycle()::getLifecyclePolicyAsync);
        long expectedPolicyModifiedDate = getResponse.getPolicies().get(policy.getName()).getModifiedDate();


        createIndex("foo-01", Settings.builder().put("index.lifecycle.name", policy.getName())
            .put("index.lifecycle.rollover_alias", "foo-alias").build(), "", "\"foo-alias\" : {}");

        createIndex("baz-01", Settings.builder().put("index.lifecycle.name", policy.getName())
            .put("index.lifecycle.rollover_alias", "baz-alias").build(), "", "\"baz-alias\" : {}");

        createIndex("squash", Settings.EMPTY);

        // The injected Unfollow step will run pretty rapidly here, so we need
        // to wait for it to settle into the "stable" step of waiting to be
        // ready to roll over
        assertBusy(() -> {
            ExplainLifecycleRequest req = new ExplainLifecycleRequest("foo-01", "baz-01", "squash");
            ExplainLifecycleResponse response = execute(req, highLevelClient().indexLifecycle()::explainLifecycle,
                highLevelClient().indexLifecycle()::explainLifecycleAsync);
            Map<String, IndexLifecycleExplainResponse> indexResponses = response.getIndexResponses();
            assertEquals(3, indexResponses.size());
            IndexLifecycleExplainResponse fooResponse = indexResponses.get("foo-01");
            assertNotNull(fooResponse);
            assertTrue(fooResponse.managedByILM());
            assertEquals("foo-01", fooResponse.getIndex());
            assertEquals("hot", fooResponse.getPhase());
            assertEquals("rollover", fooResponse.getAction());
            assertEquals("check-rollover-ready", fooResponse.getStep());
            assertEquals(new PhaseExecutionInfo(policy.getName(), new Phase("", hotPhase.getMinimumAge(), hotPhase.getActions()),
                1L, expectedPolicyModifiedDate), fooResponse.getPhaseExecutionInfo());
            IndexLifecycleExplainResponse bazResponse = indexResponses.get("baz-01");
            assertNotNull(bazResponse);
            assertTrue(bazResponse.managedByILM());
            assertEquals("baz-01", bazResponse.getIndex());
            assertEquals("hot", bazResponse.getPhase());
            assertEquals("rollover", bazResponse.getAction());
            assertEquals("check-rollover-ready", bazResponse.getStep());
            IndexLifecycleExplainResponse squashResponse = indexResponses.get("squash");
            assertNotNull(squashResponse);
            assertFalse(squashResponse.managedByILM());
            assertEquals("squash", squashResponse.getIndex());

        });
    }

    public void testDeleteLifecycle() throws IOException {
        String policyName = randomAlphaOfLength(10);
        LifecyclePolicy policy = createRandomPolicy(policyName);
        PutLifecyclePolicyRequest putRequest = new PutLifecyclePolicyRequest(policy);
        assertAcked(execute(putRequest, highLevelClient().indexLifecycle()::putLifecyclePolicy,
            highLevelClient().indexLifecycle()::putLifecyclePolicyAsync));

        DeleteLifecyclePolicyRequest deleteRequest = new DeleteLifecyclePolicyRequest(policy.getName());
        assertAcked(execute(deleteRequest, highLevelClient().indexLifecycle()::deleteLifecyclePolicy,
            highLevelClient().indexLifecycle()::deleteLifecyclePolicyAsync));

        GetLifecyclePolicyRequest getRequest = new GetLifecyclePolicyRequest(policyName);
        ElasticsearchStatusException ex = expectThrows(ElasticsearchStatusException.class,
            () -> execute(getRequest, highLevelClient().indexLifecycle()::getLifecyclePolicy,
                highLevelClient().indexLifecycle()::getLifecyclePolicyAsync));
        assertEquals(404, ex.status().getStatus());
    }

    public void testPutLifecycle() throws IOException {
        String name = randomAlphaOfLengthBetween(5, 20);
        LifecyclePolicy policy = createRandomPolicy(name);
        PutLifecyclePolicyRequest putRequest = new PutLifecyclePolicyRequest(policy);

        assertAcked(execute(putRequest, highLevelClient().indexLifecycle()::putLifecyclePolicy,
            highLevelClient().indexLifecycle()::putLifecyclePolicyAsync));

        GetLifecyclePolicyRequest getRequest = new GetLifecyclePolicyRequest(name);
        GetLifecyclePolicyResponse response = execute(getRequest, highLevelClient().indexLifecycle()::getLifecyclePolicy,
            highLevelClient().indexLifecycle()::getLifecyclePolicyAsync);
        assertEquals(policy, response.getPolicies().get(name).getPolicy());
    }

    public void testGetMultipleLifecyclePolicies() throws IOException {
        int numPolicies = randomIntBetween(1, 10);
        String[] policyNames = new String[numPolicies];
        LifecyclePolicy[] policies = new LifecyclePolicy[numPolicies];
        for (int i = 0; i < numPolicies; i++) {
            policyNames[i] = "policy-" + randomAlphaOfLengthBetween(5, 10);
            policies[i] = createRandomPolicy(policyNames[i]);
            PutLifecyclePolicyRequest putRequest = new PutLifecyclePolicyRequest(policies[i]);
            assertAcked(execute(putRequest, highLevelClient().indexLifecycle()::putLifecyclePolicy,
                highLevelClient().indexLifecycle()::putLifecyclePolicyAsync));
        }

        GetLifecyclePolicyRequest getRequest = new GetLifecyclePolicyRequest(randomFrom(policyNames, null));
        GetLifecyclePolicyResponse response = execute(getRequest, highLevelClient().indexLifecycle()::getLifecyclePolicy,
            highLevelClient().indexLifecycle()::getLifecyclePolicyAsync);
        List<LifecyclePolicy> retrievedPolicies = Arrays.stream(response.getPolicies().values().toArray())
            .map(p -> ((LifecyclePolicyMetadata) p).getPolicy()).collect(Collectors.toList());
        assertThat(retrievedPolicies, hasItems(policies));
    }

    public void testRetryLifecycleStep() throws IOException {
        String policyName = randomAlphaOfLength(10);
        LifecyclePolicy policy = createRandomPolicy(policyName);
        PutLifecyclePolicyRequest putRequest = new PutLifecyclePolicyRequest(policy);
        assertAcked(execute(putRequest, highLevelClient().indexLifecycle()::putLifecyclePolicy,
            highLevelClient().indexLifecycle()::putLifecyclePolicyAsync));
        createIndex("retry", Settings.builder().put("index.lifecycle.name", policy.getName()).build());
        RetryLifecyclePolicyRequest retryRequest = new RetryLifecyclePolicyRequest("retry");
        ElasticsearchStatusException ex = expectThrows(ElasticsearchStatusException.class,
            () -> execute(
                retryRequest, highLevelClient().indexLifecycle()::retryLifecyclePolicy,
                highLevelClient().indexLifecycle()::retryLifecyclePolicyAsync
            )
        );
        assertEquals(400, ex.status().getStatus());
        assertEquals(
            "Elasticsearch exception [type=illegal_argument_exception, reason=cannot retry an action for an index [retry]" +
                " that has not encountered an error when running a Lifecycle Policy]",
            ex.getRootCause().getMessage()
        );
    }
}
