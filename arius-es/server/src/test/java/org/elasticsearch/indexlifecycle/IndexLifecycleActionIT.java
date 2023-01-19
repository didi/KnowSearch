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

package org.elasticsearch.indexlifecycle;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.routing.RoutingNode;
import org.elasticsearch.cluster.routing.RoutingNodes;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;
import org.elasticsearch.test.ESIntegTestCase.Scope;
import org.elasticsearch.test.InternalTestCluster;
import org.elasticsearch.transport.TransportService;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.client.Requests.clusterHealthRequest;
import static org.elasticsearch.client.Requests.createIndexRequest;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_SHARDS;
import static org.elasticsearch.cluster.routing.ShardRoutingState.INITIALIZING;
import static org.elasticsearch.cluster.routing.ShardRoutingState.RELOCATING;
import static org.elasticsearch.cluster.routing.ShardRoutingState.STARTED;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;


@ClusterScope(scope = Scope.TEST, numDataNodes = 0)
public class IndexLifecycleActionIT extends ESIntegTestCase {
    public void testIndexLifecycleActionsWith11Shards1Backup() throws Exception {
        Settings settings = Settings.builder()
                .put(indexSettings())
                .put(SETTING_NUMBER_OF_SHARDS, 11)
                .put(SETTING_NUMBER_OF_REPLICAS, 1)
                .build();

        // start one server
        logger.info("Starting sever1");
        final String server_1 = internalCluster().startNode();
        final String node1 = getLocalNodeId(server_1);

        logger.info("Creating index [test]");
        CreateIndexResponse createIndexResponse = client().admin().indices().create(
            createIndexRequest("test").settings(settings)).actionGet();
        assertAcked(createIndexResponse);

        ClusterState clusterState = client().admin().cluster().prepareState().get().getState();
        RoutingNode routingNodeEntry1 = clusterState.getRoutingNodes().node(node1);
        assertThat(routingNodeEntry1.numberOfShardsWithState(STARTED), equalTo(11));

        logger.info("Starting server2");
        // start another server
        String server_2 = internalCluster().startNode();

        // first wait for 2 nodes in the cluster
        logger.info("Waiting for replicas to be assigned");
        ClusterHealthResponse clusterHealth = client().admin().cluster().prepareHealth()
            .setWaitForGreenStatus().setWaitForNodes("2").setWaitForNoRelocatingShards(true).setWaitForEvents(Priority.LANGUID).get();
        logger.info("Done Cluster Health, status {}", clusterHealth.getStatus());
        assertThat(clusterHealth.isTimedOut(), equalTo(false));
        assertThat(clusterHealth.getStatus(), equalTo(ClusterHealthStatus.GREEN));
        final String node2 = getLocalNodeId(server_2);

        // explicitly call reroute, so shards will get relocated to the new node (we delay it in ES in case other nodes join)
        client().admin().cluster().prepareReroute().execute().actionGet();

        clusterHealth = client().admin().cluster().health(
            clusterHealthRequest().waitForGreenStatus().waitForNodes("2").waitForNoRelocatingShards(true)).actionGet();
        assertThat(clusterHealth.isTimedOut(), equalTo(false));
        assertThat(clusterHealth.getStatus(), equalTo(ClusterHealthStatus.GREEN));
        assertThat(clusterHealth.getNumberOfDataNodes(), equalTo(2));
        assertThat(clusterHealth.getInitializingShards(), equalTo(0));
        assertThat(clusterHealth.getUnassignedShards(), equalTo(0));
        assertThat(clusterHealth.getRelocatingShards(), equalTo(0));
        assertThat(clusterHealth.getActiveShards(), equalTo(22));
        assertThat(clusterHealth.getActivePrimaryShards(), equalTo(11));


        clusterState = client().admin().cluster().prepareState().get().getState();
        assertNodesPresent(clusterState.getRoutingNodes(), node1, node2);
        routingNodeEntry1 = clusterState.getRoutingNodes().node(node1);
        assertThat(routingNodeEntry1.numberOfShardsWithState(RELOCATING), equalTo(0));
        assertThat(routingNodeEntry1.numberOfShardsWithState(STARTED), equalTo(11));
        RoutingNode routingNodeEntry2 = clusterState.getRoutingNodes().node(node2);
        assertThat(routingNodeEntry2.numberOfShardsWithState(INITIALIZING), equalTo(0));
        assertThat(routingNodeEntry2.numberOfShardsWithState(STARTED), equalTo(11));

        logger.info("Starting server3");
        // start another server
        String server_3 = internalCluster().startNode();

        // first wait for 3 nodes in the cluster
        logger.info("Waiting for replicas to be assigned");
        clusterHealth = client().admin().cluster().prepareHealth()
            .setWaitForGreenStatus().setWaitForNodes("3").setWaitForNoRelocatingShards(true).setWaitForEvents(Priority.LANGUID).get();
        assertThat(clusterHealth.isTimedOut(), equalTo(false));
        assertThat(clusterHealth.getStatus(), equalTo(ClusterHealthStatus.GREEN));


        final String node3 = getLocalNodeId(server_3);


        // explicitly call reroute, so shards will get relocated to the new node (we delay it in ES in case other nodes join)
        client().admin().cluster().prepareReroute().execute().actionGet();

        clusterHealth = client().admin().cluster().prepareHealth()
            .setWaitForGreenStatus().setWaitForNodes("3").setWaitForNoRelocatingShards(true).setWaitForEvents(Priority.LANGUID).get();
        assertThat(clusterHealth.isTimedOut(), equalTo(false));
        assertThat(clusterHealth.getStatus(), equalTo(ClusterHealthStatus.GREEN));
        assertThat(clusterHealth.getNumberOfDataNodes(), equalTo(3));
        assertThat(clusterHealth.getInitializingShards(), equalTo(0));
        assertThat(clusterHealth.getUnassignedShards(), equalTo(0));
        assertThat(clusterHealth.getRelocatingShards(), equalTo(0));
        assertThat(clusterHealth.getActiveShards(), equalTo(22));
        assertThat(clusterHealth.getActivePrimaryShards(), equalTo(11));


        clusterState = client().admin().cluster().prepareState().get().getState();
        assertNodesPresent(clusterState.getRoutingNodes(), node1, node2, node3);

        routingNodeEntry1 = clusterState.getRoutingNodes().node(node1);
        routingNodeEntry2 = clusterState.getRoutingNodes().node(node2);
        RoutingNode routingNodeEntry3 = clusterState.getRoutingNodes().node(node3);

        assertThat(routingNodeEntry1.numberOfShardsWithState(STARTED) +
            routingNodeEntry2.numberOfShardsWithState(STARTED) +
            routingNodeEntry3.numberOfShardsWithState(STARTED), equalTo(22));

        assertThat(routingNodeEntry1.numberOfShardsWithState(RELOCATING), equalTo(0));
        assertThat(routingNodeEntry1.numberOfShardsWithState(STARTED), anyOf(equalTo(7), equalTo(8)));

        assertThat(routingNodeEntry2.numberOfShardsWithState(RELOCATING), equalTo(0));
        assertThat(routingNodeEntry2.numberOfShardsWithState(STARTED), anyOf(equalTo(7), equalTo(8)));

        assertThat(routingNodeEntry3.numberOfShardsWithState(INITIALIZING), equalTo(0));
        assertThat(routingNodeEntry3.numberOfShardsWithState(STARTED), equalTo(7));

        logger.info("Closing server1");
        // kill the first server
        internalCluster().stopRandomNode(InternalTestCluster.nameFilter(server_1));
        // verify health
        logger.info("Running Cluster Health");
        clusterHealth = client().admin().cluster().prepareHealth()
            .setWaitForGreenStatus().setWaitForNodes("2").setWaitForNoRelocatingShards(true).setWaitForEvents(Priority.LANGUID).get();
        logger.info("Done Cluster Health, status {}", clusterHealth.getStatus());
        assertThat(clusterHealth.isTimedOut(), equalTo(false));
        assertThat(clusterHealth.getStatus(), equalTo(ClusterHealthStatus.GREEN));

        client().admin().cluster().prepareReroute().get();

        clusterHealth = client().admin().cluster().prepareHealth()
            .setWaitForGreenStatus().setWaitForNodes("2").setWaitForNoRelocatingShards(true).setWaitForEvents(Priority.LANGUID).get();
        assertThat(clusterHealth.isTimedOut(), equalTo(false));
        assertThat(clusterHealth.getStatus(), equalTo(ClusterHealthStatus.GREEN));
        assertThat(clusterHealth.getRelocatingShards(), equalTo(0));
        assertThat(clusterHealth.getActiveShards(), equalTo(22));
        assertThat(clusterHealth.getActivePrimaryShards(), equalTo(11));

        clusterState = client().admin().cluster().prepareState().get().getState();
        assertNodesPresent(clusterState.getRoutingNodes(), node3, node2);
        routingNodeEntry2 = clusterState.getRoutingNodes().node(node2);
        routingNodeEntry3 = clusterState.getRoutingNodes().node(node3);

        assertThat(routingNodeEntry2.numberOfShardsWithState(STARTED) + routingNodeEntry3.numberOfShardsWithState(STARTED), equalTo(22));

        assertThat(routingNodeEntry2.numberOfShardsWithState(RELOCATING), equalTo(0));
        assertThat(routingNodeEntry2.numberOfShardsWithState(STARTED), equalTo(11));

        assertThat(routingNodeEntry3.numberOfShardsWithState(RELOCATING), equalTo(0));
        assertThat(routingNodeEntry3.numberOfShardsWithState(STARTED), equalTo(11));


        logger.info("Deleting index [test]");
        // last, lets delete the index
        AcknowledgedResponse deleteIndexResponse = client().admin().indices().prepareDelete("test").execute().actionGet();
        assertThat(deleteIndexResponse.isAcknowledged(), equalTo(true));

        clusterState = client().admin().cluster().prepareState().get().getState();
        assertNodesPresent(clusterState.getRoutingNodes(), node3, node2);
        routingNodeEntry2 = clusterState.getRoutingNodes().node(node2);
        assertThat(routingNodeEntry2.isEmpty(), equalTo(true));

        routingNodeEntry3 = clusterState.getRoutingNodes().node(node3);
        assertThat(routingNodeEntry3.isEmpty(), equalTo(true));
    }

    private String getLocalNodeId(String name) {
        TransportService transportService = internalCluster().getInstance(TransportService.class, name);
        String nodeId = transportService.getLocalNode().getId();
        assertThat(nodeId, not(nullValue()));
        return nodeId;
    }

    private void assertNodesPresent(RoutingNodes routingNodes, String... nodes) {
        final Set<String> keySet = StreamSupport.stream(routingNodes.spliterator(), false)
            .map(RoutingNode::nodeId)
            .collect(Collectors.toSet());
        assertThat(keySet, containsInAnyOrder(nodes));
    }
}
