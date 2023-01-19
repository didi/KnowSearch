/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.security.action.rolemapping;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.support.WriteRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * A builder for requests to delete a role-mapping from the
 * org.elasticsearch.xpack.security.authc.support.mapper.NativeRoleMappingStore
 */
public class DeleteRoleMappingRequestBuilder extends ActionRequestBuilder<DeleteRoleMappingRequest, DeleteRoleMappingResponse>
        implements WriteRequestBuilder<DeleteRoleMappingRequestBuilder> {

    public DeleteRoleMappingRequestBuilder(ElasticsearchClient client,
                                           DeleteRoleMappingAction action) {
        super(client, action, new DeleteRoleMappingRequest());
    }

    public DeleteRoleMappingRequestBuilder name(String name) {
        request.setName(name);
        return this;
    }
}
