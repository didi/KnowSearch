/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.transform.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionListenerResponseHandler;
import org.elasticsearch.action.FailedNodeException;
import org.elasticsearch.action.TaskOperationFailure;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.tasks.TransportTasksAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.LoggerMessageFormat;
import org.elasticsearch.common.regex.Regex;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.discovery.MasterNotDiscoveredException;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData.PersistentTask;
import org.elasticsearch.persistent.PersistentTasksService;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.action.util.PageParams;
import org.elasticsearch.xpack.core.transform.TransformField;
import org.elasticsearch.xpack.core.transform.TransformMessages;
import org.elasticsearch.xpack.core.transform.action.StopTransformAction;
import org.elasticsearch.xpack.core.transform.action.StopTransformAction.Request;
import org.elasticsearch.xpack.core.transform.action.StopTransformAction.Response;
import org.elasticsearch.xpack.core.transform.transforms.TransformState;
import org.elasticsearch.xpack.core.transform.transforms.TransformTaskParams;
import org.elasticsearch.xpack.core.transform.transforms.TransformTaskState;
import org.elasticsearch.xpack.core.transform.transforms.persistence.TransformInternalIndexConstants;
import org.elasticsearch.xpack.transform.TransformServices;
import org.elasticsearch.xpack.transform.persistence.TransformConfigManager;
import org.elasticsearch.xpack.transform.transforms.TransformTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.xpack.core.transform.TransformMessages.CANNOT_STOP_FAILED_TRANSFORM;

public class TransportStopTransformAction extends TransportTasksAction<TransformTask, Request, Response, Response> {

    private static final Logger logger = LogManager.getLogger(TransportStopTransformAction.class);

    private final ThreadPool threadPool;
    private final TransformConfigManager transformConfigManager;
    private final PersistentTasksService persistentTasksService;
    private final Client client;

    @Inject
    public TransportStopTransformAction(
        TransportService transportService,
        ActionFilters actionFilters,
        ClusterService clusterService,
        ThreadPool threadPool,
        PersistentTasksService persistentTasksService,
        TransformServices transformServices,
        Client client
    ) {
        this(
            StopTransformAction.NAME,
            transportService,
            actionFilters,
            clusterService,
            threadPool,
            persistentTasksService,
            transformServices,
            client
        );
    }

    protected TransportStopTransformAction(
        String name,
        TransportService transportService,
        ActionFilters actionFilters,
        ClusterService clusterService,
        ThreadPool threadPool,
        PersistentTasksService persistentTasksService,
        TransformServices transformServices,
        Client client
    ) {
        super(name, clusterService, transportService, actionFilters, Request::new, Response::new, Response::new, ThreadPool.Names.SAME);
        this.threadPool = threadPool;
        this.transformConfigManager = transformServices.getConfigManager();
        this.persistentTasksService = persistentTasksService;
        this.client = client;
    }

    static void validateTaskState(ClusterState state, List<String> transformIds, boolean isForce) {
        PersistentTasksCustomMetaData tasks = state.metaData().custom(PersistentTasksCustomMetaData.TYPE);
        if (isForce == false && tasks != null) {
            List<String> failedTasks = new ArrayList<>();
            List<String> failedReasons = new ArrayList<>();
            for (String transformId : transformIds) {
                PersistentTasksCustomMetaData.PersistentTask<?> dfTask = tasks.getTask(transformId);
                if (dfTask != null
                    && dfTask.getState() instanceof TransformState
                    && ((TransformState) dfTask.getState()).getTaskState() == TransformTaskState.FAILED) {
                    failedTasks.add(transformId);
                    failedReasons.add(((TransformState) dfTask.getState()).getReason());
                }
            }
            if (failedTasks.isEmpty() == false) {
                String msg = failedTasks.size() == 1
                    ? TransformMessages.getMessage(CANNOT_STOP_FAILED_TRANSFORM, failedTasks.get(0), failedReasons.get(0))
                    : "Unable to stop transforms. The following transforms are in a failed state "
                        + failedTasks
                        + " with reasons "
                        + failedReasons
                        + ". Use force stop to stop the transforms.";
                throw new ElasticsearchStatusException(msg, RestStatus.CONFLICT);
            }
        }
    }

