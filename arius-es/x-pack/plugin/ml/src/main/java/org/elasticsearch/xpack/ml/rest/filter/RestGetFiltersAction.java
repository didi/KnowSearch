/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.rest.filter;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestStatusToXContentListener;
import org.elasticsearch.xpack.core.action.util.PageParams;
import org.elasticsearch.xpack.core.ml.action.GetFiltersAction;
import org.elasticsearch.xpack.core.ml.job.config.MlFilter;
import org.elasticsearch.xpack.ml.MachineLearning;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;

public class RestGetFiltersAction extends BaseRestHandler {

    private static final DeprecationLogger deprecationLogger =
        new DeprecationLogger(LogManager.getLogger(RestGetFiltersAction.class));

    public RestGetFiltersAction(RestController controller) {
        // TODO: remove deprecated endpoint in 8.0.0
        controller.registerWithDeprecatedHandler(
            GET, MachineLearning.BASE_PATH + "filters/{" + MlFilter.ID.getPreferredName() + "}", this,
            GET, MachineLearning.PRE_V7_BASE_PATH + "filters/{" + MlFilter.ID.getPreferredName() + "}", deprecationLogger);
        controller.registerWithDeprecatedHandler(
            GET, MachineLearning.BASE_PATH + "filters/", this,
            GET, MachineLearning.PRE_V7_BASE_PATH + "filters/", deprecationLogger);
    }

    @Override
    public String getName() {
        return "ml_get_filters_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        GetFiltersAction.Request getListRequest = new GetFiltersAction.Request();
        String filterId = restRequest.param(MlFilter.ID.getPreferredName());
        if (!Strings.isNullOrEmpty(filterId)) {
            getListRequest.setFilterId(filterId);
        }
        if (restRequest.hasParam(PageParams.FROM.getPreferredName()) || restRequest.hasParam(PageParams.SIZE.getPreferredName())) {
            getListRequest.setPageParams(
                    new PageParams(restRequest.paramAsInt(PageParams.FROM.getPreferredName(), PageParams.DEFAULT_FROM),
                    restRequest.paramAsInt(PageParams.SIZE.getPreferredName(), PageParams.DEFAULT_SIZE)));
        }
        return channel -> client.execute(GetFiltersAction.INSTANCE, getListRequest, new RestStatusToXContentListener<>(channel));
    }

}
