/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.ssl.rest;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestBuilderListener;
import org.elasticsearch.xpack.core.ssl.action.GetCertificateInfoAction;
import org.elasticsearch.xpack.core.ssl.action.GetCertificateInfoAction.Response;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * A REST handler to obtain information about TLS/SSL (X.509) certificates
 * @see GetCertificateInfoAction
 */
public class RestGetCertificateInfoAction extends BaseRestHandler {

    private static final DeprecationLogger deprecationLogger =
        new DeprecationLogger(LogManager.getLogger(RestGetCertificateInfoAction.class));

    public RestGetCertificateInfoAction(RestController controller) {
        // TODO: remove deprecated endpoint in 8.0.0
        controller.registerWithDeprecatedHandler(
            GET, "/_ssl/certificates", this,
            GET, "/_xpack/ssl/certificates", deprecationLogger);
    }

    @Override
    public String getName() {
        return "ssl_get_certificates";
    }

    @Override
    protected final RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        return channel -> new GetCertificateInfoAction.RequestBuilder(client, GetCertificateInfoAction.INSTANCE)
                .execute(new RestBuilderListener<Response>(channel) {
                    @Override
                    public RestResponse buildResponse(Response response, XContentBuilder builder) throws Exception {
                        return new BytesRestResponse(RestStatus.OK, response.toXContent(builder, request));
                    }
                });
    }
}
