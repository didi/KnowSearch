/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.slm.action;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;
import org.elasticsearch.xpack.core.slm.action.DeleteSnapshotLifecycleAction;

public class RestDeleteSnapshotLifecycleAction extends BaseRestHandler {

    public RestDeleteSnapshotLifecycleAction(RestController controller) {
        controller.registerHandler(RestRequest.Method.DELETE, "/_slm/policy/{name}", this);
    }

    @Override
    public String getName() {
        return "slm_delete_lifecycle";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        String lifecycleId = request.param("name");
        DeleteSnapshotLifecycleAction.Request req = new DeleteSnapshotLifecycleAction.Request(lifecycleId);
        req.timeout(request.paramAsTime("timeout", req.timeout()));
        req.masterNodeTimeout(request.paramAsTime("master_timeout", req.masterNodeTimeout()));

        return channel -> client.execute(DeleteSnapshotLifecycleAction.INSTANCE, req, new RestToXContentListener<>(channel));
    }
}
