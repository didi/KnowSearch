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

package com.didi.arius.gateway.elasticsearch.client.response.cluster;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;

public class ESClusterHealthResponse extends ESActionResponse {
    @JSONField(name = "cluster_name")
    private String clusterName;

    @JSONField(name = "status")
    private String status;

    @JSONField(name = "timed_out")
    private boolean timedOut;

    @JSONField(name = "number_of_nodes")
    private long numberOfNodes;

    @JSONField(name = "number_of_data_nodes")
    private long numberOfDataNodes;

    @JSONField(name = "active_primary_shards")
    private long activePrimaryShards;

    @JSONField(name = "active_shards")
    private long activeShards;

    @JSONField(name = "relocating_shards")
    private long relocatingShards;

    @JSONField(name = "initializing_shards")
    private long initializingShards;

    @JSONField(name = "unassigned_shards")
    private long unassignedShards;

    @JSONField(name = "delayed_unassigned_shards")
    private long delayedUnassignedShards;

    @JSONField(name = "number_of_pending_tasks")
    private long numberOfPendingTasks;

    @JSONField(name = "number_of_in_flight_fetch")
    private long numberOfInFlightFetch;

    @JSONField(name = "task_max_waiting_in_queue_millis")
    private long taskMaxWaitingInQueueMillis;

    @JSONField(name = "active_shards_percent_as_number")
    private long activeShardsPercentAsNumber;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
    }

    public long getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(long numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public long getNumberOfDataNodes() {
        return numberOfDataNodes;
    }

    public void setNumberOfDataNodes(long numberOfDataNodes) {
        this.numberOfDataNodes = numberOfDataNodes;
    }

    public long getActivePrimaryShards() {
        return activePrimaryShards;
    }

    public void setActivePrimaryShards(long activePrimaryShards) {
        this.activePrimaryShards = activePrimaryShards;
    }

    public long getActiveShards() {
        return activeShards;
    }

    public void setActiveShards(long activeShards) {
        this.activeShards = activeShards;
    }

    public long getRelocatingShards() {
        return relocatingShards;
    }

    public void setRelocatingShards(long relocatingShards) {
        this.relocatingShards = relocatingShards;
    }

    public long getInitializingShards() {
        return initializingShards;
    }

    public void setInitializingShards(long initializingShards) {
        this.initializingShards = initializingShards;
    }

    public long getUnassignedShards() {
        return unassignedShards;
    }

    public void setUnassignedShards(long unassignedShards) {
        this.unassignedShards = unassignedShards;
    }

    public long getDelayedUnassignedShards() {
        return delayedUnassignedShards;
    }

    public void setDelayedUnassignedShards(long delayedUnassignedShards) {
        this.delayedUnassignedShards = delayedUnassignedShards;
    }

    public long getNumberOfPendingTasks() {
        return numberOfPendingTasks;
    }

    public void setNumberOfPendingTasks(long numberOfPendingTasks) {
        this.numberOfPendingTasks = numberOfPendingTasks;
    }

    public long getNumberOfInFlightFetch() {
        return numberOfInFlightFetch;
    }

    public void setNumberOfInFlightFetch(long numberOfInFlightFetch) {
        this.numberOfInFlightFetch = numberOfInFlightFetch;
    }

    public long getTaskMaxWaitingInQueueMillis() {
        return taskMaxWaitingInQueueMillis;
    }

    public void setTaskMaxWaitingInQueueMillis(long taskMaxWaitingInQueueMillis) {
        this.taskMaxWaitingInQueueMillis = taskMaxWaitingInQueueMillis;
    }

    public long getActiveShardsPercentAsNumber() {
        return activeShardsPercentAsNumber;
    }

    public void setActiveShardsPercentAsNumber(long activeShardsPercentAsNumber) {
        this.activeShardsPercentAsNumber = activeShardsPercentAsNumber;
    }


    @Override
    public String toString() {
        return toJson().toJSONString();
    }

    public JSONObject toJson() {
        return (JSONObject) JSONObject.toJSON(this);
    }
}
