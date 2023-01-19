/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.transform.rest.action;


import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;
import org.elasticsearch.xpack.core.transform.TransformField;
import org.elasticsearch.xpack.core.transform.action.DeleteTransformAction;

public class RestDeleteTransformAction extends BaseRestHandler {

    public RestDeleteTransformAction(RestController controller) {
        controller.registerHandler(RestRequest.Method.DELETE,  TransformField.REST_BASE_PATH_TRANSFORMS_BY_ID, this);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) {
        if (restRequest.hasContent()) {
            throw new IllegalArgumentException("delete transform requests can not have a request body");
        }

        String id = restRequest.param(TransformField.ID.getPreferredName());
        boolean force = restRequest.paramAsBoolean(TransformField.FORCE.getPreferredName(), false);
        DeleteTransformAction.Request request = new DeleteTransformAction.Request(id, force);

        return channel -> client.execute(DeleteTransformAction.INSTANCE, request,
                new RestToXContentListener<>(channel));
    }

    @Override
    public String getName() {
        return "transform_delete_transform_action";
    }
}
