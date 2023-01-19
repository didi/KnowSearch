/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.rollup.action;

import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.persistent.PersistentTasksCustomMetaData;
import org.elasticsearch.test.AbstractWireSerializingTestCase;
import org.elasticsearch.xpack.core.rollup.ConfigTestHelpers;
import org.elasticsearch.xpack.core.rollup.action.GetRollupJobsAction;
import org.elasticsearch.xpack.core.rollup.action.GetRollupJobsAction.Request;
import org.elasticsearch.xpack.core.rollup.job.RollupJob;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GetJobsActionRequestTests extends AbstractWireSerializingTestCase<Request> {

    @Override
    protected Request createTestInstance() {
        if (randomBoolean()) {
            return new Request(MetaData.ALL);
        }
        return new Request(randomAlphaOfLengthBetween(1, 20));
    }

    @Override
    protected Writeable.Reader<Request> instanceReader() {
        return Request::new;
    }

    public void testStateCheckNoPersistentTasks() {
        GetRollupJobsAction.Request request = new GetRollupJobsAction.Request("foo");
        ClusterState state = ClusterState.builder(new ClusterName("_name"))
                .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE,
                        new PersistentTasksCustomMetaData(0L, Collections.emptyMap())))
                .build();
        boolean hasRollupJobs = TransportGetRollupJobAction.stateHasRollupJobs(request, state);
        assertFalse(hasRollupJobs);
    }

    public void testStateCheckAllNoPersistentTasks() {
        GetRollupJobsAction.Request request = new GetRollupJobsAction.Request("_all");
        ClusterState state = ClusterState.builder(new ClusterName("_name"))
                .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE,
                        new PersistentTasksCustomMetaData(0L, Collections.emptyMap())))
                .build();
        boolean hasRollupJobs = TransportGetRollupJobAction.stateHasRollupJobs(request, state);
        assertFalse(hasRollupJobs);
    }

    public void testStateCheckNoMatchingPersistentTasks() {
        GetRollupJobsAction.Request request = new GetRollupJobsAction.Request("foo");
        Map<String, PersistentTasksCustomMetaData.PersistentTask<?>> tasks
                = Collections.singletonMap("bar", new PersistentTasksCustomMetaData.PersistentTask<>("bar", "bar", null, 1, null));
        ClusterState state = ClusterState.builder(new ClusterName("_name"))
                .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE,
                        new PersistentTasksCustomMetaData(0L, tasks)))
                .build();
        boolean hasRollupJobs = TransportGetRollupJobAction.stateHasRollupJobs(request, state);
        assertFalse(hasRollupJobs);
    }

    public void testStateCheckMatchingPersistentTasks() {
        GetRollupJobsAction.Request request = new GetRollupJobsAction.Request("foo");
        RollupJob job = new RollupJob(ConfigTestHelpers.randomRollupJobConfig(random(), "foo"), Collections.emptyMap());
        Map<String, PersistentTasksCustomMetaData.PersistentTask<?>> tasks
                = Collections.singletonMap("foo", new PersistentTasksCustomMetaData.PersistentTask<>("foo", RollupJob.NAME, job, 1, null));
        ClusterState state = ClusterState.builder(new ClusterName("_name"))
                .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE,
                        new PersistentTasksCustomMetaData(0L, tasks)))
                .build();
        boolean hasRollupJobs = TransportGetRollupJobAction.stateHasRollupJobs(request, state);
        assertTrue(hasRollupJobs);
    }

    public void testStateCheckAllMatchingPersistentTasks() {
        GetRollupJobsAction.Request request = new GetRollupJobsAction.Request("_all");
        RollupJob job = new RollupJob(ConfigTestHelpers.randomRollupJobConfig(random(), "foo"), Collections.emptyMap());
        Map<String, PersistentTasksCustomMetaData.PersistentTask<?>> tasks
                = Collections.singletonMap("foo", new PersistentTasksCustomMetaData.PersistentTask<>("foo", RollupJob.NAME, job, 1, null));
        ClusterState state = ClusterState.builder(new ClusterName("_name"))
                .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE,
                        new PersistentTasksCustomMetaData(0L, tasks)))
                .build();
        boolean hasRollupJobs = TransportGetRollupJobAction.stateHasRollupJobs(request, state);
        assertTrue(hasRollupJobs);
    }

    public void testStateCheckAllWithSeveralMatchingPersistentTasks() {
        GetRollupJobsAction.Request request = new GetRollupJobsAction.Request("_all");
        RollupJob job = new RollupJob(ConfigTestHelpers.randomRollupJobConfig(random(), "foo"), Collections.emptyMap());
        RollupJob job2 = new RollupJob(ConfigTestHelpers.randomRollupJobConfig(random(), "bar"), Collections.emptyMap());
        Map<String, PersistentTasksCustomMetaData.PersistentTask<?>> tasks = new HashMap<>(2);
        tasks.put("foo", new PersistentTasksCustomMetaData.PersistentTask<>("foo", RollupJob.NAME, job, 1, null));
        tasks.put("bar", new PersistentTasksCustomMetaData.PersistentTask<>("bar", RollupJob.NAME, job2, 1, null));
        ClusterState state = ClusterState.builder(new ClusterName("_name"))
                .metaData(MetaData.builder().putCustom(PersistentTasksCustomMetaData.TYPE,
                        new PersistentTasksCustomMetaData(0L, tasks)))
                .build();
        boolean hasRollupJobs = TransportGetRollupJobAction.stateHasRollupJobs(request, state);
        assertTrue(hasRollupJobs);
    }
}


