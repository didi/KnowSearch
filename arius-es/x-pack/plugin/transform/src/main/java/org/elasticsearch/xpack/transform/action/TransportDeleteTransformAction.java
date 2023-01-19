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
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.transform.action.DeleteTransformAction;
import org.elasticsearch.xpack.core.transform.action.DeleteTransformAction.Request;
import org.elasticsearch.xpack.core.transform.action.StopTransformAction;
import org.elasticsearch.xpack.transform.TransformServices;
import org.elasticsearch.xpack.transform.notifications.TransformAuditor;
import org.elasticsearch.xpack.transform.persistence.TransformConfigManager;

import java.io.IOException;

import static org.elasticsearch.xpack.core.ClientHelper.TRANSFORM_ORIGIN;
import static org.elasticsearch.xpack.core.ClientHelper.executeAsyncWithOrigin;

public class TransportDeleteTransformAction extends TransportMasterNodeAction<Request, AcknowledgedResponse> {

    private static final Logger logger = LogManager.getLogger(TransportDeleteTransformAction.class);

    private final TransformConfigManager transformConfigManager;
    private final TransformAuditor auditor;
    private final Client client;

    @Inject
    public TransportDeleteTransformAction(
        TransportService transportService,
        ActionFilters actionFilters,
        ThreadPool threadPool,
        ClusterService clusterService,
        IndexNameExpressionResolver indexNameExpressionResolver,
        TransformServices transformServices,
        Client client
    ) {
        this(
            DeleteTransformAction.NAME,
            transportService,
            actionFilters,
            threadPool,
            clusterService,
            indexNameExpressionResolver,
            transformServices,
            client
        );
    }

    protected TransportDeleteTransformAction(
        String name,
        TransportService transportService,
        ActionFilters actionFilters,
        ThreadPool threadPool,
        ClusterService clusterService,
        IndexNameExpressionResolver indexNameExpressionResolver,
        TransformServices transformServices,
        Client client
    ) {
        super(name, transportService, clusterService, threadPool, actionFilters, Request::new, indexNameExpressionResolver);
        this.transformConfigManager = transformServices.getConfigManager();
        this.auditor = transformServices.getAuditor();
        this.client = client;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.SAME;
    }

    @Override
    protected AcknowledgedResponse read(StreamInput in) throws IOException {
        return new AcknowledgedResponse(in);
    }

    @Override
    protected void masterOperation(Request request, ClusterState state, ActionListener<AcknowledgedResponse> listener) {
        final PersistentTasksCustomMetaData pTasksMeta = state.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        if (pTasksMeta != null && pTasksMeta.getTask(request.getId()) != null && request.isForce() == false) {
            listener.onFailure(
                new ElasticsearchStatusException(
                    "Cannot delete transform [" + request.getId() + "] as the task is running. Stop the task first",
                    RestStatus.CONFLICT
                )
            );
        } else {
            ActionListener<Void> stopTransformActionListener = ActionListener.wrap(
                stopResponse -> transformConfigManager.deleteTransform(request.getId(), ActionListener.wrap(r -> {
                    logger.debug("[{}] deleted transform", request.getId());
                    auditor.info(request.getId(), "Deleted transform.");
                    listener.onResponse(new AcknowledgedResponse(r));
                }, listener::onFailure)),
                listener::onFailure
            );

            if (pTasksMeta != null && pTasksMeta.getTask(request.getId()) != null) {
                executeAsyncWithOrigin(
                    client,
                    TRANSFORM_ORIGIN,
                    StopTransformAction.INSTANCE,
                    new StopTransformAction.Request(request.getId(), true, true, null, true, false),
                    ActionListener.wrap(r -> stopTransformActionListener.onResponse(null), stopTransformActionListener::onFailure)
                );
            } else {
                stopTransformActionListener.onResponse(null);
            }
        }
    }

    @Override
    protected ClusterBlockException checkBlock(Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_READ);
    }
}
