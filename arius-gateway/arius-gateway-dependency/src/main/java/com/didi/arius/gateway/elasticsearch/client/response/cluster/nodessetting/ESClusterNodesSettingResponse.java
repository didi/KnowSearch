package com.didi.arius.gateway.elasticsearch.client.response.cluster.nodessetting;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.model.ESActionResponse;

import java.util.Map;

public class ESClusterNodesSettingResponse extends ESActionResponse {
    @JSONField(name = "cluster_name")
    private String clusterName;

    @JSONField(name = "nodes")
    private Map<String, ClusterNodeSettings> nodes;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Map<String, ClusterNodeSettings> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, ClusterNodeSettings> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return toJson().toJSONString();
    }

    public JSONObject toJson() {
        return (JSONObject) JSONObject.toJSON(this);
    }
}
