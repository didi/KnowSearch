package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.setting;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class ESClusterGetSettingsAllRequestBuilder extends ActionRequestBuilder<ESClusterGetSettingsAllRequest, ESClusterGetSettingsAllResponse, ESClusterGetSettingsAllRequestBuilder> {

    public ESClusterGetSettingsAllRequestBuilder(ElasticsearchClient client, ESClusterGetSettingsAllAction action) {
        super(client, action, new ESClusterGetSettingsAllRequest());
    }
}