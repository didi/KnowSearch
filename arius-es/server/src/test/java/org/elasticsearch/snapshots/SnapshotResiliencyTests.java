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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.SetOnce;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.RequestValidators;
import org.elasticsearch.action.StepListener;
import org.elasticsearch.action.admin.cluster.repositories.cleanup.CleanupRepositoryAction;
import org.elasticsearch.action.admin.cluster.repositories.cleanup.CleanupRepositoryRequest;
import org.elasticsearch.action.admin.cluster.repositories.cleanup.CleanupRepositoryResponse;
import org.elasticsearch.action.admin.cluster.repositories.cleanup.TransportCleanupRepositoryAction;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryAction;
import org.elasticsearch.action.admin.cluster.repositories.put.TransportPutRepositoryAction;
import org.elasticsearch.action.admin.cluster.reroute.ClusterRerouteAction;
import org.elasticsearch.action.admin.cluster.reroute.ClusterRerouteRequest;
import org.elasticsearch.action.admin.cluster.reroute.TransportClusterRerouteAction;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.create.TransportCreateSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.delete.TransportDeleteSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.restore.TransportRestoreSnapshotAction;
import org.elasticsearch.action.admin.cluster.state.ClusterStateAction;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.cluster.state.TransportClusterStateAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.create.TransportCreateIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.TransportDeleteIndexAction;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingAction;
import org.elasticsearch.action.admin.indices.mapping.put.TransportPutMappingAction;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresAction;
import org.elasticsearch.action.admin.indices.shards.TransportIndicesShardStoresAction;
import org.elasticsearch.action.bulk.BulkAction;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.bulk.TransportBulkAction;
import org.elasticsearch.action.bulk.TransportShardBulkAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.resync.TransportResyncReplicationAction;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchExecutionStatsCollector;
import org.elasticsearch.action.search.SearchPhaseController;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchTransportService;
import org.elasticsearch.action.search.TransportSearchAction;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.ActionTestUtils;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.AutoCreateIndex;
import org.elasticsearch.action.support.DestructiveOperations;
import org.elasticsearch.action.support.GroupedActionListener;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateHelper;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.ClusterModule;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ESAllocationTestCase;
import org.elasticsearch.cluster.NodeConnectionsService;
import org.elasticsearch.cluster.SnapshotsInProgress;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.action.index.NodeMappingRefreshAction;
import org.elasticsearch.cluster.action.shard.ShardStateAction;
import org.elasticsearch.cluster.coordination.ClusterBootstrapService;
import org.elasticsearch.cluster.coordination.CoordinationMetaData.VotingConfiguration;
import org.elasticsearch.cluster.coordination.CoordinationState;
import org.elasticsearch.cluster.coordination.Coordinator;
import org.elasticsearch.cluster.coordination.CoordinatorTests;
import org.elasticsearch.cluster.coordination.DeterministicTaskQueue;
import org.elasticsearch.cluster.coordination.ElectionStrategy;
import org.elasticsearch.cluster.coordination.InMemoryPersistedState;
import org.elasticsearch.cluster.coordination.MockSinglePrioritizingExecutor;
import org.elasticsearch.cluster.metadata.AliasValidator;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MetaDataCreateIndexService;
import org.elasticsearch.cluster.metadata.MetaDataDeleteIndexService;
import org.elasticsearch.cluster.metadata.MetaDataIndexUpgradeService;
import org.elasticsearch.cluster.metadata.MetaDataMappingService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodeRole;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.BatchedRerouteService;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.UnassignedInfo;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.cluster.routing.allocation.command.AllocateEmptyPrimaryAllocationCommand;
import org.elasticsearch.cluster.service.ClusterApplierService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.cluster.service.FakeThreadPoolMasterService;
import org.elasticsearch.cluster.service.MasterService;
import org.elasticsearch.common.CheckedConsumer;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.util.PageCacheRecycler;
import org.elasticsearch.common.util.concurrent.AbstractRunnable;
import org.elasticsearch.common.util.concurrent.PrioritizedEsThreadPoolExecutor;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.env.TestEnvironment;
import org.elasticsearch.gateway.MetaStateService;
import org.elasticsearch.gateway.TransportNodesListGatewayStartedShards;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisRegistry;
import org.elasticsearch.index.seqno.GlobalCheckpointSyncAction;
import org.elasticsearch.index.seqno.RetentionLeaseSyncer;
import org.elasticsearch.index.seqno.RetentionLeaseSyncer;
import org.elasticsearch.index.seqno.RetentionLeaseSyncer;
import org.elasticsearch.index.shard.PrimaryReplicaSyncer;
import org.elasticsearch.indices.IndicesModule;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.indices.breaker.NoneCircuitBreakerService;
import org.elasticsearch.indices.cluster.IndicesClusterStateService;
import org.elasticsearch.indices.flush.SyncedFlushService;
import org.elasticsearch.indices.mapper.MapperRegistry;
import org.elasticsearch.indices.recovery.PeerRecoverySourceService;
import org.elasticsearch.indices.recovery.PeerRecoveryTargetService;
import org.elasticsearch.indices.recovery.RecoverySettings;
import org.elasticsearch.ingest.IngestService;
import org.elasticsearch.node.ResponseCollectorService;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.repositories.RepositoriesService;
import org.elasticsearch.repositories.Repository;
import org.elasticsearch.repositories.RepositoryData;
import org.elasticsearch.repositories.blobstore.BlobStoreRepository;
import org.elasticsearch.repositories.blobstore.BlobStoreTestUtil;
import org.elasticsearch.repositories.fs.FsRepository;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.SearchService;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.FetchPhase;
import org.elasticsearch.snapshots.mockstore.MockEventuallyConsistentRepository;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.disruption.DisruptableMockTransport;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportException;
import org.elasticsearch.transport.TransportInterceptor;
import org.elasticsearch.transport.TransportRequest;
import org.elasticsearch.transport.TransportRequestHandler;
import org.elasticsearch.transport.TransportService;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.elasticsearch.action.support.ActionTestUtils.assertNoFailureListener;
import static org.elasticsearch.env.Environment.PATH_HOME_SETTING;
import static org.elasticsearch.node.Node.NODE_NAME_SETTING;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.Mockito.mock;

