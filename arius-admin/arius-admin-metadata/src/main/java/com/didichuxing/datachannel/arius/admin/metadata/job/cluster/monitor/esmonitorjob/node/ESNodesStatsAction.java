package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESNodesStatsAction extends Action<ESNodesStatsRequest, ESNodesStatsResponse, ESNodesStatsRequestBuilder> {

    public static final ESNodesStatsAction INSTANCE = new ESNodesStatsAction();
    public static final String NAME = "cluster:nodes/tolerance/stats";

    private ESNodesStatsAction() {
        super(NAME);
    }

    @Override
    public ESNodesStatsResponse newResponse() {
        return new ESNodesStatsResponse();
    }

    @Override
    public ESNodesStatsRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESNodesStatsRequestBuilder(client, this);
    }
}
