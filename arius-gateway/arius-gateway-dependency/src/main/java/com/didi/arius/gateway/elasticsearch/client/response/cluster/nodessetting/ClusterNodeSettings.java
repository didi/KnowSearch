package com.didi.arius.gateway.elasticsearch.client.response.cluster.nodessetting;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.response.model.node.NodeAttributes;
import com.didi.arius.gateway.elasticsearch.client.utils.JsonUtils;

import java.util.Map;

public class ClusterNodeSettings {
    @JSONField(name = "name")
    private String name;

    @JSONField(name = "transport_address")
    private String transportAddress;

    @JSONField(name = "host")
    private String host;

    @JSONField(name = "ip")
    private String ip;

    @JSONField(name = "version")
    private String version;

    @JSONField(name = "build")
    private String build;

    @JSONField(name = "http_address")
    private String httpAddress;

    @JSONField(name = "attributes")
    private NodeAttributes attributes;

    @JSONField(name = "settings")
    private JSONObject settings;


    @JSONField(serialize = false)
    public Map<String, String> getSettingsByFlat() {
        return JsonUtils.flat(settings);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransportAddress() {
        return transportAddress;
    }

    public void setTransportAddress(String transportAddress) {
        this.transportAddress = transportAddress;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getHttpAddress() {
        return httpAddress;
    }

    public void setHttpAddress(String httpAddress) {
        this.httpAddress = httpAddress;
    }

    public NodeAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(NodeAttributes attributes) {
        this.attributes = attributes;
    }

    public JSONObject getSettings() {
        return settings;
    }

    public void setSettings(JSONObject settings) {
        this.settings = settings;
    }
}