public class SnapshotResiliencyTests extends ESTestCase {

    private DeterministicTaskQueue deterministicTaskQueue;

    private TestClusterNodes testClusterNodes;

    private Path tempDir;

    /**
     * Context shared by all the node's {@link Repository} instances if the eventually consistent blobstore is to be used.
     * {@code null} if not using the eventually consistent blobstore.
     */
    @Nullable private MockEventuallyConsistentRepository.Context blobStoreContext;

    @Before
    public void createServices() {
        tempDir = createTempDir();
        if (randomBoolean()) {
            blobStoreContext = new MockEventuallyConsistentRepository.Context();
        }
        deterministicTaskQueue =
            new DeterministicTaskQueue(Settings.builder().put(NODE_NAME_SETTING.getKey(), "shared").build(), random());
    }

    @After
    public void verifyReposThenStopServices() {
        try {
            clearDisruptionsAndAwaitSync();

            final StepListener<CleanupRepositoryResponse> cleanupResponse = new StepListener<>();
            client().admin().cluster().cleanupRepository(
                new CleanupRepositoryRequest("repo"), cleanupResponse);
            final AtomicBoolean cleanedUp = new AtomicBoolean(false);
            continueOrDie(cleanupResponse, r -> cleanedUp.set(true));

            runUntil(cleanedUp::get, TimeUnit.MINUTES.toMillis(1L));

            if (blobStoreContext != null) {
                blobStoreContext.forceConsistent();
            }
            BlobStoreTestUtil.assertConsistency(
                (BlobStoreRepository) testClusterNodes.randomMasterNodeSafe().repositoriesService.repository("repo"),
                Runnable::run);
        } finally {
            testClusterNodes.nodes.values().forEach(TestClusterNodes.TestClusterNode::stop);
        }
    }

    public void testSuccessfulSnapshotAndRestore() {
        setupTestCluster(randomFrom(1, 3, 5), randomIntBetween(2, 10));

        String repoName = "repo";
        String snapshotName = "snapshot";
        final String index = "test";
        final int shards = randomIntBetween(1, 10);
        final int documents = randomIntBetween(0, 100);

        final TestClusterNodes.TestClusterNode masterNode =
            testClusterNodes.currentMaster(testClusterNodes.nodes.values().iterator().next().clusterService.state());

        final StepListener<CreateSnapshotResponse> createSnapshotResponseListener = new StepListener<>();

        continueOrDie(createRepoAndIndex(repoName, index, shards), createIndexResponse -> {
            final Runnable afterIndexing = () -> client().admin().cluster().prepareCreateSnapshot(repoName, snapshotName)
                .setWaitForCompletion(true).execute(createSnapshotResponseListener);
            if (documents == 0) {
                afterIndexing.run();
            } else {
                final BulkRequest bulkRequest = new BulkRequest().setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                for (int i = 0; i < documents; ++i) {
                    bulkRequest.add(new IndexRequest(index).source(Collections.singletonMap("foo", "bar" + i)));
                }
                final StepListener<BulkResponse> bulkResponseStepListener = new StepListener<>();
                client().bulk(bulkRequest, bulkResponseStepListener);
                continueOrDie(bulkResponseStepListener, bulkResponse -> {
                    assertFalse("Failures in bulk response: " + bulkResponse.buildFailureMessage(), bulkResponse.hasFailures());
                    assertEquals(documents, bulkResponse.getItems().length);
                    afterIndexing.run();
                });
            }
        });

        final StepListener<AcknowledgedResponse> deleteIndexListener = new StepListener<>();

        continueOrDie(createSnapshotResponseListener,
            createSnapshotResponse -> client().admin().indices().delete(new DeleteIndexRequest(index), deleteIndexListener));

        final StepListener<RestoreSnapshotResponse> restoreSnapshotResponseListener = new StepListener<>();
        continueOrDie(deleteIndexListener, ignored -> client().admin().cluster().restoreSnapshot(
            new RestoreSnapshotRequest(repoName, snapshotName).waitForCompletion(true), restoreSnapshotResponseListener));

        final StepListener<SearchResponse> searchResponseListener = new StepListener<>();
        continueOrDie(restoreSnapshotResponseListener, restoreSnapshotResponse -> {
            assertEquals(shards, restoreSnapshotResponse.getRestoreInfo().totalShards());
            client().search(
                new SearchRequest(index).source(new SearchSourceBuilder().size(0).trackTotalHits(true)), searchResponseListener);
        });

        final AtomicBoolean documentCountVerified = new AtomicBoolean();
        continueOrDie(searchResponseListener, r -> {
            assertEquals(documents, Objects.requireNonNull(r.getHits().getTotalHits()).value);
            documentCountVerified.set(true);
        });

        runUntil(documentCountVerified::get, TimeUnit.MINUTES.toMillis(5L));
        assertNotNull(createSnapshotResponseListener.result());
        assertNotNull(restoreSnapshotResponseListener.result());
        assertTrue(documentCountVerified.get());
        SnapshotsInProgress finalSnapshotsInProgress = masterNode.clusterService.state().custom(SnapshotsInProgress.TYPE);
        assertFalse(finalSnapshotsInProgress.entries().stream().anyMatch(entry -> entry.state().completed() == false));
        final Repository repository = masterNode.repositoriesService.repository(repoName);
        Collection<SnapshotId> snapshotIds = getRepositoryData(repository).getSnapshotIds();
        assertThat(snapshotIds, hasSize(1));

        final SnapshotInfo snapshotInfo = repository.getSnapshotInfo(snapshotIds.iterator().next());
        assertEquals(SnapshotState.SUCCESS, snapshotInfo.state());
        assertThat(snapshotInfo.indices(), containsInAnyOrder(index));
        assertEquals(shards, snapshotInfo.successfulShards());
        assertEquals(0, snapshotInfo.failedShards());
    }

