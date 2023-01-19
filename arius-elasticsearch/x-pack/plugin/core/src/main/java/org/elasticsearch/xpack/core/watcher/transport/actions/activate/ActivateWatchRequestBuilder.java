/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.watcher.transport.actions.activate;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * A activate watch action request builder.
 */
public class ActivateWatchRequestBuilder extends ActionRequestBuilder<ActivateWatchRequest, ActivateWatchResponse> {

    public ActivateWatchRequestBuilder(ElasticsearchClient client) {
        super(client, ActivateWatchAction.INSTANCE, new ActivateWatchRequest());
    }

    public ActivateWatchRequestBuilder(ElasticsearchClient client, String id, boolean activate) {
        super(client, ActivateWatchAction.INSTANCE, new ActivateWatchRequest(id, activate));
    }

}
