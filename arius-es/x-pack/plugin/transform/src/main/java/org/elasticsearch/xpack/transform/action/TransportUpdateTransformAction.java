/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.transform.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.logging.LoggerMessageFormat;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.license.License;
import org.elasticsearch.license.LicenseUtils;
import org.elasticsearch.license.RemoteClusterLicenseChecker;
import org.elasticsearch.license.XPackLicenseState;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.RemoteClusterService;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.ClientHelper;
import org.elasticsearch.xpack.core.XPackField;
import org.elasticsearch.xpack.core.XPackPlugin;
import org.elasticsearch.xpack.core.XPackSettings;
import org.elasticsearch.xpack.core.common.validation.SourceDestValidator;
import org.elasticsearch.xpack.core.security.SecurityContext;
import org.elasticsearch.xpack.core.security.action.user.HasPrivilegesAction;
import org.elasticsearch.xpack.core.security.action.user.HasPrivilegesRequest;
import org.elasticsearch.xpack.core.security.action.user.HasPrivilegesResponse;
import org.elasticsearch.xpack.core.security.authz.permission.ResourcePrivileges;
import org.elasticsearch.xpack.core.security.support.Exceptions;
import org.elasticsearch.xpack.core.transform.TransformMessages;
import org.elasticsearch.xpack.core.transform.action.UpdateTransformAction;
import org.elasticsearch.xpack.core.transform.action.UpdateTransformAction.Request;
import org.elasticsearch.xpack.core.transform.action.UpdateTransformAction.Response;
import org.elasticsearch.xpack.core.transform.transforms.TransformConfig;
import org.elasticsearch.xpack.core.transform.transforms.TransformConfigUpdate;
import org.elasticsearch.xpack.transform.TransformServices;
import org.elasticsearch.xpack.transform.notifications.TransformAuditor;
import org.elasticsearch.xpack.transform.persistence.SeqNoPrimaryTermAndIndex;
import org.elasticsearch.xpack.transform.persistence.TransformConfigManager;
import org.elasticsearch.xpack.transform.persistence.TransformIndex;
import org.elasticsearch.xpack.transform.transforms.pivot.Pivot;
import org.elasticsearch.xpack.transform.utils.SourceDestValidations;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.elasticsearch.xpack.transform.action.TransportPutTransformAction.buildPrivilegeCheck;

public class TransportUpdateTransformAction extends TransportMasterNodeAction<Request, Response> {

    private static final Logger logger = LogManager.getLogger(TransportUpdateTransformAction.class);
    private final XPackLicenseState licenseState;
    private final Client client;
    private final TransformConfigManager transformConfigManager;
    private final SecurityContext securityContext;
    private final TransformAuditor auditor;
    private final SourceDestValidator sourceDestValidator;

    @Inject
    public TransportUpdateTransformAction(
        Settings settings,
        TransportService transportService,
        ThreadPool threadPool,
        ActionFilters actionFilters,
        IndexNameExpressionResolver indexNameExpressionResolver,
        ClusterService clusterService,
        XPackLicenseState licenseState,
        TransformServices transformServices,
        Client client
    ) {
        this(
            UpdateTransformAction.NAME,
            settings,
            transportService,
            threadPool,
            actionFilters,
            indexNameExpressionResolver,
            clusterService,
            licenseState,
            transformServices,
            client
        );
    }

    protected TransportUpdateTransformAction(
        String name,
        Settings settings,
        TransportService transportService,
        ThreadPool threadPool,
        ActionFilters actionFilters,
        IndexNameExpressionResolver indexNameExpressionResolver,
        ClusterService clusterService,
        XPackLicenseState licenseState,
        TransformServices transformServices,
        Client client
    ) {
        super(name, transportService, clusterService, threadPool, actionFilters, Request::new, indexNameExpressionResolver);
        this.licenseState = licenseState;
        this.client = client;
        this.transformConfigManager = transformServices.getConfigManager();
        this.securityContext = XPackSettings.SECURITY_ENABLED.get(settings)
            ? new SecurityContext(settings, threadPool.getThreadContext())
            : null;
        this.auditor = transformServices.getAuditor();
        this.sourceDestValidator = new SourceDestValidator(
            indexNameExpressionResolver,
            transportService.getRemoteClusterService(),
            RemoteClusterService.ENABLE_REMOTE_CLUSTERS.get(settings)
                ? new RemoteClusterLicenseChecker(client, XPackLicenseState::isTransformAllowedForOperationMode)
                : null,
            clusterService.getNodeName(),
            License.OperationMode.BASIC.description()
        );
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.SAME;
    }

