package org.elasticsearch.cluster.routing.allocation.group.allocator;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterModule;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ESAllocationTestCase;
import org.elasticsearch.cluster.EmptyClusterInfoService;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.RoutingNode;
import org.elasticsearch.cluster.routing.RoutingNodes;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.cluster.routing.allocation.allocator.GroupBalancedShardsAllocator;
import org.elasticsearch.cluster.routing.allocation.allocator.IndexGroupSettings;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.gateway.GatewayAllocator;
import org.elasticsearch.test.gateway.TestGatewayAllocator;
import org.hamcrest.Matchers;

import java.util.stream.Collectors;

import static org.elasticsearch.cluster.routing.ShardRoutingState.INITIALIZING;
import static org.elasticsearch.cluster.routing.ShardRoutingState.STARTED;
import static org.elasticsearch.cluster.routing.ShardRoutingState.UNASSIGNED;

public class GroupBalancedShardsAllocatorTests extends ESAllocationTestCase {
    private final Logger logger = LogManager.getLogger(GroupBalancedShardsAllocatorTests.class);
    // TODO maybe we can randomize these numbers somehow
    final int numberOfNodes = 5;
    final int numberOfIndices = 12;
    final int numberOfShards = 2;
    final int numberOfReplicas = 2;

    public void testIndexBalance() {
        /* Tests balance over indices only */
        final float indexBalance = 0.8f;
        final float replicaBalance = 0.0f;
        final float groupBalance = 0.2f;
        final float balanceTreshold = 1.0f;

        Settings.Builder settings = Settings.builder();
        settings.put(ClusterModule.SHARDS_ALLOCATOR_TYPE_SETTING.getKey(), GroupAllocatorPlugin.GROUP_BALANCED_ALLOCATOR);

        settings.put(GroupBalancedShardsAllocator.INDEX_BALANCE_FACTOR_SETTING.getKey(), indexBalance);
        settings.put(GroupBalancedShardsAllocator.SHARD_BALANCE_FACTOR_SETTING.getKey(), replicaBalance);
        settings.put(GroupBalancedShardsAllocator.GROUP_BALANCE_FACTOR_SETTING.getKey(), groupBalance);
        settings.put(GroupBalancedShardsAllocator.THRESHOLD_SETTING.getKey(), balanceTreshold);

        AllocationService strategy = createGrouypAllocationService(settings.build(), new TestGatewayAllocator());

        ClusterState clusterState = initCluster(strategy);
        assertIndexBalance(clusterState.getRoutingTable(), clusterState.getRoutingNodes(), numberOfNodes, numberOfIndices,
            numberOfReplicas, numberOfShards, balanceTreshold);

        clusterState = addNode(clusterState, strategy);
        assertIndexBalance(clusterState.getRoutingTable(), clusterState.getRoutingNodes(), numberOfNodes + 1,
            numberOfIndices, numberOfReplicas, numberOfShards, balanceTreshold);

        clusterState = removeNodes(clusterState, strategy);
        assertIndexBalance(clusterState.getRoutingTable(), clusterState.getRoutingNodes(),
            (numberOfNodes + 1) - (numberOfNodes + 1) / 2, numberOfIndices, numberOfReplicas, numberOfShards, balanceTreshold);
    }

    public void testReplicaBalance() {
        /* Tests balance over replicas only */
        final float indexBalance = 0.0f;
        final float replicaBalance = 1f;
        final float groupBalance = 0.0f;
        final float balanceThreshold = 1.0f;

        Settings.Builder settings = Settings.builder();
        settings.put(ClusterModule.SHARDS_ALLOCATOR_TYPE_SETTING.getKey(), GroupAllocatorPlugin.GROUP_BALANCED_ALLOCATOR);

        settings.put(GroupBalancedShardsAllocator.INDEX_BALANCE_FACTOR_SETTING.getKey(), indexBalance);
        settings.put(GroupBalancedShardsAllocator.SHARD_BALANCE_FACTOR_SETTING.getKey(), replicaBalance);
        settings.put(GroupBalancedShardsAllocator.GROUP_BALANCE_FACTOR_SETTING.getKey(), groupBalance);
        settings.put(GroupBalancedShardsAllocator.THRESHOLD_SETTING.getKey(), balanceThreshold);

        AllocationService strategy = createGrouypAllocationService(settings.build(), new TestGatewayAllocator());

        ClusterState clusterState = initCluster(strategy);
        assertReplicaBalance(clusterState.getRoutingNodes(), numberOfNodes, numberOfIndices,
            numberOfReplicas, numberOfShards, balanceThreshold);

        clusterState = addNode(clusterState, strategy);
        assertReplicaBalance(clusterState.getRoutingNodes(), numberOfNodes + 1,
            numberOfIndices, numberOfReplicas, numberOfShards, balanceThreshold);

        clusterState = removeNodes(clusterState, strategy);
        assertReplicaBalance(clusterState.getRoutingNodes(),
            numberOfNodes + 1 - (numberOfNodes + 1) / 2, numberOfIndices, numberOfReplicas, numberOfShards, balanceThreshold);
    }

