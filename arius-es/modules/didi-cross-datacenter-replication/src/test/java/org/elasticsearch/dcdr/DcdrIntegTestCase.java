package org.elasticsearch.dcdr;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresAction;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresRequest;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.admin.indices.stats.ShardStats;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.UnassignedInfo;
import org.elasticsearch.cluster.routing.allocation.DiskThresholdSettings;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.UUIDs;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.ImmutableOpenIntMap;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.core.internal.io.IOUtils;
import org.elasticsearch.dcdr.action.CreateReplicationAction;
import org.elasticsearch.dcdr.action.DeleteReplicationAction;
import org.elasticsearch.dcdr.action.ReplicationStatsAction;
import org.elasticsearch.dcdr.translog.primary.CompositeDCDRStats;
import org.elasticsearch.dcdr.translog.primary.DCDRStats;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.store.IndicesStore;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.InternalSettingsPlugin;
import org.elasticsearch.test.InternalTestCluster;
import org.elasticsearch.test.MockHttpTransport;
import org.elasticsearch.test.NodeConfigurationSource;
import org.elasticsearch.test.TestCluster;
import org.elasticsearch.test.transport.MockTransportService;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.transport.nio.MockNioTransportPlugin;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.discovery.DiscoveryModule.DISCOVERY_SEED_PROVIDERS_SETTING;
import static org.elasticsearch.discovery.SettingsBasedSeedHostsProvider.DISCOVERY_SEED_HOSTS_SETTING;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertNoFailures;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

/**
 * author weizijun
 * date：2019-09-17
 */
public abstract class DcdrIntegTestCase extends ESIntegTestCase {

    private static ClusterGroup clusterGroup;

    static class ClusterGroup implements Closeable {

        final InternalTestCluster primaryCluster;
        final InternalTestCluster replicaCluster;

        ClusterGroup(InternalTestCluster primaryCluster, InternalTestCluster replicaCluster) {
            this.primaryCluster = primaryCluster;
            this.replicaCluster = replicaCluster;
        }

        @Override
        public void close() throws IOException {
            IOUtils.close(primaryCluster, replicaCluster);
        }
    }

    protected Settings primaryClusterSettings() {
        return Settings.EMPTY;
    }

    protected Settings replicaClusterSettings() {
        return Settings.EMPTY;
    }

    @Before
    public final void startClusters() throws Exception {
        if (clusterGroup != null && reuseClusters()) {
            clusterGroup.primaryCluster.ensureAtMostNumDataNodes(numberOfNodesPerCluster());
            clusterGroup.replicaCluster.ensureAtMostNumDataNodes(numberOfNodesPerCluster());
            return;
        }

        stopClusters();
        NodeConfigurationSource nodeConfigurationSource = createNodeConfigurationSource();
        Collection<Class<? extends Plugin>> mockPlugins = Arrays.asList(ESIntegTestCase.TestSeedPlugin.class,
            MockHttpTransport.TestPlugin.class, MockTransportService.TestPlugin.class,
            MockNioTransportPlugin.class, InternalSettingsPlugin.class, DCDR.class);

        InternalTestCluster primaryCluster = new InternalTestCluster(randomLong(), createTempDir(), true, true, numberOfNodesPerCluster(),
            numberOfNodesPerCluster(), UUIDs.randomBase64UUID(random()), nodeConfigurationSource, 0, "primary", mockPlugins,
            Function.identity());
        InternalTestCluster replicaCluster = new InternalTestCluster(randomLong(), createTempDir(), true, true, numberOfNodesPerCluster(),
            numberOfNodesPerCluster(), UUIDs.randomBase64UUID(random()), nodeConfigurationSource, 0, "replica", mockPlugins,
            Function.identity());
        clusterGroup = new ClusterGroup(primaryCluster, replicaCluster);

        primaryCluster.beforeTest(random(), 0.0D);
        primaryCluster.ensureAtLeastNumDataNodes(numberOfNodesPerCluster());
        assertBusy(() -> {
            ClusterService clusterService = primaryCluster.getInstance(ClusterService.class);
        });
        replicaCluster.beforeTest(random(), 0.0D);
        replicaCluster.ensureAtLeastNumDataNodes(numberOfNodesPerCluster());
        assertBusy(() -> {
            ClusterService clusterService = replicaCluster.getInstance(ClusterService.class);
        });

        ClusterUpdateSettingsRequest updateSettingsRequest = new ClusterUpdateSettingsRequest();
        String address = replicaCluster.getDataNodeInstance(TransportService.class).boundAddress().publishAddress().toString();
        updateSettingsRequest.persistentSettings(Settings.builder().put("cluster.remote.replica_cluster.seeds", address));
        assertAcked(primaryClient().admin().cluster().updateSettings(updateSettingsRequest).actionGet());
    }

