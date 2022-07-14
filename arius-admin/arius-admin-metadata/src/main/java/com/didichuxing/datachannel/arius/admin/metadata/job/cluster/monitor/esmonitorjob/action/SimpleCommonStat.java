package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.action;

import com.alibaba.fastjson.annotation.JSONField;
import com.didiglobal.logi.elasticsearch.client.response.model.indices.Routing;

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