    static Tuple<Set<String>, Set<String>> findTasksWithoutConfig(ClusterState state, String transformId) {
        PersistentTasksCustomMetaData tasks = state.metaData().custom(PersistentTasksCustomMetaData.TYPE);

        Set<String> taskIds = new HashSet<>();
        Set<String> executorNodes = new HashSet<>();

        if (tasks != null) {
            Predicate<PersistentTask<?>> taskMatcher = Strings.isAllOrWildcard(new String[] { transformId }) ? t -> true : t -> {
                TransformTaskParams transformParams = (TransformTaskParams) t.getParams();
                return Regex.simpleMatch(transformId, transformParams.getId());
            };

            for (PersistentTasksCustomMetaData.PersistentTask<?> pTask : tasks.findTasks(TransformField.TASK_NAME, taskMatcher)) {
                executorNodes.add(pTask.getExecutorNode());
                taskIds.add(pTask.getId());
            }
        }

        return new Tuple<>(taskIds, executorNodes);
    }

    @Override
    protected void doExecute(Task task, Request request, ActionListener<Response> listener) {
        final ClusterState state = clusterService.state();
        final DiscoveryNodes nodes = state.nodes();
        if (nodes.isLocalNodeElectedMaster() == false) {
            // Delegates stop transform to elected master node so it becomes the coordinating node.
            if (nodes.getMasterNode() == null) {
                listener.onFailure(new MasterNotDiscoveredException("no known master node"));
            } else {
                transportService.sendRequest(
                    nodes.getMasterNode(),
                    actionName,
                    request,
                    new ActionListenerResponseHandler<>(listener, Response::new)
                );
            }
        } else {
            final ActionListener<Response> finalListener;
            if (request.waitForCompletion()) {
                finalListener = waitForStopListener(request, listener);
            } else {
                finalListener = listener;
            }

            transformConfigManager.expandTransformIds(
                request.getId(),
                new PageParams(0, 10_000),
                request.isAllowNoMatch(),
                ActionListener.wrap(hitsAndIds -> {
                    validateTaskState(state, hitsAndIds.v2(), request.isForce());
                    request.setExpandedIds(new HashSet<>(hitsAndIds.v2()));
                    request.setNodes(TransformNodes.transformTaskNodes(hitsAndIds.v2(), state));
                    super.doExecute(task, request, finalListener);
                }, e -> {
                    if (e instanceof ResourceNotFoundException) {
                        Tuple<Set<String>, Set<String>> runningTasksAndNodes = findTasksWithoutConfig(state, request.getId());
                        if (runningTasksAndNodes.v1().isEmpty()) {
                            listener.onFailure(e);
                            // found transforms without a config
                        } else if (request.isForce()) {
                            request.setExpandedIds(runningTasksAndNodes.v1());
                            request.setNodes(runningTasksAndNodes.v2().toArray(new String[0]));
                            super.doExecute(task, request, finalListener);
                        } else {
                            listener.onFailure(
                                new ElasticsearchStatusException(
                                    TransformMessages.getMessage(
                                        TransformMessages.REST_STOP_TRANSFORM_WITHOUT_CONFIG,
                                        Strings.arrayToCommaDelimitedString(runningTasksAndNodes.v1().toArray(new String[0]))
                                    ),
                                    RestStatus.CONFLICT
                                )
                            );
                        }
                    } else {
                        listener.onFailure(e);
                    }
                })
            );
        }
    }

    @Override
    protected void taskOperation(Request request, TransformTask transformTask, ActionListener<Response> listener) {

        Set<String> ids = request.getExpandedIds();
        if (ids == null) {
            listener.onFailure(new IllegalStateException("Request does not have expandedIds set"));
            return;
        }

        if (ids.contains(transformTask.getTransformId())) {
            transformTask.setShouldStopAtCheckpoint(request.isWaitForCheckpoint(), ActionListener.wrap(r -> {
                try {
                    transformTask.stop(request.isForce(), request.isWaitForCheckpoint());
                    listener.onResponse(new Response(true));
                } catch (ElasticsearchException ex) {
                    listener.onFailure(ex);
                }
            },
                e -> listener.onFailure(
                    new ElasticsearchStatusException(
                        "Failed to update transform task [{}] state value should_stop_at_checkpoint from [{}] to [{}]",
                        RestStatus.CONFLICT,
                        transformTask.getTransformId(),
                        transformTask.getState().shouldStopAtNextCheckpoint(),
                        request.isWaitForCheckpoint()
                    )
                )
            ));
        } else {
            listener.onFailure(
                new RuntimeException(
                    "ID of transform task [" + transformTask.getTransformId() + "] does not match request's ID [" + request.getId() + "]"
                )
            );
        }
    }

