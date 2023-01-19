/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.action;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.ml.action.FlushJobAction;
import org.elasticsearch.xpack.ml.job.process.autodetect.AutodetectProcessManager;
import org.elasticsearch.xpack.ml.job.process.autodetect.params.FlushJobParams;
import org.elasticsearch.xpack.ml.job.process.autodetect.params.TimeRange;

public class TransportFlushJobAction extends TransportJobTaskAction<FlushJobAction.Request, FlushJobAction.Response> {

    @Inject
    public TransportFlushJobAction(TransportService transportService, ClusterService clusterService, ActionFilters actionFilters,
                                   AutodetectProcessManager processManager) {
        super(FlushJobAction.NAME, clusterService, transportService, actionFilters,
            FlushJobAction.Request::new, FlushJobAction.Response::new, ThreadPool.Names.SAME, processManager);
        // ThreadPool.Names.SAME, because operations is executed by autodetect worker thread
    }

    @Override
    protected void taskOperation(FlushJobAction.Request request, TransportOpenJobAction.JobTask task,
                                 ActionListener<FlushJobAction.Response> listener) {
        FlushJobParams.Builder paramsBuilder = FlushJobParams.builder();
        paramsBuilder.calcInterim(request.getCalcInterim());
        if (request.getAdvanceTime() != null) {
            paramsBuilder.advanceTime(request.getAdvanceTime());
        }
        if (request.getSkipTime() != null) {
            paramsBuilder.skipTime(request.getSkipTime());
        }
        TimeRange.Builder timeRangeBuilder = TimeRange.builder();
        if (request.getStart() != null) {
            timeRangeBuilder.startTime(request.getStart());
        }
        if (request.getEnd() != null) {
            timeRangeBuilder.endTime(request.getEnd());
        }
        paramsBuilder.forTimeRange(timeRangeBuilder.build());
        processManager.flushJob(task, paramsBuilder.build(), ActionListener.wrap(
                flushAcknowledgement -> {
                    listener.onResponse(new FlushJobAction.Response(true,
                            flushAcknowledgement == null ? null : flushAcknowledgement.getLastFinalizedBucketEnd()));
                }, listener::onFailure
        ));
    }
}
