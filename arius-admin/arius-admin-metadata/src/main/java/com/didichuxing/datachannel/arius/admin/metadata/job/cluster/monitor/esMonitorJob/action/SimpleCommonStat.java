package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esMonitorJob.action;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.model.indices.Routing;

public class SimpleCommonStat {
    @JSONField(name = "routing")
    private Routing routing;


    public Routing getRouting() {
        return routing;
    }

    public void setRouting(Routing routing) {
        this.routing = routing;
    }

}
