/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.AckedClusterStateUpdateTask;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.coordination.NoMasterBlockService;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodeRole;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.routing.TestShardRouting;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.gateway.GatewayService;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.core.watcher.WatcherMetaData;
import org.elasticsearch.xpack.core.watcher.WatcherState;
import org.elasticsearch.xpack.core.watcher.watch.Watch;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.elasticsearch.cluster.routing.ShardRoutingState.RELOCATING;
import static org.elasticsearch.cluster.routing.ShardRoutingState.STARTED;
import static org.elasticsearch.xpack.core.watcher.support.WatcherIndexTemplateRegistryField.HISTORY_TEMPLATE_NAME;
import static org.elasticsearch.xpack.core.watcher.support.WatcherIndexTemplateRegistryField.TRIGGERED_TEMPLATE_NAME;
import static org.elasticsearch.xpack.core.watcher.support.WatcherIndexTemplateRegistryField.WATCHES_TEMPLATE_NAME;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class WatcherLifeCycleServiceTests extends ESTestCase {

    private WatcherService watcherService;
    private WatcherLifeCycleService lifeCycleService;

    @Before
    public void prepareServices() {
        ClusterService clusterService = mock(ClusterService.class);
        Answer<Object> answer = invocationOnMock -> {
            AckedClusterStateUpdateTask updateTask = (AckedClusterStateUpdateTask) invocationOnMock.getArguments()[1];
            updateTask.onAllNodesAcked(null);
            return null;
        };
        doAnswer(answer).when(clusterService).submitStateUpdateTask(anyString(), any(ClusterStateUpdateTask.class));
        watcherService = mock(WatcherService.class);
        lifeCycleService = new WatcherLifeCycleService(clusterService, watcherService);
    }

    public void testNoRestartWithoutAllocationIdsConfigured() {
        IndexRoutingTable indexRoutingTable = IndexRoutingTable.builder(new Index("anything", "foo")).build();
        ClusterState previousClusterState = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1").add(newNode("node_1")))
            .routingTable(RoutingTable.builder().add(indexRoutingTable).build())
            .build();

        IndexRoutingTable watchRoutingTable = IndexRoutingTable.builder(new Index(Watch.INDEX, "foo")).build();
        ClusterState clusterState = ClusterState.builder(new ClusterName("my-cluster"))
            .metaData(MetaData.builder()
                .put(IndexTemplateMetaData.builder(HISTORY_TEMPLATE_NAME).patterns(randomIndexPatterns()))
                .put(IndexTemplateMetaData.builder(TRIGGERED_TEMPLATE_NAME).patterns(randomIndexPatterns()))
                .put(IndexTemplateMetaData.builder(WATCHES_TEMPLATE_NAME).patterns(randomIndexPatterns()))
                .build())
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1").add(newNode("node_1")))
            .routingTable(RoutingTable.builder().add(watchRoutingTable).build())
            .build();

        when(watcherService.validate(clusterState)).thenReturn(true);
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterState, previousClusterState));
        verifyZeroInteractions(watcherService);

        // Trying to start a second time, but that should have no effect.
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterState, previousClusterState));
        verifyZeroInteractions(watcherService);
    }

    public void testStartWithStateNotRecoveredBlock() {
        DiscoveryNodes.Builder nodes = new DiscoveryNodes.Builder().masterNodeId("id1").localNodeId("id1");
        ClusterState clusterState = ClusterState.builder(new ClusterName("my-cluster"))
            .blocks(ClusterBlocks.builder().addGlobalBlock(GatewayService.STATE_NOT_RECOVERED_BLOCK))
            .nodes(nodes).build();
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterState, clusterState));
        verifyZeroInteractions(watcherService);
    }

    public void testShutdown() {
        IndexRoutingTable watchRoutingTable = IndexRoutingTable.builder(new Index(Watch.INDEX, "foo")).build();
        ClusterState clusterState = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1").add(newNode("node_1")))
            .routingTable(RoutingTable.builder().add(watchRoutingTable).build())
            .metaData(MetaData.builder()
                .put(IndexTemplateMetaData.builder(HISTORY_TEMPLATE_NAME).patterns(randomIndexPatterns()))
                .put(IndexTemplateMetaData.builder(TRIGGERED_TEMPLATE_NAME).patterns(randomIndexPatterns()))
                .put(IndexTemplateMetaData.builder(WATCHES_TEMPLATE_NAME).patterns(randomIndexPatterns()))
                .build())
            .build();

        when(watcherService.validate(clusterState)).thenReturn(true);

        lifeCycleService.shutDown();
        verify(watcherService, never()).stop(anyString(), any());
        verify(watcherService, times(1)).shutDown(any());

        reset(watcherService);
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterState, clusterState));
        verifyZeroInteractions(watcherService);
    }

    public void testManualStartStop() {
        Index index = new Index(Watch.INDEX, "uuid");
        IndexRoutingTable.Builder indexRoutingTableBuilder = IndexRoutingTable.builder(index);
        indexRoutingTableBuilder.addShard(
            TestShardRouting.newShardRouting(Watch.INDEX, 0, "node_1", true, ShardRoutingState.STARTED));
        IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(Watch.INDEX).settings(settings(Version.CURRENT)
            .put(IndexMetaData.INDEX_FORMAT_SETTING.getKey(), 6)) // the internal index format, required
            .numberOfShards(1).numberOfReplicas(0);
        MetaData.Builder metaDataBuilder = MetaData.builder()
            .put(indexMetaDataBuilder)
            .put(IndexTemplateMetaData.builder(HISTORY_TEMPLATE_NAME).patterns(randomIndexPatterns()))
            .put(IndexTemplateMetaData.builder(TRIGGERED_TEMPLATE_NAME).patterns(randomIndexPatterns()))
            .put(IndexTemplateMetaData.builder(WATCHES_TEMPLATE_NAME).patterns(randomIndexPatterns()));
        if (randomBoolean()) {
            metaDataBuilder.putCustom(WatcherMetaData.TYPE, new WatcherMetaData(false));
        }
        MetaData metaData = metaDataBuilder.build();
        IndexRoutingTable indexRoutingTable = indexRoutingTableBuilder.build();
        ClusterState clusterState = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1").add(newNode("node_1")))
            .routingTable(RoutingTable.builder().add(indexRoutingTable).build())
            .metaData(metaData)
            .build();

        when(watcherService.validate(clusterState)).thenReturn(true);

        // mark watcher manually as stopped
        ClusterState stoppedClusterState = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1").add(newNode("node_1")))
            .routingTable(RoutingTable.builder().add(indexRoutingTable).build())
            .metaData(MetaData.builder(metaData).putCustom(WatcherMetaData.TYPE, new WatcherMetaData(true)).build())
            .build();

        lifeCycleService.clusterChanged(new ClusterChangedEvent("foo", stoppedClusterState, clusterState));
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(watcherService, times(1))
            .stop(eq("watcher manually marked to shutdown by cluster state update"), captor.capture());
        assertEquals(WatcherState.STOPPING, lifeCycleService.getState());
        captor.getValue().run();
        assertEquals(WatcherState.STOPPED, lifeCycleService.getState());

        // Starting via cluster state update, as the watcher metadata block is removed/set to true
        reset(watcherService);
        when(watcherService.validate(clusterState)).thenReturn(true);
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterState, stoppedClusterState));
        verify(watcherService, times(1)).start(eq(clusterState), anyObject());

        // no change, keep going
        reset(watcherService);
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterState, clusterState));
        verifyZeroInteractions(watcherService);
    }

    public void testNoLocalShards() {
        Index watchIndex = new Index(Watch.INDEX, "foo");
        ShardId shardId = new ShardId(watchIndex, 0);
        DiscoveryNodes nodes = new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1")
            .add(newNode("node_1")).add(newNode("node_2"))
            .build();
        IndexMetaData indexMetaData = IndexMetaData.builder(Watch.INDEX)
            .settings(Settings.builder()
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(IndexMetaData.INDEX_FORMAT_SETTING.getKey(), 6)
            ).build();

        IndexRoutingTable watchRoutingTable = IndexRoutingTable.builder(watchIndex)
            .addShard(randomBoolean() ?
                TestShardRouting.newShardRouting(shardId, "node_1", true, STARTED) :
                TestShardRouting.newShardRouting(shardId, "node_1", "node_2", true, RELOCATING))
            .build();
        ClusterState clusterStateWithLocalShards = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(nodes)
            .routingTable(RoutingTable.builder().add(watchRoutingTable).build())
            .metaData(MetaData.builder().put(indexMetaData, false))
            .build();

        // shard moved over to node 2
        IndexRoutingTable watchRoutingTableNode2 = IndexRoutingTable.builder(watchIndex)
            .addShard(randomBoolean() ?
                TestShardRouting.newShardRouting(shardId, "node_2", true, STARTED) :
                TestShardRouting.newShardRouting(shardId, "node_2", "node_1", true, RELOCATING))
            .build();
        ClusterState clusterStateWithoutLocalShards = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(nodes)
            .routingTable(RoutingTable.builder().add(watchRoutingTableNode2).build())
            .metaData(MetaData.builder().put(indexMetaData, false))
            .build();

        // set current allocation ids
        when(watcherService.validate(eq(clusterStateWithLocalShards))).thenReturn(true);
        when(watcherService.validate(eq(clusterStateWithoutLocalShards))).thenReturn(false);
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterStateWithLocalShards, clusterStateWithoutLocalShards));
        verify(watcherService, times(1)).reload(eq(clusterStateWithLocalShards), eq("new local watcher shard allocation ids"));
        verify(watcherService, times(1)).validate(eq(clusterStateWithLocalShards));
        verifyNoMoreInteractions(watcherService);

        // no more local shards, lets pause execution
        reset(watcherService);
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterStateWithoutLocalShards, clusterStateWithLocalShards));
        verify(watcherService, times(1)).pauseExecution(eq("no local watcher shards found"));
        verifyNoMoreInteractions(watcherService);

        // no further invocations should happen if the cluster state does not change in regard to local shards
        reset(watcherService);
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterStateWithoutLocalShards, clusterStateWithoutLocalShards));
        verifyZeroInteractions(watcherService);
    }

    public void testReplicaWasAddedOrRemoved() {
        Index watchIndex = new Index(Watch.INDEX, "foo");
        ShardId shardId = new ShardId(watchIndex, 0);
        ShardId secondShardId = new ShardId(watchIndex, 1);
        DiscoveryNodes discoveryNodes = new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1")
            .add(newNode("node_1"))
            .add(newNode("node_2"))
            .build();

        ShardRouting firstShardOnSecondNode = TestShardRouting.newShardRouting(shardId, "node_2", true, STARTED);
        ShardRouting secondShardOnFirstNode = TestShardRouting.newShardRouting(secondShardId, "node_1", true, STARTED);

        IndexRoutingTable previousWatchRoutingTable = IndexRoutingTable.builder(watchIndex)
            .addShard(secondShardOnFirstNode)
            .addShard(firstShardOnSecondNode)
            .build();

        IndexMetaData indexMetaData = IndexMetaData.builder(Watch.INDEX)
            .settings(Settings.builder()
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(IndexMetaData.INDEX_FORMAT_SETTING.getKey(), 6)
            ).build();

        ClusterState stateWithPrimaryShard = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(discoveryNodes)
            .routingTable(RoutingTable.builder().add(previousWatchRoutingTable).build())
            .metaData(MetaData.builder().put(indexMetaData, false))
            .build();

        // add a replica in the local node
        boolean addShardOnLocalNode = randomBoolean();
        final ShardRouting addedShardRouting;
        if (addShardOnLocalNode) {
            addedShardRouting = TestShardRouting.newShardRouting(shardId, "node_1", false, STARTED);
        } else {
            addedShardRouting = TestShardRouting.newShardRouting(secondShardId, "node_2", false, STARTED);
        }

        IndexRoutingTable currentWatchRoutingTable = IndexRoutingTable.builder(watchIndex)
            .addShard(secondShardOnFirstNode)
            .addShard(firstShardOnSecondNode)
            .addShard(addedShardRouting)
            .build();

        ClusterState stateWithReplicaAdded = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(discoveryNodes)
            .routingTable(RoutingTable.builder().add(currentWatchRoutingTable).build())
            .metaData(MetaData.builder().put(indexMetaData, false))
            .build();

        // randomize between addition or removal of a replica
        boolean replicaAdded = randomBoolean();
        ClusterChangedEvent firstEvent;
        ClusterChangedEvent secondEvent;
        if (replicaAdded) {
            firstEvent = new ClusterChangedEvent("any", stateWithPrimaryShard, stateWithReplicaAdded);
            secondEvent = new ClusterChangedEvent("any", stateWithReplicaAdded, stateWithPrimaryShard);
        } else {
            firstEvent = new ClusterChangedEvent("any", stateWithReplicaAdded, stateWithPrimaryShard);
            secondEvent = new ClusterChangedEvent("any", stateWithPrimaryShard, stateWithReplicaAdded);
        }

        when(watcherService.validate(eq(firstEvent.state()))).thenReturn(true);
        lifeCycleService.clusterChanged(firstEvent);
        verify(watcherService).reload(eq(firstEvent.state()), anyString());

        reset(watcherService);
        when(watcherService.validate(eq(secondEvent.state()))).thenReturn(true);
        lifeCycleService.clusterChanged(secondEvent);
        verify(watcherService).reload(eq(secondEvent.state()), anyString());
    }

    // make sure that cluster state changes can be processed on nodes that do not hold data
    public void testNonDataNode() {
        Index index = new Index(Watch.INDEX, "foo");
        ShardId shardId = new ShardId(index, 0);
        ShardRouting shardRouting = TestShardRouting.newShardRouting(shardId, "node2", true, STARTED);
        IndexRoutingTable.Builder indexRoutingTable = IndexRoutingTable.builder(index).addShard(shardRouting);

        DiscoveryNode node1 = new DiscoveryNode("node_1", ESTestCase.buildNewFakeTransportAddress(), Collections.emptyMap(),
            new HashSet<>(asList(randomFrom(DiscoveryNodeRole.INGEST_ROLE, DiscoveryNodeRole.MASTER_ROLE))), Version.CURRENT);

        DiscoveryNode node2 = new DiscoveryNode("node_2", ESTestCase.buildNewFakeTransportAddress(), Collections.emptyMap(),
            new HashSet<>(asList(DiscoveryNodeRole.DATA_ROLE)), Version.CURRENT);

        DiscoveryNode node3 = new DiscoveryNode("node_3", ESTestCase.buildNewFakeTransportAddress(), Collections.emptyMap(),
            new HashSet<>(asList(DiscoveryNodeRole.DATA_ROLE)), Version.CURRENT);

        IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(Watch.INDEX)
            .settings(Settings.builder()
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
            );

        ClusterState previousState = ClusterState.builder(new ClusterName("my-cluster"))
            .metaData(MetaData.builder().put(indexMetaDataBuilder))
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1").add(node1).add(node2).add(node3))
            .routingTable(RoutingTable.builder().add(indexRoutingTable).build())
            .build();

        IndexMetaData.Builder newIndexMetaDataBuilder = IndexMetaData.builder(Watch.INDEX)
            .settings(Settings.builder()
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 1)
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
            );

        ShardRouting replicaShardRouting = TestShardRouting.newShardRouting(shardId, "node3", false, STARTED);
        IndexRoutingTable.Builder newRoutingTable = IndexRoutingTable.builder(index).addShard(shardRouting).addShard(replicaShardRouting);
        ClusterState currentState = ClusterState.builder(new ClusterName("my-cluster"))
            .metaData(MetaData.builder().put(newIndexMetaDataBuilder))
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1").add(node1).add(node2).add(node3))
            .routingTable(RoutingTable.builder().add(newRoutingTable).build())
            .build();

        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", currentState, previousState));
        verify(watcherService, times(0)).pauseExecution(anyObject());
        verify(watcherService, times(0)).reload(any(), any());
    }

    public void testThatMissingWatcherIndexMetadataOnlyResetsOnce() {
        Index watchIndex = new Index(Watch.INDEX, "foo");
        ShardId shardId = new ShardId(watchIndex, 0);
        IndexRoutingTable watchRoutingTable = IndexRoutingTable.builder(watchIndex)
            .addShard(TestShardRouting.newShardRouting(shardId, "node_1", true, STARTED)).build();
        DiscoveryNodes nodes = new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1").add(newNode("node_1")).build();

        IndexMetaData.Builder newIndexMetaDataBuilder = IndexMetaData.builder(Watch.INDEX)
            .settings(Settings.builder()
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(IndexMetaData.INDEX_FORMAT_SETTING.getKey(), 6)
            );

        ClusterState clusterStateWithWatcherIndex = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(nodes)
            .routingTable(RoutingTable.builder().add(watchRoutingTable).build())
            .metaData(MetaData.builder().put(newIndexMetaDataBuilder))
            .build();

        ClusterState clusterStateWithoutWatcherIndex = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(nodes)
            .build();

        when(watcherService.validate(eq(clusterStateWithWatcherIndex))).thenReturn(true);
        when(watcherService.validate(eq(clusterStateWithoutWatcherIndex))).thenReturn(false);

        // first add the shard allocation ids, by going from empty cs to CS with watcher index
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterStateWithWatcherIndex, clusterStateWithoutWatcherIndex));
        verify(watcherService).reload(eq(clusterStateWithWatcherIndex), anyString());

        // now remove watches index, and ensure that pausing is only called once, no matter how often called (i.e. each CS update)
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterStateWithoutWatcherIndex, clusterStateWithWatcherIndex));
        verify(watcherService, times(1)).pauseExecution(anyObject());

        reset(watcherService);
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", clusterStateWithoutWatcherIndex, clusterStateWithWatcherIndex));
        verifyZeroInteractions(watcherService);
    }

    public void testWatcherServiceDoesNotStartIfIndexTemplatesAreMissing() throws Exception {
        DiscoveryNodes nodes = new DiscoveryNodes.Builder()
            .masterNodeId("node_1").localNodeId("node_1")
            .add(newNode("node_1"))
            .build();

        MetaData.Builder metaDataBuilder = MetaData.builder();
        boolean isHistoryTemplateAdded = randomBoolean();
        if (isHistoryTemplateAdded) {
            metaDataBuilder.put(IndexTemplateMetaData.builder(HISTORY_TEMPLATE_NAME).patterns(randomIndexPatterns()));
        }
        boolean isTriggeredTemplateAdded = randomBoolean();
        if (isTriggeredTemplateAdded) {
            metaDataBuilder.put(IndexTemplateMetaData.builder(TRIGGERED_TEMPLATE_NAME).patterns(randomIndexPatterns()));
        }
        boolean isWatchesTemplateAdded = randomBoolean();
        if (isWatchesTemplateAdded) {
            // ensure not all templates are added, otherwise life cycle service would start
            if ((isHistoryTemplateAdded || isTriggeredTemplateAdded) == false) {
                metaDataBuilder.put(IndexTemplateMetaData.builder(WATCHES_TEMPLATE_NAME).patterns(randomIndexPatterns()));
            }
        }
        ClusterState state = ClusterState.builder(new ClusterName("my-cluster")).nodes(nodes).metaData(metaDataBuilder).build();
        when(watcherService.validate(eq(state))).thenReturn(true);

        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", state, state));
        verify(watcherService, times(0)).start(any(ClusterState.class), anyObject());
    }

    public void testWatcherStopsWhenMasterNodeIsMissing() {
        startWatcher();

        DiscoveryNodes nodes = new DiscoveryNodes.Builder()
            .localNodeId("node_1")
            .add(newNode("node_1"))
            .build();
        ClusterState state = ClusterState.builder(new ClusterName("my-cluster")).nodes(nodes).build();
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", state, state));
        verify(watcherService, times(1)).pauseExecution(eq("no master node"));
    }

    public void testWatcherStopsOnClusterLevelBlock() {
        startWatcher();

        DiscoveryNodes nodes = new DiscoveryNodes.Builder()
            .localNodeId("node_1")
            .masterNodeId("node_1")
            .add(newNode("node_1"))
            .build();
        ClusterBlocks clusterBlocks = ClusterBlocks.builder().addGlobalBlock(NoMasterBlockService.NO_MASTER_BLOCK_WRITES).build();
        ClusterState state = ClusterState.builder(new ClusterName("my-cluster")).nodes(nodes).blocks(clusterBlocks).build();
        lifeCycleService.clusterChanged(new ClusterChangedEvent("any", state, state));
        verify(watcherService, times(1)).pauseExecution(eq("write level cluster block"));
    }

    public void testMasterOnlyNodeCanStart() {
        List<DiscoveryNodeRole> roles =
                Collections.singletonList(randomFrom(DiscoveryNodeRole.MASTER_ROLE, DiscoveryNodeRole.INGEST_ROLE));
        ClusterState state = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1")
                .add(new DiscoveryNode("node_1", ESTestCase.buildNewFakeTransportAddress(), Collections.emptyMap(),
                    new HashSet<>(roles), Version.CURRENT))).build();

        lifeCycleService.clusterChanged(new ClusterChangedEvent("test", state, state));
        assertThat(lifeCycleService.getState(), is(WatcherState.STARTED));
    }

    public void testDataNodeWithoutDataCanStart() {
        MetaData metaData = MetaData.builder().put(IndexTemplateMetaData.builder(HISTORY_TEMPLATE_NAME).patterns(randomIndexPatterns()))
            .put(IndexTemplateMetaData.builder(TRIGGERED_TEMPLATE_NAME).patterns(randomIndexPatterns()))
            .put(IndexTemplateMetaData.builder(WATCHES_TEMPLATE_NAME).patterns(randomIndexPatterns()))
            .build();
        ClusterState state = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1").add(newNode("node_1")))
            .metaData(metaData)
            .build();

        lifeCycleService.clusterChanged(new ClusterChangedEvent("test", state, state));
        assertThat(lifeCycleService.getState(), is(WatcherState.STARTED));
    }

    // this emulates a node outage somewhere in the cluster that carried a watcher shard
    // the number of shards remains the same, but we need to ensure that watcher properly reloads
    // previously we only checked the local shard allocations, but we also need to check if shards in the cluster have changed
    public void testWatcherReloadsOnNodeOutageWithWatcherShard() {
        Index watchIndex = new Index(Watch.INDEX, "foo");
        ShardId shardId = new ShardId(watchIndex, 0);
        String localNodeId = randomFrom("node_1", "node_2");
        String outageNodeId = localNodeId.equals("node_1") ? "node_2" : "node_1";
        DiscoveryNodes previousDiscoveryNodes = new DiscoveryNodes.Builder().masterNodeId(localNodeId).localNodeId(localNodeId)
            .add(newNode(localNodeId))
            .add(newNode(outageNodeId))
            .build();

        ShardRouting replicaShardRouting = TestShardRouting.newShardRouting(shardId, localNodeId, false, STARTED);
        ShardRouting primartShardRouting = TestShardRouting.newShardRouting(shardId, outageNodeId, true, STARTED);
        IndexRoutingTable previousWatchRoutingTable = IndexRoutingTable.builder(watchIndex)
            .addShard(replicaShardRouting)
            .addShard(primartShardRouting)
            .build();

        IndexMetaData indexMetaData = IndexMetaData.builder(Watch.INDEX)
            .settings(Settings.builder()
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(IndexMetaData.INDEX_FORMAT_SETTING.getKey(), 6)
            ).build();

        ClusterState previousState = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(previousDiscoveryNodes)
            .routingTable(RoutingTable.builder().add(previousWatchRoutingTable).build())
            .metaData(MetaData.builder().put(indexMetaData, false))
            .build();

        ShardRouting nowPrimaryShardRouting = replicaShardRouting.moveActiveReplicaToPrimary();
        IndexRoutingTable currentWatchRoutingTable = IndexRoutingTable.builder(watchIndex)
            .addShard(nowPrimaryShardRouting)
            .build();

        DiscoveryNodes currentDiscoveryNodes = new DiscoveryNodes.Builder().masterNodeId(localNodeId).localNodeId(localNodeId)
            .add(newNode(localNodeId))
            .build();

        ClusterState currentState = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(currentDiscoveryNodes)
            .routingTable(RoutingTable.builder().add(currentWatchRoutingTable).build())
            .metaData(MetaData.builder().put(indexMetaData, false))
            .build();

        // initialize the previous state, so all the allocation ids are loaded
        when(watcherService.validate(anyObject())).thenReturn(true);
        lifeCycleService.clusterChanged(new ClusterChangedEvent("whatever", previousState, currentState));

        reset(watcherService);
        when(watcherService.validate(anyObject())).thenReturn(true);
        ClusterChangedEvent event = new ClusterChangedEvent("whatever", currentState, previousState);
        lifeCycleService.clusterChanged(event);
        verify(watcherService).reload(eq(event.state()), anyString());
    }

    private void startWatcher() {
        Index index = new Index(Watch.INDEX, "uuid");
        IndexRoutingTable.Builder indexRoutingTableBuilder = IndexRoutingTable.builder(index);
        indexRoutingTableBuilder.addShard(
            TestShardRouting.newShardRouting(Watch.INDEX, 0, "node_1", true, ShardRoutingState.STARTED));
        IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(Watch.INDEX).settings(settings(Version.CURRENT)
            .put(IndexMetaData.INDEX_FORMAT_SETTING.getKey(), 6)) // the internal index format, required
            .numberOfShards(1).numberOfReplicas(0);
        MetaData metaData = MetaData.builder().put(IndexTemplateMetaData.builder(HISTORY_TEMPLATE_NAME).patterns(randomIndexPatterns()))
            .put(IndexTemplateMetaData.builder(TRIGGERED_TEMPLATE_NAME).patterns(randomIndexPatterns()))
            .put(IndexTemplateMetaData.builder(WATCHES_TEMPLATE_NAME).patterns(randomIndexPatterns())).put(indexMetaDataBuilder)
            .build();
        ClusterState state = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1")
                .add(newNode("node_1")))
            .routingTable(RoutingTable.builder().add(indexRoutingTableBuilder.build()).build())
            .metaData(metaData)
            .build();
        ClusterState emptyState = ClusterState.builder(new ClusterName("my-cluster"))
            .nodes(new DiscoveryNodes.Builder().masterNodeId("node_1").localNodeId("node_1")
                .add(newNode("node_1")))
            .metaData(metaData)
            .build();

        when(watcherService.validate(state)).thenReturn(true);

        lifeCycleService.clusterChanged(new ClusterChangedEvent("foo", state, emptyState));
        assertThat(lifeCycleService.getState(), is(WatcherState.STARTED));
        verify(watcherService, times(1)).reload(eq(state), anyString());
        assertThat(lifeCycleService.shardRoutings(), hasSize(1));

        // reset the mock, the user has to mock everything themselves again
        reset(watcherService);
    }

    private List<String> randomIndexPatterns() {
        return IntStream.range(0, between(1, 10))
            .mapToObj(n -> randomAlphaOfLengthBetween(1, 100))
            .collect(Collectors.toList());
    }

    private static DiscoveryNode newNode(String nodeName) {
        return newNode(nodeName, Version.CURRENT);
    }

    private static DiscoveryNode newNode(String nodeName, Version version) {
        return new DiscoveryNode(nodeName, ESTestCase.buildNewFakeTransportAddress(), Collections.emptyMap(),
            DiscoveryNodeRole.BUILT_IN_ROLES, version);
    }
}
