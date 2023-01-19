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

package org.elasticsearch.cluster.routing;

/**
 * Records changes made to {@link RoutingNodes} during an allocation round.
 */
public interface RoutingChangesObserver {
    /**
     * Called when unassigned shard is initialized. Does not include initializing relocation target shards.
     */
    void shardInitialized(ShardRouting unassignedShard, ShardRouting initializedShard);

    /**
     * Called when an initializing shard is started.
     */
    void shardStarted(ShardRouting initializingShard, ShardRouting startedShard);

    /**
     * Called when relocation of a started shard is initiated.
     */
    void relocationStarted(ShardRouting startedShard, ShardRouting sourceRelocatingShard, ShardRouting targetRelocatingShard);

    /**
     * Called when an unassigned shard's unassigned information was updated
     */
    void unassignedInfoUpdated(ShardRouting unassignedShard, ShardRouting updatedShard, UnassignedInfo newUnassignedInfo);

    /**
     * Called when a shard is failed or cancelled.
     */
    void shardFailed(ShardRouting failedShard, UnassignedInfo unassignedInfo);

    /**
     * Called on relocation source when relocation completes after relocation target is started.
     */
    void relocationCompleted(ShardRouting removedRelocationSource);

    /**
     * cancel relocation when shard failed.
     */
    void cancelRelocation(ShardRouting relocatingShard, ShardRouting cancelledShard);

    /**
     * remove relocationNodeId when source shard failed.
     */
    void removeRelocationSource(ShardRouting relocationShard, ShardRouting shardRouting);

    /**
     * remove shardRouting.
     */
    void remove(ShardRouting shardRouting);

    /**
     * Called on replica relocation target when replica relocation source fails. Promotes the replica relocation target to ordinary
     * initializing shard.
     */
    void relocationSourceRemoved(ShardRouting removedReplicaRelocationSource);

    /**
     * Called when started replica is promoted to primary.
     */
    void replicaPromoted(ShardRouting replicaShard, ShardRouting primary);

    /**
     * Called when an initializing replica is reinitialized. This happens when a primary relocation completes, which
     * reinitializes all currently initializing replicas as their recovery source node changes
     */
    void initializedReplicaReinitialized(ShardRouting oldReplica, ShardRouting reinitializedReplica);


    /**
     * Abstract implementation of {@link RoutingChangesObserver} that does not take any action. Useful for subclasses that only override
     * certain methods.
     */
    class AbstractRoutingChangesObserver implements RoutingChangesObserver {

        @Override
        public void shardInitialized(ShardRouting unassignedShard, ShardRouting initializedShard) {

        }

        @Override
        public void shardStarted(ShardRouting initializingShard, ShardRouting startedShard) {

        }

        @Override
        public void relocationStarted(ShardRouting startedShard, ShardRouting sourceRelocatingShard, ShardRouting targetRelocatingShard) {

        }

        @Override
        public void unassignedInfoUpdated(ShardRouting unassignedShard, ShardRouting updatedShard, UnassignedInfo newUnassignedInfo) {

        }

        @Override
        public void shardFailed(ShardRouting activeShard, UnassignedInfo unassignedInfo) {

        }

        @Override
        public void relocationCompleted(ShardRouting removedRelocationSource) {

        }

        @Override
        public void cancelRelocation(ShardRouting relocatingShard, ShardRouting cancelledShard) {

        }

        @Override
        public void removeRelocationSource(ShardRouting relocationShard, ShardRouting shardRouting) {

        }

        @Override
        public void remove(ShardRouting shardRouting) {

        }

        @Override
        public void relocationSourceRemoved(ShardRouting removedReplicaRelocationSource) {

        }

        @Override
        public void replicaPromoted(ShardRouting replicaShard, ShardRouting primary) {

        }

        @Override
        public void initializedReplicaReinitialized(ShardRouting oldReplica, ShardRouting reinitializedReplica) {

        }
    }

    class DelegatingRoutingChangesObserver implements RoutingChangesObserver {

        private final RoutingChangesObserver[] routingChangesObservers;

        public DelegatingRoutingChangesObserver(RoutingChangesObserver... routingChangesObservers) {
            this.routingChangesObservers = routingChangesObservers;
        }

        @Override
        public void shardInitialized(ShardRouting unassignedShard, ShardRouting initializedShard) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.shardInitialized(unassignedShard, initializedShard);
            }
        }

        @Override
        public void shardStarted(ShardRouting initializingShard, ShardRouting startedShard) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.shardStarted(initializingShard, startedShard);
            }
        }

        @Override
        public void relocationStarted(ShardRouting startedShard, ShardRouting sourceRelocatingShard, ShardRouting targetRelocatingShard) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.relocationStarted(startedShard, sourceRelocatingShard, targetRelocatingShard);
            }
        }

        @Override
        public void unassignedInfoUpdated(ShardRouting unassignedShard, ShardRouting updatedShard, UnassignedInfo newUnassignedInfo) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.unassignedInfoUpdated(unassignedShard, updatedShard, newUnassignedInfo);
            }
        }

        @Override
        public void shardFailed(ShardRouting activeShard, UnassignedInfo unassignedInfo) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.shardFailed(activeShard, unassignedInfo);
            }
        }

        @Override
        public void relocationCompleted(ShardRouting removedRelocationSource) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.relocationCompleted(removedRelocationSource);
            }
        }

        @Override
        public void cancelRelocation(ShardRouting relocatingShard, ShardRouting cancelledShard) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.cancelRelocation(relocatingShard, cancelledShard);
            }
        }

        @Override
        public void removeRelocationSource(ShardRouting relocationShard, ShardRouting shardRouting) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.removeRelocationSource(relocationShard, shardRouting);
            }
        }

        @Override
        public void remove(ShardRouting shardRouting) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.remove(shardRouting);
            }
        }

        @Override
        public void relocationSourceRemoved(ShardRouting removedReplicaRelocationSource) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.relocationSourceRemoved(removedReplicaRelocationSource);
            }
        }

        @Override
        public void replicaPromoted(ShardRouting replicaShard, ShardRouting primary) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.replicaPromoted(replicaShard, primary);
            }
        }

        @Override
        public void initializedReplicaReinitialized(ShardRouting oldReplica, ShardRouting reinitializedReplica) {
            for (RoutingChangesObserver routingChangesObserver : routingChangesObservers) {
                routingChangesObserver.initializedReplicaReinitialized(oldReplica, reinitializedReplica);
            }
        }
    }
}