    public void testGroupBalance() {
        final float indexBalance = 0.8f;
        final float replicaBalance = 0.0f;
        final float groupBalance = 0.2f;
        final float balanceTreshold = 1.0f;

        Settings.Builder settings = Settings.builder();
        settings.put(ClusterModule.SHARDS_ALLOCATOR_TYPE_SETTING.getKey(), GroupAllocatorPlugin.GROUP_BALANCED_ALLOCATOR);

        settings.put(GroupBalancedShardsAllocator.INDEX_BALANCE_FACTOR_SETTING.getKey(), indexBalance);
        settings.put(GroupBalancedShardsAllocator.SHARD_BALANCE_FACTOR_SETTING.getKey(), replicaBalance);
        settings.put(GroupBalancedShardsAllocator.GROUP_BALANCE_FACTOR_SETTING.getKey(), groupBalance);
        settings.put(GroupBalancedShardsAllocator.THRESHOLD_SETTING.getKey(), balanceTreshold);

        AllocationService strategy = createGrouypAllocationService(settings.build(), new TestGatewayAllocator());

        logger.info("start " + numberOfNodes + " nodes");
        DiscoveryNodes.Builder nodes = DiscoveryNodes.builder();
        for (int i = 0; i < numberOfNodes; i++) {
            String name = "node" + i;
            nodes.add(newNode(name));
        }

        ClusterState clusterState = ClusterState.builder(org.elasticsearch.cluster.ClusterName.CLUSTER_NAME_SETTING
            .getDefault(Settings.EMPTY)).nodes(nodes).build();

        String group = "test_group";

        // test_1, 2 shard, 2 replicas, factor=1
        String test_1 = "test_1";
        clusterState = addIndex(strategy, clusterState, test_1, 2, 2, group, 1);

        assertNodeShardsMinMax(clusterState.getRoutingNodes(), 1, 2);

        // test_2, 1 shard, 0 replica, factor=1
        String test_2 = "test_2";
        clusterState = addIndex(strategy, clusterState, test_2, 1, 0, group, 1);

        assertNodeShardsMinMax(clusterState.getRoutingNodes(), 1, 2);

        // test_3, 1 shard, 1 replica, factor=1
        String test_3 = "test_3";
        clusterState = addIndex(strategy, clusterState, test_3, 1, 1, group, 1);

        assertNodeShardsMinMax(clusterState.getRoutingNodes(), 1, 2);

        // test_4, 1 shard, 0 replica, factor=1
        String test_4 = "test_4";
        clusterState = addIndex(strategy, clusterState, test_4, 1, 0, group, 1);

        assertNodeShardsMinMax(clusterState.getRoutingNodes(), 2, 2);

        // test_5, 3 shard, 1 replica, factor=0.1
        String test_5 = "test_5";
        clusterState = addIndex(strategy, clusterState, test_5, 3, 1, group, 0.1f);

        assertNodeShardsMinMax(clusterState.getRoutingNodes(), 3, 4);

        // test_6, 1 shard, 1 replica, factor=0.5
        String test_6 = "test_6";
        clusterState = addIndex(strategy, clusterState, test_6, 1, 1, group, 0.5f);

        assertNodeShardsMinMax(clusterState.getRoutingNodes(), 3, 4);

        // test_7, 1 shard, 1 replica, factor=0.2
        String test_7 = "test_7";
        clusterState = addIndex(strategy, clusterState, test_7, 1, 1, group, 0.3f);

        assertNodeShardsMinMax(clusterState.getRoutingNodes(), 4, 4);

        // test_8, 1 shard, 0 replica, factor=0.1
        String test_8 = "test_8";
        clusterState = addIndex(strategy, clusterState, test_8, 1, 0, group, 0.1f);

        assertNodeShardsMinMax(clusterState.getRoutingNodes(), 4, 5);

        // test_9, 1 shard, 0 replica, factor=0.1
        String test_9 = "test_9";
        clusterState = addIndex(strategy, clusterState, test_9, 1, 0, group, 0.1f);

        assertNodeShardsMinMax(clusterState.getRoutingNodes(), 4, 6);

        // test_8 and test_9 's node are the same.
        assertEquals(clusterState.getRoutingTable().index(test_8).randomAllActiveShardsIt().nextOrNull().currentNodeId(), clusterState.getRoutingTable().index(test_9).randomAllActiveShardsIt().nextOrNull().currentNodeId());

        clusterState = addNode(clusterState, strategy);

        assertNodeShardsMinMax(clusterState.getRoutingNodes(), 3, 5);
    }

