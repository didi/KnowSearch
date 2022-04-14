package com.didi.arius.gateway.elasticsearch.client.response.indices.stats;

import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.response.model.indices.CommonStat;

import java.util.List;
import java.util.Map;

public class IndexNodes {
    @JSONField(name = "primaries")
    private CommonStat primaries;

    @JSONField(name = "total")
    private CommonStat total;

    @JSONField(name = "shards")
    private Map<String, List<CommonStat>> shards;

    public IndexNodes() {
        // pass
    }

    public CommonStat getPrimaries() {
        return primaries;
    }

    public void setPrimaries(CommonStat primaries) {
        this.primaries = primaries;
    }

    public CommonStat getTotal() {
        return total;
    }

    public void setTotal(CommonStat total) {
        this.total = total;
    }

    public Map<String, List<CommonStat>> getShards() {
        return shards;
    }

    public void setShards(Map<String, List<CommonStat>> shards) {
        this.shards = shards;
    }
}
