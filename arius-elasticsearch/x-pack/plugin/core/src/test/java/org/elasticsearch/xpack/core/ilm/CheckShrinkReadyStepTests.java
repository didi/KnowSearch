/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.ilm;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.routing.TestShardRouting;
import org.elasticsearch.cluster.routing.UnassignedInfo;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.node.Node;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;

public class CheckShrinkReadyStepTests extends AbstractStepTestCase<CheckShrinkReadyStep> {

    @Override
    public CheckShrinkReadyStep createRandomInstance() {
        Step.StepKey stepKey = randomStepKey();
        Step.StepKey nextStepKey = randomStepKey();

        return new CheckShrinkReadyStep(stepKey, nextStepKey);
    }

    @Override
    public CheckShrinkReadyStep mutateInstance(CheckShrinkReadyStep instance) {
        Step.StepKey key = instance.getKey();
        Step.StepKey nextKey = instance.getNextStepKey();

        switch (between(0, 1)) {
            case 0:
                key = new Step.StepKey(key.getPhase(), key.getAction(), key.getName() + randomAlphaOfLength(5));
                break;
            case 1:
                nextKey = new Step.StepKey(key.getPhase(), key.getAction(), key.getName() + randomAlphaOfLength(5));
                break;
            default:
                throw new AssertionError("Illegal randomisation branch");
        }

        return new CheckShrinkReadyStep(key, nextKey);
    }

    @Override
    public CheckShrinkReadyStep copyInstance(CheckShrinkReadyStep instance) {
        return new CheckShrinkReadyStep(instance.getKey(), instance.getNextStepKey());
    }

