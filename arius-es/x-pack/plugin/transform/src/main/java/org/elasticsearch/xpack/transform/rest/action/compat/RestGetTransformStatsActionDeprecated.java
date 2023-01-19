/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.transform.rest.action.compat;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;
import org.elasticsearch.xpack.core.action.util.PageParams;
import org.elasticsearch.xpack.core.transform.TransformField;
import org.elasticsearch.xpack.core.transform.TransformMessages;
import org.elasticsearch.xpack.core.transform.action.GetTransformStatsAction;
import org.elasticsearch.xpack.core.transform.action.compat.GetTransformStatsActionDeprecated;

import static org.elasticsearch.xpack.core.transform.TransformField.ALLOW_NO_MATCH;

public class RestGetTransformStatsActionDeprecated extends BaseRestHandler {

    private static final DeprecationLogger deprecationLogger = new DeprecationLogger(
            LogManager.getLogger(RestGetTransformStatsActionDeprecated.class));

    public RestGetTransformStatsActionDeprecated(RestController controller) {
        controller.registerAsDeprecatedHandler(RestRequest.Method.GET, TransformField.REST_BASE_PATH_TRANSFORMS_DEPRECATED + "_stats",
                this, TransformMessages.REST_DEPRECATED_ENDPOINT, deprecationLogger);
        controller.registerAsDeprecatedHandler(RestRequest.Method.GET,
                TransformField.REST_BASE_PATH_TRANSFORMS_BY_ID_DEPRECATED + "_stats", this,
                TransformMessages.REST_DEPRECATED_ENDPOINT, deprecationLogger);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) {
        String id = restRequest.param(TransformField.ID.getPreferredName());
        GetTransformStatsAction.Request request = new GetTransformStatsAction.Request(id);
        request.setAllowNoMatch(restRequest.paramAsBoolean(ALLOW_NO_MATCH.getPreferredName(), true));
        if (restRequest.hasParam(PageParams.FROM.getPreferredName()) || restRequest.hasParam(PageParams.SIZE.getPreferredName())) {
            request.setPageParams(
                new PageParams(restRequest.paramAsInt(PageParams.FROM.getPreferredName(), PageParams.DEFAULT_FROM),
                    restRequest.paramAsInt(PageParams.SIZE.getPreferredName(), PageParams.DEFAULT_SIZE)));
        }
        return channel -> client.execute(GetTransformStatsActionDeprecated.INSTANCE, request,
                new RestToXContentListener<>(channel));
    }

    @Override
    public String getName() {
        return "data_frame_get_transforms_stats_action";
    }
}
