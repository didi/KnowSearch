/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.action.rolemapping;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.security.action.rolemapping.DeleteRoleMappingAction;
import org.elasticsearch.xpack.core.security.action.rolemapping.DeleteRoleMappingRequest;
import org.elasticsearch.xpack.core.security.action.rolemapping.DeleteRoleMappingResponse;
import org.elasticsearch.xpack.security.authc.support.mapper.NativeRoleMappingStore;

public class TransportDeleteRoleMappingAction
        extends HandledTransportAction<DeleteRoleMappingRequest, DeleteRoleMappingResponse> {

    private final NativeRoleMappingStore roleMappingStore;

    @Inject
    public TransportDeleteRoleMappingAction(ActionFilters actionFilters, TransportService transportService,
                                            NativeRoleMappingStore roleMappingStore) {
        super(DeleteRoleMappingAction.NAME, transportService, actionFilters, DeleteRoleMappingRequest::new);
        this.roleMappingStore = roleMappingStore;
    }

    @Override
    protected void doExecute(Task task, DeleteRoleMappingRequest request, ActionListener<DeleteRoleMappingResponse> listener) {
        roleMappingStore.deleteRoleMapping(request, new ActionListener<Boolean>() {
            @Override
            public void onResponse(Boolean found) {
                listener.onResponse(new DeleteRoleMappingResponse(found));
            }

            @Override
            public void onFailure(Exception t) {
                listener.onFailure(t);
            }
        });
    }
}