    @Override
    protected StopTransformAction.Response newResponse(
        Request request,
        List<Response> tasks,
        List<TaskOperationFailure> taskOperationFailures,
        List<FailedNodeException> failedNodeExceptions
    ) {

        if (taskOperationFailures.isEmpty() == false || failedNodeExceptions.isEmpty() == false) {
            return new Response(taskOperationFailures, failedNodeExceptions, false);
        }

        // if tasks is empty allMatch is 'vacuously satisfied'
        return new Response(tasks.stream().allMatch(Response::isAcknowledged));
    }

    private ActionListener<Response> waitForStopListener(Request request, ActionListener<Response> listener) {

        ActionListener<Response> onStopListener = ActionListener.wrap(
            waitResponse -> client.admin()
                .indices()
                .prepareRefresh(TransformInternalIndexConstants.LATEST_INDEX_NAME)
                .execute(ActionListener.wrap(r -> listener.onResponse(waitResponse), e -> {
                    logger.info("Failed to refresh internal index after delete", e);
                    listener.onResponse(waitResponse);
                })),
            listener::onFailure
        );
        return ActionListener.wrap(
            response -> {
                // If there were failures attempting to stop the tasks, we don't know if they will actually stop.
                // It is better to respond to the user now than allow for the persistent task waiting to timeout
                if (response.getTaskFailures().isEmpty() == false || response.getNodeFailures().isEmpty() == false) {
                    RestStatus status = firstNotOKStatus(response.getTaskFailures(), response.getNodeFailures());
                    listener.onFailure(buildException(response.getTaskFailures(), response.getNodeFailures(), status));
                    return;
                }
                // Wait until the persistent task is stopped
                // Switch over to Generic threadpool so we don't block the network thread
                threadPool.generic()
                    .execute(
                        () -> waitForTransformStopped(request.getExpandedIds(), request.getTimeout(), request.isForce(), onStopListener)
                    );
            },
            listener::onFailure
        );
    }

    static ElasticsearchStatusException buildException(
        List<TaskOperationFailure> taskOperationFailures,
        List<ElasticsearchException> elasticsearchExceptions,
        RestStatus status
    ) {
        List<Exception> exceptions = Stream.concat(
            taskOperationFailures.stream().map(TaskOperationFailure::getCause),
            elasticsearchExceptions.stream()
        ).collect(Collectors.toList());

        ElasticsearchStatusException elasticsearchStatusException = new ElasticsearchStatusException(
            exceptions.get(0).getMessage(),
            status
        );

        for (int i = 1; i < exceptions.size(); i++) {
            elasticsearchStatusException.addSuppressed(exceptions.get(i));
        }
        return elasticsearchStatusException;
    }

    static RestStatus firstNotOKStatus(List<TaskOperationFailure> taskOperationFailures, List<ElasticsearchException> exceptions) {
        RestStatus status = RestStatus.OK;

        for (TaskOperationFailure taskOperationFailure : taskOperationFailures) {
            status = taskOperationFailure.getStatus();
            if (RestStatus.OK.equals(status) == false) {
                break;
            }
        }
        if (status == RestStatus.OK) {
            for (ElasticsearchException exception : exceptions) {
                // As it stands right now, this will ALWAYS be INTERNAL_SERVER_ERROR.
                // FailedNodeException does not overwrite the `status()` method and the logic in ElasticsearchException
                // Just returns an INTERNAL_SERVER_ERROR
                status = exception.status();
                if (RestStatus.OK.equals(status) == false) {
                    break;
                }
            }
        }
        // If all the previous exceptions don't have a valid status, we have an unknown error.
        return status == RestStatus.OK ? RestStatus.INTERNAL_SERVER_ERROR : status;
    }

