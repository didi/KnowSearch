/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ilm;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.routing.TestShardRouting;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.node.Node;
import org.elasticsearch.xpack.core.ilm.ClusterStateWaitStep.Result;
import org.elasticsearch.xpack.core.ilm.Step.StepKey;

public class ShrunkShardsAllocatedStepTests extends AbstractStepTestCase<ShrunkShardsAllocatedStep> {

    @Override
    public ShrunkShardsAllocatedStep createRandomInstance() {
        StepKey stepKey = randomStepKey();
        StepKey nextStepKey = randomStepKey();
        String shrunkIndexPrefix = randomAlphaOfLength(10);
        return new ShrunkShardsAllocatedStep(stepKey, nextStepKey, shrunkIndexPrefix);
    }

    @Override
    public ShrunkShardsAllocatedStep mutateInstance(ShrunkShardsAllocatedStep instance) {
        StepKey key = instance.getKey();
        StepKey nextKey = instance.getNextStepKey();
        String shrunkIndexPrefix = instance.getShrunkIndexPrefix();

        switch (between(0, 2)) {
        case 0:
            key = new StepKey(key.getPhase(), key.getAction(), key.getName() + randomAlphaOfLength(5));
            break;
        case 1:
            nextKey = new StepKey(key.getPhase(), key.getAction(), key.getName() + randomAlphaOfLength(5));
            break;
        case 2:
            shrunkIndexPrefix += randomAlphaOfLength(5);
            break;
        default:
                throw new AssertionError("Illegal randomisation branch");
        }

        return new ShrunkShardsAllocatedStep(key, nextKey, shrunkIndexPrefix);
    }

    @Override
    public ShrunkShardsAllocatedStep copyInstance(ShrunkShardsAllocatedStep instance) {
        return new ShrunkShardsAllocatedStep(instance.getKey(), instance.getNextStepKey(), instance.getShrunkIndexPrefix());
    }

    public void testConditionMet() {
        ShrunkShardsAllocatedStep step = createRandomInstance();
        int shrinkNumberOfShards = randomIntBetween(1, 5);
        int originalNumberOfShards = randomIntBetween(1, 5);
        String originalIndexName = randomAlphaOfLength(5);
        IndexMetaData originalIndexMetadata = IndexMetaData.builder(originalIndexName)
            .settings(settings(Version.CURRENT))
            .numberOfShards(originalNumberOfShards)
            .numberOfReplicas(0).build();
        IndexMetaData shrunkIndexMetadata = IndexMetaData.builder(step.getShrunkIndexPrefix() + originalIndexName)
                .settings(settings(Version.CURRENT))
                .numberOfShards(shrinkNumberOfShards)
                .numberOfReplicas(0).build();
        MetaData metaData = MetaData.builder()
            .persistentSettings(settings(Version.CURRENT).build())
            .put(IndexMetaData.builder(originalIndexMetadata))
            .put(IndexMetaData.builder(shrunkIndexMetadata))
            .build();
        Index shrinkIndex = shrunkIndexMetadata.getIndex();

        String nodeId = randomAlphaOfLength(10);
        DiscoveryNode masterNode = DiscoveryNode.createLocal(settings(Version.CURRENT)
                .put(Node.NODE_MASTER_SETTING.getKey(), true).build(),
            new TransportAddress(TransportAddress.META_ADDRESS, 9300), nodeId);

        IndexRoutingTable.Builder builder = IndexRoutingTable.builder(shrinkIndex);
        for (int i = 0; i < shrinkNumberOfShards; i++) {
            builder.addShard(TestShardRouting.newShardRouting(new ShardId(shrinkIndex, i),
                nodeId, true, ShardRoutingState.STARTED));
        }
        ClusterState clusterState = ClusterState.builder(ClusterName.DEFAULT)
            .metaData(metaData)
            .nodes(DiscoveryNodes.builder().localNodeId(nodeId).masterNodeId(nodeId).add(masterNode).build())
            .routingTable(RoutingTable.builder().add(builder.build()).build()).build();

        Result result = step.isConditionMet(originalIndexMetadata.getIndex(), clusterState);
        assertTrue(result.isComplete());
        assertNull(result.getInfomationContext());
    }