    protected void disableDelayedAllocation(String index) {
        UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(index);
        Settings.Builder settingsBuilder = Settings.builder();
        settingsBuilder.put(UnassignedInfo.INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING.getKey(), 0);
        updateSettingsRequest.settings(settingsBuilder);
        assertAcked(replicaClient().admin().indices().updateSettings(updateSettingsRequest).actionGet());
    }

    @After
    public void afterTest() throws Exception {
        String masterNode = clusterGroup.replicaCluster.getMasterName();
        ClusterService clusterService = clusterGroup.replicaCluster.getInstance(ClusterService.class, masterNode);
        // TODO removeCCRRelatedMetadataFromClusterState(clusterService);

        try {
            clusterGroup.primaryCluster.beforeIndexDeletion();
            clusterGroup.primaryCluster.assertSeqNos();
            clusterGroup.primaryCluster.assertSameDocIdsOnShards();
            clusterGroup.primaryCluster.assertConsistentHistoryBetweenTranslogAndLuceneIndex();

            clusterGroup.replicaCluster.beforeIndexDeletion();
            clusterGroup.replicaCluster.assertSeqNos();
            clusterGroup.replicaCluster.assertSameDocIdsOnShards();
            clusterGroup.replicaCluster.assertConsistentHistoryBetweenTranslogAndLuceneIndex();
        } finally {
            clusterGroup.primaryCluster.wipe(Collections.emptySet());
            clusterGroup.replicaCluster.wipe(Collections.emptySet());
        }
    }

    private NodeConfigurationSource createNodeConfigurationSource() {
        Settings.Builder builder = Settings.builder();
        // Default the watermarks to absurdly low to prevent the tests
        // from failing on nodes without enough disk space
        builder.put(DiskThresholdSettings.CLUSTER_ROUTING_ALLOCATION_LOW_DISK_WATERMARK_SETTING.getKey(), "1b");
        builder.put(DiskThresholdSettings.CLUSTER_ROUTING_ALLOCATION_HIGH_DISK_WATERMARK_SETTING.getKey(), "1b");
        builder.put(DiskThresholdSettings.CLUSTER_ROUTING_ALLOCATION_DISK_FLOOD_STAGE_WATERMARK_SETTING.getKey(), "1b");
        builder.put(ScriptService.SCRIPT_MAX_COMPILATIONS_RATE.getKey(), "2048/1m");
        // wait short time for other active shards before actually deleting, default 30s not needed in tests
        builder.put(IndicesStore.INDICES_STORE_DELETE_SHARD_TIMEOUT.getKey(), new TimeValue(1, TimeUnit.SECONDS));
        builder.put(NetworkModule.TRANSPORT_TYPE_KEY, getTestTransportType());
        builder.putList(DISCOVERY_SEED_HOSTS_SETTING.getKey()); // empty list disables a port scan for other nodes
        builder.putList(DISCOVERY_SEED_PROVIDERS_SETTING.getKey(), "file");

        return new NodeConfigurationSource() {
            @Override
            public Settings nodeSettings(int nodeOrdinal) {
                return builder.build();
            }

            @Override
            public Path nodeConfigPath(int nodeOrdinal) {
                return null;
            }

            @Override
            public Collection<Class<? extends Plugin>> nodePlugins() {
                return Arrays.asList(DCDR.class);
            }

            @Override
            public Settings transportClientSettings() {
                return super.transportClientSettings();
            }

            @Override
            public Collection<Class<? extends Plugin>> transportClientPlugins() {
                return Arrays.asList(MockNioTransportPlugin.class);
            }
        };
    }

    @AfterClass
    public static void stopClusters() throws IOException {
        IOUtils.close(clusterGroup);
        clusterGroup = null;
    }

    protected int numberOfNodesPerCluster() {
        return 5;
    }

    protected boolean reuseClusters() {
        return true;
    }

    protected final Client primaryClient() {
        return clusterGroup.primaryCluster.client();
    }

    protected final Client replicaClient() {
        return clusterGroup.replicaCluster.client();
    }