    @Override
    protected Response read(StreamInput in) throws IOException {
        return new Response(in);
    }

    @Override
    protected void masterOperation(Request request, ClusterState clusterState, ActionListener<Response> listener) {

        if (!licenseState.isTransformAllowed()) {
            listener.onFailure(LicenseUtils.newComplianceException(XPackField.TRANSFORM));
            return;
        }

        XPackPlugin.checkReadyForXPackCustomMetadata(clusterState);

        // set headers to run transform as calling user
        Map<String, String> filteredHeaders = threadPool.getThreadContext()
            .getHeaders()
            .entrySet()
            .stream()
            .filter(e -> ClientHelper.SECURITY_HEADER_FILTERS.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        TransformConfigUpdate update = request.getUpdate();
        update.setHeaders(filteredHeaders);

        // GET transform and attempt to update
        // We don't want the update to complete if the config changed between GET and INDEX
        transformConfigManager.getTransformConfigurationForUpdate(request.getId(), ActionListener.wrap(configAndVersion -> {
            final TransformConfig config = configAndVersion.v1();
            // If it is a noop don't bother even writing the doc, save the cycles, just return here.
            if (update.isNoop(config)) {
                listener.onResponse(new Response(config));
                return;
            }
            TransformConfig updatedConfig = update.apply(config);
            sourceDestValidator.validate(
                clusterState,
                updatedConfig.getSource().getIndex(),
                updatedConfig.getDestination().getIndex(),
                request.isDeferValidation() ? SourceDestValidations.NON_DEFERABLE_VALIDATIONS : SourceDestValidations.ALL_VALIDATIONS,
                ActionListener.wrap(
                    validationResponse -> {
                        checkPriviledgesAndUpdateTransform(request, clusterState, updatedConfig, configAndVersion.v2(), listener);
                    },
                    listener::onFailure
                )
            );

        }, listener::onFailure));
    }

    @Override
    protected ClusterBlockException checkBlock(Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
    }

    private void handlePrivsResponse(
        String username,
        Request request,
        TransformConfig config,
        SeqNoPrimaryTermAndIndex seqNoPrimaryTermAndIndex,
        ClusterState clusterState,
        HasPrivilegesResponse privilegesResponse,
        ActionListener<Response> listener
    ) {
        if (privilegesResponse.isCompleteMatch()) {
            updateTransform(request, config, seqNoPrimaryTermAndIndex, clusterState, listener);
        } else {
            List<String> indices = privilegesResponse.getIndexPrivileges()
                .stream()
                .map(ResourcePrivileges::getResource)
                .collect(Collectors.toList());

            listener.onFailure(
                Exceptions.authorizationError(
                    "Cannot update transform [{}] because user {} lacks all the required permissions for indices: {}",
                    request.getId(),
                    username,
                    indices
                )
            );
        }
    }

    private void checkPriviledgesAndUpdateTransform(
        Request request,
        ClusterState clusterState,
        TransformConfig config,
        SeqNoPrimaryTermAndIndex seqNoPrimaryTermAndIndex,
        ActionListener<Response> listener
    ) {
        // Early check to verify that the user can create the destination index and can read from the source
        if (licenseState.isAuthAllowed() && request.isDeferValidation() == false) {
            final String username = securityContext.getUser().principal();
            HasPrivilegesRequest privRequest = buildPrivilegeCheck(config, indexNameExpressionResolver, clusterState, username);
            ActionListener<HasPrivilegesResponse> privResponseListener = ActionListener.wrap(
                r -> handlePrivsResponse(username, request, config, seqNoPrimaryTermAndIndex, clusterState, r, listener),
                listener::onFailure
            );

            client.execute(HasPrivilegesAction.INSTANCE, privRequest, privResponseListener);
        } else { // No security enabled, just create the transform
            updateTransform(request, config, seqNoPrimaryTermAndIndex, clusterState, listener);
        }
    }

    private void updateTransform(
        Request request,
        TransformConfig config,
        SeqNoPrimaryTermAndIndex seqNoPrimaryTermAndIndex,
        ClusterState clusterState,
        ActionListener<Response> listener
    ) {

        final Pivot pivot = new Pivot(config.getPivotConfig());

        // <3> Return to the listener
        ActionListener<Boolean> putTransformConfigurationListener = ActionListener.wrap(putTransformConfigurationResult -> {
            auditor.info(config.getId(), "updated transform.");
            transformConfigManager.deleteOldTransformConfigurations(request.getId(), ActionListener.wrap(r -> {
                logger.trace("[{}] successfully deleted old transform configurations", request.getId());
                listener.onResponse(new Response(config));
            }, e -> {
                logger.warn(LoggerMessageFormat.format("[{}] failed deleting old transform configurations.", request.getId()), e);
                listener.onResponse(new Response(config));
            }));
        },
            // If we failed to INDEX AND we created the destination index, the destination index will still be around
            // This is a similar behavior to _start
            listener::onFailure
        );

        // <2> Update our transform
        ActionListener<Void> createDestinationListener = ActionListener.wrap(
            createDestResponse -> transformConfigManager.updateTransformConfiguration(
                config,
                seqNoPrimaryTermAndIndex,
                putTransformConfigurationListener
            ),
            listener::onFailure
        );

        // <1> Create destination index if necessary
        ActionListener<Boolean> pivotValidationListener = ActionListener.wrap(validationResult -> {
            String[] dest = indexNameExpressionResolver.concreteIndexNames(
                clusterState,
                IndicesOptions.lenientExpandOpen(),
                config.getDestination().getIndex()
            );
            String[] src = indexNameExpressionResolver.concreteIndexNames(
                clusterState,
                IndicesOptions.lenientExpandOpen(),
                config.getSource().getIndex()
            );
            // If we are running, we should verify that the destination index exists and create it if it does not
            if (PersistentTasksCustomMetaData.getTaskWithId(clusterState, request.getId()) != null && dest.length == 0
            // Verify we have source indices. The user could defer_validations and if the task is already running
            // we allow source indices to disappear. If the source and destination indices do not exist, don't do anything
            // the transform will just have to dynamically create the destination index without special mapping.
                && src.length > 0) {
                createDestination(pivot, config, createDestinationListener);
            } else {
                createDestinationListener.onResponse(null);
            }
        }, validationException -> {
            if (validationException instanceof ElasticsearchStatusException) {
                listener.onFailure(
                    new ElasticsearchStatusException(
                        TransformMessages.REST_PUT_TRANSFORM_FAILED_TO_VALIDATE_CONFIGURATION,
                        ((ElasticsearchStatusException) validationException).status(),
                        validationException
                    )
                );
            } else {
                listener.onFailure(
                    new ElasticsearchStatusException(
                        TransformMessages.REST_PUT_TRANSFORM_FAILED_TO_VALIDATE_CONFIGURATION,
                        RestStatus.INTERNAL_SERVER_ERROR,
                        validationException
                    )
                );
            }
        });

        try {
            pivot.validateConfig();
        } catch (ElasticsearchStatusException e) {
            listener.onFailure(
                new ElasticsearchStatusException(TransformMessages.REST_PUT_TRANSFORM_FAILED_TO_VALIDATE_CONFIGURATION, e.status(), e)
            );
            return;
        } catch (Exception e) {
            listener.onFailure(
                new ElasticsearchStatusException(
                    TransformMessages.REST_PUT_TRANSFORM_FAILED_TO_VALIDATE_CONFIGURATION,
                    RestStatus.INTERNAL_SERVER_ERROR,
                    e
                )
            );
            return;
        }

        // <0> Validate the pivot if necessary
        if (request.isDeferValidation()) {
            pivotValidationListener.onResponse(true);
        } else {
            pivot.validateQuery(client, config.getSource(), pivotValidationListener);
        }
    }

    private void createDestination(Pivot pivot, TransformConfig config, ActionListener<Void> listener) {
        ActionListener<Map<String, String>> deduceMappingsListener = ActionListener.wrap(
            mappings -> TransformIndex.createDestinationIndex(
                client,
                Clock.systemUTC(),
                config,
                mappings,
                ActionListener.wrap(r -> listener.onResponse(null), listener::onFailure)
            ),
            deduceTargetMappingsException -> listener.onFailure(
                new RuntimeException(TransformMessages.REST_PUT_TRANSFORM_FAILED_TO_DEDUCE_DEST_MAPPINGS, deduceTargetMappingsException)
            )
        );

        pivot.deduceMappings(client, config.getSource(), deduceMappingsListener);
    }
}