    public void testConcurrentSnapshotCreateAndDeleteOther() {
        setupTestCluster(randomFrom(1, 3, 5), randomIntBetween(2, 10));

        String repoName = "repo";
        String snapshotName = "snapshot";
        final String index = "test";
        final int shards = randomIntBetween(1, 10);

        TestClusterNodes.TestClusterNode masterNode =
            testClusterNodes.currentMaster(testClusterNodes.nodes.values().iterator().next().clusterService.state());

        final StepListener<CreateSnapshotResponse> createSnapshotResponseStepListener = new StepListener<>();

        continueOrDie(createRepoAndIndex(repoName, index, shards),
            createIndexResponse -> client().admin().cluster().prepareCreateSnapshot(repoName, snapshotName)
                .setWaitForCompletion(true).execute(createSnapshotResponseStepListener));

        final StepListener<CreateSnapshotResponse> createOtherSnapshotResponseStepListener = new StepListener<>();

        continueOrDie(createSnapshotResponseStepListener,
            createSnapshotResponse -> client().admin().cluster().prepareCreateSnapshot(repoName, "snapshot-2")
                .execute(createOtherSnapshotResponseStepListener));

        final StepListener<Boolean> deleteSnapshotStepListener = new StepListener<>();

        continueOrDie(createOtherSnapshotResponseStepListener,
            createSnapshotResponse -> client().admin().cluster().deleteSnapshot(
                new DeleteSnapshotRequest(repoName, snapshotName), ActionListener.wrap(
                    resp -> deleteSnapshotStepListener.onResponse(true),
                    e -> {
                        final Throwable unwrapped =
                            ExceptionsHelper.unwrap(e, ConcurrentSnapshotExecutionException.class);
                        assertThat(unwrapped, instanceOf(ConcurrentSnapshotExecutionException.class));
                        deleteSnapshotStepListener.onResponse(false);
                    })));

        final StepListener<CreateSnapshotResponse> createAnotherSnapshotResponseStepListener = new StepListener<>();

        continueOrDie(deleteSnapshotStepListener, deleted -> {
            if (deleted) {
                // The delete worked out, creating a third snapshot
                client().admin().cluster().prepareCreateSnapshot(repoName, snapshotName).setWaitForCompletion(true)
                    .execute(createAnotherSnapshotResponseStepListener);
                continueOrDie(createAnotherSnapshotResponseStepListener, createSnapshotResponse ->
                    assertEquals(createSnapshotResponse.getSnapshotInfo().state(), SnapshotState.SUCCESS));
            } else {
                createAnotherSnapshotResponseStepListener.onResponse(null);
            }
        });

        deterministicTaskQueue.runAllRunnableTasks();

        SnapshotsInProgress finalSnapshotsInProgress = masterNode.clusterService.state().custom(SnapshotsInProgress.TYPE);
        assertFalse(finalSnapshotsInProgress.entries().stream().anyMatch(entry -> entry.state().completed() == false));
        final Repository repository = masterNode.repositoriesService.repository(repoName);
        Collection<SnapshotId> snapshotIds = getRepositoryData(repository).getSnapshotIds();
        // We end up with two snapshots no matter if the delete worked out or not
        assertThat(snapshotIds, hasSize(2));

        for (SnapshotId snapshotId : snapshotIds) {
            final SnapshotInfo snapshotInfo = repository.getSnapshotInfo(snapshotId);
            assertEquals(SnapshotState.SUCCESS, snapshotInfo.state());
            assertThat(snapshotInfo.indices(), containsInAnyOrder(index));
            assertEquals(shards, snapshotInfo.successfulShards());
            assertEquals(0, snapshotInfo.failedShards());
        }
    }

    /**
     * Simulates concurrent restarts of data and master nodes as well as relocating a primary shard, while starting and subsequently
     * deleting a snapshot.
     */
    private RepositoryData getRepositoryData(Repository repository) {
        final PlainActionFuture<RepositoryData> res = PlainActionFuture.newFuture();
        repository.getRepositoryData(res);
        deterministicTaskQueue.runAllRunnableTasks();
        assertTrue(res.isDone());
        return res.actionGet();
    }

