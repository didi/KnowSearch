/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.core.slm.history;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterModule;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.TriFunction;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.ClusterServiceUtils;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.client.NoOpClient;
import org.elasticsearch.threadpool.TestThreadPool;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.xpack.core.ilm.DeleteAction;
import org.elasticsearch.xpack.core.ilm.IndexLifecycleMetadata;
import org.elasticsearch.xpack.core.ilm.LifecycleAction;
import org.elasticsearch.xpack.core.ilm.LifecyclePolicy;
import org.elasticsearch.xpack.core.ilm.LifecyclePolicyMetadata;
import org.elasticsearch.xpack.core.ilm.LifecycleType;
import org.elasticsearch.xpack.core.ilm.OperationMode;
import org.elasticsearch.xpack.core.ilm.RolloverAction;
import org.elasticsearch.xpack.core.ilm.TimeseriesLifecycleType;
import org.elasticsearch.xpack.core.ilm.action.PutLifecycleAction;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.elasticsearch.mock.orig.Mockito.when;
import static org.elasticsearch.xpack.core.ilm.LifecycleSettings.SLM_HISTORY_INDEX_ENABLED_SETTING;
import static org.elasticsearch.xpack.core.slm.history.SnapshotLifecycleTemplateRegistry.SLM_POLICY_NAME;
import static org.elasticsearch.xpack.core.slm.history.SnapshotLifecycleTemplateRegistry.SLM_TEMPLATE_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class SnapshotLifecycleTemplateRegistryTests extends ESTestCase {
    private SnapshotLifecycleTemplateRegistry registry;
    private NamedXContentRegistry xContentRegistry;
    private ClusterService clusterService;
    private ThreadPool threadPool;
    private VerifyingClient client;

    @Before
    public void createRegistryAndClient() {
        threadPool = new TestThreadPool(this.getClass().getName());
        client = new VerifyingClient(threadPool);
        clusterService = ClusterServiceUtils.createClusterService(threadPool);
        List<NamedXContentRegistry.Entry> entries = new ArrayList<>(ClusterModule.getNamedXWriteables());
        entries.addAll(Arrays.asList(
            new NamedXContentRegistry.Entry(LifecycleType.class, new ParseField(TimeseriesLifecycleType.TYPE),
                (p) -> TimeseriesLifecycleType.INSTANCE),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(RolloverAction.NAME), RolloverAction::parse),
            new NamedXContentRegistry.Entry(LifecycleAction.class, new ParseField(DeleteAction.NAME), DeleteAction::parse)));
        xContentRegistry = new NamedXContentRegistry(entries);
        registry = new SnapshotLifecycleTemplateRegistry(Settings.EMPTY, clusterService, threadPool, client, xContentRegistry);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        threadPool.shutdownNow();
    }

    public void testDisabledDoesNotAddTemplates() {
        Settings settings = Settings.builder().put(SLM_HISTORY_INDEX_ENABLED_SETTING.getKey(), false).build();
        SnapshotLifecycleTemplateRegistry disabledRegistry = new SnapshotLifecycleTemplateRegistry(settings, clusterService, threadPool,
            client, xContentRegistry);
        assertThat(disabledRegistry.getTemplateConfigs(), hasSize(0));
        assertThat(disabledRegistry.getPolicyConfigs(), hasSize(0));
    }

    @AwaitsFix(bugUrl = "https://github.com/elastic/elasticsearch/issues/43950")
    public void testThatNonExistingTemplatesAreAddedImmediately() throws Exception {
        DiscoveryNode node = new DiscoveryNode("node", ESTestCase.buildNewFakeTransportAddress(), Version.CURRENT);
        DiscoveryNodes nodes = DiscoveryNodes.builder().localNodeId("node").masterNodeId("node").add(node).build();

        ClusterChangedEvent event = createClusterChangedEvent(Collections.emptyList(), nodes);

        AtomicInteger calledTimes = new AtomicInteger(0);
        client.setVerifier((action, request, listener) -> {
            if (action instanceof PutIndexTemplateAction) {
                calledTimes.incrementAndGet();
                assertThat(action, instanceOf(PutIndexTemplateAction.class));
                assertThat(request, instanceOf(PutIndexTemplateRequest.class));
                final PutIndexTemplateRequest putRequest = (PutIndexTemplateRequest) request;
                assertThat(putRequest.name(), equalTo(SLM_TEMPLATE_NAME));
                assertThat(putRequest.settings().get("index.lifecycle.name"), equalTo(SLM_POLICY_NAME));
                assertNotNull(listener);
                return new TestPutIndexTemplateResponse(true);
            } else if (action instanceof PutLifecycleAction) {
                // Ignore this, it's verified in another test
                return new PutLifecycleAction.Response(true);
            } else {
                fail("client called with unexpected request:" + request.toString());
                return null;
            }
        });
        registry.clusterChanged(event);
        assertBusy(() -> assertThat(calledTimes.get(), equalTo(registry.getTemplateConfigs().size())));

        calledTimes.set(0);
        // now delete one template from the cluster state and lets retry
        ClusterChangedEvent newEvent = createClusterChangedEvent(Collections.emptyList(), nodes);
        registry.clusterChanged(newEvent);
        assertBusy(() -> assertThat(calledTimes.get(), equalTo(1)));
    }

    public void testThatNonExistingPoliciesAreAddedImmediately() throws Exception {
        DiscoveryNode node = new DiscoveryNode("node", ESTestCase.buildNewFakeTransportAddress(), Version.CURRENT);
        DiscoveryNodes nodes = DiscoveryNodes.builder().localNodeId("node").masterNodeId("node").add(node).build();

        AtomicInteger calledTimes = new AtomicInteger(0);
        client.setVerifier((action, request, listener) -> {
            if (action instanceof PutLifecycleAction) {
                calledTimes.incrementAndGet();
                assertThat(action, instanceOf(PutLifecycleAction.class));
                assertThat(request, instanceOf(PutLifecycleAction.Request.class));
                final PutLifecycleAction.Request putRequest = (PutLifecycleAction.Request) request;
                assertThat(putRequest.getPolicy().getName(), equalTo(SLM_POLICY_NAME));
                assertNotNull(listener);
                return new PutLifecycleAction.Response(true);
            } else if (action instanceof PutIndexTemplateAction) {
                // Ignore this, it's verified in another test
                return new TestPutIndexTemplateResponse(true);
            } else {
                fail("client called with unexpected request:" + request.toString());
                return null;
            }
        });

        ClusterChangedEvent event = createClusterChangedEvent(Collections.emptyList(), nodes);
        registry.clusterChanged(event);
        assertBusy(() -> assertThat(calledTimes.get(), equalTo(1)));
    }

    public void testPolicyAlreadyExists() {
        DiscoveryNode node = new DiscoveryNode("node", ESTestCase.buildNewFakeTransportAddress(), Version.CURRENT);
        DiscoveryNodes nodes = DiscoveryNodes.builder().localNodeId("node").masterNodeId("node").add(node).build();

        Map<String, LifecyclePolicy> policyMap = new HashMap<>();
        List<LifecyclePolicy> policies = registry.getPolicyConfigs().stream()
            .map(policyConfig -> policyConfig.load(xContentRegistry))
            .collect(Collectors.toList());
        assertThat(policies, hasSize(1));
        LifecyclePolicy policy = policies.get(0);
        policyMap.put(policy.getName(), policy);

        client.setVerifier((action, request, listener) -> {
            if (action instanceof PutIndexTemplateAction) {
                // Ignore this, it's verified in another test
                return new TestPutIndexTemplateResponse(true);
            } else if (action instanceof PutLifecycleAction) {
                fail("if the policy already exists it should be re-put");
            } else {
                fail("client called with unexpected request:" + request.toString());
            }
            return null;
        });

        ClusterChangedEvent event = createClusterChangedEvent(Collections.emptyList(), policyMap, nodes);
        registry.clusterChanged(event);
    }

    public void testPolicyAlreadyExistsButDiffers() throws IOException {
        DiscoveryNode node = new DiscoveryNode("node", ESTestCase.buildNewFakeTransportAddress(), Version.CURRENT);
        DiscoveryNodes nodes = DiscoveryNodes.builder().localNodeId("node").masterNodeId("node").add(node).build();

        Map<String, LifecyclePolicy> policyMap = new HashMap<>();
        String policyStr = "{\"phases\":{\"delete\":{\"min_age\":\"1m\",\"actions\":{\"delete\":{}}}}}";
        List<LifecyclePolicy> policies = registry.getPolicyConfigs().stream()
            .map(policyConfig -> policyConfig.load(xContentRegistry))
            .collect(Collectors.toList());
        assertThat(policies, hasSize(1));
        LifecyclePolicy policy = policies.get(0);

        client.setVerifier((action, request, listener) -> {
            if (action instanceof PutIndexTemplateAction) {
                // Ignore this, it's verified in another test
                return new TestPutIndexTemplateResponse(true);
            } else if (action instanceof PutLifecycleAction) {
                fail("if the policy already exists it should be re-put");
            } else {
                fail("client called with unexpected request:" + request.toString());
            }
            return null;
        });

        try (XContentParser parser = XContentType.JSON.xContent()
                .createParser(xContentRegistry, LoggingDeprecationHandler.THROW_UNSUPPORTED_OPERATION, policyStr)) {
            LifecyclePolicy different = LifecyclePolicy.parse(parser, policy.getName());
            policyMap.put(policy.getName(), different);
            ClusterChangedEvent event = createClusterChangedEvent(Collections.emptyList(), policyMap, nodes);
            registry.clusterChanged(event);
        }
    }

    public void testThatMissingMasterNodeDoesNothing() {
        DiscoveryNode localNode = new DiscoveryNode("node", ESTestCase.buildNewFakeTransportAddress(), Version.CURRENT);
        DiscoveryNodes nodes = DiscoveryNodes.builder().localNodeId("node").add(localNode).build();

        client.setVerifier((a,r,l) -> {
            fail("if the master is missing nothing should happen");
            return null;
        });

        ClusterChangedEvent event = createClusterChangedEvent(Arrays.asList(SLM_TEMPLATE_NAME), nodes);
        registry.clusterChanged(event);
    }

    public void testValidate() {
        assertFalse(registry.validate(createClusterState(Settings.EMPTY, Collections.emptyList(), Collections.emptyMap(), null)));
        assertFalse(registry.validate(createClusterState(Settings.EMPTY, Collections.singletonList(SLM_TEMPLATE_NAME),
            Collections.emptyMap(), null)));

        Map<String, LifecyclePolicy> policyMap = new HashMap<>();
        policyMap.put(SLM_POLICY_NAME, new LifecyclePolicy(SLM_POLICY_NAME, new HashMap<>()));
        assertFalse(registry.validate(createClusterState(Settings.EMPTY, Collections.emptyList(), policyMap, null)));

        assertTrue(registry.validate(createClusterState(Settings.EMPTY, Collections.singletonList(SLM_TEMPLATE_NAME), policyMap, null)));
    }

    // -------------

    /**
     * A client that delegates to a verifying function for action/request/listener
     */
    public static class VerifyingClient extends NoOpClient {

        private TriFunction<ActionType<?>, ActionRequest, ActionListener<?>, ActionResponse> verifier = (a, r, l) -> {
            fail("verifier not set");
            return null;
        };

        VerifyingClient(ThreadPool threadPool) {
            super(threadPool);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected <Request extends ActionRequest, Response extends ActionResponse> void doExecute(ActionType<Response> action,
                                                                                                  Request request,
                                                                                                  ActionListener<Response> listener) {
            try {
                listener.onResponse((Response) verifier.apply(action, request, listener));
            } catch (Exception e) {
                listener.onFailure(e);
            }
        }

        public VerifyingClient setVerifier(TriFunction<ActionType<?>, ActionRequest, ActionListener<?>, ActionResponse> verifier) {
            this.verifier = verifier;
            return this;
        }
    }

    private ClusterChangedEvent createClusterChangedEvent(List<String> existingTemplateNames, DiscoveryNodes nodes) {
        return createClusterChangedEvent(existingTemplateNames, Collections.emptyMap(), nodes);
    }

    private ClusterChangedEvent createClusterChangedEvent(List<String> existingTemplateNames,
                                                          Map<String, LifecyclePolicy> existingPolicies,
                                                          DiscoveryNodes nodes) {
        ClusterState cs = createClusterState(Settings.EMPTY, existingTemplateNames, existingPolicies, nodes);
        ClusterChangedEvent realEvent = new ClusterChangedEvent("created-from-test", cs,
            ClusterState.builder(new ClusterName("test")).build());
        ClusterChangedEvent event = spy(realEvent);
        when(event.localNodeMaster()).thenReturn(nodes.isLocalNodeElectedMaster());

        return event;
    }

    private ClusterState createClusterState(Settings nodeSettings,
                                            List<String> existingTemplateNames,
                                            Map<String, LifecyclePolicy> existingPolicies,
                                            DiscoveryNodes nodes) {
        ImmutableOpenMap.Builder<String, IndexTemplateMetaData> indexTemplates = ImmutableOpenMap.builder();
        for (String name : existingTemplateNames) {
            indexTemplates.put(name, mock(IndexTemplateMetaData.class));
        }

        Map<String, LifecyclePolicyMetadata> existingILMMeta = existingPolicies.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new LifecyclePolicyMetadata(e.getValue(), Collections.emptyMap(), 1, 1)));
        IndexLifecycleMetadata ilmMeta = new IndexLifecycleMetadata(existingILMMeta, OperationMode.RUNNING);

        return ClusterState.builder(new ClusterName("test"))
            .metaData(MetaData.builder()
                .templates(indexTemplates.build())
                .transientSettings(nodeSettings)
                .putCustom(IndexLifecycleMetadata.TYPE, ilmMeta)
                .build())
            .blocks(new ClusterBlocks.Builder().build())
            .nodes(nodes)
            .build();
    }

    private static class TestPutIndexTemplateResponse extends AcknowledgedResponse {
        TestPutIndexTemplateResponse(boolean acknowledged) {
            super(acknowledged);
        }
    }
}
