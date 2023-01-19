/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.security.rest.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.license.LicenseUtils;
import org.elasticsearch.license.XPackLicenseState;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestBuilderListener;
import org.elasticsearch.xpack.core.security.action.DelegatePkiAuthenticationAction;
import org.elasticsearch.xpack.core.security.action.DelegatePkiAuthenticationRequest;
import org.elasticsearch.xpack.security.action.TransportDelegatePkiAuthenticationAction;
import org.elasticsearch.xpack.security.authc.Realms;
import org.elasticsearch.xpack.core.security.action.DelegatePkiAuthenticationResponse;
import org.elasticsearch.xpack.core.security.authc.pki.PkiRealmSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * Implements the exchange of an {@code X509Certificate} chain into an access token. The chain is represented as an ordered string array.
 * Each string in the array is a base64-encoded (Section 4 of RFC4648 - not base64url-encoded) DER PKIX certificate value.
 * See also {@link TransportDelegatePkiAuthenticationAction}.
 */
public final class RestDelegatePkiAuthenticationAction extends SecurityBaseRestHandler {

    protected Logger logger = LogManager.getLogger(RestDelegatePkiAuthenticationAction.class);

    public RestDelegatePkiAuthenticationAction(Settings settings, RestController controller, XPackLicenseState xPackLicenseState) {
        super(settings, xPackLicenseState);
        controller.registerHandler(POST, "/_security/delegate_pki", this);
    }

    @Override
    protected Exception checkFeatureAvailable(RestRequest request) {
        Exception failedFeature = super.checkFeatureAvailable(request);
        if (failedFeature != null) {
            return failedFeature;
        } else if (Realms.isRealmTypeAvailable(licenseState.allowedRealmType(), PkiRealmSettings.TYPE)) {
            return null;
        } else {
            logger.info("The '{}' realm is not available under the current license", PkiRealmSettings.TYPE);
            return LicenseUtils.newComplianceException(PkiRealmSettings.TYPE);
        }
    }

    @Override
    protected RestChannelConsumer innerPrepareRequest(RestRequest request, NodeClient client) throws IOException {
        try (XContentParser parser = request.contentParser()) {
            final DelegatePkiAuthenticationRequest delegatePkiRequest = DelegatePkiAuthenticationRequest.fromXContent(parser);
            return channel -> client.execute(DelegatePkiAuthenticationAction.INSTANCE, delegatePkiRequest,
                    new RestBuilderListener<DelegatePkiAuthenticationResponse>(channel) {
                        @Override
                        public RestResponse buildResponse(DelegatePkiAuthenticationResponse delegatePkiResponse, XContentBuilder builder)
                                throws Exception {
                            delegatePkiResponse.toXContent(builder, channel.request());
                            return new BytesRestResponse(RestStatus.OK, builder);
                        }
                    });
        }
    }

    @Override
    public String getName() {
        return "delegate_pki_action";
    }
}
