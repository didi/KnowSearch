/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.rest.action.saml;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.license.XPackLicenseState;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestBuilderListener;
import org.elasticsearch.xpack.core.security.action.saml.SamlLogoutAction;
import org.elasticsearch.xpack.core.security.action.saml.SamlLogoutRequest;
import org.elasticsearch.xpack.core.security.action.saml.SamlLogoutResponse;

import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * Invalidates the provided security token, and if the associated SAML realm support logout, generates
 * a SAML logout request ({@code &lt;LogoutRequest&gt;}).
 * This logout request is returned in the REST response as a redirect URI, and the REST client should
 * make it available to the browser.
 */
public class RestSamlLogoutAction extends SamlBaseRestHandler {

    private static final DeprecationLogger deprecationLogger = new DeprecationLogger(LogManager.getLogger(RestSamlLogoutAction.class));
    static final ObjectParser<SamlLogoutRequest, Void> PARSER = new ObjectParser<>("saml_logout", SamlLogoutRequest::new);

    static {
        PARSER.declareString(SamlLogoutRequest::setToken, new ParseField("token"));
        PARSER.declareString(SamlLogoutRequest::setRefreshToken, new ParseField("refresh_token"));
    }

    public RestSamlLogoutAction(Settings settings, RestController controller, XPackLicenseState licenseState) {
        super(settings, licenseState);
        // TODO: remove deprecated endpoint in 8.0.0
        controller.registerWithDeprecatedHandler(
            POST, "/_security/saml/logout", this,
            POST, "/_xpack/security/saml/logout", deprecationLogger);
    }

    @Override
    public String getName() {
        return "security_saml_logout_action";
    }

    @Override
    public RestChannelConsumer innerPrepareRequest(RestRequest request, NodeClient client) throws IOException {
        try (XContentParser parser = request.contentParser()) {
            final SamlLogoutRequest logoutRequest = PARSER.parse(parser, null);
            return channel -> client.execute(SamlLogoutAction.INSTANCE, logoutRequest,
                    new RestBuilderListener<SamlLogoutResponse>(channel) {
                        @Override
                        public RestResponse buildResponse(SamlLogoutResponse response, XContentBuilder builder) throws Exception {
                            builder.startObject();
                            builder.field("redirect", response.getRedirectUrl());
                            builder.endObject();
                            return new BytesRestResponse(RestStatus.OK, builder);
                        }
                    });
        }
    }
}
