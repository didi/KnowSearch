package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESNodesAction extends Action<ESNodesRequest, ESNodesResponse, ESNodesRequestBuilder> {

    public static final ESNodesAction INSTANCE = new ESNodesAction();
    public static final String NAME = "cluster:nodes/tolerance";

    private ESNodesAction() {
        super(NAME);
    }

    @Override
    public ESNodesResponse newResponse() {
        return new ESNodesResponse();
    }

    @Override
    public ESNodesRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESNodesRequestBuilder(client, this);
    }
}
