package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESNodesRequestBuilder extends ActionRequestBuilder<ESNodesRequest, ESNodesResponse, ESNodesRequestBuilder> {

    public ESNodesRequestBuilder(ElasticsearchClient client, ESNodesAction action) {
        super(client, action, new ESNodesRequest());
    }

    public ESNodesRequestBuilder clearFlag() {
        request.clear();
        return this;
    }

    public ESNodesRequestBuilder nodeIds(String nodeIds) {
        request.nodeIds(nodeIds);
        return this;
    }

    public ESNodesRequestBuilder addFlag(String flagName) {
        request.flag(flagName);
        return this;
    }

    public ESNodesRequestBuilder timeout(String timeout) {
        request.timeout(timeout);
        return this;
    }
}
