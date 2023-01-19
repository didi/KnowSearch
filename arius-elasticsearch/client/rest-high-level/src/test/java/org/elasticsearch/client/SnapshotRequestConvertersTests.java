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

package org.elasticsearch.client;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.elasticsearch.action.admin.cluster.repositories.delete.DeleteRepositoryRequest;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesRequest;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryRequest;
import org.elasticsearch.action.admin.cluster.repositories.verify.VerifyRepositoryRequest;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsRequest;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotsStatusRequest;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.repositories.fs.FsRepository;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SnapshotRequestConvertersTests extends ESTestCase {

    public void testGetRepositories() {
        Map<String, String> expectedParams = new HashMap<>();
        StringBuilder endpoint = new StringBuilder("/_snapshot");

        GetRepositoriesRequest getRepositoriesRequest = new GetRepositoriesRequest();
        RequestConvertersTests.setRandomMasterTimeout(getRepositoriesRequest, expectedParams);
        RequestConvertersTests.setRandomLocal(getRepositoriesRequest::local, expectedParams);

        if (randomBoolean()) {
            String[] entries = new String[]{"a", "b", "c"};
            getRepositoriesRequest.repositories(entries);
            endpoint.append("/" + String.join(",", entries));
        }

        Request request = SnapshotRequestConverters.getRepositories(getRepositoriesRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint.toString()));
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(expectedParams, equalTo(request.getParameters()));
    }

    public void testCreateRepository() throws IOException {
        String repository = RequestConvertersTests.randomIndicesNames(1, 1)[0];
        String endpoint = "/_snapshot/" + repository;
        Path repositoryLocation = PathUtils.get(".");
        PutRepositoryRequest putRepositoryRequest = new PutRepositoryRequest(repository);
        putRepositoryRequest.type(FsRepository.TYPE);
        final boolean verify = randomBoolean();
        putRepositoryRequest.verify(verify);

        putRepositoryRequest.settings(
            Settings.builder()
                .put(FsRepository.LOCATION_SETTING.getKey(), repositoryLocation)
                .put(FsRepository.COMPRESS_SETTING.getKey(), randomBoolean())
                .put(FsRepository.CHUNK_SIZE_SETTING.getKey(), randomIntBetween(100, 1000), ByteSizeUnit.BYTES)
                .build());

        Request request = SnapshotRequestConverters.createRepository(putRepositoryRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint));
        assertThat(request.getMethod(), equalTo(HttpPut.METHOD_NAME));
        if (verify) {
            assertThat(request.getParameters().get("verify"), nullValue());
        } else {
            assertThat(request.getParameters().get("verify"), equalTo("false"));
        }
        RequestConvertersTests.assertToXContentBody(putRepositoryRequest, request.getEntity());
    }

    public void testDeleteRepository() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = RequestConvertersTests.randomIndicesNames(1, 1)[0];

        StringBuilder endpoint = new StringBuilder("/_snapshot/" + repository);

        DeleteRepositoryRequest deleteRepositoryRequest = new DeleteRepositoryRequest();
        deleteRepositoryRequest.name(repository);
        RequestConvertersTests.setRandomMasterTimeout(deleteRepositoryRequest, expectedParams);
        RequestConvertersTests.setRandomTimeout(deleteRepositoryRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request request = SnapshotRequestConverters.deleteRepository(deleteRepositoryRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint.toString()));
        assertThat(request.getMethod(), equalTo(HttpDelete.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertNull(request.getEntity());
    }

    public void testVerifyRepository() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = RequestConvertersTests.randomIndicesNames(1, 1)[0];
        String endpoint = "/_snapshot/" + repository + "/_verify";

        VerifyRepositoryRequest verifyRepositoryRequest = new VerifyRepositoryRequest(repository);
        RequestConvertersTests.setRandomMasterTimeout(verifyRepositoryRequest, expectedParams);
        RequestConvertersTests.setRandomTimeout(verifyRepositoryRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request request = SnapshotRequestConverters.verifyRepository(verifyRepositoryRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint));
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
    }

    public void testCreateSnapshot() throws IOException {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = RequestConvertersTests.randomIndicesNames(1, 1)[0];
        String snapshot = "snapshot-" + generateRandomStringArray(1, randomInt(10), false, false)[0];
        String endpoint = "/_snapshot/" + repository + "/" + snapshot;

        CreateSnapshotRequest createSnapshotRequest = new CreateSnapshotRequest(repository, snapshot);
        RequestConvertersTests.setRandomMasterTimeout(createSnapshotRequest, expectedParams);
        Boolean waitForCompletion = randomBoolean();
        createSnapshotRequest.waitForCompletion(waitForCompletion);

        expectedParams.put("wait_for_completion", waitForCompletion.toString());

        Request request = SnapshotRequestConverters.createSnapshot(createSnapshotRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint));
        assertThat(request.getMethod(), equalTo(HttpPut.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
        RequestConvertersTests.assertToXContentBody(createSnapshotRequest, request.getEntity());
    }

    public void testGetSnapshots() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = RequestConvertersTests.randomIndicesNames(1, 1)[0];
        String snapshot1 = "snapshot1-" + randomAlphaOfLengthBetween(2, 5).toLowerCase(Locale.ROOT);
        String snapshot2 = "snapshot2-" + randomAlphaOfLengthBetween(2, 5).toLowerCase(Locale.ROOT);

        String endpoint = String.format(Locale.ROOT, "/_snapshot/%s/%s,%s", repository, snapshot1, snapshot2);

        GetSnapshotsRequest getSnapshotsRequest = new GetSnapshotsRequest();
        getSnapshotsRequest.repository(repository);
        getSnapshotsRequest.snapshots(Arrays.asList(snapshot1, snapshot2).toArray(new String[0]));
        RequestConvertersTests.setRandomMasterTimeout(getSnapshotsRequest, expectedParams);

        if (randomBoolean()) {
            boolean ignoreUnavailable = randomBoolean();
            getSnapshotsRequest.ignoreUnavailable(ignoreUnavailable);
            expectedParams.put("ignore_unavailable", Boolean.toString(ignoreUnavailable));
        } else {
            expectedParams.put("ignore_unavailable", Boolean.FALSE.toString());
        }

        if (randomBoolean()) {
            boolean verbose = randomBoolean();
            getSnapshotsRequest.verbose(verbose);
            expectedParams.put("verbose", Boolean.toString(verbose));
        } else {
            expectedParams.put("verbose", Boolean.TRUE.toString());
        }

        Request request = SnapshotRequestConverters.getSnapshots(getSnapshotsRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint));
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertNull(request.getEntity());
    }

    public void testGetAllSnapshots() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = RequestConvertersTests.randomIndicesNames(1, 1)[0];

        String endpoint = String.format(Locale.ROOT, "/_snapshot/%s/_all", repository);

        GetSnapshotsRequest getSnapshotsRequest = new GetSnapshotsRequest(repository);
        RequestConvertersTests.setRandomMasterTimeout(getSnapshotsRequest, expectedParams);

        boolean ignoreUnavailable = randomBoolean();
        getSnapshotsRequest.ignoreUnavailable(ignoreUnavailable);
        expectedParams.put("ignore_unavailable", Boolean.toString(ignoreUnavailable));

        boolean verbose = randomBoolean();
        getSnapshotsRequest.verbose(verbose);
        expectedParams.put("verbose", Boolean.toString(verbose));

        Request request = SnapshotRequestConverters.getSnapshots(getSnapshotsRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint));
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertNull(request.getEntity());
    }

    public void testSnapshotsStatus() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = RequestConvertersTests.randomIndicesNames(1, 1)[0];
        String[] snapshots = RequestConvertersTests.randomIndicesNames(1, 5);
        StringBuilder snapshotNames = new StringBuilder(snapshots[0]);
        for (int idx = 1; idx < snapshots.length; idx++) {
            snapshotNames.append(",").append(snapshots[idx]);
        }
        boolean ignoreUnavailable = randomBoolean();
        String endpoint = "/_snapshot/" + repository + "/" + snapshotNames.toString() + "/_status";

        SnapshotsStatusRequest snapshotsStatusRequest = new SnapshotsStatusRequest(repository, snapshots);
        RequestConvertersTests.setRandomMasterTimeout(snapshotsStatusRequest, expectedParams);
        snapshotsStatusRequest.ignoreUnavailable(ignoreUnavailable);
        expectedParams.put("ignore_unavailable", Boolean.toString(ignoreUnavailable));

        Request request = SnapshotRequestConverters.snapshotsStatus(snapshotsStatusRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint));
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getEntity(), is(nullValue()));
    }

    public void testRestoreSnapshot() throws IOException {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = RequestConvertersTests.randomIndicesNames(1, 1)[0];
        String snapshot = "snapshot-" + randomAlphaOfLengthBetween(2, 5).toLowerCase(Locale.ROOT);
        String endpoint = String.format(Locale.ROOT, "/_snapshot/%s/%s/_restore", repository, snapshot);

        RestoreSnapshotRequest restoreSnapshotRequest = new RestoreSnapshotRequest(repository, snapshot);
        RequestConvertersTests.setRandomMasterTimeout(restoreSnapshotRequest, expectedParams);
        boolean waitForCompletion = randomBoolean();
        restoreSnapshotRequest.waitForCompletion(waitForCompletion);
        expectedParams.put("wait_for_completion", Boolean.toString(waitForCompletion));

        if (randomBoolean()) {
            String timeout = randomTimeValue();
            restoreSnapshotRequest.masterNodeTimeout(timeout);
            expectedParams.put("master_timeout", timeout);
        }

        Request request = SnapshotRequestConverters.restoreSnapshot(restoreSnapshotRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint));
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
        RequestConvertersTests.assertToXContentBody(restoreSnapshotRequest, request.getEntity());
    }

    public void testDeleteSnapshot() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = RequestConvertersTests.randomIndicesNames(1, 1)[0];
        String snapshot = "snapshot-" + randomAlphaOfLengthBetween(2, 5).toLowerCase(Locale.ROOT);

        String endpoint = String.format(Locale.ROOT, "/_snapshot/%s/%s", repository, snapshot);

        DeleteSnapshotRequest deleteSnapshotRequest = new DeleteSnapshotRequest();
        deleteSnapshotRequest.repository(repository);
        deleteSnapshotRequest.snapshot(snapshot);
        RequestConvertersTests.setRandomMasterTimeout(deleteSnapshotRequest, expectedParams);

        Request request = SnapshotRequestConverters.deleteSnapshot(deleteSnapshotRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint));
        assertThat(request.getMethod(), equalTo(HttpDelete.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertNull(request.getEntity());
    }
}
