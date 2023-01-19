/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.discovery;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.SnapshotDeletionsInProgress;
import org.elasticsearch.cluster.SnapshotsInProgress;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.snapshots.ConcurrentSnapshotExecutionException;
import org.elasticsearch.snapshots.SnapshotInfo;
import org.elasticsearch.snapshots.SnapshotMissingException;
import org.elasticsearch.snapshots.SnapshotState;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.disruption.NetworkDisruption;
import org.elasticsearch.test.transport.MockTransportService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;

/**
 * Tests snapshot operations during disruptions.
 */
@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.TEST, numDataNodes = 0, transportClientRatio = 0)
public class SnapshotDisruptionIT extends ESIntegTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Arrays.asList(MockTransportService.TestPlugin.class);
    }

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return Settings.builder().put(super.nodeSettings(nodeOrdinal))
            .put(AbstractDisruptionTestCase.DEFAULT_SETTINGS)
            .build();
    }

    public void testDisruptionOnSnapshotInitialization() throws Exception {
        final String idxName = "test";
        final List<String> allMasterEligibleNodes = internalCluster().startMasterOnlyNodes(3);
        final String dataNode = internalCluster().startDataOnlyNode();
        ensureStableCluster(4);

        createRandomIndex(idxName);

        logger.info("-->  creating repository");
        assertAcked(client().admin().cluster().preparePutRepository("test-repo")
            .setType("fs").setSettings(Settings.builder()
                .put("location", randomRepoPath())
                .put("compress", randomBoolean())
                .put("chunk_size", randomIntBetween(100, 1000), ByteSizeUnit.BYTES)));

        // Writing incompatible snapshot can cause this test to fail due to a race condition in repo initialization
        // by the current master and the former master. It is not causing any issues in real life scenario, but
        // might make this test to fail. We are going to complete initialization of the snapshot to prevent this failures.
        logger.info("-->  initializing the repository");
        assertEquals(SnapshotState.SUCCESS, client().admin().cluster().prepareCreateSnapshot("test-repo", "test-snap-1")
            .setWaitForCompletion(true).setIncludeGlobalState(true).setIndices().get().getSnapshotInfo().state());

        final String masterNode1 = internalCluster().getMasterName();
        Set<String> otherNodes = new HashSet<>();
        otherNodes.addAll(allMasterEligibleNodes);
        otherNodes.remove(masterNode1);
        otherNodes.add(dataNode);

        NetworkDisruption networkDisruption =
            new NetworkDisruption(new NetworkDisruption.TwoPartitions(Collections.singleton(masterNode1), otherNodes),
                new NetworkDisruption.NetworkUnresponsive());
        internalCluster().setDisruptionScheme(networkDisruption);

        ClusterService clusterService = internalCluster().clusterService(masterNode1);
        CountDownLatch disruptionStarted = new CountDownLatch(1);
        clusterService.addListener(new ClusterStateListener() {
            @Override
            public void clusterChanged(ClusterChangedEvent event) {
                SnapshotsInProgress snapshots = event.state().custom(SnapshotsInProgress.TYPE);
                if (snapshots != null && snapshots.entries().size() > 0) {
                    if (snapshots.entries().get(0).state() == SnapshotsInProgress.State.INIT) {
                        // The snapshot started, we can start disruption so the INIT state will arrive to another master node
                        logger.info("--> starting disruption");
                        networkDisruption.startDisrupting();
                        clusterService.removeListener(this);
                        disruptionStarted.countDown();
                    }
                }
            }
        });

        logger.info("--> starting snapshot");
        ActionFuture<CreateSnapshotResponse> future = client(masterNode1).admin().cluster()
            .prepareCreateSnapshot("test-repo", "test-snap-2").setWaitForCompletion(false).setIndices(idxName).execute();

        logger.info("--> waiting for disruption to start");
        assertTrue(disruptionStarted.await(1, TimeUnit.MINUTES));

        assertAllSnapshotsCompleted();

        logger.info("--> verify that snapshot was successful or no longer exist");
        assertBusy(() -> {
            try {
                assertSnapshotExists("test-repo", "test-snap-2");
            } catch (SnapshotMissingException exception) {
                logger.info("--> done verifying, snapshot doesn't exist");
            }
        }, 1, TimeUnit.MINUTES);

        logger.info("--> stopping disrupting");
        networkDisruption.stopDisrupting();
        ensureStableCluster(4, masterNode1);
        logger.info("--> done");

        try {
            future.get();
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause.getCause() instanceof ConcurrentSnapshotExecutionException) {
                logger.info("--> got exception from race in master operation retries");
            } else {
                logger.info("--> got exception from hanged master", ex);
            }
        }

        assertAllSnapshotsCompleted();
    }

    private void assertAllSnapshotsCompleted() throws Exception {
        logger.info("--> wait until the snapshot is done");
        assertBusy(() -> {
            ClusterState state = dataNodeClient().admin().cluster().prepareState().get().getState();
            SnapshotsInProgress snapshots = state.custom(SnapshotsInProgress.TYPE);
            SnapshotDeletionsInProgress snapshotDeletionsInProgress = state.custom(SnapshotDeletionsInProgress.TYPE);
            if (snapshots != null && snapshots.entries().isEmpty() == false) {
                logger.info("Current snapshot state [{}]", snapshots.entries().get(0).state());
                fail("Snapshot is still running");
            } else if (snapshotDeletionsInProgress != null && snapshotDeletionsInProgress.hasDeletionsInProgress()) {
                logger.info("Current snapshot deletion state [{}]", snapshotDeletionsInProgress);
                fail("Snapshot deletion is still running");
            } else {
                logger.info("Snapshot is no longer in the cluster state");
            }
        }, 1L, TimeUnit.MINUTES);
    }

    private void assertSnapshotExists(String repository, String snapshot) {
        GetSnapshotsResponse snapshotsStatusResponse = dataNodeClient().admin().cluster().prepareGetSnapshots(repository)
                .setSnapshots(snapshot).get();
        SnapshotInfo snapshotInfo = snapshotsStatusResponse.getSnapshots().get(0);
        assertEquals(SnapshotState.SUCCESS, snapshotInfo.state());
        assertEquals(snapshotInfo.totalShards(), snapshotInfo.successfulShards());
        assertEquals(0, snapshotInfo.failedShards());
        logger.info("--> done verifying, snapshot exists");
    }

    private void createRandomIndex(String idxName) throws InterruptedException {
        assertAcked(prepareCreate(idxName, 0, Settings.builder().put("number_of_shards", between(1, 20))
            .put("number_of_replicas", 0)));
        logger.info("--> indexing some data");
        final int numdocs = randomIntBetween(10, 100);
        IndexRequestBuilder[] builders = new IndexRequestBuilder[numdocs];
        for (int i = 0; i < builders.length; i++) {
            builders[i] = client().prepareIndex(idxName, "type1", Integer.toString(i)).setSource("field1", "bar " + i);
        }
        indexRandom(true, builders);
    }
}
