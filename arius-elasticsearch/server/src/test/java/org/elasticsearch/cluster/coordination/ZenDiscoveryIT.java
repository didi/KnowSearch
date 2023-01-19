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

package org.elasticsearch.cluster.coordination;

import org.elasticsearch.Version;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.action.admin.indices.recovery.RecoveryResponse;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.discovery.DiscoveryStats;
import org.elasticsearch.discovery.zen.FaultDetection;
import org.elasticsearch.node.Node;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.TestCustomMetaData;
import org.elasticsearch.transport.RemoteTransportException;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.TEST, numDataNodes = 0, numClientNodes = 0)
public class ZenDiscoveryIT extends ESIntegTestCase {

    public void testNoShardRelocationsOccurWhenElectedMasterNodeFails() throws Exception {
        Settings defaultSettings = Settings.builder()
                .put(FaultDetection.PING_TIMEOUT_SETTING.getKey(), "1s")
                .put(FaultDetection.PING_RETRIES_SETTING.getKey(), "1")
                .build();

        Settings masterNodeSettings = Settings.builder()
                .put(Node.NODE_DATA_SETTING.getKey(), false)
                .put(defaultSettings)
                .build();
        internalCluster().startNodes(2, masterNodeSettings);
        Settings dateNodeSettings = Settings.builder()
                .put(Node.NODE_MASTER_SETTING.getKey(), false)
                .put(defaultSettings)
                .build();
        internalCluster().startNodes(2, dateNodeSettings);
        ClusterHealthResponse clusterHealthResponse = client().admin().cluster().prepareHealth()
                .setWaitForEvents(Priority.LANGUID)
                .setWaitForNodes("4")
                .setWaitForNoRelocatingShards(true)
                .get();
        assertThat(clusterHealthResponse.isTimedOut(), is(false));

        createIndex("test");
        ensureSearchable("test");
        RecoveryResponse r = client().admin().indices().prepareRecoveries("test").get();
        int numRecoveriesBeforeNewMaster = r.shardRecoveryStates().get("test").size();

        final String oldMaster = internalCluster().getMasterName();
        internalCluster().stopCurrentMasterNode();
        assertBusy(() -> {
            String current = internalCluster().getMasterName();
            assertThat(current, notNullValue());
            assertThat(current, not(equalTo(oldMaster)));
        });
        ensureSearchable("test");

        r = client().admin().indices().prepareRecoveries("test").get();
        int numRecoveriesAfterNewMaster = r.shardRecoveryStates().get("test").size();
        assertThat(numRecoveriesAfterNewMaster, equalTo(numRecoveriesBeforeNewMaster));
    }

    public void testHandleNodeJoin_incompatibleClusterState()
            throws InterruptedException, ExecutionException, TimeoutException {
        String masterNode = internalCluster().startMasterOnlyNode();
        String node1 = internalCluster().startNode();
        ClusterService clusterService = internalCluster().getInstance(ClusterService.class, node1);
        Coordinator coordinator = (Coordinator) internalCluster().getInstance(Discovery.class, masterNode);
        final ClusterState state = clusterService.state();
        MetaData.Builder mdBuilder = MetaData.builder(state.metaData());
        mdBuilder.putCustom(CustomMetaData.TYPE, new CustomMetaData("data"));
        ClusterState stateWithCustomMetaData = ClusterState.builder(state).metaData(mdBuilder).build();

        final CompletableFuture<Throwable> future = new CompletableFuture<>();
        DiscoveryNode node = state.nodes().getLocalNode();

        coordinator.sendValidateJoinRequest(stateWithCustomMetaData, new JoinRequest(node, Optional.empty()),
                new JoinHelper.JoinCallback() {
            @Override
            public void onSuccess() {
                future.completeExceptionally(new AssertionError("onSuccess should not be called"));
            }

            @Override
            public void onFailure(Exception e) {
                future.complete(e);
            }
        });

        Throwable t = future.get(10, TimeUnit.SECONDS);

        assertTrue(t instanceof IllegalStateException);
        assertTrue(t.getCause() instanceof RemoteTransportException);
        assertTrue(t.getCause().getCause() instanceof IllegalArgumentException);
        assertThat(t.getCause().getCause().getMessage(), containsString("Unknown NamedWriteable"));
    }

    public static class CustomMetaData extends TestCustomMetaData {
        public static final String TYPE = "custom_md";

        CustomMetaData(String data) {
            super(data);
        }

        @Override
        public String getWriteableName() {
            return TYPE;
        }

        @Override
        public Version getMinimalSupportedVersion() {
            return Version.CURRENT;
        }

        @Override
        public EnumSet<MetaData.XContentContext> context() {
            return EnumSet.of(MetaData.XContentContext.GATEWAY, MetaData.XContentContext.SNAPSHOT);
        }
    }

    public void testDiscoveryStats() throws Exception {
        internalCluster().startNode();
        ensureGreen(); // ensures that all events are processed (in particular state recovery fully completed)
        assertBusy(() ->
            assertThat(internalCluster().clusterService(internalCluster().getMasterName()).getMasterService().numberOfPendingTasks(),
                equalTo(0))); // see https://github.com/elastic/elasticsearch/issues/24388

        logger.info("--> request node discovery stats");
        NodesStatsResponse statsResponse = client().admin().cluster().prepareNodesStats().clear().setDiscovery(true).get();
        assertThat(statsResponse.getNodes().size(), equalTo(1));

        DiscoveryStats stats = statsResponse.getNodes().get(0).getDiscoveryStats();
        assertThat(stats.getQueueStats(), notNullValue());
        assertThat(stats.getQueueStats().getTotal(), equalTo(0));
        assertThat(stats.getQueueStats().getCommitted(), equalTo(0));
        assertThat(stats.getQueueStats().getPending(), equalTo(0));

        assertThat(stats.getPublishStats(), notNullValue());
        assertThat(stats.getPublishStats().getFullClusterStateReceivedCount(), greaterThanOrEqualTo(0L));
        assertThat(stats.getPublishStats().getIncompatibleClusterStateDiffReceivedCount(), equalTo(0L));
        assertThat(stats.getPublishStats().getCompatibleClusterStateDiffReceivedCount(), greaterThanOrEqualTo(0L));

        XContentBuilder builder = XContentFactory.jsonBuilder().prettyPrint();
        builder.startObject();
        stats.toXContent(builder, ToXContent.EMPTY_PARAMS);
        builder.endObject();
    }
}
