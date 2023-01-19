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
package org.elasticsearch.transport;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.OriginalIndices;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.AbstractScopedSettings;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.core.internal.io.IOUtils;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.transport.MockTransportService;
import org.elasticsearch.threadpool.TestThreadPool;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

public class RemoteClusterServiceTests extends ESTestCase {

    private final ThreadPool threadPool = new TestThreadPool(getClass().getName());

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        ThreadPool.terminate(threadPool, 10, TimeUnit.SECONDS);
    }

    private MockTransportService startTransport(String id, List<DiscoveryNode> knownNodes, Version version) {
        return startTransport(id, knownNodes, version, Settings.EMPTY);
    }

    private MockTransportService startTransport(
            final String id,
            final List<DiscoveryNode> knownNodes,
            final Version version,
            final Settings settings) {
        return RemoteClusterConnectionTests.startTransport(
                id, knownNodes, version, threadPool, settings);
    }

    public void testSettingsAreRegistered() {
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(RemoteClusterService.REMOTE_CLUSTER_SKIP_UNAVAILABLE));
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(RemoteClusterService.REMOTE_INITIAL_CONNECTION_TIMEOUT_SETTING));
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(RemoteClusterService.REMOTE_NODE_ATTRIBUTE));
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(RemoteClusterService.REMOTE_CLUSTER_PING_SCHEDULE));
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(RemoteClusterService.REMOTE_CLUSTER_COMPRESS));
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(RemoteConnectionStrategy.REMOTE_CONNECTION_MODE));
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(SniffConnectionStrategy.REMOTE_CONNECTIONS_PER_CLUSTER));
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(SniffConnectionStrategy.REMOTE_CLUSTER_SEEDS));
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(SniffConnectionStrategy.REMOTE_NODE_CONNECTIONS));
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(ProxyConnectionStrategy.REMOTE_CLUSTER_ADDRESSES));
        assertTrue(ClusterSettings.BUILT_IN_CLUSTER_SETTINGS.contains(ProxyConnectionStrategy.REMOTE_SOCKET_CONNECTIONS));
    }

    public void testRemoteClusterSeedSetting() {
        // simple validation
        Settings settings = Settings.builder()
            .put("cluster.remote.foo.seeds", "192.168.0.1:8080")
            .put("cluster.remote.bar.seeds", "[::1]:9090").build();
        SniffConnectionStrategy.REMOTE_CLUSTER_SEEDS.getAllConcreteSettings(settings).forEach(setting -> setting.get(settings));

        Settings brokenSettings = Settings.builder()
            .put("cluster.remote.foo.seeds", "192.168.0.1").build();
        expectThrows(IllegalArgumentException.class, () ->
            SniffConnectionStrategy.REMOTE_CLUSTER_SEEDS
                .getAllConcreteSettings(brokenSettings).forEach(setting -> setting.get(brokenSettings)));

        Settings brokenPortSettings = Settings.builder()
            .put("cluster.remote.foo.seeds", "192.168.0.1:123456789123456789").build();
        Exception e = expectThrows(
            IllegalArgumentException.class,
            () -> SniffConnectionStrategy.REMOTE_CLUSTER_SEEDS.getAllConcreteSettings(brokenSettings)
                .forEach(setting -> setting.get(brokenPortSettings))
        );
        assertEquals("failed to parse port", e.getMessage());
    }

    public void testGroupClusterIndices() throws IOException {
        List<DiscoveryNode> knownNodes = new CopyOnWriteArrayList<>();
        try (MockTransportService cluster1Transport = startTransport("cluster_1_node", knownNodes, Version.CURRENT);
             MockTransportService cluster2Transport = startTransport("cluster_2_node", knownNodes, Version.CURRENT)) {
            DiscoveryNode cluster1Seed = cluster1Transport.getLocalDiscoNode();
            DiscoveryNode cluster2Seed = cluster2Transport.getLocalDiscoNode();
            knownNodes.add(cluster1Transport.getLocalDiscoNode());
            knownNodes.add(cluster2Transport.getLocalDiscoNode());
            Collections.shuffle(knownNodes, random());

            try (MockTransportService transportService = MockTransportService.createNewService(Settings.EMPTY, Version.CURRENT, threadPool,
                null)) {
                transportService.start();
                transportService.acceptIncomingRequests();
                Settings.Builder builder = Settings.builder();
                builder.putList("cluster.remote.cluster_1.seeds", cluster1Seed.getAddress().toString());
                builder.putList("cluster.remote.cluster_2.seeds", cluster2Seed.getAddress().toString());
                try (RemoteClusterService service = new RemoteClusterService(builder.build(), transportService)) {
                    assertFalse(service.isCrossClusterSearchEnabled());
                    service.initializeRemoteClusters();
                    assertTrue(service.isCrossClusterSearchEnabled());
                    assertTrue(service.isRemoteClusterRegistered("cluster_1"));
                    assertTrue(service.isRemoteClusterRegistered("cluster_2"));
                    assertFalse(service.isRemoteClusterRegistered("foo"));
                    Map<String, List<String>> perClusterIndices = service.groupClusterIndices(service.getRemoteClusterNames(),
                        new String[]{"foo:bar", "cluster_1:bar", "cluster_2:foo:bar", "cluster_1:test", "cluster_2:foo*", "foo",
                            "cluster*:baz", "*:boo", "no*match:boo"},
                        i -> false);
                    List<String> localIndices = perClusterIndices.remove(RemoteClusterAware.LOCAL_CLUSTER_GROUP_KEY);
                    assertNotNull(localIndices);
                    assertEquals(Arrays.asList("foo:bar", "foo", "no*match:boo"), localIndices);
                    assertEquals(2, perClusterIndices.size());
                    assertEquals(Arrays.asList("bar", "test", "baz", "boo"), perClusterIndices.get("cluster_1"));
                    assertEquals(Arrays.asList("foo:bar", "foo*", "baz", "boo"), perClusterIndices.get("cluster_2"));

                    IllegalArgumentException iae = expectThrows(IllegalArgumentException.class, () ->
                        service.groupClusterIndices(service.getRemoteClusterNames(), new String[]{"foo:bar", "cluster_1:bar",
                            "cluster_2:foo:bar", "cluster_1:test", "cluster_2:foo*", "foo"}, "cluster_1:bar"::equals));

                    assertEquals("Can not filter indices; index cluster_1:bar exists but there is also a remote cluster named:" +
                            " cluster_1", iae.getMessage());
                }
            }
        }
    }

    public void testGroupIndices() throws IOException {
        List<DiscoveryNode> knownNodes = new CopyOnWriteArrayList<>();
        try (MockTransportService cluster1Transport = startTransport("cluster_1_node", knownNodes, Version.CURRENT);
             MockTransportService cluster2Transport = startTransport("cluster_2_node", knownNodes, Version.CURRENT)) {
            DiscoveryNode cluster1Seed = cluster1Transport.getLocalDiscoNode();
            DiscoveryNode cluster2Seed = cluster2Transport.getLocalDiscoNode();
            knownNodes.add(cluster1Transport.getLocalDiscoNode());
            knownNodes.add(cluster2Transport.getLocalDiscoNode());
            Collections.shuffle(knownNodes, random());

            try (MockTransportService transportService = MockTransportService.createNewService(Settings.EMPTY, Version.CURRENT, threadPool,
                null)) {
                transportService.start();
                transportService.acceptIncomingRequests();
                Settings.Builder builder = Settings.builder();
                builder.putList("cluster.remote.cluster_1.seeds", cluster1Seed.getAddress().toString());
                builder.putList("cluster.remote.cluster_2.seeds", cluster2Seed.getAddress().toString());
                try (RemoteClusterService service = new RemoteClusterService(builder.build(), transportService)) {
                    assertFalse(service.isCrossClusterSearchEnabled());
                    service.initializeRemoteClusters();
                    assertTrue(service.isCrossClusterSearchEnabled());
                    assertTrue(service.isRemoteClusterRegistered("cluster_1"));
                    assertTrue(service.isRemoteClusterRegistered("cluster_2"));
                    assertFalse(service.isRemoteClusterRegistered("foo"));
                    {
                        Map<String, OriginalIndices> perClusterIndices = service.groupIndices(IndicesOptions.LENIENT_EXPAND_OPEN,
                            new String[]{"foo:bar", "cluster_1:bar", "cluster_2:foo:bar", "cluster_1:test", "cluster_2:foo*", "foo",
                                "cluster*:baz", "*:boo", "no*match:boo"},
                            i -> false);
                        assertEquals(3, perClusterIndices.size());
                        assertArrayEquals(new String[]{"foo:bar", "foo", "no*match:boo"},
                            perClusterIndices.get(RemoteClusterAware.LOCAL_CLUSTER_GROUP_KEY).indices());
                        assertArrayEquals(new String[]{"bar", "test", "baz", "boo"}, perClusterIndices.get("cluster_1").indices());
                        assertArrayEquals(new String[]{"foo:bar", "foo*", "baz", "boo"}, perClusterIndices.get("cluster_2").indices());
                    }
                    {
                        IllegalArgumentException iae = expectThrows(IllegalArgumentException.class, () ->
                            service.groupClusterIndices(service.getRemoteClusterNames(), new String[]{"foo:bar", "cluster_1:bar",
                                "cluster_2:foo:bar", "cluster_1:test", "cluster_2:foo*", "foo"}, "cluster_1:bar"::equals));
                        assertEquals("Can not filter indices; index cluster_1:bar exists but there is also a remote cluster named:" +
                            " cluster_1", iae.getMessage());
                    }
                    {
                        Map<String, OriginalIndices> perClusterIndices = service.groupIndices(IndicesOptions.LENIENT_EXPAND_OPEN,
                            new String[]{"cluster_1:bar", "cluster_2:foo*"},
                            i -> false);
                        assertEquals(2, perClusterIndices.size());
                        assertArrayEquals(new String[]{"bar"}, perClusterIndices.get("cluster_1").indices());
                        assertArrayEquals(new String[]{"foo*"}, perClusterIndices.get("cluster_2").indices());
                    }
                    {
                        Map<String, OriginalIndices> perClusterIndices = service.groupIndices(IndicesOptions.LENIENT_EXPAND_OPEN,
                            Strings.EMPTY_ARRAY,
                            i -> false);
                        assertEquals(1, perClusterIndices.size());
                        assertArrayEquals(Strings.EMPTY_ARRAY, perClusterIndices.get(RemoteClusterAware.LOCAL_CLUSTER_GROUP_KEY).indices());
                    }
                }
            }
        }
    }

    public void testIncrementallyAddClusters() throws IOException {
        List<DiscoveryNode> knownNodes = new CopyOnWriteArrayList<>();
        try (MockTransportService cluster1Transport = startTransport("cluster_1_node", knownNodes, Version.CURRENT);
             MockTransportService cluster2Transport = startTransport("cluster_2_node", knownNodes, Version.CURRENT)) {
            DiscoveryNode cluster1Seed = cluster1Transport.getLocalDiscoNode();
            DiscoveryNode cluster2Seed = cluster2Transport.getLocalDiscoNode();
            knownNodes.add(cluster1Transport.getLocalDiscoNode());
            knownNodes.add(cluster2Transport.getLocalDiscoNode());
            Collections.shuffle(knownNodes, random());

            try (MockTransportService transportService = MockTransportService.createNewService(Settings.EMPTY, Version.CURRENT, threadPool,
                null)) {
                transportService.start();
                transportService.acceptIncomingRequests();
                Settings.Builder builder = Settings.builder();
                builder.putList("cluster.remote.cluster_1.seeds", cluster1Seed.getAddress().toString());
                builder.putList("cluster.remote.cluster_2.seeds", cluster2Seed.getAddress().toString());
                try (RemoteClusterService service = new RemoteClusterService(Settings.EMPTY, transportService)) {
                    assertFalse(service.isCrossClusterSearchEnabled());
                    service.initializeRemoteClusters();
                    assertFalse(service.isCrossClusterSearchEnabled());
                    Settings cluster1Settings = createSettings("cluster_1",
                        Collections.singletonList(cluster1Seed.getAddress().toString()));
                    PlainActionFuture<Void> clusterAdded = PlainActionFuture.newFuture();
                    // Add the cluster on a different thread to test that we wait for a new cluster to
                    // connect before returning.
                    new Thread(() -> {
                        try {
                            service.validateAndUpdateRemoteCluster("cluster_1", cluster1Settings);
                            clusterAdded.onResponse(null);
                        } catch (Exception e) {
                            clusterAdded.onFailure(e);
                        }
                    }).start();
                    clusterAdded.actionGet();
                    assertTrue(service.isCrossClusterSearchEnabled());
                    assertTrue(service.isRemoteClusterRegistered("cluster_1"));
                    Settings cluster2Settings = createSettings("cluster_2",
                        Collections.singletonList(cluster2Seed.getAddress().toString()));
                    service.validateAndUpdateRemoteCluster("cluster_2", cluster2Settings);
                    assertTrue(service.isCrossClusterSearchEnabled());
                    assertTrue(service.isRemoteClusterRegistered("cluster_1"));
                    assertTrue(service.isRemoteClusterRegistered("cluster_2"));
                    Settings cluster2SettingsDisabled = createSettings("cluster_2", Collections.emptyList());
                    service.validateAndUpdateRemoteCluster("cluster_2", cluster2SettingsDisabled);
                    assertFalse(service.isRemoteClusterRegistered("cluster_2"));
                    IllegalArgumentException iae = expectThrows(IllegalArgumentException.class,
                        () -> service.validateAndUpdateRemoteCluster(RemoteClusterAware.LOCAL_CLUSTER_GROUP_KEY, Settings.EMPTY));
                    assertEquals("remote clusters must not have the empty string as its key", iae.getMessage());
                }
            }
        }
    }

    public void testDefaultPingSchedule() throws IOException {
        List<DiscoveryNode> knownNodes = new CopyOnWriteArrayList<>();
        try (MockTransportService seedTransport = startTransport("cluster_1_node", knownNodes, Version.CURRENT)) {
            DiscoveryNode seedNode = seedTransport.getLocalDiscoNode();
            knownNodes.add(seedTransport.getLocalDiscoNode());
            TimeValue pingSchedule;
            Settings.Builder settingsBuilder = Settings.builder();
            settingsBuilder.putList("cluster.remote.cluster_1.seeds", seedNode.getAddress().toString());
            if (randomBoolean()) {
                pingSchedule = TimeValue.timeValueSeconds(randomIntBetween(1, 10));
                settingsBuilder.put(TransportSettings.PING_SCHEDULE.getKey(), pingSchedule).build();
            } else {
                pingSchedule = TimeValue.MINUS_ONE;
            }
            Settings settings = settingsBuilder.build();
            try (MockTransportService transportService = MockTransportService.createNewService(settings,
                Version.CURRENT, threadPool, null)) {
                transportService.start();
                transportService.acceptIncomingRequests();
                try (RemoteClusterService service = new RemoteClusterService(settings, transportService)) {
                    assertFalse(service.isCrossClusterSearchEnabled());
                    service.initializeRemoteClusters();
                    assertTrue(service.isCrossClusterSearchEnabled());
                    service.validateAndUpdateRemoteCluster("cluster_1",
                        createSettings("cluster_1", Collections.singletonList(seedNode.getAddress().toString())));
                    assertTrue(service.isCrossClusterSearchEnabled());
                    assertTrue(service.isRemoteClusterRegistered("cluster_1"));
                    RemoteClusterConnection remoteClusterConnection = service.getRemoteClusterConnection("cluster_1");
                    assertEquals(pingSchedule, remoteClusterConnection.getConnectionManager().getConnectionProfile().getPingInterval());
                }
            }
        }
    }

    public void testCustomPingSchedule() throws IOException {
        List<DiscoveryNode> knownNodes = new CopyOnWriteArrayList<>();
        try (MockTransportService cluster1Transport = startTransport("cluster_1_node", knownNodes, Version.CURRENT);
             MockTransportService cluster2Transport = startTransport("cluster_2_node", knownNodes, Version.CURRENT)) {
            DiscoveryNode cluster1Seed = cluster1Transport.getLocalDiscoNode();
            DiscoveryNode cluster2Seed = cluster2Transport.getLocalDiscoNode();
            knownNodes.add(cluster1Transport.getLocalDiscoNode());
            knownNodes.add(cluster2Transport.getLocalDiscoNode());
            Collections.shuffle(knownNodes, random());
            Settings.Builder settingsBuilder = Settings.builder();
            if (randomBoolean()) {
                settingsBuilder.put(TransportSettings.PING_SCHEDULE.getKey(), TimeValue.timeValueSeconds(randomIntBetween(1, 10)));
            }
            Settings transportSettings = settingsBuilder.build();

            try (MockTransportService transportService = MockTransportService.createNewService(transportSettings, Version.CURRENT,
                threadPool, null)) {
                transportService.start();
                transportService.acceptIncomingRequests();
                Settings.Builder builder = Settings.builder();
                builder.putList("cluster.remote.cluster_1.seeds", cluster1Seed.getAddress().toString());
                builder.putList("cluster.remote.cluster_2.seeds", cluster2Seed.getAddress().toString());
                TimeValue pingSchedule1 = // randomBoolean() ? TimeValue.MINUS_ONE :
                    TimeValue.timeValueSeconds(randomIntBetween(1, 10));
                builder.put("cluster.remote.cluster_1.transport.ping_schedule", pingSchedule1);
                TimeValue pingSchedule2 = //randomBoolean() ? TimeValue.MINUS_ONE :
                    TimeValue.timeValueSeconds(randomIntBetween(1, 10));
                builder.put("cluster.remote.cluster_2.transport.ping_schedule", pingSchedule2);
                try (RemoteClusterService service = new RemoteClusterService(builder.build(), transportService)) {
                    service.initializeRemoteClusters();
                    assertTrue(service.isRemoteClusterRegistered("cluster_1"));
                    RemoteClusterConnection remoteClusterConnection1 = service.getRemoteClusterConnection("cluster_1");
                    assertEquals(pingSchedule1, remoteClusterConnection1.getConnectionManager().getConnectionProfile().getPingInterval());
                    RemoteClusterConnection remoteClusterConnection2 = service.getRemoteClusterConnection("cluster_2");
                    assertEquals(pingSchedule2, remoteClusterConnection2.getConnectionManager().getConnectionProfile().getPingInterval());
                }
            }
        }
    }

    public void testChangeSettings() throws Exception {
        List<DiscoveryNode> knownNodes = new CopyOnWriteArrayList<>();
        try (MockTransportService cluster1Transport = startTransport("cluster_1_node", knownNodes, Version.CURRENT)) {
            DiscoveryNode cluster1Seed = cluster1Transport.getLocalDiscoNode();
            knownNodes.add(cluster1Transport.getLocalDiscoNode());
            Collections.shuffle(knownNodes, random());

            try (MockTransportService transportService = MockTransportService.createNewService(Settings.EMPTY, Version.CURRENT,
                threadPool, null)) {
                transportService.start();
                transportService.acceptIncomingRequests();
                Settings.Builder builder = Settings.builder();
                builder.putList("cluster.remote.cluster_1.seeds", cluster1Seed.getAddress().toString());
                try (RemoteClusterService service = new RemoteClusterService(builder.build(), transportService)) {
                    service.initializeRemoteClusters();
                    RemoteClusterConnection remoteClusterConnection = service.getRemoteClusterConnection("cluster_1");
                    Settings.Builder settingsChange = Settings.builder();
                    TimeValue pingSchedule = TimeValue.timeValueSeconds(randomIntBetween(6, 8));
                    settingsChange.put("cluster.remote.cluster_1.transport.ping_schedule", pingSchedule);
                    boolean compressionEnabled = true;
                    settingsChange.put("cluster.remote.cluster_1.transport.compress", compressionEnabled);
                    settingsChange.putList("cluster.remote.cluster_1.seeds", cluster1Seed.getAddress().toString());
                    service.validateAndUpdateRemoteCluster("cluster_1", settingsChange.build());
                    assertBusy(remoteClusterConnection::isClosed);

                    remoteClusterConnection = service.getRemoteClusterConnection("cluster_1");
                    ConnectionProfile connectionProfile = remoteClusterConnection.getConnectionManager().getConnectionProfile();
                    assertEquals(pingSchedule, connectionProfile.getPingInterval());
                    assertEquals(compressionEnabled, connectionProfile.getCompressionEnabled());
                }
            }
        }
    }

    public void testRemoteNodeAttribute() throws IOException, InterruptedException {
        final Settings settings =
                Settings.builder().put("cluster.remote.node.attr", "gateway").build();
        final List<DiscoveryNode> knownNodes = new CopyOnWriteArrayList<>();
        final Settings gateway = Settings.builder().put("node.attr.gateway", true).build();
        try (MockTransportService c1N1 =
                     startTransport("cluster_1_node_1", knownNodes, Version.CURRENT);
             MockTransportService c1N2 =
                     startTransport("cluster_1_node_2", knownNodes, Version.CURRENT, gateway);
             MockTransportService c2N1 =
                     startTransport("cluster_2_node_1", knownNodes, Version.CURRENT);
             MockTransportService c2N2 =
                     startTransport("cluster_2_node_2", knownNodes, Version.CURRENT, gateway)) {
            final DiscoveryNode c1N1Node = c1N1.getLocalDiscoNode();
            final DiscoveryNode c1N2Node = c1N2.getLocalDiscoNode();
            final DiscoveryNode c2N1Node = c2N1.getLocalDiscoNode();
            final DiscoveryNode c2N2Node = c2N2.getLocalDiscoNode();
            knownNodes.add(c1N1Node);
            knownNodes.add(c1N2Node);
            knownNodes.add(c2N1Node);
            knownNodes.add(c2N2Node);
            Collections.shuffle(knownNodes, random());

            try (MockTransportService transportService = MockTransportService.createNewService(
                    settings,
                    Version.CURRENT,
                    threadPool,
                    null)) {
                transportService.start();
                transportService.acceptIncomingRequests();
                final Settings.Builder builder = Settings.builder();
                builder.putList(
                        "cluster.remote.cluster_1.seed", c1N1Node.getAddress().toString());
                builder.putList(
                        "cluster.remote.cluster_2.seed", c2N1Node.getAddress().toString());
                try (RemoteClusterService service =
                             new RemoteClusterService(settings, transportService)) {
                    assertFalse(service.isCrossClusterSearchEnabled());
                    service.initializeRemoteClusters();
                    assertFalse(service.isCrossClusterSearchEnabled());

                    final CountDownLatch firstLatch = new CountDownLatch(1);
                    service.updateRemoteCluster(
                            "cluster_1",
                            createSettings("cluster_1", Arrays.asList(c1N1Node.getAddress().toString(), c1N2Node.getAddress().toString())),
                            connectionListener(firstLatch));
                    firstLatch.await();

                    final CountDownLatch secondLatch = new CountDownLatch(1);
                    service.updateRemoteCluster(
                            "cluster_2",
                            createSettings("cluster_2", Arrays.asList(c2N1Node.getAddress().toString(), c2N2Node.getAddress().toString())),
                            connectionListener(secondLatch));
                    secondLatch.await();

                    assertTrue(service.isCrossClusterSearchEnabled());
                    assertTrue(service.isRemoteClusterRegistered("cluster_1"));
                    assertFalse(service.isRemoteNodeConnected("cluster_1", c1N1Node));
                    assertTrue(service.isRemoteNodeConnected("cluster_1", c1N2Node));
                    assertTrue(service.isRemoteClusterRegistered("cluster_2"));
                    assertFalse(service.isRemoteNodeConnected("cluster_2", c2N1Node));
                    assertTrue(service.isRemoteNodeConnected("cluster_2", c2N2Node));
                    assertEquals(0, transportService.getConnectionManager().size());
                }
            }
        }
    }

    public void testRemoteNodeRoles() throws IOException, InterruptedException {
        final Settings settings = Settings.EMPTY;
        final List<DiscoveryNode> knownNodes = new CopyOnWriteArrayList<>();
        final Settings data = Settings.builder().put("node.master", false).build();
        final Settings dedicatedMaster = Settings.builder().put("node.data", false).put("node.ingest", "false").build();
        try (MockTransportService c1N1 =
                     startTransport("cluster_1_node_1", knownNodes, Version.CURRENT, dedicatedMaster);
             MockTransportService c1N2 =
                     startTransport("cluster_1_node_2", knownNodes, Version.CURRENT, data);
             MockTransportService c2N1 =
                     startTransport("cluster_2_node_1", knownNodes, Version.CURRENT, dedicatedMaster);
             MockTransportService c2N2 =
                     startTransport("cluster_2_node_2", knownNodes, Version.CURRENT, data)) {
            final DiscoveryNode c1N1Node = c1N1.getLocalDiscoNode();
            final DiscoveryNode c1N2Node = c1N2.getLocalDiscoNode();
            final DiscoveryNode c2N1Node = c2N1.getLocalDiscoNode();
            final DiscoveryNode c2N2Node = c2N2.getLocalDiscoNode();
            knownNodes.add(c1N1Node);
            knownNodes.add(c1N2Node);
            knownNodes.add(c2N1Node);
            knownNodes.add(c2N2Node);
            Collections.shuffle(knownNodes, random());

            try (MockTransportService transportService = MockTransportService.createNewService(
                    settings,
                    Version.CURRENT,
                    threadPool,
                    null)) {
                transportService.start();
                transportService.acceptIncomingRequests();
                final Settings.Builder builder = Settings.builder();
                builder.putList("cluster.remote.cluster_1.seed", c1N1Node.getAddress().toString());
                builder.putList("cluster.remote.cluster_2.seed", c2N1Node.getAddress().toString());
                try (RemoteClusterService service = new RemoteClusterService(settings, transportService)) {
                    assertFalse(service.isCrossClusterSearchEnabled());
                    service.initializeRemoteClusters();
                    assertFalse(service.isCrossClusterSearchEnabled());

                    final CountDownLatch firstLatch = new CountDownLatch(1);
                    service.updateRemoteCluster(
                            "cluster_1",
                            createSettings("cluster_1", Arrays.asList(c1N1Node.getAddress().toString(), c1N2Node.getAddress().toString())),
                            connectionListener(firstLatch));
                    firstLatch.await();

                    final CountDownLatch secondLatch = new CountDownLatch(1);
                    service.updateRemoteCluster(
                            "cluster_2",
                            createSettings("cluster_2", Arrays.asList(c2N1Node.getAddress().toString(), c2N2Node.getAddress().toString())),
                            connectionListener(secondLatch));
                    secondLatch.await();

                    assertTrue(service.isCrossClusterSearchEnabled());
                    assertTrue(service.isRemoteClusterRegistered("cluster_1"));
                    assertFalse(service.isRemoteNodeConnected("cluster_1", c1N1Node));
                    assertTrue(service.isRemoteNodeConnected("cluster_1", c1N2Node));
                    assertTrue(service.isRemoteClusterRegistered("cluster_2"));
                    assertFalse(service.isRemoteNodeConnected("cluster_2", c2N1Node));
                    assertTrue(service.isRemoteNodeConnected("cluster_2", c2N2Node));
                    assertEquals(0, transportService.getConnectionManager().size());
                }
            }
        }
    }

    private ActionListener<Void> connectionListener(final CountDownLatch latch) {
        return ActionListener.wrap(x -> latch.countDown(), x -> fail());
    }

    public void testCollectNodes() throws InterruptedException, IOException {
        final Settings settings = Settings.EMPTY;
        final List<DiscoveryNode> knownNodes_c1 = new CopyOnWriteArrayList<>();
        final List<DiscoveryNode> knownNodes_c2 = new CopyOnWriteArrayList<>();

        try (MockTransportService c1N1 =
                 startTransport("cluster_1_node_1", knownNodes_c1, Version.CURRENT, settings);
             MockTransportService c1N2 =
                 startTransport("cluster_1_node_2", knownNodes_c1, Version.CURRENT, settings);
             MockTransportService c2N1 =
                 startTransport("cluster_2_node_1", knownNodes_c2, Version.CURRENT, settings);
             MockTransportService c2N2 =
                 startTransport("cluster_2_node_2", knownNodes_c2, Version.CURRENT, settings)) {
            final DiscoveryNode c1N1Node = c1N1.getLocalDiscoNode();
            final DiscoveryNode c1N2Node = c1N2.getLocalDiscoNode();
            final DiscoveryNode c2N1Node = c2N1.getLocalDiscoNode();
            final DiscoveryNode c2N2Node = c2N2.getLocalDiscoNode();
            knownNodes_c1.add(c1N1Node);
            knownNodes_c1.add(c1N2Node);
            knownNodes_c2.add(c2N1Node);
            knownNodes_c2.add(c2N2Node);
            Collections.shuffle(knownNodes_c1, random());
            Collections.shuffle(knownNodes_c2, random());

            try (MockTransportService transportService = MockTransportService.createNewService(
                settings,
                Version.CURRENT,
                threadPool,
                null)) {
                transportService.start();
                transportService.acceptIncomingRequests();
                final Settings.Builder builder = Settings.builder();
                builder.putList(
                    "cluster.remote.cluster_1.seed", c1N1Node.getAddress().toString());
                builder.putList(
                    "cluster.remote.cluster_2.seed", c2N1Node.getAddress().toString());
                try (RemoteClusterService service =
                         new RemoteClusterService(settings, transportService)) {
                    assertFalse(service.isCrossClusterSearchEnabled());
                    service.initializeRemoteClusters();
                    assertFalse(service.isCrossClusterSearchEnabled());

                    final CountDownLatch firstLatch = new CountDownLatch(1);

                    service.updateRemoteCluster("cluster_1",
                        createSettings("cluster_1", Arrays.asList(c1N1Node.getAddress().toString(), c1N2Node.getAddress().toString())),
                        connectionListener(firstLatch));
                    firstLatch.await();

                    final CountDownLatch secondLatch = new CountDownLatch(1);
                    service.updateRemoteCluster(
                        "cluster_2",
                        createSettings("cluster_2", Arrays.asList(c2N1Node.getAddress().toString(), c2N2Node.getAddress().toString())),
                        connectionListener(secondLatch));
                    secondLatch.await();
                    CountDownLatch latch = new CountDownLatch(1);
                    service.collectNodes(new HashSet<>(Arrays.asList("cluster_1", "cluster_2")),
                        new ActionListener<BiFunction<String, String, DiscoveryNode>>() {
                        @Override
                        public void onResponse(BiFunction<String, String, DiscoveryNode> func) {
                            try {
                                assertEquals(c1N1Node, func.apply("cluster_1", c1N1Node.getId()));
                                assertEquals(c1N2Node, func.apply("cluster_1", c1N2Node.getId()));
                                assertEquals(c2N1Node, func.apply("cluster_2", c2N1Node.getId()));
                                assertEquals(c2N2Node, func.apply("cluster_2", c2N2Node.getId()));
                            } finally {
                                latch.countDown();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            try {
                                throw new AssertionError(e);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                    latch.await();
                    {
                        CountDownLatch failLatch = new CountDownLatch(1);
                        AtomicReference<Exception> ex = new AtomicReference<>();
                        service.collectNodes(new HashSet<>(Arrays.asList("cluster_1", "cluster_2", "no such cluster")),
                            new ActionListener<BiFunction<String, String, DiscoveryNode>>() {
                                @Override
                                public void onResponse(BiFunction<String, String, DiscoveryNode> stringStringDiscoveryNodeBiFunction) {
                                    try {
                                        fail("should not be called");
                                    } finally {
                                        failLatch.countDown();
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    try {
                                        ex.set(e);
                                    } finally {
                                        failLatch.countDown();
                                    }
                                }
                            });
                        failLatch.await();
                        assertNotNull(ex.get());
                        assertTrue(ex.get() instanceof NoSuchRemoteClusterException);
                        assertEquals("no such remote cluster: [no such cluster]", ex.get().getMessage());
                    }
                    {
                        logger.info("closing all source nodes");
                        // close all targets and check for the transport level failure path
                        IOUtils.close(c1N1, c1N2, c2N1, c2N2);
                        logger.info("all source nodes are closed");
                        CountDownLatch failLatch = new CountDownLatch(1);
                        AtomicReference<Exception> ex = new AtomicReference<>();
                        service.collectNodes(new HashSet<>(Arrays.asList("cluster_1", "cluster_2")),
                            new ActionListener<BiFunction<String, String, DiscoveryNode>>() {
                                @Override
                                public void onResponse(BiFunction<String, String, DiscoveryNode> stringStringDiscoveryNodeBiFunction) {
                                    try {
                                        fail("should not be called");
                                    } finally {
                                        failLatch.countDown();
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    try {
                                        ex.set(e);
                                    } finally {
                                        failLatch.countDown();
                                    }
                                }
                            });
                        failLatch.await();
                        assertNotNull(ex.get());
                        if (ex.get() instanceof  IllegalStateException) {
                            assertThat(ex.get().getMessage(), equalTo("no seed node left"));
                        } else {
                            assertThat(ex.get(),
                                either(instanceOf(TransportException.class)).or(instanceOf(NoSuchRemoteClusterException.class)));
                        }
                    }
                }
            }
        }
    }

    public void testRemoteClusterSkipIfDisconnectedSetting() {
        {
            Settings settings = Settings.builder()
                    .put("cluster.remote.foo.skip_unavailable", true)
                    .put("cluster.remote.bar.skip_unavailable", false).build();
            RemoteClusterService.REMOTE_CLUSTER_SKIP_UNAVAILABLE.getAllConcreteSettings(settings).forEach(setting -> setting.get(settings));
        }
        {
            Settings brokenSettings = Settings.builder()
                    .put("cluster.remote.foo.skip_unavailable", "broken").build();
            expectThrows(IllegalArgumentException.class, () ->
                    RemoteClusterService.REMOTE_CLUSTER_SKIP_UNAVAILABLE.getAllConcreteSettings(brokenSettings)
                            .forEach(setting -> setting.get(brokenSettings)));
        }

        AbstractScopedSettings service = new ClusterSettings(Settings.EMPTY,
                new HashSet<>(Arrays.asList(SniffConnectionStrategy.REMOTE_CLUSTER_SEEDS,
                    RemoteClusterService.REMOTE_CLUSTER_SKIP_UNAVAILABLE)));
        {
            Settings settings = Settings.builder().put("cluster.remote.foo.skip_unavailable", randomBoolean()).build();
            IllegalArgumentException iae = expectThrows(IllegalArgumentException.class, () -> service.validate(settings, true));
            assertEquals("missing required setting [cluster.remote.foo.seeds] for setting [cluster.remote.foo.skip_unavailable]",
                    iae.getMessage());
        }
        {
            try (MockTransportService remoteSeedTransport = startTransport("seed", new CopyOnWriteArrayList<>(), Version.CURRENT)) {
                String seed = remoteSeedTransport.getLocalDiscoNode().getAddress().toString();
                service.validate(Settings.builder().put("cluster.remote.foo.skip_unavailable", randomBoolean())
                        .put("cluster.remote.foo.seeds", seed).build(), true);
                service.validate(Settings.builder().put("cluster.remote.foo.seeds", seed).build(), true);

                AbstractScopedSettings service2 = new ClusterSettings(Settings.builder().put("cluster.remote.foo.seeds", seed).build(),
                        new HashSet<>(Arrays.asList(SniffConnectionStrategy.REMOTE_CLUSTER_SEEDS,
                            RemoteClusterService.REMOTE_CLUSTER_SKIP_UNAVAILABLE)));
                service2.validate(Settings.builder().put("cluster.remote.foo.skip_unavailable", randomBoolean()).build(), false);
            }
        }
    }

    public void testReconnectWhenStrategySettingsUpdated() throws Exception {
        List<DiscoveryNode> knownNodes = new CopyOnWriteArrayList<>();
        try (MockTransportService cluster_node_0 = startTransport("cluster_node_0", knownNodes, Version.CURRENT);
             MockTransportService cluster_node_1 = startTransport("cluster_node_1", knownNodes, Version.CURRENT)) {

            final DiscoveryNode node0 = cluster_node_0.getLocalDiscoNode();
            final DiscoveryNode node1 = cluster_node_1.getLocalDiscoNode();
            knownNodes.add(node0);
            knownNodes.add(node1);
            Collections.shuffle(knownNodes, random());

            try (MockTransportService transportService =
                     MockTransportService.createNewService(Settings.EMPTY, Version.CURRENT, threadPool, null)) {
                transportService.start();
                transportService.acceptIncomingRequests();

                final Settings.Builder builder = Settings.builder();
                builder.putList("cluster.remote.cluster_test.seeds", Collections.singletonList(node0.getAddress().toString()));
                try (RemoteClusterService service = new RemoteClusterService(builder.build(), transportService)) {
                    assertFalse(service.isCrossClusterSearchEnabled());
                    service.initializeRemoteClusters();
                    assertTrue(service.isCrossClusterSearchEnabled());

                    final RemoteClusterConnection firstRemoteClusterConnection = service.getRemoteClusterConnection("cluster_test");
                    assertTrue(firstRemoteClusterConnection.isNodeConnected(node0));
                    assertTrue(firstRemoteClusterConnection.isNodeConnected(node1));
                    assertEquals(2, firstRemoteClusterConnection.getNumNodesConnected());
                    assertFalse(firstRemoteClusterConnection.isClosed());

                    final CountDownLatch firstLatch = new CountDownLatch(1);
                    service.updateRemoteCluster(
                        "cluster_test",
                        createSettings("cluster_test", Collections.singletonList(node0.getAddress().toString())),
                        connectionListener(firstLatch));
                    firstLatch.await();

                    assertTrue(service.isCrossClusterSearchEnabled());
                    assertTrue(firstRemoteClusterConnection.isNodeConnected(node0));
                    assertTrue(firstRemoteClusterConnection.isNodeConnected(node1));
                    assertEquals(2, firstRemoteClusterConnection.getNumNodesConnected());
                    assertFalse(firstRemoteClusterConnection.isClosed());
                    assertSame(firstRemoteClusterConnection, service.getRemoteClusterConnection("cluster_test"));

                    final List<String> newSeeds = new ArrayList<>();
                    newSeeds.add(node1.getAddress().toString());
                    if (randomBoolean()) {
                        newSeeds.add(node0.getAddress().toString());
                        Collections.shuffle(newSeeds, random());
                    }

                    final CountDownLatch secondLatch = new CountDownLatch(1);
                    service.updateRemoteCluster(
                        "cluster_test",
                        createSettings("cluster_test", newSeeds),
                        connectionListener(secondLatch));
                    secondLatch.await();

                    assertTrue(service.isCrossClusterSearchEnabled());
                    assertBusy(() -> {
                        assertFalse(firstRemoteClusterConnection.isNodeConnected(node0));
                        assertFalse(firstRemoteClusterConnection.isNodeConnected(node1));
                        assertEquals(0, firstRemoteClusterConnection.getNumNodesConnected());
                        assertTrue(firstRemoteClusterConnection.isClosed());
                    });

                    final RemoteClusterConnection secondRemoteClusterConnection = service.getRemoteClusterConnection("cluster_test");
                    assertTrue(secondRemoteClusterConnection.isNodeConnected(node0));
                    assertTrue(secondRemoteClusterConnection.isNodeConnected(node1));
                    assertEquals(2, secondRemoteClusterConnection.getNumNodesConnected());
                    assertFalse(secondRemoteClusterConnection.isClosed());
                }
            }
        }
    }

    public static void updateSkipUnavailable(RemoteClusterService service, String clusterAlias, boolean skipUnavailable) {
        RemoteClusterConnection connection = service.getRemoteClusterConnection(clusterAlias);
        connection.updateSkipUnavailable(skipUnavailable);
    }

    public static void addConnectionListener(RemoteClusterService service, TransportConnectionListener listener) {
        for (RemoteClusterConnection connection : service.getConnections()) {
            ConnectionManager connectionManager = connection.getConnectionManager();
            connectionManager.addListener(listener);
        }
    }

    public void testSkipUnavailable() {
        List<DiscoveryNode> knownNodes = new CopyOnWriteArrayList<>();
        try (MockTransportService seedTransport = startTransport("seed_node", knownNodes, Version.CURRENT)) {
            DiscoveryNode seedNode = seedTransport.getLocalDiscoNode();
            knownNodes.add(seedNode);
            Settings.Builder builder = Settings.builder();
            builder.putList("cluster.remote.cluster1.seeds", seedTransport.getLocalDiscoNode().getAddress().toString());
            try (MockTransportService service = MockTransportService.createNewService(builder.build(), Version.CURRENT, threadPool, null)) {
                service.start();
                service.acceptIncomingRequests();

                assertFalse(service.getRemoteClusterService().isSkipUnavailable("cluster1"));

                if (randomBoolean()) {
                    updateSkipUnavailable(service.getRemoteClusterService(), "cluster1", false);
                    assertFalse(service.getRemoteClusterService().isSkipUnavailable("cluster1"));
                }

                updateSkipUnavailable(service.getRemoteClusterService(), "cluster1", true);
                assertTrue(service.getRemoteClusterService().isSkipUnavailable("cluster1"));
            }
        }
    }

    private static Settings createSettings(String clusterAlias, List<String> seeds) {
        Settings.Builder builder = Settings.builder();
        builder.put(SniffConnectionStrategy.REMOTE_CLUSTER_SEEDS.getConcreteSettingForNamespace(clusterAlias).getKey(),
            Strings.collectionToCommaDelimitedString(seeds));
        return builder.build();
    }
}
