/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.authz.interceptor;

import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.license.XPackLicenseState;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.xpack.core.security.authz.AuthorizationEngine;
import org.elasticsearch.xpack.core.security.authz.AuthorizationEngine.AuthorizationInfo;
import org.elasticsearch.xpack.core.security.authz.AuthorizationEngine.RequestInfo;
import org.elasticsearch.xpack.core.security.authz.AuthorizationServiceField;
import org.elasticsearch.xpack.core.security.authz.accesscontrol.IndicesAccessControl;
import org.elasticsearch.xpack.core.security.support.Exceptions;
import org.elasticsearch.xpack.security.audit.AuditTrailService;
import org.elasticsearch.xpack.security.audit.AuditUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.action.support.ContextPreservingActionListener.wrapPreservingContext;

public final class IndicesAliasesRequestInterceptor implements RequestInterceptor {

    private final ThreadContext threadContext;
    private final XPackLicenseState licenseState;
    private final AuditTrailService auditTrailService;

    public IndicesAliasesRequestInterceptor(ThreadContext threadContext, XPackLicenseState licenseState,
                                            AuditTrailService auditTrailService) {
        this.threadContext = threadContext;
        this.licenseState = licenseState;
        this.auditTrailService = auditTrailService;
    }

    @Override
    public void intercept(RequestInfo requestInfo, AuthorizationEngine authorizationEngine, AuthorizationInfo authorizationInfo,
                          ActionListener<Void> listener) {
        if (requestInfo.getRequest() instanceof IndicesAliasesRequest) {
            final IndicesAliasesRequest request = (IndicesAliasesRequest) requestInfo.getRequest();
            final XPackLicenseState frozenLicenseState = licenseState.copyCurrentLicenseState();
            if (frozenLicenseState.isAuthAllowed()) {
                if (frozenLicenseState.isDocumentAndFieldLevelSecurityAllowed()) {
                    IndicesAccessControl indicesAccessControl =
                        threadContext.getTransient(AuthorizationServiceField.INDICES_PERMISSIONS_KEY);
                    for (IndicesAliasesRequest.AliasActions aliasAction : request.getAliasActions()) {
                        if (aliasAction.actionType() == IndicesAliasesRequest.AliasActions.Type.ADD) {
                            for (String index : aliasAction.indices()) {
                                IndicesAccessControl.IndexAccessControl indexAccessControl =
                                    indicesAccessControl.getIndexPermissions(index);
                                if (indexAccessControl != null) {
                                    final boolean fls = indexAccessControl.getFieldPermissions().hasFieldLevelSecurity();
                                    final boolean dls = indexAccessControl.getDocumentPermissions().hasDocumentLevelPermissions();
                                    if (fls || dls) {
                                        listener.onFailure(new ElasticsearchSecurityException("Alias requests are not allowed for " +
                                            "users who have field or document level security enabled on one of the indices",
                                            RestStatus.BAD_REQUEST));
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

            Map<String, List<String>> indexToAliasesMap = request.getAliasActions().stream()
                .filter(aliasAction -> aliasAction.actionType() == IndicesAliasesRequest.AliasActions.Type.ADD)
                .flatMap(aliasActions ->
                    Arrays.stream(aliasActions.indices())
                        .map(indexName -> new Tuple<>(indexName, Arrays.asList(aliasActions.aliases()))))
                .collect(Collectors.toMap(Tuple::v1, Tuple::v2, (existing, toMerge) -> {
                    List<String> list = new ArrayList<>(existing.size() + toMerge.size());
                    list.addAll(existing);
                    list.addAll(toMerge);
                    return list;
                }));
            authorizationEngine.validateIndexPermissionsAreSubset(requestInfo, authorizationInfo, indexToAliasesMap,
                wrapPreservingContext(ActionListener.wrap(authzResult -> {
                    if (authzResult.isGranted()) {
                        // do not audit success again
                        listener.onResponse(null);
                    } else {
                        auditTrailService.accessDenied(AuditUtil.extractRequestId(threadContext), requestInfo.getAuthentication(),
                            requestInfo.getAction(), request, authorizationInfo);
                        listener.onFailure(Exceptions.authorizationError("Adding an alias is not allowed when the alias " +
                            "has more permissions than any of the indices"));
                    }
                }, listener::onFailure), threadContext));
            } else {
                listener.onResponse(null);
            }
        } else {
            listener.onResponse(null);
        }
    }
}
