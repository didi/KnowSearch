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
package org.elasticsearch.cluster.serialization;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ESAllocationTestCase;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;

import java.util.Arrays;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.hamcrest.Matchers.containsString;

public class ClusterStateToStringTests extends ESAllocationTestCase {
    public void testClusterStateSerialization() throws Exception {
        MetaData metaData = MetaData.builder()
                .put(IndexMetaData.builder("test_idx").settings(settings(Version.CURRENT)).numberOfShards(10).numberOfReplicas(1))
                .put(IndexTemplateMetaData.builder("test_template")
                    .patterns(Arrays.asList(generateRandomStringArray(10, 100, false,false))).build())
                .build();

        RoutingTable routingTable = RoutingTable.builder()
                .addAsNew(metaData.index("test_idx"))
                .build();

        DiscoveryNodes nodes = DiscoveryNodes.builder().add(new DiscoveryNode("node_foo", buildNewFakeTransportAddress(),
                emptyMap(), emptySet(), Version.CURRENT)).localNodeId("node_foo").masterNodeId("node_foo").build();

        ClusterState clusterState = ClusterState.builder(ClusterName.CLUSTER_NAME_SETTING.getDefault(Settings.EMPTY)).nodes(nodes)
            .metaData(metaData).routingTable(routingTable).build();

        AllocationService strategy = createAllocationService();
        clusterState = ClusterState.builder(clusterState).routingTable(strategy.reroute(clusterState, "reroute").routingTable()).build();

        String clusterStateString = Strings.toString(clusterState);
        assertNotNull(clusterStateString);

        assertThat(clusterStateString, containsString("test_idx"));
        assertThat(clusterStateString, containsString("test_template"));
        assertThat(clusterStateString, containsString("node_foo"));
    }
}
