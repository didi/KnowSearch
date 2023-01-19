/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.rest.results;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;
import org.elasticsearch.xpack.core.action.util.PageParams;
import org.elasticsearch.xpack.core.ml.action.GetInfluencersAction;
import org.elasticsearch.xpack.core.ml.job.config.Job;
import org.elasticsearch.xpack.ml.MachineLearning;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class RestGetInfluencersAction extends BaseRestHandler {

    private static final DeprecationLogger deprecationLogger =
        new DeprecationLogger(LogManager.getLogger(RestGetInfluencersAction.class));

    public RestGetInfluencersAction(RestController controller) {
        // TODO: remove deprecated endpoint in 8.0.0
        controller.registerWithDeprecatedHandler(
            GET, MachineLearning.BASE_PATH + "anomaly_detectors/{" + Job.ID.getPreferredName() + "}/results/influencers",
                this,
            GET, MachineLearning.PRE_V7_BASE_PATH + "anomaly_detectors/{" + Job.ID.getPreferredName() + "}/results/influencers",
                deprecationLogger);
        // endpoints that support body parameters must also accept POST
        controller.registerWithDeprecatedHandler(
            POST, MachineLearning.BASE_PATH + "anomaly_detectors/{" + Job.ID.getPreferredName() + "}/results/influencers",
                this,
            POST, MachineLearning.PRE_V7_BASE_PATH + "anomaly_detectors/{" + Job.ID.getPreferredName() + "}/results/influencers",
                deprecationLogger);
    }

    @Override
    public String getName() {
        return "ml_get_influencers_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        String jobId = restRequest.param(Job.ID.getPreferredName());
        String start = restRequest.param(GetInfluencersAction.Request.START.getPreferredName());
        String end = restRequest.param(GetInfluencersAction.Request.END.getPreferredName());
        final GetInfluencersAction.Request request;
        if (restRequest.hasContentOrSourceParam()) {
            XContentParser parser = restRequest.contentOrSourceParamParser();
            request = GetInfluencersAction.Request.parseRequest(jobId, parser);
        } else {
            request = new GetInfluencersAction.Request(jobId);
            request.setStart(start);
            request.setEnd(end);
            request.setExcludeInterim(restRequest.paramAsBoolean(GetInfluencersAction.Request.EXCLUDE_INTERIM.getPreferredName(),
                    request.isExcludeInterim()));
            request.setPageParams(new PageParams(restRequest.paramAsInt(PageParams.FROM.getPreferredName(), PageParams.DEFAULT_FROM),
                    restRequest.paramAsInt(PageParams.SIZE.getPreferredName(), PageParams.DEFAULT_SIZE)));
            request.setInfluencerScore(
                    Double.parseDouble(restRequest.param(GetInfluencersAction.Request.INFLUENCER_SCORE.getPreferredName(),
                            String.valueOf(request.getInfluencerScore()))));
            request.setSort(restRequest.param(GetInfluencersAction.Request.SORT_FIELD.getPreferredName(), request.getSort()));
            request.setDescending(restRequest.paramAsBoolean(GetInfluencersAction.Request.DESCENDING_SORT.getPreferredName(),
                    request.isDescending()));
        }

        return channel -> client.execute(GetInfluencersAction.INSTANCE, request, new RestToXContentListener<>(channel));
    }
}
