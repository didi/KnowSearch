package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESNodeStatsAction extends Action<ESNodeStatsRequest, ESNodeStatsResponse, ESNodeStatsRequestBuilder> {

    public static final ESNodeStatsAction INSTANCE = new ESNodeStatsAction();
    public static final String NAME = "cluster:nodes/tolerance/stats";

    private ESNodeStatsAction() {
        super(NAME);
    }

    @Override
    public ESNodeStatsResponse newResponse() {
        return new ESNodeStatsResponse();
    }

    @Override
    public ESNodeStatsRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESNodeStatsRequestBuilder(client, this);
    }
}
