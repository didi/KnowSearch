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

import org.elasticsearch.cluster.routing.RoutingChangesObserver;
import org.elasticsearch.cluster.routing.RoutingNodes;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.UnassignedInfo;

/**
 * Records if changes were made to {@link RoutingNodes} during an allocation round.
 */
public class RoutingNodesChangedObserver implements RoutingChangesObserver {
    private boolean changed;

    private RoutingTable.Builder builder;

    /**
     * Returns whether changes were made
     */
    public boolean isChanged() {
        return changed;
    }

    @Override
    public void shardInitialized(ShardRouting unassignedShard, ShardRouting initializedShard) {
        assert unassignedShard.unassigned() : "expected unassigned shard " + unassignedShard;
        assert initializedShard.initializing() : "expected initializing shard " + initializedShard;

        builder.updateShard(unassignedShard, initializedShard);

        setChanged();
    }

    @Override
    public void shardStarted(ShardRouting initializingShard, ShardRouting startedShard) {
        assert initializingShard.initializing() : "expected initializing shard " + initializingShard;
        assert startedShard.started() : "expected started shard " + startedShard;

        builder.updateShard(initializingShard, startedShard);

        setChanged();
    }

    @Override
    public void relocationStarted(ShardRouting startedShard, ShardRouting sourceRelocatingShard, ShardRouting targetRelocatingShard) {
        assert startedShard.started() : "expected started shard " + startedShard;
        assert targetRelocatingShard.isRelocationTarget() : "expected relocation target shard " + targetRelocatingShard;

        builder.updateShard(startedShard, sourceRelocatingShard);

        setChanged();
    }

    @Override
    public void unassignedInfoUpdated(ShardRouting unassignedShard, ShardRouting updatedShard, UnassignedInfo newUnassignedInfo) {
        assert unassignedShard.unassigned() || updatedShard.unassigned() : "expected unassigned shard " + unassignedShard;

        builder.updateShard(unassignedShard, updatedShard);

        setChanged();
    }

    @Override
    public void shardFailed(ShardRouting failedShard, UnassignedInfo unassignedInfo) {
        assert failedShard.assignedToNode() : "expected assigned shard " + failedShard;

        builder.updateShard(failedShard, null);

        setChanged();
    }

    @Override
    public void relocationCompleted(ShardRouting removedRelocationSource) {
        assert removedRelocationSource.relocating() : "expected relocating shard " + removedRelocationSource;

        builder.updateShard(removedRelocationSource, null);

        setChanged();
    }

    @Override
    public void cancelRelocation(ShardRouting relocatingShard, ShardRouting cancelledShard) {
        assert relocatingShard.relocating() : "expected relocating shard " + relocatingShard;

        builder.updateShard(relocatingShard, cancelledShard);

        setChanged();
    }

    @Override
    public void removeRelocationSource(ShardRouting relocationShard, ShardRouting shardRouting) {
        assert relocationShard.relocatingNodeId() != null : "expected relocation node id shard " + relocationShard;

        builder.updateShard(relocationShard, shardRouting);

        setChanged();
    }

    @Override
    public void remove(ShardRouting shardRouting) {
        assert shardRouting.assignedToNode() : "expected assigned shard " + shardRouting;

        builder.updateShard(shardRouting, null);

        setChanged();
    }

    @Override
    public void relocationSourceRemoved(ShardRouting removedReplicaRelocationSource) {
        assert removedReplicaRelocationSource.primary() == false && removedReplicaRelocationSource.isRelocationTarget() :
            "expected replica relocation target shard " + removedReplicaRelocationSource;

        builder.updateShard(removedReplicaRelocationSource, null);

        setChanged();
    }

    @Override
    public void replicaPromoted(ShardRouting replicaShard, ShardRouting primary) {
        assert replicaShard.started() && replicaShard.primary() == false : "expected started replica shard " + replicaShard;

        builder.updateShard(replicaShard, primary);

        setChanged();
    }

    @Override
    public void initializedReplicaReinitialized(ShardRouting oldReplica, ShardRouting reinitializedReplica) {
        assert oldReplica.initializing() && oldReplica.primary() == false :
            "expected initializing replica shard " + oldReplica;
        assert reinitializedReplica.initializing() && reinitializedReplica.primary() == false :
            "expected reinitialized replica shard " + reinitializedReplica;
        assert oldReplica.allocationId().getId().equals(reinitializedReplica.allocationId().getId()) == false :
            "expected allocation id to change for reinitialized replica shard (old: " + oldReplica + " new: " + reinitializedReplica + ")";

        builder.updateShard(oldReplica, reinitializedReplica);

        setChanged();
    }

    /**
     * Marks the allocation as changed.
     */
    private void setChanged() {
        changed = true;
    }

    public void setBuilder(RoutingTable.Builder builder) {
        this.builder = builder;
    }
}
