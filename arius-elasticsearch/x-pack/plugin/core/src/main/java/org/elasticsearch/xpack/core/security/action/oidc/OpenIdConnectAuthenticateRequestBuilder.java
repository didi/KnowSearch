/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.action.oidc;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Request builder for populating a {@link OpenIdConnectAuthenticateRequest}
 */
public class OpenIdConnectAuthenticateRequestBuilder
    extends ActionRequestBuilder<OpenIdConnectAuthenticateRequest, OpenIdConnectAuthenticateResponse> {

    public OpenIdConnectAuthenticateRequestBuilder(ElasticsearchClient client) {
        super(client, OpenIdConnectAuthenticateAction.INSTANCE, new OpenIdConnectAuthenticateRequest());
    }

    public OpenIdConnectAuthenticateRequestBuilder redirectUri(String redirectUri) {
        request.setRedirectUri(redirectUri);
        return this;
    }

    public OpenIdConnectAuthenticateRequestBuilder state(String state) {
        request.setState(state);
        return this;
    }

    public OpenIdConnectAuthenticateRequestBuilder nonce(String nonce) {
        request.setNonce(nonce);
        return this;
    }

}
