package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESNodeRequestBuilder extends ActionRequestBuilder<ESNodeRequest, ESNodeResponse, ESNodeRequestBuilder> {

    public ESNodeRequestBuilder(ElasticsearchClient client, ESNodeAction action) {
        super(client, action, new ESNodeRequest());
    }

    public ESNodeRequestBuilder clearFlag() {
        request.clear();
        return this;
    }

    public ESNodeRequestBuilder nodeIds(String nodeIds) {
        request.nodeIds(nodeIds);
        return this;
    }

    public ESNodeRequestBuilder addFlag(String flagName) {
        request.flag(flagName);
        return this;
    }

    public ESNodeRequestBuilder timeout(String timeout) {
        request.timeout(timeout);
        return this;
    }
}
