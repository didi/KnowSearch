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
package org.elasticsearch.snapshots;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsRequest;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotStatus;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotsStatusRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.SnapshotsInProgress;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.core.internal.io.IOUtils;
import org.elasticsearch.repositories.RepositoriesService;
import org.elasticsearch.repositories.Repository;
import org.elasticsearch.repositories.blobstore.BlobStoreRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class SnapshotStatusApisIT extends AbstractSnapshotIntegTestCase {

    public void testStatusApiConsistency() {
        Client client = client();

        logger.info("-->  creating repository");
        assertAcked(client.admin().cluster().preparePutRepository("test-repo").setType("fs").setSettings(
            Settings.builder().put("location", randomRepoPath()).build()));

        createIndex("test-idx-1", "test-idx-2", "test-idx-3");
        ensureGreen();

        logger.info("--> indexing some data");
        for (int i = 0; i < 100; i++) {
            index("test-idx-1", "_doc", Integer.toString(i), "foo", "bar" + i);
            index("test-idx-2", "_doc", Integer.toString(i), "foo", "baz" + i);
            index("test-idx-3", "_doc", Integer.toString(i), "foo", "baz" + i);
        }
        refresh();

        logger.info("--> snapshot");
        CreateSnapshotResponse createSnapshotResponse = client.admin().cluster().prepareCreateSnapshot("test-repo", "test-snap")
            .setWaitForCompletion(true).get();
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), greaterThan(0));
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(),
            equalTo(createSnapshotResponse.getSnapshotInfo().totalShards()));

        List<SnapshotInfo> snapshotInfos = client.admin().cluster().prepareGetSnapshots("test-repo").get().getSnapshots();
        assertThat(snapshotInfos.size(), equalTo(1));
        SnapshotInfo snapshotInfo = snapshotInfos.get(0);
        assertThat(snapshotInfo.state(), equalTo(SnapshotState.SUCCESS));
        assertThat(snapshotInfo.version(), equalTo(Version.CURRENT));

        final List<SnapshotStatus> snapshotStatus = client.admin().cluster().snapshotsStatus(
            new SnapshotsStatusRequest("test-repo", new String[]{"test-snap"})).actionGet().getSnapshots();
        assertThat(snapshotStatus.size(), equalTo(1));
        final SnapshotStatus snStatus = snapshotStatus.get(0);
        assertEquals(snStatus.getStats().getStartTime(), snapshotInfo.startTime());
        assertEquals(snStatus.getStats().getTime(), snapshotInfo.endTime() - snapshotInfo.startTime());
    }

    public void testStatusAPICallInProgressSnapshot() throws Exception {
        Client client = client();

        logger.info("-->  creating repository");
        assertAcked(client.admin().cluster().preparePutRepository("test-repo").setType("mock").setSettings(
            Settings.builder().put("location", randomRepoPath()).put("block_on_data", true)));

        createIndex("test-idx-1");
        ensureGreen();

        logger.info("--> indexing some data");
        for (int i = 0; i < 100; i++) {
            index("test-idx-1", "_doc", Integer.toString(i), "foo", "bar" + i);
        }
        refresh();

        logger.info("--> snapshot");
        ActionFuture<CreateSnapshotResponse> createSnapshotResponseActionFuture =
            client.admin().cluster().prepareCreateSnapshot("test-repo", "test-snap").setWaitForCompletion(true).execute();

        logger.info("--> wait for data nodes to get blocked");
        waitForBlockOnAnyDataNode("test-repo", TimeValue.timeValueMinutes(1));

        assertBusy(() -> assertEquals(SnapshotsInProgress.State.STARTED, client.admin().cluster().snapshotsStatus(
            new SnapshotsStatusRequest("test-repo", new String[]{"test-snap"})).actionGet().getSnapshots().get(0).getState()), 1L,
            TimeUnit.MINUTES);

        logger.info("--> unblock all data nodes");
        unblockAllDataNodes("test-repo");

        logger.info("--> wait for snapshot to finish");
        createSnapshotResponseActionFuture.actionGet();
    }

    public void testExceptionOnMissingSnapBlob() throws IOException {
        disableRepoConsistencyCheck("This test intentionally corrupts the repository");

        logger.info("--> creating repository");
        final Path repoPath = randomRepoPath();
        assertAcked(client().admin().cluster().preparePutRepository("test-repo").setType("fs").setSettings(
            Settings.builder().put("location", repoPath).build()));

        logger.info("--> snapshot");
        final CreateSnapshotResponse response =
            client().admin().cluster().prepareCreateSnapshot("test-repo", "test-snap").setWaitForCompletion(true).get();

        logger.info("--> delete snap-${uuid}.dat file for this snapshot to simulate concurrent delete");
        IOUtils.rm(repoPath.resolve(BlobStoreRepository.SNAPSHOT_PREFIX + response.getSnapshotInfo().snapshotId().getUUID() + ".dat"));

        expectThrows(SnapshotMissingException.class, () -> client().admin().cluster()
            .getSnapshots(new GetSnapshotsRequest("test-repo", new String[] {"test-snap"})).actionGet());
    }

    public void testExceptionOnMissingShardLevelSnapBlob() throws IOException {
        disableRepoConsistencyCheck("This test intentionally corrupts the repository");

        logger.info("--> creating repository");
        final Path repoPath = randomRepoPath();
        assertAcked(client().admin().cluster().preparePutRepository("test-repo").setType("fs").setSettings(
            Settings.builder().put("location", repoPath).build()));

        createIndex("test-idx-1");
        ensureGreen();

        logger.info("--> indexing some data");
        for (int i = 0; i < 100; i++) {
            index("test-idx-1", "_doc", Integer.toString(i), "foo", "bar" + i);
        }
        refresh();

        logger.info("--> snapshot");
        final CreateSnapshotResponse response =
            client().admin().cluster().prepareCreateSnapshot("test-repo", "test-snap").setWaitForCompletion(true).get();

        logger.info("--> delete shard-level snap-${uuid}.dat file for one shard in this snapshot to simulate concurrent delete");
        final RepositoriesService service = internalCluster().getMasterNodeInstance(RepositoriesService.class);
        final Repository repository = service.repository("test-repo");
        final String indexRepoId = getRepositoryData(repository).resolveIndexId(response.getSnapshotInfo().indices().get(0)).getId();
        IOUtils.rm(repoPath.resolve("indices").resolve(indexRepoId).resolve("0").resolve(
            BlobStoreRepository.SNAPSHOT_PREFIX + response.getSnapshotInfo().snapshotId().getUUID() + ".dat"));

        expectThrows(SnapshotMissingException.class, () -> client().admin().cluster()
            .prepareSnapshotStatus("test-repo").setSnapshots("test-snap").execute().actionGet());
    }
}