    private StepListener<CreateIndexResponse> createRepoAndIndex(String repoName, String index, int shards) {
        final StepListener<AcknowledgedResponse> createRepositoryListener = new StepListener<>();

        client().admin().cluster().preparePutRepository(repoName).setType(FsRepository.TYPE)
            .setSettings(Settings.builder().put("location", randomAlphaOfLength(10))).execute(createRepositoryListener);

        final StepListener<CreateIndexResponse> createIndexResponseStepListener = new StepListener<>();

        continueOrDie(createRepositoryListener, acknowledgedResponse -> client().admin().indices().create(
            new CreateIndexRequest(index).waitForActiveShards(ActiveShardCount.ALL).settings(defaultIndexSettings(shards)),
            createIndexResponseStepListener));

        return createIndexResponseStepListener;
    }

    private void clearDisruptionsAndAwaitSync() {
        testClusterNodes.clearNetworkDisruptions();
        stabilize();
    }

    private void disconnectOrRestartDataNode() {
        if (randomBoolean()) {
            disconnectRandomDataNode();
        } else {
            testClusterNodes.randomDataNode().ifPresent(TestClusterNodes.TestClusterNode::restart);
        }
    }

    private void disconnectOrRestartMasterNode() {
        testClusterNodes.randomMasterNode().ifPresent(masterNode -> {
            if (randomBoolean()) {
                testClusterNodes.disconnectNode(masterNode);
            } else {
                masterNode.restart();
            }
        });
    }

    private void disconnectRandomDataNode() {
        testClusterNodes.randomDataNode().ifPresent(n -> testClusterNodes.disconnectNode(n));
    }

    private void startCluster() {
        final ClusterState initialClusterState =
            new ClusterState.Builder(ClusterName.DEFAULT).nodes(testClusterNodes.discoveryNodes()).build();
        testClusterNodes.nodes.values().forEach(testClusterNode -> testClusterNode.start(initialClusterState));

        deterministicTaskQueue.advanceTime();
        deterministicTaskQueue.runAllRunnableTasks();

        final VotingConfiguration votingConfiguration = new VotingConfiguration(testClusterNodes.nodes.values().stream().map(n -> n.node)
                .filter(DiscoveryNode::isMasterNode).map(DiscoveryNode::getId).collect(Collectors.toSet()));
        testClusterNodes.nodes.values().stream().filter(n -> n.node.isMasterNode()).forEach(
            testClusterNode -> testClusterNode.coordinator.setInitialConfiguration(votingConfiguration));
        // Connect all nodes to each other
        testClusterNodes.nodes.values().forEach(node -> testClusterNodes.nodes.values().forEach(
            n -> n.transportService.connectToNode(node.node, null,
                ActionTestUtils.assertNoFailureListener(c -> logger.info("--> Connected [{}] to [{}]", n.node, node.node)))));
        stabilize();
    }

    private void stabilize() {
        runUntil(
            () -> {
                final Collection<ClusterState> clusterStates =
                    testClusterNodes.nodes.values().stream().map(node -> node.clusterService.state()).collect(Collectors.toList());
                final Set<String> masterNodeIds = clusterStates.stream()
                    .map(clusterState -> clusterState.nodes().getMasterNodeId()).collect(Collectors.toSet());
                final Set<Long> terms = clusterStates.stream().map(ClusterState::term).collect(Collectors.toSet());
                final List<Long> versions = clusterStates.stream().map(ClusterState::version).distinct().collect(Collectors.toList());
                return versions.size() == 1 && masterNodeIds.size() == 1 && masterNodeIds.contains(null) == false && terms.size() == 1;
            },
            TimeUnit.MINUTES.toMillis(1L)
        );
    }

    private void runUntil(Supplier<Boolean> fulfilled, long timeout) {
        final long start = deterministicTaskQueue.getCurrentTimeMillis();
        while (timeout > deterministicTaskQueue.getCurrentTimeMillis() - start) {
            if (fulfilled.get()) {
                return;
            }
            deterministicTaskQueue.runAllRunnableTasks();
            deterministicTaskQueue.advanceTime();
        }
        fail("Condition wasn't fulfilled.");
    }

    private void setupTestCluster(int masterNodes, int dataNodes) {
        testClusterNodes = new TestClusterNodes(masterNodes, dataNodes);
        startCluster();
    }

    private void scheduleSoon(Runnable runnable) {
        deterministicTaskQueue.scheduleAt(deterministicTaskQueue.getCurrentTimeMillis() + randomLongBetween(0, 100L), runnable);
    }

    private void scheduleNow(Runnable runnable) {
        deterministicTaskQueue.scheduleNow(runnable);
    }

