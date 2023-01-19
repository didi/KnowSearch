/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.transform.rest.action.compat;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;
import org.elasticsearch.xpack.core.transform.TransformField;
import org.elasticsearch.xpack.core.transform.TransformMessages;
import org.elasticsearch.xpack.core.transform.action.StopTransformAction;
import org.elasticsearch.xpack.core.transform.action.compat.StopTransformActionDeprecated;

public class RestStopTransformActionDeprecated extends BaseRestHandler {

    private static final DeprecationLogger deprecationLogger = new DeprecationLogger(
            LogManager.getLogger(RestStopTransformActionDeprecated.class));

    public RestStopTransformActionDeprecated(RestController controller) {
        controller.registerAsDeprecatedHandler(RestRequest.Method.POST, TransformField.REST_BASE_PATH_TRANSFORMS_BY_ID_DEPRECATED + "_stop",
                this, TransformMessages.REST_DEPRECATED_ENDPOINT, deprecationLogger);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) {
        String id = restRequest.param(TransformField.ID.getPreferredName());
        TimeValue timeout = restRequest.paramAsTime(TransformField.TIMEOUT.getPreferredName(),
                StopTransformAction.DEFAULT_TIMEOUT);
        boolean waitForCompletion = restRequest.paramAsBoolean(TransformField.WAIT_FOR_COMPLETION.getPreferredName(), false);
        boolean force = restRequest.paramAsBoolean(TransformField.FORCE.getPreferredName(), false);
        boolean allowNoMatch = restRequest.paramAsBoolean(TransformField.ALLOW_NO_MATCH.getPreferredName(), false);
        boolean waitForCheckpoint = restRequest.paramAsBoolean(TransformField.WAIT_FOR_CHECKPOINT.getPreferredName(), false);


        StopTransformAction.Request request = new StopTransformAction.Request(id,
            waitForCompletion,
            force,
            timeout,
            allowNoMatch,
            waitForCheckpoint);

        return channel -> client.execute(StopTransformActionDeprecated.INSTANCE, request,
                new RestToXContentListener<>(channel));
    }

    @Override
    public String getName() {
        return "data_frame_stop_transform_action";
    }
}
