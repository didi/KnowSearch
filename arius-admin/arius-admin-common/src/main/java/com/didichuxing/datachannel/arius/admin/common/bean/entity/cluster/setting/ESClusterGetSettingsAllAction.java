package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ESClusterGetSettingsAllAction extends Action<ESClusterGetSettingsAllRequest, ESClusterGetSettingsAllResponse, ESClusterGetSettingsAllRequestBuilder> {

    public static final ESClusterGetSettingsAllAction INSTANCE = new ESClusterGetSettingsAllAction();
    public static final String NAME = "cluster:settings/get";

    private ESClusterGetSettingsAllAction() {
        super(NAME);
    }

    @Override
    public ESClusterGetSettingsAllResponse newResponse() {
        return new ESClusterGetSettingsAllResponse();
    }

    @Override
    public ESClusterGetSettingsAllRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ESClusterGetSettingsAllRequestBuilder(client, this);
    }
}
