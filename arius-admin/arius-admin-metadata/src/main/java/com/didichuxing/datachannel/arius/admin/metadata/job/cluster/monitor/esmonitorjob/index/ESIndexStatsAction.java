package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.index;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESIndexStatsAction extends Action<ESIndexStatsRequest, ESIndexStatsResponse, ESIndexStatsRequestBuilder> {

    public static final ESIndexStatsAction INSTANCE = new ESIndexStatsAction();
    public static final String NAME = "indices:stats/tolerance";

    private ESIndexStatsAction() {
        super(NAME);
    }

    @Override
    public ESIndexStatsResponse newResponse() {
        return new ESIndexStatsResponse();
    }

    @Override
    public ESIndexStatsRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESIndexStatsRequestBuilder(client, this);
    }
}