    private ClusterState addIndex(AllocationService strategy, ClusterState clusterState, String indexName, int shardNum, int replicas, String group, float factor) {
        MetaData.Builder metaDataBuilder = MetaData.builder(clusterState.metaData());
        RoutingTable.Builder routingTableBuilder = RoutingTable.builder(clusterState.routingTable());
        IndexMetaData.Builder index = IndexMetaData.builder(indexName).settings(settings(Version.CURRENT).put(IndexGroupSettings.INDEX_GROUP_NAME, group).put(IndexGroupSettings.INDEX_GROUP_FACTOR, factor))
            .numberOfShards(shardNum).numberOfReplicas(replicas);
        metaDataBuilder = metaDataBuilder.put(index);

        MetaData metaData = metaDataBuilder.build();

        routingTableBuilder.addAsNew(metaData.index(indexName));

        RoutingTable initialRoutingTable = routingTableBuilder.build();

        clusterState = ClusterState.builder(clusterState).metaData(metaData).routingTable(initialRoutingTable).build();
        clusterState = strategy.reroute(clusterState, "reroute");

        logger.info("restart all the primary shards, replicas will start initializing");
        RoutingNodes routingNodes = clusterState.getRoutingNodes();
        clusterState = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING));

        logger.info("start the replica shards");
        routingNodes = clusterState.getRoutingNodes();
        clusterState = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING));

        logger.info("complete rebalancing");
        clusterState = applyStartedShardsUntilNoChange(clusterState, strategy);

        return clusterState;
    }

    private ClusterState initCluster(AllocationService strategy) {
        MetaData.Builder metaDataBuilder = MetaData.builder();
        RoutingTable.Builder routingTableBuilder = RoutingTable.builder();

        for (int i = 0; i < numberOfIndices; i++) {
            IndexMetaData.Builder index = IndexMetaData.builder("test" + i).settings(settings(Version.CURRENT))
                .numberOfShards(numberOfShards).numberOfReplicas(numberOfReplicas);
            metaDataBuilder = metaDataBuilder.put(index);
        }

        MetaData metaData = metaDataBuilder.build();

        for (ObjectCursor<IndexMetaData> cursor : metaData.indices().values()) {
            routingTableBuilder.addAsNew(cursor.value);
        }

        RoutingTable initialRoutingTable = routingTableBuilder.build();


        logger.info("start " + numberOfNodes + " nodes");
        DiscoveryNodes.Builder nodes = DiscoveryNodes.builder();
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add(newNode("node" + i));
        }
        ClusterState clusterState = ClusterState.builder(org.elasticsearch.cluster.ClusterName.CLUSTER_NAME_SETTING
            .getDefault(Settings.EMPTY)).nodes(nodes).metaData(metaData).routingTable(initialRoutingTable).build();
        clusterState = strategy.reroute(clusterState, "reroute");

        logger.info("restart all the primary shards, replicas will start initializing");
        RoutingNodes routingNodes = clusterState.getRoutingNodes();
        clusterState = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING));

        logger.info("start the replica shards");
        routingNodes = clusterState.getRoutingNodes();
        clusterState = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING));

        logger.info("complete rebalancing");
        return applyStartedShardsUntilNoChange(clusterState, strategy);
    }

    private ClusterState addNode(ClusterState clusterState, AllocationService strategy) {
        logger.info("now, start 1 more node, check that rebalancing will happen because we set it to always");
        clusterState = ClusterState.builder(clusterState).nodes(DiscoveryNodes.builder(clusterState.nodes())
            .add(newNode("node" + numberOfNodes)))
            .build();

        RoutingTable routingTable = strategy.reroute(clusterState, "reroute").routingTable();
        clusterState = ClusterState.builder(clusterState).routingTable(routingTable).build();

        // move initializing to started
        return applyStartedShardsUntilNoChange(clusterState, strategy);
    }

    private ClusterState removeNodes(ClusterState clusterState, AllocationService strategy) {
        logger.info("Removing half the nodes (" + (numberOfNodes + 1) / 2 + ")");
        DiscoveryNodes.Builder nodes = DiscoveryNodes.builder(clusterState.nodes());

        boolean removed = false;
        for (int i = (numberOfNodes + 1) / 2; i <= numberOfNodes; i++) {
            nodes.remove("node" + i);
            removed = true;
        }

        clusterState = ClusterState.builder(clusterState).nodes(nodes.build()).build();
        if (removed) {
            clusterState = strategy.disassociateDeadNodes(clusterState, randomBoolean(), "removed nodes");
        }

        logger.info("start all the primary shards, replicas will start initializing");
        RoutingNodes routingNodes = clusterState.getRoutingNodes();
        clusterState = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING));

        logger.info("start the replica shards");
        routingNodes = clusterState.getRoutingNodes();
        clusterState = strategy.applyStartedShards(clusterState, routingNodes.shardsWithState(INITIALIZING));

        logger.info("rebalancing");
        clusterState = strategy.reroute(clusterState, "reroute");

        logger.info("complete rebalancing");
        return applyStartedShardsUntilNoChange(clusterState, strategy);
    }


    private void assertReplicaBalance(RoutingNodes nodes, int numberOfNodes, int numberOfIndices, int numberOfReplicas,
                                      int numberOfShards, float treshold) {
        final int unassigned = nodes.unassigned().size();

        if (unassigned > 0) {
            // Ensure that if there any unassigned shards, all of their replicas are unassigned as well
            // (i.e. unassigned count is always [replicas] + 1 for each shard unassigned shardId)
            nodes.shardsWithState(UNASSIGNED).stream().collect(
                Collectors.toMap(
                    ShardRouting::shardId,
                    s -> 1,
                    (a, b) -> a + b
                )).values().forEach(
                count -> assertEquals(numberOfReplicas + 1, count.longValue())
            );
        }
        assertEquals(numberOfNodes, nodes.size());

        final int numShards = numberOfIndices * numberOfShards * (numberOfReplicas + 1) - unassigned;
        final float avgNumShards = (float) (numShards) / (float) (numberOfNodes);
        final int minAvgNumberOfShards = Math.round(Math.round(Math.floor(avgNumShards - treshold)));
        final int maxAvgNumberOfShards = Math.round(Math.round(Math.ceil(avgNumShards + treshold)));

        for (RoutingNode node : nodes) {
            assertThat(node.shardsWithState(STARTED).size(), Matchers.greaterThanOrEqualTo(minAvgNumberOfShards));
            assertThat(node.shardsWithState(STARTED).size(), Matchers.lessThanOrEqualTo(maxAvgNumberOfShards));
        }
    }

    private void assertIndexBalance(RoutingTable routingTable, RoutingNodes nodes, int numberOfNodes, int numberOfIndices,
                                    int numberOfReplicas, int numberOfShards, float treshold) {

        final int numShards = numberOfShards * (numberOfReplicas + 1);
        final float avgNumShards = (float) (numShards) / (float) (numberOfNodes);
        final int minAvgNumberOfShards = Math.round(Math.round(Math.floor(avgNumShards - treshold)));
        final int maxAvgNumberOfShards = Math.round(Math.round(Math.ceil(avgNumShards + treshold)));

        for (ObjectCursor<String> index : routingTable.indicesRouting().keys()) {
            for (RoutingNode node : nodes) {
                assertThat(node.shardsWithState(index.value, STARTED).size(), Matchers.greaterThanOrEqualTo(minAvgNumberOfShards));
                assertThat(node.shardsWithState(index.value, STARTED).size(), Matchers.lessThanOrEqualTo(maxAvgNumberOfShards));
            }
        }
    }

    private void assertNodeShardsMinMax(RoutingNodes nodes, int min, int max) {
        int mapMin = Integer.MAX_VALUE;
        int mapMax = 0;
        for (RoutingNode node : nodes) {
            int count = node.size();
            if (count > mapMax) {
                mapMax = count;
            }

            if (count < mapMin) {
                mapMin = count;
            }
        }

        assertEquals(max, mapMax);
        assertEquals(min, mapMin);
    }

    private static final ClusterSettings EMPTY_CLUSTER_SETTINGS =
        new ClusterSettings(Settings.EMPTY, ClusterSettings.BUILT_IN_CLUSTER_SETTINGS);
    private MockAllocationService createGrouypAllocationService(Settings settings, GatewayAllocator gatewayAllocator) {
        return new MockAllocationService(
            randomAllocationDeciders(settings, EMPTY_CLUSTER_SETTINGS, random()),
            gatewayAllocator, new GroupBalancedShardsAllocator(settings), EmptyClusterInfoService.INSTANCE);
    }


}
