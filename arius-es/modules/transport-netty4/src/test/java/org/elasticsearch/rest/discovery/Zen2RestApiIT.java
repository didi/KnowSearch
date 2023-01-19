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

package org.elasticsearch.rest.discovery;

import org.apache.http.HttpHost;
import org.elasticsearch.ESNetty4IntegTestCase;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.routing.UnassignedInfo;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.http.HttpServerTransport;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.InternalTestCluster;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;

// These tests are here today so they have access to a proper REST client. They cannot be in :server:integTest since the REST client needs a
// proper transport implementation, and they cannot be REST tests today since they need to restart nodes. When #35599 and friends land we
// should be able to move these tests to run against a proper cluster instead. TODO do this.
@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.TEST, numDataNodes = 0, transportClientRatio = 0, autoManageMasterNodes = false)
public class Zen2RestApiIT extends ESNetty4IntegTestCase {

    @Override
    protected boolean addMockHttpTransport() {
        return false; // enable http
    }

    public void testRollingRestartOfTwoNodeCluster() throws Exception {
        internalCluster().setBootstrapMasterNodeIndex(1);
        final List<String> nodes = internalCluster().startNodes(2);
        createIndex("test",
            Settings.builder()
                .put(UnassignedInfo.INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING.getKey(), TimeValue.ZERO) // assign shards
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 2) // causes rebalancing
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 1)
                .build());
        ensureGreen("test");

        RestClient restClient = getRestClient();

        internalCluster().rollingRestart(new InternalTestCluster.RestartCallback() {
            @Override
            public void doAfterNodes(int n, Client client) throws IOException {
                ensureGreen("test");
                Response response =
                    restClient.performRequest(new Request("POST", "/_cluster/voting_config_exclusions/" +
                        internalCluster().getNodeNames()[n]));
                assertThat(response.getStatusLine().getStatusCode(), is(200));
            }

            @Override
            public Settings onNodeStopped(String nodeName) throws IOException {
                String viaNode = randomValueOtherThan(nodeName, () -> randomFrom(nodes));

                List<Node> allNodes = restClient.getNodes();
                try {
                    restClient.setNodes(
                        Collections.singletonList(
                            new Node(
                                HttpHost.create(
                                    internalCluster().getInstance(HttpServerTransport.class, viaNode)
                                        .boundAddress().publishAddress().toString()
                                )
                            )
                        )
                    );
                    Response deleteResponse = restClient.performRequest(new Request("DELETE", "/_cluster/voting_config_exclusions"));
                    assertThat(deleteResponse.getStatusLine().getStatusCode(), is(200));

                    ClusterHealthResponse clusterHealthResponse = client(viaNode).admin().cluster().prepareHealth()
                        .setWaitForEvents(Priority.LANGUID)
                        .setWaitForNodes(Integer.toString(1))
                        .setTimeout(TimeValue.timeValueSeconds(30L))
                        .setWaitForYellowStatus()
                        .get();
                    assertFalse(nodeName, clusterHealthResponse.isTimedOut());
                    return Settings.EMPTY;
                } finally {
                    restClient.setNodes(allNodes);
                }
            }
        });
        ensureStableCluster(2);
        ensureGreen("test");
        assertThat(internalCluster().size(), is(2));
    }

    public void testClearVotingTombstonesNotWaitingForRemoval() throws Exception {
        internalCluster().setBootstrapMasterNodeIndex(2);
        List<String> nodes = internalCluster().startNodes(3);
        ensureStableCluster(3);
        RestClient restClient = getRestClient();
        Response response = restClient.performRequest(new Request("POST", "/_cluster/voting_config_exclusions/" + nodes.get(2)));
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertThat(response.getEntity().getContentLength(), is(0L));
        Response deleteResponse = restClient.performRequest(
            new Request("DELETE", "/_cluster/voting_config_exclusions/?wait_for_removal=false"));
        assertThat(deleteResponse.getStatusLine().getStatusCode(), is(200));
        assertThat(deleteResponse.getEntity().getContentLength(), is(0L));
    }

    public void testClearVotingTombstonesWaitingForRemoval() throws Exception {
        internalCluster().setBootstrapMasterNodeIndex(2);
        List<String> nodes = internalCluster().startNodes(3);
        ensureStableCluster(3);
        RestClient restClient = getRestClient();
        String nodeToWithdraw = nodes.get(randomIntBetween(0, 2));
        Response response = restClient.performRequest(new Request("POST", "/_cluster/voting_config_exclusions/" + nodeToWithdraw));
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertThat(response.getEntity().getContentLength(), is(0L));
        internalCluster().stopRandomNode(InternalTestCluster.nameFilter(nodeToWithdraw));
        Response deleteResponse = restClient.performRequest(new Request("DELETE", "/_cluster/voting_config_exclusions"));
        assertThat(deleteResponse.getStatusLine().getStatusCode(), is(200));
        assertThat(deleteResponse.getEntity().getContentLength(), is(0L));
    }

    public void testFailsOnUnknownNode() throws Exception {
        internalCluster().setBootstrapMasterNodeIndex(2);
        internalCluster().startNodes(3);
        ensureStableCluster(3);
        RestClient restClient = getRestClient();
        try {
            restClient.performRequest(new Request("POST", "/_cluster/voting_config_exclusions/invalid"));
            fail("Invalid node name should throw.");
        } catch (ResponseException e) {
            assertThat(e.getResponse().getStatusLine().getStatusCode(), is(400));
            assertThat(
                e.getMessage(),
                Matchers.containsString("add voting config exclusions request for [invalid] matched no master-eligible nodes")
            );
        }
    }

    public void testRemoveTwoNodesAtOnce() throws Exception {
        internalCluster().setBootstrapMasterNodeIndex(2);
        List<String> nodes = internalCluster().startNodes(3);
        ensureStableCluster(3);
        RestClient restClient = getRestClient();
        Response response = restClient.performRequest(new Request("POST", "/_cluster/voting_config_exclusions/" +
            nodes.get(2) + "," + nodes.get(0)));
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertThat(response.getEntity().getContentLength(), is(0L));
        internalCluster().stopRandomNode(InternalTestCluster.nameFilter(nodes.get(0)));
        internalCluster().stopRandomNode(InternalTestCluster.nameFilter(nodes.get(2)));
        ensureStableCluster(1);
    }
}
