/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.authc.saml;

import org.elasticsearch.common.Nullable;
import org.opensaml.security.x509.X509Credential;

import java.util.Collections;
import java.util.List;

/**
 * A simple container class that holds all configuration related to a SAML Service Provider (SP).
 */
public class SpConfiguration {

    private final String entityId;
    private final String ascUrl;
    private final String logoutUrl;
    private final SigningConfiguration signingConfiguration;
    private final List<String> reqAuthnCtxClassRef;
    private final List<X509Credential> encryptionCredentials;

    public SpConfiguration(final String entityId, final String ascUrl, final String logoutUrl,
                           final SigningConfiguration signingConfiguration, @Nullable final List<X509Credential> encryptionCredential,
                           final List<String> authnCtxClassRef) {
        this.entityId = entityId;
        this.ascUrl = ascUrl;
        this.logoutUrl = logoutUrl;
        this.signingConfiguration = signingConfiguration;
        if (encryptionCredential != null) {
            this.encryptionCredentials = Collections.unmodifiableList(encryptionCredential);
        } else {
            this.encryptionCredentials = Collections.<X509Credential>emptyList();
        }
        this.reqAuthnCtxClassRef = authnCtxClassRef;
    }

    /**
     * The SAML identifier (as a URI) for the Sp
     */
    String getEntityId() {
        return entityId;
    }

    String getAscUrl() {
        return ascUrl;
    }

    String getLogoutUrl() {
        return logoutUrl;
    }

    List<X509Credential> getEncryptionCredentials() {
        return encryptionCredentials;
    }

    SigningConfiguration getSigningConfiguration() {
        return signingConfiguration;
    }

    List<String> getReqAuthnCtxClassRef() {
        return reqAuthnCtxClassRef;
    }
}
