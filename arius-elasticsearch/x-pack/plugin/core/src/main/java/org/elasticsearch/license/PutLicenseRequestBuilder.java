/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.license;

import org.elasticsearch.action.support.master.AcknowledgedRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.protocol.xpack.license.PutLicenseResponse;

/**
 * Register license request builder
 */
public class PutLicenseRequestBuilder extends AcknowledgedRequestBuilder<PutLicenseRequest, PutLicenseResponse, PutLicenseRequestBuilder> {

    public PutLicenseRequestBuilder(ElasticsearchClient client) {
        this(client, PutLicenseAction.INSTANCE);
    }

    /**
     * Constructs register license request
     *
     * @param client elasticsearch client
     */
    public PutLicenseRequestBuilder(ElasticsearchClient client, PutLicenseAction action) {
        super(client, action, new PutLicenseRequest());
    }

    /**
     * Sets the license
     *
     * @param license license
     * @return this builder
     */
    public PutLicenseRequestBuilder setLicense(License license) {
        request.license(license);
        return this;
    }

    public PutLicenseRequestBuilder setLicense(BytesReference licenseSource, XContentType xContentType) {
        request.license(licenseSource, xContentType);
        return this;
    }

    public PutLicenseRequestBuilder setAcknowledge(boolean acknowledge) {
        request.acknowledge(acknowledge);
        return this;
    }
}
