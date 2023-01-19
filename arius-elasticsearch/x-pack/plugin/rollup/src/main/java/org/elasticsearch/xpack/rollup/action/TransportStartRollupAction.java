/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.rollup.action;

import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.FailedNodeException;
import org.elasticsearch.action.TaskOperationFailure;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.tasks.TransportTasksAction;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.license.LicenseUtils;
import org.elasticsearch.license.XPackLicenseState;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.XPackField;
import org.elasticsearch.xpack.core.rollup.action.StartRollupJobAction;
import org.elasticsearch.xpack.rollup.job.RollupJobTask;

import java.util.List;
import java.util.function.Consumer;

public class TransportStartRollupAction extends TransportTasksAction<RollupJobTask, StartRollupJobAction.Request,
        StartRollupJobAction.Response, StartRollupJobAction.Response> {

    private final XPackLicenseState licenseState;

    @Inject
    public TransportStartRollupAction(TransportService transportService, ActionFilters actionFilters, ClusterService clusterService,
                                      XPackLicenseState licenseState) {
        super(StartRollupJobAction.NAME, clusterService, transportService, actionFilters, StartRollupJobAction.Request::new,
            StartRollupJobAction.Response::new, StartRollupJobAction.Response::new, ThreadPool.Names.SAME);
        this.licenseState = licenseState;
    }


    @Override
    protected void processTasks(StartRollupJobAction.Request request, Consumer<RollupJobTask> operation) {
        TransportTaskHelper.doProcessTasks(request.getId(), operation, taskManager);
    }

    @Override
    protected void doExecute(Task task, StartRollupJobAction.Request request, ActionListener<StartRollupJobAction.Response> listener) {

        if (!licenseState.isRollupAllowed()) {
            listener.onFailure(LicenseUtils.newComplianceException(XPackField.ROLLUP));
            return;
        }

        super.doExecute(task, request, listener);
    }


    @Override
    protected void taskOperation(StartRollupJobAction.Request request,
                                 RollupJobTask jobTask,
                                 ActionListener<StartRollupJobAction.Response> listener) {
        if (jobTask.getConfig().getId().equals(request.getId())) {
            jobTask.start(listener);
        } else {
            listener.onFailure(new RuntimeException("ID of rollup task [" + jobTask.getConfig().getId()
                    + "] does not match request's ID [" + request.getId() + "]"));
        }
    }

    @Override
    protected StartRollupJobAction.Response newResponse(StartRollupJobAction.Request request, List<StartRollupJobAction.Response> tasks,
                                                        List<TaskOperationFailure> taskOperationFailures,
                                                        List<FailedNodeException> failedNodeExceptions) {

        if (taskOperationFailures.isEmpty() == false) {
            throw org.elasticsearch.ExceptionsHelper
                    .convertToElastic(taskOperationFailures.get(0).getCause());
        } else if (failedNodeExceptions.isEmpty() == false) {
            throw org.elasticsearch.ExceptionsHelper
                    .convertToElastic(failedNodeExceptions.get(0));
        }

        // Either the job doesn't exist (the user didn't create it yet) or was deleted after the StartAPI executed.
        // In either case, let the user know
        if (tasks.size() == 0) {
            throw new ResourceNotFoundException("Task for Rollup Job [" + request.getId() + "] not found");
        }

        assert tasks.size() == 1;

        boolean allStarted = tasks.stream().allMatch(StartRollupJobAction.Response::isStarted);
        return new StartRollupJobAction.Response(allStarted);
    }

}
