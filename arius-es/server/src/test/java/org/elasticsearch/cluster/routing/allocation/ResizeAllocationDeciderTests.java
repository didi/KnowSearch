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

import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ESAllocationTestCase;
import org.elasticsearch.cluster.EmptyClusterInfoService;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.RecoverySource;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.routing.TestShardRouting;
import org.elasticsearch.cluster.routing.allocation.allocator.BalancedShardsAllocator;
import org.elasticsearch.cluster.routing.allocation.decider.AllocationDeciders;
import org.elasticsearch.cluster.routing.allocation.decider.Decision;
import org.elasticsearch.cluster.routing.allocation.decider.ResizeAllocationDecider;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.test.gateway.TestGatewayAllocator;

import java.util.Collections;

import static org.elasticsearch.cluster.routing.ShardRoutingState.INITIALIZING;
import static org.elasticsearch.cluster.routing.ShardRoutingState.STARTED;
import static org.elasticsearch.cluster.routing.ShardRoutingState.UNASSIGNED;


public class ResizeAllocationDeciderTests extends ESAllocationTestCase {

    private AllocationService strategy;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        strategy = new AllocationService(new AllocationDeciders(
            Collections.singleton(new ResizeAllocationDecider())),
            new TestGatewayAllocator(), new BalancedShardsAllocator(Settings.EMPTY), EmptyClusterInfoService.INSTANCE);
    }

    private ClusterState createInitialClusterState(boolean startShards) {
        MetaData.Builder metaBuilder = MetaData.builder();
        metaBuilder.put(IndexMetaData.builder("source").settings(settings(Version.CURRENT))
            .numberOfShards(2).numberOfReplicas(0).setRoutingNumShards(16));
        MetaData metaData = metaBuilder.build();
        RoutingTable.Builder routingTableBuilder = RoutingTable.builder();
        routingTableBuilder.addAsNew(metaData.index("source"));

        RoutingTable routingTable = routingTableBuilder.build();
        ClusterState clusterState = ClusterState.builder(ClusterName.CLUSTER_NAME_SETTING.getDefault(Settings.EMPTY))
            .metaData(metaData).routingTable(routingTable).build();
        clusterState = ClusterState.builder(clusterState).nodes(DiscoveryNodes.builder().add(newNode("node1", Version.CURRENT)).add(newNode
            ("node2", Version.CURRENT)))
            .build();
        RoutingTable prevRoutingTable = routingTable;
        routingTable = strategy.reroute(clusterState, "reroute").routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();

        assertEquals(prevRoutingTable.index("source").shards().size(), 2);
        assertEquals(prevRoutingTable.index("source").shard(0).shards().get(0).state(), UNASSIGNED);
        assertEquals(prevRoutingTable.index("source").shard(1).shards().get(0).state(), UNASSIGNED);


        assertEquals(routingTable.index("source").shards().size(), 2);

        assertEquals(routingTable.index("source").shard(0).shards().get(0).state(), INITIALIZING);
        assertEquals(routingTable.index("source").shard(1).shards().get(0).state(), INITIALIZING);


        if (startShards) {
            clusterState = startShardsAndReroute(strategy, clusterState,
                routingTable.index("source").shard(0).shards().get(0),
                routingTable.index("source").shard(1).shards().get(0));
            routingTable = clusterState.routingTable();
            assertEquals(routingTable.index("source").shards().size(), 2);
            assertEquals(routingTable.index("source").shard(0).shards().get(0).state(), STARTED);
            assertEquals(routingTable.index("source").shard(1).shards().get(0).state(), STARTED);

        }
        return clusterState;
    }

    public void testNonResizeRouting() {
        ClusterState clusterState = createInitialClusterState(true);
        ResizeAllocationDecider resizeAllocationDecider = new ResizeAllocationDecider();
        RoutingAllocation routingAllocation = new RoutingAllocation(null, null, clusterState, null, 0);
        ShardRouting shardRouting = TestShardRouting.newShardRouting("non-resize", 0, null, true, ShardRoutingState.UNASSIGNED);
        assertEquals(Decision.ALWAYS, resizeAllocationDecider.canAllocate(shardRouting, routingAllocation));
        assertEquals(Decision.ALWAYS, resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node1"),
            routingAllocation));
    }

    public void testShrink() { // we don't handle shrink yet
        ClusterState clusterState = createInitialClusterState(true);
        MetaData.Builder metaBuilder = MetaData.builder(clusterState.metaData());
        metaBuilder.put(IndexMetaData.builder("target").settings(settings(Version.CURRENT)
            .put(IndexMetaData.INDEX_RESIZE_SOURCE_NAME.getKey(), "source")
            .put(IndexMetaData.INDEX_RESIZE_SOURCE_UUID_KEY, IndexMetaData.INDEX_UUID_NA_VALUE))
            .numberOfShards(1).numberOfReplicas(0));
        MetaData metaData = metaBuilder.build();
        RoutingTable.Builder routingTableBuilder = RoutingTable.builder(clusterState.routingTable());
        routingTableBuilder.addAsNew(metaData.index("target"));

        clusterState = ClusterState.builder(clusterState)
            .routingTable(routingTableBuilder.build())
            .metaData(metaData).build();
        Index idx = clusterState.metaData().index("target").getIndex();

        ResizeAllocationDecider resizeAllocationDecider = new ResizeAllocationDecider();
        RoutingAllocation routingAllocation = new RoutingAllocation(null, null, clusterState, null, 0);
        ShardRouting shardRouting = TestShardRouting.newShardRouting(new ShardId(idx, 0), null, true, ShardRoutingState.UNASSIGNED,
            RecoverySource.LocalShardsRecoverySource.INSTANCE);
        assertEquals(Decision.ALWAYS, resizeAllocationDecider.canAllocate(shardRouting, routingAllocation));
        assertEquals(Decision.ALWAYS, resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node1"),
            routingAllocation));
        assertEquals(Decision.ALWAYS, resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node2"),
            routingAllocation));
    }

    public void testSourceNotActive() {
        ClusterState clusterState = createInitialClusterState(false);
        MetaData.Builder metaBuilder = MetaData.builder(clusterState.metaData());
        metaBuilder.put(IndexMetaData.builder("target").settings(settings(Version.CURRENT)
            .put(IndexMetaData.INDEX_RESIZE_SOURCE_NAME.getKey(), "source")
            .put(IndexMetaData.INDEX_RESIZE_SOURCE_UUID_KEY, IndexMetaData.INDEX_UUID_NA_VALUE))
            .numberOfShards(4).numberOfReplicas(0));
        MetaData metaData = metaBuilder.build();
        RoutingTable.Builder routingTableBuilder = RoutingTable.builder(clusterState.routingTable());
        routingTableBuilder.addAsNew(metaData.index("target"));

        clusterState = ClusterState.builder(clusterState)
            .routingTable(routingTableBuilder.build())
            .metaData(metaData).build();
        Index idx = clusterState.metaData().index("target").getIndex();


        ResizeAllocationDecider resizeAllocationDecider = new ResizeAllocationDecider();
        RoutingAllocation routingAllocation = new RoutingAllocation(null, clusterState.getRoutingNodes(), clusterState, null, 0);
        int shardId = randomIntBetween(0, 3);
        int sourceShardId = IndexMetaData.selectSplitShard(shardId, clusterState.metaData().index("source"), 4).id();
        ShardRouting shardRouting = TestShardRouting.newShardRouting(new ShardId(idx, shardId), null, true, ShardRoutingState.UNASSIGNED,
            RecoverySource.LocalShardsRecoverySource.INSTANCE);
        assertEquals(Decision.NO, resizeAllocationDecider.canAllocate(shardRouting, routingAllocation));
        assertEquals(Decision.NO, resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node1"),
            routingAllocation));
        assertEquals(Decision.NO, resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node2"),
            routingAllocation));

        routingAllocation.debugDecision(true);
        assertEquals("source primary shard [[source][" + sourceShardId + "]] is not active",
            resizeAllocationDecider.canAllocate(shardRouting, routingAllocation).getExplanation());
        assertEquals("source primary shard [[source][" + sourceShardId + "]] is not active",
            resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node0"),
            routingAllocation).getExplanation());
        assertEquals("source primary shard [[source][" + sourceShardId + "]] is not active",
            resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node1"),
            routingAllocation).getExplanation());
    }

    public void testSourcePrimaryActive() {
        ClusterState clusterState = createInitialClusterState(true);
        MetaData.Builder metaBuilder = MetaData.builder(clusterState.metaData());
        metaBuilder.put(IndexMetaData.builder("target").settings(settings(Version.CURRENT)
            .put(IndexMetaData.INDEX_RESIZE_SOURCE_NAME.getKey(), "source")
            .put(IndexMetaData.INDEX_RESIZE_SOURCE_UUID_KEY, IndexMetaData.INDEX_UUID_NA_VALUE))
            .numberOfShards(4).numberOfReplicas(0));
        MetaData metaData = metaBuilder.build();
        RoutingTable.Builder routingTableBuilder = RoutingTable.builder(clusterState.routingTable());
        routingTableBuilder.addAsNew(metaData.index("target"));

        clusterState = ClusterState.builder(clusterState)
            .routingTable(routingTableBuilder.build())
            .metaData(metaData).build();
        Index idx = clusterState.metaData().index("target").getIndex();


        ResizeAllocationDecider resizeAllocationDecider = new ResizeAllocationDecider();
        RoutingAllocation routingAllocation = new RoutingAllocation(null, clusterState.getRoutingNodes(), clusterState, null, 0);
        int shardId = randomIntBetween(0, 3);
        int sourceShardId = IndexMetaData.selectSplitShard(shardId, clusterState.metaData().index("source"), 4).id();
        ShardRouting shardRouting = TestShardRouting.newShardRouting(new ShardId(idx, shardId), null, true, ShardRoutingState.UNASSIGNED,
            RecoverySource.LocalShardsRecoverySource.INSTANCE);
        assertEquals(Decision.YES, resizeAllocationDecider.canAllocate(shardRouting, routingAllocation));

        String allowedNode = clusterState.getRoutingTable().index("source").shard(sourceShardId).primaryShard().currentNodeId();

        if ("node1".equals(allowedNode)) {
            assertEquals(Decision.YES, resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node1"),
                routingAllocation));
            assertEquals(Decision.NO, resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node2"),
                routingAllocation));
        } else {
            assertEquals(Decision.NO, resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node1"),
                routingAllocation));
            assertEquals(Decision.YES, resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node2"),
                routingAllocation));
        }

        routingAllocation.debugDecision(true);
        assertEquals("source primary is active", resizeAllocationDecider.canAllocate(shardRouting, routingAllocation).getExplanation());

        if ("node1".equals(allowedNode)) {
            assertEquals("source primary is allocated on this node",
                resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node1"),
                    routingAllocation).getExplanation());
            assertEquals("source primary is allocated on another node",
                resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node2"),
                    routingAllocation).getExplanation());
        } else {
            assertEquals("source primary is allocated on another node",
                resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node1"),
                    routingAllocation).getExplanation());
            assertEquals("source primary is allocated on this node",
                resizeAllocationDecider.canAllocate(shardRouting, clusterState.getRoutingNodes().node("node2"),
                    routingAllocation).getExplanation());
        }
    }
}
