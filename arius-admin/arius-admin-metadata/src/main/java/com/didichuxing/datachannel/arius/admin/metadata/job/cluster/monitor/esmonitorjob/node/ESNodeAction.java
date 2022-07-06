package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESNodeAction extends Action<ESNodeRequest, ESNodeResponse, ESNodeRequestBuilder> {

    public static final ESNodeAction INSTANCE = new ESNodeAction();
    public static final String NAME = "cluster:nodes/tolerance";

    private ESNodeAction() {
        super(NAME);
    }

    @Override
    public ESNodeResponse newResponse() {
        return new ESNodeResponse();
    }

    @Override
    public ESNodeRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESNodeRequestBuilder(client, this);
    }
}
