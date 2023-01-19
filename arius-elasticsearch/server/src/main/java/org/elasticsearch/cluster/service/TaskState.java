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

package org.elasticsearch.cluster.service;

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodeRole;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * task execution time
 */
public class TaskState {

    private final ClusterState clusterState;
    private String source;
    private String taskType;
    private String taskResult;
    private String failure;
    private boolean clusterStateChanged;
    private long insertionOrder;
    private Priority priority;
    private long submitTimeMillis;
    private long processTimeMillis;
    private long queue;
    private long execute;
    private long publish;
    private long process;
    private long total;
    private Map<String, Long> appliers = new HashMap<>();
    private Map<String, Long> listeners = new HashMap<>();

    public TaskState(ClusterState clusterState, String source, Priority priority) {
        this.clusterState = clusterState;
        this.source = source;
        this.priority = priority;
        this.submitTimeMillis = System.currentTimeMillis();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(String taskResult) {
        this.taskResult = taskResult;
    }

    public String getFailure() {
        return failure;
    }

    public void setFailure(String failure) {
        this.failure = failure;
    }

    public long getInsertionOrder() {
        return insertionOrder;
    }

    public void setInsertionOrder(long insertionOrder) {
        this.insertionOrder = insertionOrder;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public long getSubmitTimeMillis() {
        return submitTimeMillis;
    }

    public void setSubmitTimeMillis(long submitTimeMillis) {
        this.submitTimeMillis = submitTimeMillis;
    }

    public long getProcessTimeMillis() {
        return processTimeMillis;
    }

    public void setProcessTimeMillis(long processTimeMillis) {
        this.processTimeMillis = processTimeMillis;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProcess() {
        return process;
    }

    public void setProcess(long process) {
        this.process = process;
    }

    public long getQueue() {
        return queue;
    }

    public void setQueue(long queue) {
        this.queue = queue;
    }

    public long getExecute() {
        return execute;
    }

    public void setExecute(long execute) {
        this.execute = execute;
    }

    public long getPublish() {
        return publish;
    }

    public void setPublish(long publish) {
        this.publish = publish;
    }

    public boolean isClusterStateChanged() {
        return clusterStateChanged;
    }

    public void setClusterStateChanged(boolean clusterStateChanged) {
        this.clusterStateChanged = clusterStateChanged;
    }

    public Map<String, Long> getAppliers() {
        return appliers;
    }

    public void setAppliers(Map<String, Long> appliers) {
        this.appliers = appliers;
    }

    public Map<String, Long> getListeners() {
        return listeners;
    }

    public void setListeners(Map<String, Long> listeners) {
        this.listeners = listeners;
    }

    @Override
    public String toString() {
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();

            jsonBuilder.field("clusterName", clusterState.getClusterName().value());
            jsonBuilder.field("totalNodes", clusterState.nodes().getSize());
            jsonBuilder.field("totalIndices", clusterState.metaData().indices().size());
            jsonBuilder.field("totalShards", clusterState.metaData().getTotalNumberOfShards());
            jsonBuilder.field("avgShardsPerNode", clusterState.metaData().getTotalNumberOfShards() / clusterState.nodes().getSize());
            DiscoveryNode localNode = clusterState.nodes().getLocalNode();
            jsonBuilder.field("node", localNode.getName());
            jsonBuilder.field("ip", localNode.getHostAddress());
            jsonBuilder.array("roles", localNode.getRoles().stream().map(DiscoveryNodeRole::roleName).toArray(String[]::new));
            jsonBuilder.field("isActiveMaster", clusterState.nodes().isLocalNodeElectedMaster());
            jsonBuilder.field("source", source);
            jsonBuilder.field("taskType", taskType);
            jsonBuilder.field("taskResult", taskResult);
            if (Strings.hasText(failure)) {
                jsonBuilder.field("failure", failure);
            }
            jsonBuilder.field("clusterStateChanged", clusterStateChanged);
            jsonBuilder.field("insertionOrder", insertionOrder);
            jsonBuilder.field("priority", priority.name());
            jsonBuilder.field("submitTimeMillis", submitTimeMillis);
            jsonBuilder.field("processTimeMillis", processTimeMillis);
            jsonBuilder.field("queue", queue);
            jsonBuilder.field("execute", execute);
            jsonBuilder.field("publish", publish);
            jsonBuilder.field("process", process);
            jsonBuilder.field("total", total);

            jsonBuilder.startObject("appliers");
            for (Map.Entry<String, Long> entry : this.appliers.entrySet()) {
                jsonBuilder.field(entry.getKey(), entry.getValue());
            }
            jsonBuilder.endObject();

            jsonBuilder.startObject("listeners");
            for (Map.Entry<String, Long> entry : this.listeners.entrySet()) {
                jsonBuilder.field(entry.getKey(), entry.getValue());
            }
            jsonBuilder.endObject();

            jsonBuilder.endObject();

            jsonBuilder.flush();
            BytesReference bytes = BytesReference.bytes(jsonBuilder);
            return bytes.utf8ToString();
        } catch (Exception e) {}

        return null;
    }

    public void parseSourceToTaskType() {
        if (Strings.hasText(source)) {
            int i = source.indexOf(":");
            if (i > 0) {
                this.taskType = source.substring(0, i);
            } else {
                this.taskType = source;
            }
        } else {
            this.taskType = "null";
        }
    }
}
