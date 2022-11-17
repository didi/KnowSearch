package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.action;

import com.alibaba.fastjson.annotation.JSONField;
import com.didiglobal.knowframework.elasticsearch.client.response.model.indices.CommonStat;

import java.util.List;
import java.util.Map;

public class SimpleIndexNode {
    @JSONField(name = "primaries")
    private CommonStat                          primaries;

    @JSONField(name = "total")
    private CommonStat                          total;

    @JSONField(name = "shards")
    private Map<String, List<SimpleCommonStat>> shards;

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

    public Map<String, List<SimpleCommonStat>> getShards() {
        return shards;
    }

    public void setShards(Map<String, List<SimpleCommonStat>> shards) {
        this.shards = shards;
    }
}