    public void testNoSetting() {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));
        Map<String, String> requires = AllocateActionTests.randomMap(1, 5);
        Settings.Builder existingSettings = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT.id)
            .put(IndexMetaData.SETTING_INDEX_UUID, index.getUUID());
        Settings.Builder expectedSettings = Settings.builder();
        Settings.Builder node1Settings = Settings.builder();
        Settings.Builder node2Settings = Settings.builder();
        requires.forEach((k, v) -> {
            existingSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            expectedSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            node1Settings.put(Node.NODE_ATTRIBUTES.getKey() + k, v);
        });

        IndexRoutingTable.Builder indexRoutingTable = IndexRoutingTable.builder(index)
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node1", true, ShardRoutingState.STARTED));

        CheckShrinkReadyStep step = createRandomInstance();
        IllegalStateException e = expectThrows(IllegalStateException.class, () -> {
            assertAllocateStatus(index, 1, 0, step, existingSettings, node1Settings, node2Settings, indexRoutingTable,
                new ClusterStateWaitStep.Result(true, null));
        });
        assertThat(e.getMessage(), containsString("Cannot check shrink allocation as there are no allocation rules by _id"));
    }

    public void testConditionMet() {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));
        Map<String, String> requires = AllocateActionTests.randomMap(1, 5);
        Settings.Builder existingSettings = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT.id)
            .put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_PREFIX + "._id", "node1")
            .put(IndexMetaData.SETTING_INDEX_UUID, index.getUUID());
        Settings.Builder expectedSettings = Settings.builder();
        Settings.Builder node1Settings = Settings.builder();
        Settings.Builder node2Settings = Settings.builder();
        requires.forEach((k, v) -> {
            existingSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            expectedSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            node1Settings.put(Node.NODE_ATTRIBUTES.getKey() + k, v);
        });

        IndexRoutingTable.Builder indexRoutingTable = IndexRoutingTable.builder(index)
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node1", true, ShardRoutingState.STARTED));

        CheckShrinkReadyStep step = createRandomInstance();
        assertAllocateStatus(index, 1, 0, step, existingSettings, node1Settings, node2Settings, indexRoutingTable,
            new ClusterStateWaitStep.Result(true, null));
    }

    public void testConditionMetOnlyOneCopyAllocated() {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));
        Map<String, String> requires = AllocateActionTests.randomMap(1, 5);
        Settings.Builder existingSettings = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT.id)
            .put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_PREFIX + "._id", "node1")
            .put(IndexMetaData.SETTING_INDEX_UUID, index.getUUID());
        Settings.Builder expectedSettings = Settings.builder();
        Settings.Builder node1Settings = Settings.builder();
        Settings.Builder node2Settings = Settings.builder();
        requires.forEach((k, v) -> {
            existingSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            expectedSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            node1Settings.put(Node.NODE_ATTRIBUTES.getKey() + k, v);
        });
        boolean primaryOnNode1 = randomBoolean();
        IndexRoutingTable.Builder indexRoutingTable = IndexRoutingTable.builder(index)
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node1", primaryOnNode1, ShardRoutingState.STARTED))
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node2", primaryOnNode1 == false,
                ShardRoutingState.STARTED));

        CheckShrinkReadyStep step = new CheckShrinkReadyStep(randomStepKey(), randomStepKey());
        assertAllocateStatus(index, 1, 0, step, existingSettings, node1Settings, node2Settings, indexRoutingTable,
            new ClusterStateWaitStep.Result(true, null));
    }

    public void testConditionNotMetDueToRelocation() {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));
        Map<String, String> requires = AllocateActionTests.randomMap(1, 5);
        Settings.Builder existingSettings = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT.id)
            .put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_PREFIX + "._id", "node1")
            .put(IndexMetaData.SETTING_INDEX_UUID, index.getUUID());
        Settings.Builder expectedSettings = Settings.builder();
        Settings.Builder node1Settings = Settings.builder();
        Settings.Builder node2Settings = Settings.builder();
        requires.forEach((k, v) -> {
            existingSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            expectedSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            node1Settings.put(Node.NODE_ATTRIBUTES.getKey() + k, v);
        });
        boolean primaryOnNode1 = randomBoolean();
        ShardRouting shardOnNode1 = TestShardRouting.newShardRouting(new ShardId(index, 0),
            "node1", primaryOnNode1, ShardRoutingState.STARTED);
        shardOnNode1 = shardOnNode1.relocate("node3", 230);
        IndexRoutingTable.Builder indexRoutingTable = IndexRoutingTable.builder(index)
            .addShard(shardOnNode1)
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node2", primaryOnNode1 == false,
                ShardRoutingState.STARTED));

        CheckShrinkReadyStep step = new CheckShrinkReadyStep(randomStepKey(), randomStepKey());
        assertAllocateStatus(index, 1, 0, step, existingSettings, node1Settings, node2Settings, indexRoutingTable,
            new ClusterStateWaitStep.Result(false, new CheckShrinkReadyStep.Info("node1", 1, 1)));
    }

    public void testExecuteAllocateNotComplete() throws Exception {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));
        Map<String, String> requires = AllocateActionTests.randomMap(1, 5);
        Settings.Builder existingSettings = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT.id)
            .put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_PREFIX + "._id", "node1")
            .put(IndexMetaData.SETTING_INDEX_UUID, index.getUUID());
        Settings.Builder expectedSettings = Settings.builder();
        Settings.Builder node1Settings = Settings.builder();
        Settings.Builder node2Settings = Settings.builder();
        requires.forEach((k, v) -> {
            existingSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            expectedSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            node1Settings.put(Node.NODE_ATTRIBUTES.getKey() + k, v);
        });

        IndexRoutingTable.Builder indexRoutingTable = IndexRoutingTable.builder(index)
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node1", true, ShardRoutingState.STARTED))
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 1), "node2", true, ShardRoutingState.STARTED));

        CheckShrinkReadyStep step = createRandomInstance();
        assertAllocateStatus(index, 2, 0, step, existingSettings, node1Settings, node2Settings, indexRoutingTable,
            new ClusterStateWaitStep.Result(false, new CheckShrinkReadyStep.Info("node1", 2, 1)));
    }

    public void testExecuteAllocateNotCompleteOnlyOneCopyAllocated() throws Exception {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));
        Map<String, String> requires = AllocateActionTests.randomMap(1, 5);
        Settings.Builder existingSettings = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT.id)
            .put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_PREFIX + "._id", "node1")
            .put(IndexMetaData.SETTING_INDEX_UUID, index.getUUID());
        Settings.Builder expectedSettings = Settings.builder();
        Settings.Builder node1Settings = Settings.builder();
        Settings.Builder node2Settings = Settings.builder();
        requires.forEach((k, v) -> {
            existingSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            expectedSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            node1Settings.put(Node.NODE_ATTRIBUTES.getKey() + k, v);
        });

        boolean primaryOnNode1 = randomBoolean();
        IndexRoutingTable.Builder indexRoutingTable = IndexRoutingTable.builder(index)
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node1", primaryOnNode1, ShardRoutingState.STARTED))
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node2", primaryOnNode1 == false,
                ShardRoutingState.STARTED));

        CheckShrinkReadyStep step = new CheckShrinkReadyStep(randomStepKey(), randomStepKey());
        assertAllocateStatus(index, 2, 0, step, existingSettings, node1Settings, node2Settings, indexRoutingTable,
            new ClusterStateWaitStep.Result(false, new CheckShrinkReadyStep.Info("node1", 2, 1)));
    }

    public void testExecuteAllocateReplicaUnassigned() {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));
        Map<String, String> requires = AllocateActionTests.randomMap(1, 5);
        Settings.Builder existingSettings = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT.id)
            .put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_PREFIX + "._id", "node1")
            .put(IndexMetaData.SETTING_INDEX_UUID, index.getUUID());
        Settings.Builder expectedSettings = Settings.builder();
        Settings.Builder node1Settings = Settings.builder();
        Settings.Builder node2Settings = Settings.builder();
        requires.forEach((k, v) -> {
            existingSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            expectedSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
            node1Settings.put(Node.NODE_ATTRIBUTES.getKey() + k, v);
        });

        IndexRoutingTable.Builder indexRoutingTable = IndexRoutingTable.builder(index)
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node1", true, ShardRoutingState.STARTED))
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), null, null, false, ShardRoutingState.UNASSIGNED,
                new UnassignedInfo(randomFrom(UnassignedInfo.Reason.values()), "the shard is intentionally unassigned")));

        CheckShrinkReadyStep step = createRandomInstance();
        assertAllocateStatus(index, 1, 1, step, existingSettings, node1Settings, node2Settings, indexRoutingTable,
            new ClusterStateWaitStep.Result(true, null));
    }

    /**
     * this  tests the scenario where
     *
     * PUT index
     * {
     *  "settings": {
     *   "number_of_replicas": 0,
     *   "number_of_shards": 1
     *  }
     * }
     *
     * PUT index/_settings
     * {
     *  "number_of_replicas": 1,
     *  "index.routing.allocation.include._id": "{node-name}"
     * }
     */
    public void testExecuteReplicasNotAllocatedOnSingleNode() {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));
        Map<String, String> requires = Collections.singletonMap("_id", "node1");
        Settings.Builder existingSettings = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT.id)
            .put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_PREFIX + "._id", "node1")
            .put(IndexMetaData.SETTING_INDEX_UUID, index.getUUID());
        Settings.Builder expectedSettings = Settings.builder();
        Settings.Builder node1Settings = Settings.builder();
        Settings.Builder node2Settings = Settings.builder();
        requires.forEach((k, v) -> {
            expectedSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
        });

        IndexRoutingTable.Builder indexRoutingTable = IndexRoutingTable.builder(index)
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node1", true, ShardRoutingState.STARTED))
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 1), "node1", false, ShardRoutingState.STARTED))
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 1), "node2", true, ShardRoutingState.STARTED))
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), null, null, false, ShardRoutingState.UNASSIGNED,
                new UnassignedInfo(UnassignedInfo.Reason.REPLICA_ADDED, "no attempt")));

        CheckShrinkReadyStep step = createRandomInstance();
        assertAllocateStatus(index, 2, 1, step, existingSettings, node1Settings, node2Settings, indexRoutingTable,
            new ClusterStateWaitStep.Result(true, null));
    }

    public void testExecuteReplicasButCopiesNotPresent() {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));
        Map<String, String> requires = Collections.singletonMap("_id", "node1");
        Settings.Builder existingSettings = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT.id)
            .put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_PREFIX + "._id", "node1")
            .put(IndexMetaData.SETTING_INDEX_UUID, index.getUUID());
        Settings.Builder expectedSettings = Settings.builder();
        Settings.Builder node1Settings = Settings.builder();
        Settings.Builder node2Settings = Settings.builder();
        requires.forEach((k, v) -> {
            expectedSettings.put(IndexMetaData.INDEX_ROUTING_REQUIRE_GROUP_SETTING.getKey() + k, v);
        });

        IndexRoutingTable.Builder indexRoutingTable = IndexRoutingTable.builder(index)
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), "node1", true, ShardRoutingState.STARTED))
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 1), "node2", false, ShardRoutingState.STARTED))
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 1), "node3", true, ShardRoutingState.STARTED))
            .addShard(TestShardRouting.newShardRouting(new ShardId(index, 0), null, null, false, ShardRoutingState.UNASSIGNED,
                new UnassignedInfo(UnassignedInfo.Reason.REPLICA_ADDED, "no attempt")));

        CheckShrinkReadyStep step = createRandomInstance();
        assertAllocateStatus(index, 2, 1, step, existingSettings, node1Settings, node2Settings, indexRoutingTable,
            new ClusterStateWaitStep.Result(false, new CheckShrinkReadyStep.Info("node1", 2, 1)));
    }

    public void testExecuteIndexMissing() throws Exception {
        Index index = new Index(randomAlphaOfLengthBetween(1, 20), randomAlphaOfLengthBetween(1, 20));
        ClusterState clusterState = ClusterState.builder(ClusterState.EMPTY_STATE).build();

        CheckShrinkReadyStep step = createRandomInstance();

        ClusterStateWaitStep.Result actualResult = step.isConditionMet(index, clusterState);
        assertFalse(actualResult.isComplete());
        assertNull(actualResult.getInfomationContext());
    }

    private void assertAllocateStatus(Index index, int shards, int replicas, CheckShrinkReadyStep step, Settings.Builder existingSettings,
                                      Settings.Builder node1Settings, Settings.Builder node2Settings,
                                      IndexRoutingTable.Builder indexRoutingTable, ClusterStateWaitStep.Result expectedResult) {
        IndexMetaData indexMetadata = IndexMetaData.builder(index.getName()).settings(existingSettings).numberOfShards(shards)
            .numberOfReplicas(replicas).build();
        ImmutableOpenMap.Builder<String, IndexMetaData> indices = ImmutableOpenMap.<String, IndexMetaData> builder().fPut(index.getName(),
            indexMetadata);

        ClusterState clusterState = ClusterState.builder(ClusterState.EMPTY_STATE).metaData(MetaData.builder().indices(indices.build()))
            .nodes(DiscoveryNodes.builder()
                .add(DiscoveryNode.createLocal(Settings.builder().put(node1Settings.build())
                        .put(Node.NODE_NAME_SETTING.getKey(), "node1").build(),
                    new TransportAddress(TransportAddress.META_ADDRESS, 9200),
                    "node1"))
                .add(DiscoveryNode.createLocal(Settings.builder().put(node2Settings.build())
                        .put(Node.NODE_NAME_SETTING.getKey(), "node2").build(),
                    new TransportAddress(TransportAddress.META_ADDRESS, 9201),
                    "node2")))
            .routingTable(RoutingTable.builder().add(indexRoutingTable).build()).build();
        ClusterStateWaitStep.Result actualResult = step.isConditionMet(index, clusterState);
        assertEquals(expectedResult.isComplete(), actualResult.isComplete());
        assertEquals(expectedResult.getInfomationContext(), actualResult.getInfomationContext());
    }
}
