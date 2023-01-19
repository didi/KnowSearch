/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.lease.Releasable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData;
import org.elasticsearch.threadpool.Scheduler;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.xpack.core.ml.action.DeleteExpiredDataAction;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

import static org.elasticsearch.xpack.core.ClientHelper.ML_ORIGIN;
import static org.elasticsearch.xpack.core.ClientHelper.executeAsyncWithOrigin;

/**
 * A service that runs once a day and triggers maintenance tasks.
 */
public class MlDailyMaintenanceService implements Releasable {

    private static final Logger LOGGER = LogManager.getLogger(MlDailyMaintenanceService.class);

    private static final int MAX_TIME_OFFSET_MINUTES = 120;

    private final ThreadPool threadPool;
    private final Client client;
    private final ClusterService clusterService;
    private final MlAssignmentNotifier mlAssignmentNotifier;

    /**
     * An interface to abstract the calculation of the delay to the next execution.
     * Needed to enable testing.
     */
    private final Supplier<TimeValue> schedulerProvider;

    private volatile Scheduler.Cancellable cancellable;

    MlDailyMaintenanceService(ThreadPool threadPool, Client client, ClusterService clusterService,
                              MlAssignmentNotifier mlAssignmentNotifier, Supplier<TimeValue> scheduleProvider) {
        this.threadPool = Objects.requireNonNull(threadPool);
        this.client = Objects.requireNonNull(client);
        this.clusterService = Objects.requireNonNull(clusterService);
        this.mlAssignmentNotifier = Objects.requireNonNull(mlAssignmentNotifier);
        this.schedulerProvider = Objects.requireNonNull(scheduleProvider);
    }

    public MlDailyMaintenanceService(ClusterName clusterName, ThreadPool threadPool, Client client, ClusterService clusterService,
                                     MlAssignmentNotifier mlAssignmentNotifier) {
        this(threadPool, client, clusterService, mlAssignmentNotifier, () -> delayToNextTime(clusterName));
    }

    /**
     * Calculates the delay until the next time the maintenance should be triggered.
     * The next time is 30 minutes past midnight of the following day plus a random
     * offset. The random offset is added in order to avoid multiple clusters
     * running the maintenance tasks at the same time. A cluster with a given name
     * shall have the same offset throughout its life.
     *
     * @param clusterName the cluster name is used to seed the random offset
     * @return the delay to the next time the maintenance should be triggered
     */
    private static TimeValue delayToNextTime(ClusterName clusterName) {
        Random random = new Random(clusterName.hashCode());
        int minutesOffset = random.ints(0, MAX_TIME_OFFSET_MINUTES).findFirst().getAsInt();

        ZonedDateTime now = ZonedDateTime.now(Clock.systemDefaultZone());
        ZonedDateTime next = now.plusDays(1)
            .toLocalDate()
            .atStartOfDay(now.getZone())
            .plusMinutes(30)
            .plusMinutes(minutesOffset);
        return TimeValue.timeValueMillis(next.toInstant().toEpochMilli() - now.toInstant().toEpochMilli());
    }

    public synchronized void start() {
        LOGGER.debug("Starting ML daily maintenance service");
        scheduleNext();
    }

    public synchronized void stop() {
        LOGGER.debug("Stopping ML daily maintenance service");
        if (cancellable != null && cancellable.isCancelled() == false) {
            cancellable.cancel();
        }
    }

    public boolean isStarted() {
        return cancellable != null;
    }

    @Override
    public void close() {
        stop();
    }

    private synchronized void scheduleNext() {
        try {
            cancellable = threadPool.schedule(this::triggerTasks, schedulerProvider.get(), ThreadPool.Names.GENERIC);
        } catch (EsRejectedExecutionException e) {
            if (e.isExecutorShutdown()) {
                LOGGER.debug("failed to schedule next maintenance task; shutting down", e);
            } else {
                throw e;
            }
        }
    }

    private void triggerTasks() {
        try {
            LOGGER.info("triggering scheduled [ML] maintenance tasks");
            executeAsyncWithOrigin(client, ML_ORIGIN, DeleteExpiredDataAction.INSTANCE, new DeleteExpiredDataAction.Request(),
                ActionListener.wrap(
                    response -> {
                        if (response.isDeleted()) {
                            LOGGER.info("Successfully completed [ML] maintenance tasks");
                        } else {
                            LOGGER.info("Halting [ML] maintenance tasks before completion as elapsed time is too great");
                        }
                    },
                    e -> LOGGER.error("An error occurred during maintenance tasks execution", e)));
            auditUnassignedMlTasks(clusterService.state());
        } finally {
            scheduleNext();
        }
    }

    /**
     * The idea of this is that if tasks are unassigned for days on end then they'll get a duplicate
     * audit warning every day, and that will mean they'll permanently have a yellow triangle next
     * to their entries in the UI jobs list.  (This functionality may need revisiting if the condition
     * for displaying a yellow triangle in the UI jobs list changes.)
     */
    private void auditUnassignedMlTasks(ClusterState state) {
        PersistentTasksCustomMetaData tasks = state.getMetaData().custom(PersistentTasksCustomMetaData.TYPE);
        if (tasks != null) {
            mlAssignmentNotifier.auditUnassignedMlTasks(state.nodes(), tasks);
        }
    }
}
