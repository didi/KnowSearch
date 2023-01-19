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

package org.elasticsearch.cluster.routing.allocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ESAllocationTestCase;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.RoutingNodes;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.common.settings.Settings;

import static org.elasticsearch.cluster.routing.ShardRoutingState.INITIALIZING;
import static org.elasticsearch.cluster.routing.ShardRoutingState.STARTED;
import static org.hamcrest.Matchers.equalTo;

public class PrimaryNotRelocatedWhileBeingRecoveredTests extends ESAllocationTestCase {
    private final Logger logger = LogManager.getLogger(PrimaryNotRelocatedWhileBeingRecoveredTests.class);

    public void testPrimaryNotRelocatedWhileBeingRecoveredFrom() {
        AllocationService strategy = createAllocationService(Settings.builder()
                .put("cluster.routing.allocation.node_concurrent_recoveries", 10)
                .put("cluster.routing.allocation.concurrent_source_recoveries", 10)
                .put("cluster.routing.allocation.node_initial_primaries_recoveries", 10)
                .build());

        logger.info("Building initial routing table");

        MetaData metaData = MetaData.builder()
                .put(IndexMetaData.builder("test").settings(settings(Version.CURRENT)).numberOfShards(5).numberOfReplicas(1))
                .build();

        RoutingTable initialRoutingTable = RoutingTable.builder()
                .addAsNew(metaData.index("test"))
                .build();

        ClusterState clusterState = ClusterState.builder(org.elasticsearch.cluster.ClusterName.CLUSTER_NAME_SETTING
            .getDefault(Settings.EMPTY)).metaData(metaData).routingTable(initialRoutingTable).build();

        logger.info("Adding two nodes and performing rerouting");
        clusterState = ClusterState.builder(clusterState).nodes(DiscoveryNodes.builder().add(newNode("node1"))).build();
        clusterState = strategy.reroute(clusterState, "reroute");

        logger.info("Start the primary shard (on node1)");
        RoutingNodes routingNodes = clusterState.getRoutingNodes();
        clusterState = startInitializingShardsAndReroute(strategy, clusterState, routingNodes.node("node1"));

        assertThat(clusterState.routingTable().shardsWithState(STARTED).size(), equalTo(5));

        logger.info("start another node, replica will start recovering form primary");
        clusterState = ClusterState.builder(clusterState)
            .nodes(DiscoveryNodes.builder(clusterState.nodes()).add(newNode("node2"))).build();
        clusterState = strategy.reroute(clusterState, "reroute");

        assertThat(clusterState.routingTable().shardsWithState(STARTED).size(), equalTo(5));
        assertThat(clusterState.routingTable().shardsWithState(INITIALIZING).size(), equalTo(5));

        logger.info("start another node, make sure the primary is not relocated");
        clusterState = ClusterState.builder(clusterState)
            .nodes(DiscoveryNodes.builder(clusterState.nodes()).add(newNode("node3"))).build();
        clusterState = strategy.reroute(clusterState, "reroute");

        assertThat(clusterState.routingTable().shardsWithState(STARTED).size(), equalTo(5));
        assertThat(clusterState.routingTable().shardsWithState(INITIALIZING).size(), equalTo(5));
    }
}
