package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didiglobal.logi.elasticsearch.client.model.ESActionResponse;
import com.didiglobal.logi.elasticsearch.client.response.cluster.nodes.ClusterNodeInfo;

import java.util.Map;

public class ESNodesResponse extends ESActionResponse {
    @JSONField(name = "cluster_name")
    private String clusterName;

    @JSONField(name = "nodes")
    private Map<String, ClusterNodeInfo> nodes;

    private int failedNodes;

    public int getFailedNodes() {
        return failedNodes;
    }

    public void setFailedNodes(int failedNodes) {
        this.failedNodes = failedNodes;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Map<String, ClusterNodeInfo> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, ClusterNodeInfo> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return toJson().toJSONString();
    }

    public JSONObject toJson() {
        return (JSONObject) JSON.toJSON(this);
    }
}