    protected final InternalTestCluster getPrimaryCluster() {
        return clusterGroup.primaryCluster;
    }

    protected final InternalTestCluster getReplicaCluster() {
        return clusterGroup.replicaCluster;
    }

    protected final ClusterHealthStatus ensurePrimaryYellow(String... indices) {
        return ensureColor(clusterGroup.primaryCluster, ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(60), false, indices);
    }

    protected final ClusterHealthStatus ensurePrimaryGreen(String... indices) {
        logger.info("ensure green leader indices {}", Arrays.toString(indices));
        return ensureColor(clusterGroup.primaryCluster, ClusterHealthStatus.GREEN, TimeValue.timeValueSeconds(60), false, indices);
    }

    protected final ClusterHealthStatus ensureReplicaGreen(String... indices) {
        logger.info("ensure green follower indices {}", Arrays.toString(indices));
        return ensureColor(clusterGroup.replicaCluster, ClusterHealthStatus.GREEN, TimeValue.timeValueSeconds(60), false, indices);
    }

    private ClusterHealthStatus ensureColor(TestCluster testCluster,
                                            ClusterHealthStatus clusterHealthStatus,
                                            TimeValue timeout,
                                            boolean waitForNoInitializingShards,
                                            String... indices) {
        String color = clusterHealthStatus.name().toLowerCase(Locale.ROOT);
        String method = "ensure" + Strings.capitalize(color);

        ClusterHealthRequest healthRequest = Requests.clusterHealthRequest(indices)
            .timeout(timeout)
            .masterNodeTimeout(timeout)
            .waitForStatus(clusterHealthStatus)
            .waitForEvents(Priority.LANGUID)
            .waitForNoRelocatingShards(true)
            .waitForNoInitializingShards(waitForNoInitializingShards)
            .waitForNodes(Integer.toString(testCluster.size()));

        ClusterHealthResponse actionGet = testCluster.client().admin().cluster().health(healthRequest).actionGet();
        if (actionGet.isTimedOut()) {
            logger.info("{} timed out, cluster state:\n{}\n{}",
                method,
                testCluster.client().admin().cluster().prepareState().get().getState(),
                testCluster.client().admin().cluster().preparePendingClusterTasks().get());
            fail("timed out waiting for " + color + " state");
        }
        assertThat("Expected at least " + clusterHealthStatus + " but got " + actionGet.getStatus(),
            actionGet.getStatus().value(), lessThanOrEqualTo(clusterHealthStatus.value()));
        logger.debug("indices {} are {}", indices.length == 0 ? "[_all]" : indices, color);
        return actionGet.getStatus();
    }

    protected final Index resolvePrimaryIndex(String index) {
        GetIndexResponse getIndexResponse = primaryClient().admin().indices().prepareGetIndex().setIndices(index).get();
        assertTrue("index " + index + " not found", getIndexResponse.getSettings().containsKey(index));
        String uuid = getIndexResponse.getSettings().get(index).get(IndexMetaData.SETTING_INDEX_UUID);
        return new Index(index, uuid);
    }

    protected final Index resolveReplicaIndex(String index) {
        GetIndexResponse getIndexResponse = replicaClient().admin().indices().prepareGetIndex().setIndices(index).get();
        assertTrue("index " + index + " not found", getIndexResponse.getSettings().containsKey(index));
        String uuid = getIndexResponse.getSettings().get(index).get(IndexMetaData.SETTING_INDEX_UUID);
        return new Index(index, uuid);
    }

    protected final RefreshResponse refresh(Client client, String... indices) {
        RefreshResponse actionGet = client.admin().indices().prepareRefresh(indices).execute().actionGet();
        assertNoFailures(actionGet);
        return actionGet;
    }

    public static CreateReplicationAction.Request createReplication(String primaryIndex, String replicaIndex) {
        CreateReplicationAction.Request request = new CreateReplicationAction.Request();
        request.setPrimaryIndex(primaryIndex);
        request.setReplicaIndex(replicaIndex);
        request.setReplicaCluster("replica_cluster");
        return request;
    }

    public static DeleteReplicationAction.Request deleteReplication(String primaryIndex, String replicaIndex) {
        DeleteReplicationAction.Request request = new DeleteReplicationAction.Request();
        request.setPrimaryIndex(primaryIndex);
        request.setReplicaIndex(replicaIndex);
        request.setReplicaCluster("replica_cluster");
        return request;
    }