    private static Settings defaultIndexSettings(int shards) {
        // TODO: randomize replica count settings once recovery operations aren't blocking anymore
        return Settings.builder()
            .put(IndexMetaData.INDEX_NUMBER_OF_SHARDS_SETTING.getKey(), shards)
            .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 0).build();
    }

    private static <T> void continueOrDie(StepListener<T> listener, CheckedConsumer<T, Exception> onResponse) {
        listener.whenComplete(onResponse, e -> {
            throw new AssertionError(e);
        });
    }

    private static <T> ActionListener<T> noopListener() {
        return ActionListener.wrap(() -> {});
    }

    public NodeClient client() {
        // Select from sorted list of nodes
        final List<TestClusterNodes.TestClusterNode> nodes = testClusterNodes.nodes.values().stream()
            .filter(n -> testClusterNodes.disconnectedNodes.contains(n.node.getName()) == false)
            .sorted(Comparator.comparing(n -> n.node.getName())).collect(Collectors.toList());
        if (nodes.isEmpty()) {
            throw new AssertionError("No nodes available");
        }
        return randomFrom(nodes).client;
    }

    /**
     * Create a {@link Environment} with random path.home and path.repo
     **/
    private Environment createEnvironment(String nodeName) {
        return TestEnvironment.newEnvironment(Settings.builder()
            .put(NODE_NAME_SETTING.getKey(), nodeName)
            .put(PATH_HOME_SETTING.getKey(), tempDir.resolve(nodeName).toAbsolutePath())
            .put(Environment.PATH_REPO_SETTING.getKey(), tempDir.resolve("repo").toAbsolutePath())
            .putList(ClusterBootstrapService.INITIAL_MASTER_NODES_SETTING.getKey(),
                ClusterBootstrapService.INITIAL_MASTER_NODES_SETTING.get(Settings.EMPTY))
            .put(MappingUpdatedAction.INDICES_MAX_IN_FLIGHT_UPDATES_SETTING.getKey(), 1000) // o.w. some tests might block
            .build());
    }

    private static ClusterState stateForNode(ClusterState state, DiscoveryNode node) {
        // Remove and add back local node to update ephemeral id on restarts
        return ClusterState.builder(state).nodes(DiscoveryNodes.builder(
            state.nodes()).remove(node.getId()).add(node).localNodeId(node.getId())).build();
    }

    private final class TestClusterNodes {

        // LinkedHashMap so we have deterministic ordering when iterating over the map in tests
        private final Map<String, TestClusterNode> nodes = new LinkedHashMap<>();

        /**
         * Node names that are disconnected from all other nodes.
         */
        private final Set<String> disconnectedNodes = new HashSet<>();

        TestClusterNodes(int masterNodes, int dataNodes) {
            for (int i = 0; i < masterNodes; ++i) {
                nodes.computeIfAbsent("node" + i, nodeName -> {
                    try {
                        return newMasterNode(nodeName);
                    } catch (IOException e) {
                        throw new AssertionError(e);
                    }
                });
            }
            for (int i = 0; i < dataNodes; ++i) {
                nodes.computeIfAbsent("data-node" + i, nodeName -> {
                    try {
                        return newDataNode(nodeName);
                    } catch (IOException e) {
                        throw new AssertionError(e);
                    }
                });
            }
        }

        public TestClusterNode nodeById(final String nodeId) {
            return nodes.values().stream().filter(n -> n.node.getId().equals(nodeId)).findFirst()
                .orElseThrow(() -> new AssertionError("Could not find node by id [" + nodeId + ']'));
        }

        private TestClusterNode newMasterNode(String nodeName) throws IOException {
            return newNode(nodeName, DiscoveryNodeRole.MASTER_ROLE);
        }

        private TestClusterNode newDataNode(String nodeName) throws IOException {
            return newNode(nodeName, DiscoveryNodeRole.DATA_ROLE);
        }

        private TestClusterNode newNode(String nodeName, DiscoveryNodeRole role) throws IOException {
            return new TestClusterNode(
                new DiscoveryNode(nodeName, randomAlphaOfLength(10), buildNewFakeTransportAddress(), emptyMap(),
                    Collections.singleton(role), Version.CURRENT));
        }

        public TestClusterNode randomMasterNodeSafe() {
            return randomMasterNode().orElseThrow(() -> new AssertionError("Expected to find at least one connected master node"));
        }

        public Optional<TestClusterNode> randomMasterNode() {
            // Select from sorted list of data-nodes here to not have deterministic behaviour
            final List<TestClusterNode> masterNodes = testClusterNodes.nodes.values().stream()
                .filter(n -> n.node.isMasterNode())
                .filter(n -> disconnectedNodes.contains(n.node.getName()) == false)
                .sorted(Comparator.comparing(n -> n.node.getName())).collect(Collectors.toList());
            return masterNodes.isEmpty() ? Optional.empty() : Optional.of(randomFrom(masterNodes));
        }

        public void stopNode(TestClusterNode node) {
            node.stop();
            nodes.remove(node.node.getName());
        }

        public TestClusterNode randomDataNodeSafe(String... excludedNames) {
            return randomDataNode(excludedNames).orElseThrow(() -> new AssertionError("Could not find another data node."));
        }

        public Optional<TestClusterNode> randomDataNode(String... excludedNames) {
            // Select from sorted list of data-nodes here to not have deterministic behaviour
            final List<TestClusterNode> dataNodes = testClusterNodes.nodes.values().stream().filter(n -> n.node.isDataNode())
                .filter(n -> {
                    for (final String nodeName : excludedNames) {
                        if (n.node.getName().equals(nodeName)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted(Comparator.comparing(n -> n.node.getName())).collect(Collectors.toList());
            return dataNodes.isEmpty() ? Optional.empty() : Optional.ofNullable(randomFrom(dataNodes));
        }

        public void disconnectNode(TestClusterNode node) {
            if (disconnectedNodes.contains(node.node.getName())) {
                return;
            }
            testClusterNodes.nodes.values().forEach(n -> n.transportService.getConnectionManager().disconnectFromNode(node.node));
            disconnectedNodes.add(node.node.getName());
        }

        public void clearNetworkDisruptions() {
            final Set<String> disconnectedNodes = new HashSet<>(this.disconnectedNodes);
            this.disconnectedNodes.clear();
            disconnectedNodes.forEach(nodeName -> {
                if (testClusterNodes.nodes.containsKey(nodeName)) {
                    final DiscoveryNode node = testClusterNodes.nodes.get(nodeName).node;
                    testClusterNodes.nodes.values().forEach(
                        n -> n.transportService.openConnection(node, null,
                            ActionTestUtils.assertNoFailureListener(c -> logger.debug("--> Connected [{}] to [{}]", n.node, node))));
                }
            });
        }

        /**
         * Builds a {@link DiscoveryNodes} instance that holds the nodes in this test cluster.
         * @return DiscoveryNodes
         */
        public DiscoveryNodes discoveryNodes() {
            DiscoveryNodes.Builder builder = DiscoveryNodes.builder();
            nodes.values().forEach(node -> builder.add(node.node));
            return builder.build();
        }

        /**
         * Returns the {@link TestClusterNode} for the master node in the given {@link ClusterState}.
         * @param state ClusterState
         * @return Master Node
         */
        public TestClusterNode currentMaster(ClusterState state) {
            TestClusterNode master = nodes.get(state.nodes().getMasterNode().getName());
            assertNotNull(master);
            assertTrue(master.node.isMasterNode());
            return master;
        }

        private final class TestClusterNode {

            private final Logger logger = LogManager.getLogger(TestClusterNode.class);

            private final NamedWriteableRegistry namedWriteableRegistry = new NamedWriteableRegistry(Stream.concat(
                ClusterModule.getNamedWriteables().stream(), NetworkModule.getNamedWriteables().stream()).collect(Collectors.toList()));

            private final TransportService transportService;

            private final ClusterService clusterService;

            private final RepositoriesService repositoriesService;

            private final SnapshotsService snapshotsService;

            private final SnapshotShardsService snapshotShardsService;

            private final IndicesService indicesService;

            private final IndicesClusterStateService indicesClusterStateService;

            private final DiscoveryNode node;

            private final MasterService masterService;

            private final AllocationService allocationService;

            private final NodeClient client;

            private final NodeEnvironment nodeEnv;

            private final DisruptableMockTransport mockTransport;

            private final ThreadPool threadPool;

            private Coordinator coordinator;

            TestClusterNode(DiscoveryNode node) throws IOException {
                this.node = node;
                final Environment environment = createEnvironment(node.getName());
                masterService = new FakeThreadPoolMasterService(node.getName(), "test", deterministicTaskQueue::scheduleNow);
                final Settings settings = environment.settings();
                final ClusterSettings clusterSettings = new ClusterSettings(settings, ClusterSettings.BUILT_IN_CLUSTER_SETTINGS);
                threadPool = deterministicTaskQueue.getThreadPool();
                clusterService = new ClusterService(settings, clusterSettings, masterService,
                    new ClusterApplierService(node.getName(), settings, clusterSettings, threadPool) {
                        @Override
                        protected PrioritizedEsThreadPoolExecutor createThreadPoolExecutor() {
                            return new MockSinglePrioritizingExecutor(node.getName(), deterministicTaskQueue);
                        }

                        @Override
                        protected void connectToNodesAndWait(ClusterState newClusterState) {
                            // don't do anything, and don't block
                        }
                    });
                mockTransport = new DisruptableMockTransport(node, logger) {
                    @Override
                    protected ConnectionStatus getConnectionStatus(DiscoveryNode destination) {
                        if (node.equals(destination)) {
                            return ConnectionStatus.CONNECTED;
                        }
                        // Check if both nodes are still part of the cluster
                        if (nodes.containsKey(node.getName()) == false || nodes.containsKey(destination.getName()) == false) {
                            return ConnectionStatus.DISCONNECTED;
                        }
                        return disconnectedNodes.contains(node.getName()) || disconnectedNodes.contains(destination.getName())
                            ? ConnectionStatus.DISCONNECTED : ConnectionStatus.CONNECTED;
                    }

                    @Override
                    protected Optional<DisruptableMockTransport> getDisruptableMockTransport(TransportAddress address) {
                        return nodes.values().stream().map(cn -> cn.mockTransport)
                            .filter(transport -> transport.getLocalNode().getAddress().equals(address))
                            .findAny();
                    }

                    @Override
                    protected void execute(Runnable runnable) {
                        scheduleNow(CoordinatorTests.onNodeLog(getLocalNode(), runnable));
                    }

                    @Override
                    protected NamedWriteableRegistry writeableRegistry() {
                        return namedWriteableRegistry;
                    }
                };
                transportService = mockTransport.createTransportService(
                    settings, deterministicTaskQueue.getThreadPool(runnable -> CoordinatorTests.onNodeLog(node, runnable)),
                    new TransportInterceptor() {
                        @Override
                        public <T extends TransportRequest> TransportRequestHandler<T> interceptHandler(String action, String executor,
                            boolean forceExecution, TransportRequestHandler<T> actualHandler) {
                            // TODO: Remove this hack once recoveries are async and can be used in these tests
                            if (action.startsWith("internal:index/shard/recovery")) {
                                return (request, channel, task) -> scheduleSoon(
                                    new AbstractRunnable() {
                                        @Override
                                        protected void doRun() throws Exception {
                                            channel.sendResponse(new TransportException(new IOException("failed to recover shard")));
                                        }

                                        @Override
                                        public void onFailure(final Exception e) {
                                            throw new AssertionError(e);
                                        }
                                    });
                            } else {
                                return actualHandler;
                            }
                        }
                    },
                    a -> node, null, emptySet()
                );
                final IndexNameExpressionResolver indexNameExpressionResolver = new IndexNameExpressionResolver();
                repositoriesService = new RepositoriesService(
                    settings, clusterService, transportService,
                    Collections.singletonMap(FsRepository.TYPE, getRepoFactory(environment)), emptyMap(), threadPool
                );
                snapshotsService =
                    new SnapshotsService(settings, clusterService, indexNameExpressionResolver, repositoriesService, threadPool);
                nodeEnv = new NodeEnvironment(settings, environment);
                final NamedXContentRegistry namedXContentRegistry = new NamedXContentRegistry(Collections.emptyList());
                final ScriptService scriptService = new ScriptService(settings, emptyMap(), emptyMap());
                client = new NodeClient(settings, threadPool);
                allocationService = ESAllocationTestCase.createAllocationService(settings);
                final IndexScopedSettings indexScopedSettings =
                    new IndexScopedSettings(settings, IndexScopedSettings.BUILT_IN_INDEX_SETTINGS);
                final BigArrays bigArrays = new BigArrays(new PageCacheRecycler(settings), null, "test");
                final MapperRegistry mapperRegistry = new IndicesModule(Collections.emptyList()).getMapperRegistry();
                indicesService = new IndicesService(
                    settings,
                    mock(PluginsService.class),
                    nodeEnv,
                    namedXContentRegistry,
                    new AnalysisRegistry(environment, emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(),
                        emptyMap(), emptyMap(), emptyMap(), emptyMap()),
                    indexNameExpressionResolver,
                    mapperRegistry,
                    namedWriteableRegistry,
                    threadPool,
                    indexScopedSettings,
                    new NoneCircuitBreakerService(),
                    bigArrays,
                    scriptService,
                    clusterService,
                    client,
                    new MetaStateService(nodeEnv, namedXContentRegistry),
                    Collections.emptyList(),
                    emptyMap()
                );
                final RecoverySettings recoverySettings = new RecoverySettings(settings, clusterSettings);
                final ActionFilters actionFilters = new ActionFilters(emptySet());
                snapshotShardsService = new SnapshotShardsService(
                    settings, clusterService, repositoriesService, threadPool,
                    transportService, indicesService, actionFilters, indexNameExpressionResolver);
                final ShardStateAction shardStateAction = new ShardStateAction(
                    clusterService, transportService, allocationService,
                    new BatchedRerouteService(clusterService, allocationService::reroute),
                    threadPool
                );
                final MetaDataMappingService metaDataMappingService = new MetaDataMappingService(clusterService, indicesService);
                indicesClusterStateService = new IndicesClusterStateService(
                    settings,
                    indicesService,
                    clusterService,
                    threadPool,
                    new PeerRecoveryTargetService(threadPool, transportService, recoverySettings, clusterService),
                    shardStateAction,
                    new NodeMappingRefreshAction(transportService, metaDataMappingService),
                    repositoriesService,
                    mock(SearchService.class),
                    new SyncedFlushService(indicesService, clusterService, transportService, indexNameExpressionResolver),
                    new PeerRecoverySourceService(transportService, indicesService, recoverySettings),
                    snapshotShardsService,
                    new PrimaryReplicaSyncer(
                        transportService,
                        new TransportResyncReplicationAction(
                            settings,
                            transportService,
                            clusterService,
                            indicesService,
                            threadPool,
                            shardStateAction,
                            actionFilters)),
                    new GlobalCheckpointSyncAction(
                        settings,
                        transportService,
                        clusterService,
                        indicesService,
                        threadPool,
                        shardStateAction,
                        actionFilters),
                    RetentionLeaseSyncer.EMPTY);
            Map<ActionType, TransportAction> actions = new HashMap<>();
                final MetaDataCreateIndexService metaDataCreateIndexService = new MetaDataCreateIndexService(settings, clusterService,
                    indicesService,
                    allocationService, new AliasValidator(), environment, indexScopedSettings,
                    threadPool, namedXContentRegistry, false);
                actions.put(CreateIndexAction.INSTANCE,
                    new TransportCreateIndexAction(
                        transportService, clusterService, threadPool,
                        metaDataCreateIndexService,
                        actionFilters, indexNameExpressionResolver
                    ));
                final MappingUpdatedAction mappingUpdatedAction = new MappingUpdatedAction(settings, clusterSettings);
                mappingUpdatedAction.setClient(client);
            final TransportShardBulkAction transportShardBulkAction = new TransportShardBulkAction(settings, transportService,
                clusterService, indicesService, threadPool, shardStateAction, mappingUpdatedAction, new UpdateHelper(scriptService),
                actionFilters);
                actions.put(BulkAction.INSTANCE,
                    new TransportBulkAction(threadPool, transportService, clusterService,
                        new IngestService(
                            clusterService, threadPool, environment, scriptService,
                            new AnalysisModule(environment, Collections.emptyList()).getAnalysisRegistry(),
                            Collections.emptyList(), client),
                    transportShardBulkAction, client, actionFilters, indexNameExpressionResolver,
                        new AutoCreateIndex(settings, clusterSettings, indexNameExpressionResolver)
                    ));
                final RestoreService restoreService = new RestoreService(
                    clusterService, repositoriesService, allocationService,
                    metaDataCreateIndexService,
                    new MetaDataIndexUpgradeService(
                        settings, namedXContentRegistry,
                        mapperRegistry,
                        indexScopedSettings),
                    clusterSettings
                );
                actions.put(PutMappingAction.INSTANCE,
                    new TransportPutMappingAction(transportService, clusterService, threadPool, metaDataMappingService,
                        actionFilters, indexNameExpressionResolver, new RequestValidators<>(Collections.emptyList())));
                final ResponseCollectorService responseCollectorService = new ResponseCollectorService(clusterService);
                final SearchTransportService searchTransportService = new SearchTransportService(transportService,
                    SearchExecutionStatsCollector.makeWrapper(responseCollectorService));
                final SearchService searchService = new SearchService(clusterService, indicesService, threadPool, scriptService,
                    bigArrays, new FetchPhase(Collections.emptyList()), responseCollectorService);
                actions.put(SearchAction.INSTANCE,
                    new TransportSearchAction(threadPool, transportService, searchService,
                        searchTransportService, new SearchPhaseController(searchService::createReduceContext), clusterService,
                        actionFilters, indexNameExpressionResolver));
                actions.put(RestoreSnapshotAction.INSTANCE,
                    new TransportRestoreSnapshotAction(transportService, clusterService, threadPool, restoreService, actionFilters,
                        indexNameExpressionResolver));
                actions.put(DeleteIndexAction.INSTANCE,
                    new TransportDeleteIndexAction(
                        transportService, clusterService, threadPool,
                        new MetaDataDeleteIndexService(settings, clusterService, allocationService), actionFilters,
                        indexNameExpressionResolver, new DestructiveOperations(settings, clusterSettings)));
                actions.put(PutRepositoryAction.INSTANCE,
                    new TransportPutRepositoryAction(
                        transportService, clusterService, repositoriesService, threadPool,
                        actionFilters, indexNameExpressionResolver
                    ));
                actions.put(CleanupRepositoryAction.INSTANCE, new TransportCleanupRepositoryAction(transportService, clusterService,
                    repositoriesService, snapshotsService, threadPool, actionFilters, indexNameExpressionResolver));
                actions.put(CreateSnapshotAction.INSTANCE,
                    new TransportCreateSnapshotAction(
                        transportService, clusterService, threadPool,
                        snapshotsService, actionFilters, indexNameExpressionResolver
                    ));
                actions.put(ClusterRerouteAction.INSTANCE,
                    new TransportClusterRerouteAction(transportService, clusterService, threadPool, allocationService,
                        actionFilters, indexNameExpressionResolver));
                actions.put(ClusterStateAction.INSTANCE,
                    new TransportClusterStateAction(transportService, clusterService, threadPool,
                        actionFilters, indexNameExpressionResolver));
                actions.put(IndicesShardStoresAction.INSTANCE,
                    new TransportIndicesShardStoresAction(
                        transportService, clusterService, threadPool, actionFilters, indexNameExpressionResolver,
                        new TransportNodesListGatewayStartedShards(settings,
                        threadPool, clusterService, transportService, actionFilters, nodeEnv, indicesService, namedXContentRegistry))
            );
                actions.put(DeleteSnapshotAction.INSTANCE,
                    new TransportDeleteSnapshotAction(
                        transportService, clusterService, threadPool,
                        snapshotsService, actionFilters, indexNameExpressionResolver
                    ));
                client.initialize(actions, transportService.getTaskManager(),
                    () -> clusterService.localNode().getId(), transportService.getRemoteClusterService());
            }

            private Repository.Factory getRepoFactory(Environment environment) {
                // Run half the tests with the eventually consistent repository
                if (blobStoreContext == null) {
                    return metaData -> new FsRepository(metaData, environment, xContentRegistry(), clusterService) {
                        @Override
                        protected void assertSnapshotOrGenericThread() {
                            // eliminate thread name check as we create repo in the test thread
                        }
                    };
                } else {
                    return metaData ->
                        new MockEventuallyConsistentRepository(metaData, xContentRegistry(), clusterService, blobStoreContext, random());
                }
            }
            public void restart() {
                testClusterNodes.disconnectNode(this);
                final ClusterState oldState = this.clusterService.state();
                stop();
                nodes.remove(node.getName());
                scheduleSoon(() -> {
                    try {
                        final TestClusterNode restartedNode = new TestClusterNode(
                            new DiscoveryNode(node.getName(), node.getId(), node.getAddress(), emptyMap(),
                                node.getRoles(), Version.CURRENT));
                        nodes.put(node.getName(), restartedNode);
                        restartedNode.start(oldState);
                    } catch (IOException e) {
                        throw new AssertionError(e);
                    }
                });
            }

            public void stop() {
                testClusterNodes.disconnectNode(this);
                indicesService.close();
                clusterService.close();
                indicesClusterStateService.close();
                if (coordinator != null) {
                    coordinator.close();
                }
                nodeEnv.close();
            }

            public void start(ClusterState initialState) {
                transportService.start();
                transportService.acceptIncomingRequests();
                snapshotsService.start();
                snapshotShardsService.start();
                repositoriesService.start();
                final CoordinationState.PersistedState persistedState =
                    new InMemoryPersistedState(initialState.term(), stateForNode(initialState, node));
                coordinator = new Coordinator(node.getName(), clusterService.getSettings(),
                    clusterService.getClusterSettings(), transportService, namedWriteableRegistry,
                    allocationService, masterService, () -> persistedState,
                    hostsResolver -> nodes.values().stream().filter(n -> n.node.isMasterNode())
                        .map(n -> n.node.getAddress()).collect(Collectors.toList()),
                    clusterService.getClusterApplierService(), Collections.emptyList(), random(),
                    new BatchedRerouteService(clusterService, allocationService::reroute), ElectionStrategy.DEFAULT_INSTANCE);
                masterService.setClusterStatePublisher(coordinator);
                coordinator.start();
                masterService.start();
                clusterService.getClusterApplierService().setNodeConnectionsService(
                    new NodeConnectionsService(clusterService.getSettings(), threadPool, transportService));
                clusterService.getClusterApplierService().start();
                indicesService.start();
                indicesClusterStateService.start();
                coordinator.startInitialJoin();
            }
        }
    }
}
