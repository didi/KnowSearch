/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.action.oidc;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Request builder for populating a {@link OpenIdConnectPrepareAuthenticationRequest}
 */
public class OpenIdConnectPrepareAuthenticationRequestBuilder
    extends ActionRequestBuilder<OpenIdConnectPrepareAuthenticationRequest, OpenIdConnectPrepareAuthenticationResponse> {

    public OpenIdConnectPrepareAuthenticationRequestBuilder(ElasticsearchClient client) {
        super(client, OpenIdConnectPrepareAuthenticationAction.INSTANCE, new OpenIdConnectPrepareAuthenticationRequest());
    }

    public OpenIdConnectPrepareAuthenticationRequestBuilder realmName(String name) {
        request.setRealmName(name);
        return this;
    }
}