    public void testConditionNotMetBecauseOfActive() {
        ShrunkShardsAllocatedStep step = createRandomInstance();
        int shrinkNumberOfShards = randomIntBetween(1, 5);
        int originalNumberOfShards = randomIntBetween(1, 5);
        String originalIndexName = randomAlphaOfLength(5);
        IndexMetaData originalIndexMetadata = IndexMetaData.builder(originalIndexName)
            .settings(settings(Version.CURRENT))
            .numberOfShards(originalNumberOfShards)
            .numberOfReplicas(0).build();
        IndexMetaData shrunkIndexMetadata = IndexMetaData.builder(step.getShrunkIndexPrefix() + originalIndexName)
                .settings(settings(Version.CURRENT))
                .numberOfShards(shrinkNumberOfShards)
                .numberOfReplicas(0).build();
        MetaData metaData = MetaData.builder()
            .persistentSettings(settings(Version.CURRENT).build())
            .put(IndexMetaData.builder(originalIndexMetadata))
            .put(IndexMetaData.builder(shrunkIndexMetadata))
            .build();
        Index shrinkIndex = shrunkIndexMetadata.getIndex();

        String nodeId = randomAlphaOfLength(10);
        DiscoveryNode masterNode = DiscoveryNode.createLocal(settings(Version.CURRENT)
                .put(Node.NODE_MASTER_SETTING.getKey(), true).build(),
            new TransportAddress(TransportAddress.META_ADDRESS, 9300), nodeId);

        IndexRoutingTable.Builder builder = IndexRoutingTable.builder(shrinkIndex);
        for (int i = 0; i < shrinkNumberOfShards; i++) {
            builder.addShard(TestShardRouting.newShardRouting(new ShardId(shrinkIndex, i),
                nodeId, true, ShardRoutingState.INITIALIZING));
        }
        ClusterState clusterState = ClusterState.builder(ClusterName.DEFAULT)
            .metaData(metaData)
            .nodes(DiscoveryNodes.builder().localNodeId(nodeId).masterNodeId(nodeId).add(masterNode).build())
            .routingTable(RoutingTable.builder().add(builder.build()).build()).build();

        Result result = step.isConditionMet(originalIndexMetadata.getIndex(), clusterState);
        assertFalse(result.isComplete());
        assertEquals(new ShrunkShardsAllocatedStep.Info(true, shrinkNumberOfShards, false),
                result.getInfomationContext());
    }

    public void testConditionNotMetBecauseOfShrunkIndexDoesntExistYet() {
        ShrunkShardsAllocatedStep step = createRandomInstance();
        int originalNumberOfShards = randomIntBetween(1, 5);
        String originalIndexName = randomAlphaOfLength(5);
        IndexMetaData originalIndexMetadata = IndexMetaData.builder(originalIndexName)
            .settings(settings(Version.CURRENT))
            .numberOfShards(originalNumberOfShards)
            .numberOfReplicas(0).build();
        MetaData metaData = MetaData.builder()
            .persistentSettings(settings(Version.CURRENT).build())
            .put(IndexMetaData.builder(originalIndexMetadata))
            .build();

        String nodeId = randomAlphaOfLength(10);
        DiscoveryNode masterNode = DiscoveryNode.createLocal(settings(Version.CURRENT)
                .put(Node.NODE_MASTER_SETTING.getKey(), true).build(),
            new TransportAddress(TransportAddress.META_ADDRESS, 9300), nodeId);
        ClusterState clusterState = ClusterState.builder(ClusterName.DEFAULT)
            .metaData(metaData)
            .nodes(DiscoveryNodes.builder().localNodeId(nodeId).masterNodeId(nodeId).add(masterNode).build())
            .build();

        Result result = step.isConditionMet(originalIndexMetadata.getIndex(), clusterState);
        assertFalse(result.isComplete());
        assertEquals(new ShrunkShardsAllocatedStep.Info(false, -1, false), result.getInfomationContext());
    }
}