    private void waitForTransformStopped(
        Set<String> persistentTaskIds,
        TimeValue timeout,
        boolean force,
        ActionListener<Response> listener
    ) {
        // This map is accessed in the predicate and the listener callbacks
        final Map<String, ElasticsearchException> exceptions = new ConcurrentHashMap<>();
        persistentTasksService.waitForPersistentTasksCondition(persistentTasksCustomMetaData -> {
            if (persistentTasksCustomMetaData == null) {
                return true;
            }
            for (String persistentTaskId : persistentTaskIds) {
                PersistentTasksCustomMetaData.PersistentTask<?> transformsTask = persistentTasksCustomMetaData.getTask(persistentTaskId);
                // Either the task has successfully stopped or we have seen that it has failed
                if (transformsTask == null || exceptions.containsKey(persistentTaskId)) {
                    continue;
                }

                // If force is true, then it should eventually go away, don't add it to the collection of failures.
                TransformState taskState = (TransformState) transformsTask.getState();
                if (force == false && taskState != null && taskState.getTaskState() == TransformTaskState.FAILED) {
                    exceptions.put(
                        persistentTaskId,
                        new ElasticsearchStatusException(
                            TransformMessages.getMessage(CANNOT_STOP_FAILED_TRANSFORM, persistentTaskId, taskState.getReason()),
                            RestStatus.CONFLICT
                        )
                    );

                    // If all the tasks are now flagged as failed, do not wait for another ClusterState update.
                    // Return to the caller as soon as possible
                    return persistentTasksCustomMetaData.tasks().stream().allMatch(p -> exceptions.containsKey(p.getId()));
                }
                return false;
            }
            return true;
        }, timeout, ActionListener.wrap(r -> {
            // No exceptions AND the tasks have gone away
            if (exceptions.isEmpty()) {
                listener.onResponse(new Response(Boolean.TRUE));
                return;
            }
            // We are only stopping one task, so if there is a failure, it is the only one
            if (persistentTaskIds.size() == 1) {
                listener.onFailure(exceptions.get(persistentTaskIds.iterator().next()));
                return;
            }

            Set<String> stoppedTasks = new HashSet<>(persistentTaskIds);
            stoppedTasks.removeAll(exceptions.keySet());
            String message = stoppedTasks.isEmpty()
                ? "Could not stop any of the tasks as all were failed. Use force stop to stop the transforms."
                : LoggerMessageFormat.format(
                    "Successfully stopped [{}] transforms. "
                        + "Could not stop the transforms {} as they were failed. Use force stop to stop the transforms.",
                    stoppedTasks.size(),
                    exceptions.keySet()
                );

            listener.onFailure(new ElasticsearchStatusException(message, RestStatus.CONFLICT));
        }, e -> {
            // waitForPersistentTasksCondition throws a IllegalStateException on timeout
            if (e instanceof IllegalStateException && e.getMessage().startsWith("Timed out")) {
                PersistentTasksCustomMetaData persistentTasksCustomMetaData = clusterService.state()
                    .metaData()
                    .custom(PersistentTasksCustomMetaData.TYPE);

                if (persistentTasksCustomMetaData == null) {
                    listener.onResponse(new Response(Boolean.TRUE));
                    return;
                }

                // collect which tasks are still running
                Set<String> stillRunningTasks = new HashSet<>();
                for (String persistentTaskId : persistentTaskIds) {
                    if (persistentTasksCustomMetaData.getTask(persistentTaskId) != null) {
                        stillRunningTasks.add(persistentTaskId);
                    }
                }

                if (stillRunningTasks.isEmpty()) {
                    // should not happen
                    listener.onResponse(new Response(Boolean.TRUE));
                    return;
                } else {
                    StringBuilder message = new StringBuilder();
                    if (persistentTaskIds.size() - stillRunningTasks.size() - exceptions.size() > 0) {
                        message.append("Successfully stopped [");
                        message.append(persistentTaskIds.size() - stillRunningTasks.size() - exceptions.size());
                        message.append("] transforms. ");
                    }

                    if (exceptions.size() > 0) {
                        message.append("Could not stop the transforms ");
                        message.append(exceptions.keySet());
                        message.append(" as they were failed. Use force stop to stop the transforms. ");
                    }

                    if (stillRunningTasks.size() > 0) {
                        message.append("Could not stop the transforms ");
                        message.append(stillRunningTasks);
                        message.append(" as they timed out [");
                        message.append(timeout.toString());
                        message.append("].");
                    }

                    listener.onFailure(new ElasticsearchStatusException(message.toString(), RestStatus.REQUEST_TIMEOUT));
                    return;
                }
            }
            listener.onFailure(e);
        }));
    }
}