    protected String getIndexSettings(final int numberOfShards, final int numberOfReplicas,
                                      final Map<String, String> additionalIndexSettings) throws IOException {
        final String settings;
        try (XContentBuilder builder = jsonBuilder()) {
            builder.startObject();
            {
                builder.startObject("settings");
                {
                    builder.field(UnassignedInfo.INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING.getKey(), 0);
                    builder.field("index.number_of_shards", numberOfShards);
                    builder.field("index.number_of_replicas", numberOfReplicas);
                    builder.field("index.refresh_interval", "5s");
                    for (final Map.Entry<String, String> additionalSetting : additionalIndexSettings.entrySet()) {
                        builder.field(additionalSetting.getKey(), additionalSetting.getValue());
                    }
                }
                builder.endObject();
                builder.startObject("mappings");
                {
                    builder.startObject("type");
                    {
                        builder.startObject("properties");
                        {
                            builder.startObject("field");
                            {
                                builder.field("type", "long");
                            }
                            builder.endObject();
                        }
                        builder.endObject();
                    }
                    builder.endObject();
                }
                builder.endObject();
            }
            builder.endObject();
            settings = BytesReference.bytes(builder).utf8ToString();
        }
        return settings;
    }

    protected boolean assertReplicationDone(String primaryIndex, String replicaIndex, int numberOfPrimaryShards) {
        try {
            for (int i = 0; i < 30; i++) {
                Thread.sleep(1000);

                ReplicationStatsAction.Request statsRequest = new ReplicationStatsAction.Request();
                statsRequest.setIndices(Strings.splitStringByCommaToArray(primaryIndex));
                ReplicationStatsAction.Response statsResponse = primaryClient().execute(ReplicationStatsAction.INSTANCE, statsRequest).get();
                if (statsResponse.getDcdrStats().size() != numberOfPrimaryShards) {
                    continue;
                }

                boolean done = true;
                for (CompositeDCDRStats compositeDCDRStats : statsResponse.getDcdrStats()) {
                    for (DCDRStats dcdrStats : compositeDCDRStats.getDcdrStatsList()) {
                        if (!dcdrStats.getReplicaIndex().equals(replicaIndex)) {
                            continue;
                        }

                        if (dcdrStats.getPrimaryMaxSeqNo() != dcdrStats.getReplicaMaxSeqNo()) {
                            done = false;
                            break;
                        }
                    }
                }

                if (done) {
                    return true;
                }

            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    protected ShardStats getShardStats(ShardId shardId, Client client) {
        IndicesStatsRequest stats = new IndicesStatsRequest();
        stats.indices(shardId.getIndexName());
        stats.translog(true);

        IndicesStatsResponse response = client.admin().indices().stats(stats).actionGet();

        ShardStats[] shardStatsArr = response.getShards();
        for (ShardStats shardStats : shardStatsArr) {
            if (shardStats.getShardRouting().shardId().id() == shardId.id() && shardStats.getShardRouting().primary()) {
                return shardStats;
            }
        }

        return null;
    }

    protected DiscoveryNode getTargetNode(ShardId shardId, Client client) {
        IndicesShardStoresRequest request = new IndicesShardStoresRequest(shardId.getIndexName());
        request.shardStatuses("all");
        IndicesShardStoresResponse response = null;
        try {
            response = client.execute(IndicesShardStoresAction.INSTANCE, request).get();
        } catch (Exception e) {
            return null;
        }
        if (response.getStoreStatuses().size() != 1) {
            return null;
        }
        ObjectObjectCursor<String, ImmutableOpenIntMap<List<IndicesShardStoresResponse.StoreStatus>>> indexShards = response.getStoreStatuses().iterator().next();
        if (!indexShards.key.equals(shardId.getIndexName())) {
            return null;
        }

        ImmutableOpenIntMap<List<IndicesShardStoresResponse.StoreStatus>> shards = indexShards.value;
        List<IndicesShardStoresResponse.StoreStatus> targetShards = shards.get(shardId.getId());
        if (targetShards == null || targetShards.size() == 0) {
            return null;
        }

        for (IndicesShardStoresResponse.StoreStatus status : targetShards) {
            if (status.getAllocationStatus() == IndicesShardStoresResponse.StoreStatus.AllocationStatus.PRIMARY) {
                return status.getNode();
            }
        }

        return null;
    }
}
