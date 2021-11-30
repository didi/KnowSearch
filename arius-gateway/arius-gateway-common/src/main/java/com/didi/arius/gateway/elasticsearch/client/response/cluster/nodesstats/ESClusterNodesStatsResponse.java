package com.didi.arius.gateway.elasticsearch.client.response.cluster.nodesstats;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;

import java.util.Map;

public class ESClusterNodesStatsResponse extends ESActionResponse {
    @JSONField(name = "cluster_name")
    private String clusterName;

    @JSONField(name = "nodes")
    private Map<String, ClusterNodeStats> nodes;

    public ESClusterNodesStatsResponse() {
        // pass
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Map<String, ClusterNodeStats> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, ClusterNodeStats> nodes) {
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
