/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml;

import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.LifecycleListener;
import org.elasticsearch.env.Environment;
import org.elasticsearch.xpack.ml.datafeed.DatafeedManager;
import org.elasticsearch.xpack.ml.process.MlMemoryTracker;
import org.elasticsearch.xpack.ml.process.NativeController;
import org.elasticsearch.xpack.ml.process.NativeControllerHolder;
import org.elasticsearch.xpack.ml.job.process.autodetect.AutodetectProcessManager;

import java.io.IOException;

public class MlLifeCycleService {

    private final Environment environment;
    private final ClusterService clusterService;
    private final DatafeedManager datafeedManager;
    private final AutodetectProcessManager autodetectProcessManager;
    private final MlMemoryTracker memoryTracker;

    public MlLifeCycleService(Environment environment, ClusterService clusterService, DatafeedManager datafeedManager,
                              AutodetectProcessManager autodetectProcessManager, MlMemoryTracker memoryTracker) {
        this.environment = environment;
        this.clusterService = clusterService;
        this.datafeedManager = datafeedManager;
        this.autodetectProcessManager = autodetectProcessManager;
        this.memoryTracker = memoryTracker;
        clusterService.addLifecycleListener(new LifecycleListener() {
            @Override
            public void beforeStop() {
                stop();
            }
        });
    }

    public synchronized void stop() {
        try {
            if (MachineLearningFeatureSet.isRunningOnMlPlatform(false)) {
                // This prevents datafeeds from sending data to autodetect processes WITHOUT stopping the
                // datafeeds, so they get reallocated.  We have to do this first, otherwise the datafeeds
                // could fail if they send data to a dead autodetect process.
                if (datafeedManager != null) {
                    datafeedManager.isolateAllDatafeedsOnThisNodeBeforeShutdown();
                }
                NativeController nativeController = NativeControllerHolder.getNativeController(clusterService.getNodeName(), environment);
                if (nativeController != null) {
                    // This kills autodetect processes WITHOUT closing the jobs, so they get reallocated.
                    if (autodetectProcessManager != null) {
                        autodetectProcessManager.killAllProcessesOnThisNode();
                    }
                    nativeController.stop();
                }
            }
        } catch (IOException e) {
            // We're stopping anyway, so don't let this complicate the shutdown sequence
        }
        if (memoryTracker != null) {
            memoryTracker.stop();
        }
    }
}
