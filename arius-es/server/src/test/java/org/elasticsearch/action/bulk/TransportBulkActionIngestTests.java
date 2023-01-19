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

package org.elasticsearch.action.bulk;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.AutoCreateIndex;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateApplier;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.AtomicArray;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.ingest.IngestService;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportResponseHandler;
import org.elasticsearch.transport.TransportService;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TransportBulkActionIngestTests extends ESTestCase {

    /**
     * Index for which mock settings contain a default pipeline.
     */
    private static final String WITH_DEFAULT_PIPELINE = "index_with_default_pipeline";
    private static final String WITH_DEFAULT_PIPELINE_ALIAS = "alias_for_index_with_default_pipeline";

    private static final Settings SETTINGS =
        Settings.builder().put(AutoCreateIndex.AUTO_CREATE_INDEX_SETTING.getKey(), true).build();

    private static final Thread DUMMY_WRITE_THREAD = new Thread(ThreadPool.Names.WRITE);

    /** Services needed by bulk action */
    TransportService transportService;
    ClusterService clusterService;
    IngestService ingestService;
    ThreadPool threadPool;

    /** Arguments to callbacks we want to capture, but which require generics, so we must use @Captor */
    @Captor
    ArgumentCaptor<BiConsumer<Integer, Exception>> failureHandler;
    @Captor
    ArgumentCaptor<BiConsumer<Thread, Exception>> completionHandler;
    @Captor
    ArgumentCaptor<TransportResponseHandler<BulkResponse>> remoteResponseHandler;
    @Captor
    ArgumentCaptor<Iterable<DocWriteRequest<?>>> bulkDocsItr;

    /** The actual action we want to test, with real indexing mocked */
    TestTransportBulkAction action;

    /** Single item bulk write action that wraps index requests */
    TestSingleItemBulkWriteAction singleItemBulkWriteAction;

    /** True if the next call to the index action should act as an ingest node */
    boolean localIngest;

    /** The nodes that forwarded index requests should be cycled through. */
    DiscoveryNodes nodes;
    DiscoveryNode remoteNode1;
    DiscoveryNode remoteNode2;

    /** A subclass of the real bulk action to allow skipping real bulk indexing, and marking when it would have happened. */
    class TestTransportBulkAction extends TransportBulkAction {
        boolean isExecuted = false; // set when the "real" bulk execution happens

        boolean needToCheck; // pluggable return value for `needToCheck`

        boolean indexCreated = true; // If set to false, will be set to true by call to createIndex

        TestTransportBulkAction() {
            super(threadPool, transportService, clusterService, ingestService,
                null, null, new ActionFilters(Collections.emptySet()), null,
                new AutoCreateIndex(
                    SETTINGS, new ClusterSettings(SETTINGS, ClusterSettings.BUILT_IN_CLUSTER_SETTINGS),
                    new IndexNameExpressionResolver()
                )
            );
        }
        @Override
        protected boolean needToCheck() {
            return needToCheck;
        }
        @Override
        void executeBulk(Task task, final BulkRequest bulkRequest, final IndexTimeProvider timeProvider, final ActionListener<BulkResponse> listener,
                final AtomicArray<BulkItemResponse> responses, Map<String, IndexNotFoundException> indicesThatCannotBeCreated) {
            assertTrue(indexCreated);
            isExecuted = true;
        }

        @Override
        void createIndex(String index, TimeValue timeout, ActionListener<CreateIndexResponse> listener) {
            indexCreated = true;
            listener.onResponse(null);
        }
    }

    class TestSingleItemBulkWriteAction extends TransportSingleItemBulkWriteAction<IndexRequest, IndexResponse> {

        TestSingleItemBulkWriteAction(TestTransportBulkAction bulkAction) {
            super(IndexAction.NAME, TransportBulkActionIngestTests.this.transportService,
                    new ActionFilters(Collections.emptySet()), IndexRequest::new, bulkAction);
        }
    }

    @Before
    public void setupAction() {
        // initialize captors, which must be members to use @Capture because of generics
        threadPool = mock(ThreadPool.class);
        final ExecutorService direct = EsExecutors.newDirectExecutorService();
        when(threadPool.executor(anyString())).thenReturn(direct);
        MockitoAnnotations.initMocks(this);
        // setup services that will be called by action
        transportService = mock(TransportService.class);
        clusterService = mock(ClusterService.class);
        localIngest = true;
        // setup nodes for local and remote
        DiscoveryNode localNode = mock(DiscoveryNode.class);
        when(localNode.isIngestNode()).thenAnswer(stub -> localIngest);
        when(clusterService.localNode()).thenReturn(localNode);
        remoteNode1 = mock(DiscoveryNode.class);
        remoteNode2 = mock(DiscoveryNode.class);
        nodes = mock(DiscoveryNodes.class);
        ImmutableOpenMap<String, DiscoveryNode> ingestNodes = ImmutableOpenMap.<String, DiscoveryNode>builder(2)
            .fPut("node1", remoteNode1).fPut("node2", remoteNode2).build();
        when(nodes.getIngestNodes()).thenReturn(ingestNodes);
        when(nodes.getMinNodeVersion()).thenReturn(Version.CURRENT);
        ClusterState state = mock(ClusterState.class);
        when(state.getNodes()).thenReturn(nodes);
        MetaData metaData = MetaData.builder().indices(ImmutableOpenMap.<String, IndexMetaData>builder()
            .putAll(
                Collections.singletonMap(
                    WITH_DEFAULT_PIPELINE,
                    IndexMetaData.builder(WITH_DEFAULT_PIPELINE).settings(
                        settings(Version.CURRENT).put(IndexSettings.DEFAULT_PIPELINE.getKey(), "default_pipeline")
                            .build()
                    ).putAlias(AliasMetaData.builder(WITH_DEFAULT_PIPELINE_ALIAS).build()).numberOfShards(1).numberOfReplicas(1).build()))
            .build()).build();
        when(state.getMetaData()).thenReturn(metaData);
        when(state.metaData()).thenReturn(metaData);
        when(clusterService.state()).thenReturn(state);
        doAnswer(invocation -> {
            ClusterChangedEvent event = mock(ClusterChangedEvent.class);
            when(event.state()).thenReturn(state);
            ((ClusterStateApplier)invocation.getArguments()[0]).applyClusterState(event);
            return null;
        }).when(clusterService).addStateApplier(any(ClusterStateApplier.class));
        // setup the mocked ingest service for capturing calls
        ingestService = mock(IngestService.class);
        action = new TestTransportBulkAction();
        singleItemBulkWriteAction = new TestSingleItemBulkWriteAction(action);
        reset(transportService); // call on construction of action
    }

    public void testIngestSkipped() throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        IndexRequest indexRequest = new IndexRequest("index", "type", "id");
        indexRequest.source(Collections.emptyMap());
        bulkRequest.add(indexRequest);
        action.execute(null, bulkRequest, ActionListener.wrap(response -> {}, exception -> {
            throw new AssertionError(exception);
        }));
        assertTrue(action.isExecuted);
        verifyZeroInteractions(ingestService);
    }

    public void testSingleItemBulkActionIngestSkipped() throws Exception {
        IndexRequest indexRequest = new IndexRequest("index", "type", "id");
        indexRequest.source(Collections.emptyMap());
        singleItemBulkWriteAction.execute(null, indexRequest, ActionListener.wrap(response -> {}, exception -> {
            throw new AssertionError(exception);
        }));
        assertTrue(action.isExecuted);
        verifyZeroInteractions(ingestService);
    }

    public void testIngestLocal() throws Exception {
        Exception exception = new Exception("fake exception");
        BulkRequest bulkRequest = new BulkRequest();
        IndexRequest indexRequest1 = new IndexRequest("index", "type", "id");
        indexRequest1.source(Collections.emptyMap());
        indexRequest1.setPipeline("testpipeline");
        IndexRequest indexRequest2 = new IndexRequest("index", "type", "id");
        indexRequest2.source(Collections.emptyMap());
        indexRequest2.setPipeline("testpipeline");
        bulkRequest.add(indexRequest1);
        bulkRequest.add(indexRequest2);

        AtomicBoolean responseCalled = new AtomicBoolean(false);
        AtomicBoolean failureCalled = new AtomicBoolean(false);
        action.execute(null, bulkRequest, ActionListener.wrap(
            response -> {
                BulkItemResponse itemResponse = response.iterator().next();
                assertThat(itemResponse.getFailure().getMessage(), containsString("fake exception"));
                responseCalled.set(true);
            },
            e -> {
                assertThat(e, sameInstance(exception));
                failureCalled.set(true);
            }));

        // check failure works, and passes through to the listener
        assertFalse(action.isExecuted); // haven't executed yet
        assertFalse(responseCalled.get());
        assertFalse(failureCalled.get());
        verify(ingestService).executeBulkRequest(eq(bulkRequest.numberOfActions()), bulkDocsItr.capture(),
            failureHandler.capture(), completionHandler.capture(), any());
        completionHandler.getValue().accept(null, exception);
        assertTrue(failureCalled.get());

        // now check success
        Iterator<DocWriteRequest<?>> req = bulkDocsItr.getValue().iterator();
        failureHandler.getValue().accept(0, exception); // have an exception for our one index request
        indexRequest2.setPipeline(IngestService.NOOP_PIPELINE_NAME); // this is done by the real pipeline execution service when processing
        completionHandler.getValue().accept(DUMMY_WRITE_THREAD, null);
        assertTrue(action.isExecuted);
        assertFalse(responseCalled.get()); // listener would only be called by real index action, not our mocked one
        verifyZeroInteractions(transportService);
    }

    public void testSingleItemBulkActionIngestLocal() throws Exception {
        Exception exception = new Exception("fake exception");
        IndexRequest indexRequest = new IndexRequest("index", "type", "id");
        indexRequest.source(Collections.emptyMap());
        indexRequest.setPipeline("testpipeline");
        AtomicBoolean responseCalled = new AtomicBoolean(false);
        AtomicBoolean failureCalled = new AtomicBoolean(false);
        singleItemBulkWriteAction.execute(null, indexRequest, ActionListener.wrap(
                response -> {
                    responseCalled.set(true);
                },
                e -> {
                    assertThat(e, sameInstance(exception));
                    failureCalled.set(true);
                }));

        // check failure works, and passes through to the listener
        assertFalse(action.isExecuted); // haven't executed yet
        assertFalse(responseCalled.get());
        assertFalse(failureCalled.get());
        verify(ingestService).executeBulkRequest(eq(1), bulkDocsItr.capture(), failureHandler.capture(),
            completionHandler.capture(), any());
        completionHandler.getValue().accept(null, exception);
        assertTrue(failureCalled.get());

        // now check success
        indexRequest.setPipeline(IngestService.NOOP_PIPELINE_NAME); // this is done by the real pipeline execution service when processing
        completionHandler.getValue().accept(DUMMY_WRITE_THREAD, null);
        assertTrue(action.isExecuted);
        assertFalse(responseCalled.get()); // listener would only be called by real index action, not our mocked one
        verifyZeroInteractions(transportService);
    }

    public void testIngestForward() throws Exception {
        localIngest = false;
        BulkRequest bulkRequest = new BulkRequest();
        IndexRequest indexRequest = new IndexRequest("index", "type", "id");
        indexRequest.source(Collections.emptyMap());
        indexRequest.setPipeline("testpipeline");
        bulkRequest.add(indexRequest);
        BulkResponse bulkResponse = mock(BulkResponse.class);
        AtomicBoolean responseCalled = new AtomicBoolean(false);
        ActionListener<BulkResponse> listener = ActionListener.wrap(
            response -> {
                responseCalled.set(true);
                assertSame(bulkResponse, response);
            },
            e -> {
                throw new AssertionError(e);
            });
        action.execute(null, bulkRequest, listener);

        // should not have executed ingest locally
        verify(ingestService, never()).executeBulkRequest(anyInt(), any(), any(), any(), any());
        // but instead should have sent to a remote node with the transport service
        ArgumentCaptor<DiscoveryNode> node = ArgumentCaptor.forClass(DiscoveryNode.class);
        verify(transportService).sendRequest(node.capture(), eq(BulkAction.NAME), any(), remoteResponseHandler.capture());
        boolean usedNode1 = node.getValue() == remoteNode1; // make sure we used one of the nodes
        if (usedNode1 == false) {
            assertSame(remoteNode2, node.getValue());
        }
        assertFalse(action.isExecuted); // no local index execution
        assertFalse(responseCalled.get()); // listener not called yet

        remoteResponseHandler.getValue().handleResponse(bulkResponse); // call the listener for the remote node
        assertTrue(responseCalled.get()); // now the listener we passed should have been delegated to by the remote listener
        assertFalse(action.isExecuted); // still no local index execution

        // now make sure ingest nodes are rotated through with a subsequent request
        reset(transportService);
        action.execute(null, bulkRequest, listener);
        verify(transportService).sendRequest(node.capture(), eq(BulkAction.NAME), any(), remoteResponseHandler.capture());
        if (usedNode1) {
            assertSame(remoteNode2, node.getValue());
        } else {
            assertSame(remoteNode1, node.getValue());
        }
    }

    public void testSingleItemBulkActionIngestForward() throws Exception {
        localIngest = false;
        IndexRequest indexRequest = new IndexRequest("index", "type", "id");
        indexRequest.source(Collections.emptyMap());
        indexRequest.setPipeline("testpipeline");
        IndexResponse indexResponse = mock(IndexResponse.class);
        AtomicBoolean responseCalled = new AtomicBoolean(false);
        ActionListener<IndexResponse> listener = ActionListener.wrap(
                response -> {
                    responseCalled.set(true);
                    assertSame(indexResponse, response);
                },
                e -> {
                    throw new AssertionError(e);
                });
        singleItemBulkWriteAction.execute(null, indexRequest, listener);

        // should not have executed ingest locally
        verify(ingestService, never()).executeBulkRequest(anyInt(), any(), any(), any(), any());
        // but instead should have sent to a remote node with the transport service
        ArgumentCaptor<DiscoveryNode> node = ArgumentCaptor.forClass(DiscoveryNode.class);
        verify(transportService).sendRequest(node.capture(), eq(BulkAction.NAME), any(), remoteResponseHandler.capture());
        boolean usedNode1 = node.getValue() == remoteNode1; // make sure we used one of the nodes
        if (usedNode1 == false) {
            assertSame(remoteNode2, node.getValue());
        }
        assertFalse(action.isExecuted); // no local index execution
        assertFalse(responseCalled.get()); // listener not called yet

        BulkItemResponse itemResponse = new BulkItemResponse(0, DocWriteRequest.OpType.CREATE, indexResponse);
        BulkItemResponse[] bulkItemResponses = new BulkItemResponse[1];
        bulkItemResponses[0] = itemResponse;
        remoteResponseHandler.getValue().handleResponse(new BulkResponse(bulkItemResponses, 0)); // call the listener for the remote node
        assertTrue(responseCalled.get()); // now the listener we passed should have been delegated to by the remote listener
        assertFalse(action.isExecuted); // still no local index execution

        // now make sure ingest nodes are rotated through with a subsequent request
        reset(transportService);
        singleItemBulkWriteAction.execute(null, indexRequest, listener);
        verify(transportService).sendRequest(node.capture(), eq(BulkAction.NAME), any(), remoteResponseHandler.capture());
        if (usedNode1) {
            assertSame(remoteNode2, node.getValue());
        } else {
            assertSame(remoteNode1, node.getValue());
        }
    }

    public void testUseDefaultPipeline() throws Exception {
        validateDefaultPipeline(new IndexRequest(WITH_DEFAULT_PIPELINE, "type", "id"));
    }

    public void testUseDefaultPipelineWithAlias() throws Exception {
        validateDefaultPipeline(new IndexRequest(WITH_DEFAULT_PIPELINE_ALIAS, "type", "id"));
    }

    public void testUseDefaultPipelineWithBulkUpsert() throws Exception {
        String indexRequestName = randomFrom(new String[]{null, WITH_DEFAULT_PIPELINE, WITH_DEFAULT_PIPELINE_ALIAS});
        validatePipelineWithBulkUpsert(indexRequestName, WITH_DEFAULT_PIPELINE);
    }

    public void testUseDefaultPipelineWithBulkUpsertWithAlias() throws Exception {
        String indexRequestName = randomFrom(new String[]{null, WITH_DEFAULT_PIPELINE, WITH_DEFAULT_PIPELINE_ALIAS});
        validatePipelineWithBulkUpsert(indexRequestName, WITH_DEFAULT_PIPELINE_ALIAS);
    }

    private void validatePipelineWithBulkUpsert(@Nullable String indexRequestIndexName, String updateRequestIndexName) throws Exception {
        Exception exception = new Exception("fake exception");
        BulkRequest bulkRequest = new BulkRequest();
        IndexRequest indexRequest1 = new IndexRequest(indexRequestIndexName, "type", "id1").source(Collections.emptyMap());
        IndexRequest indexRequest2 = new IndexRequest(indexRequestIndexName, "type", "id2").source(Collections.emptyMap());
        IndexRequest indexRequest3 = new IndexRequest(indexRequestIndexName, "type", "id3").source(Collections.emptyMap());
        UpdateRequest upsertRequest = new UpdateRequest(updateRequestIndexName, "type", "id1")
            .upsert(indexRequest1).script(mockScript("1"));
        UpdateRequest docAsUpsertRequest = new UpdateRequest(updateRequestIndexName, "type", "id2")
            .doc(indexRequest2).docAsUpsert(true);
        // this test only covers the mechanics that scripted bulk upserts will execute a default pipeline. However, in practice scripted
        // bulk upserts with a default pipeline are a bit surprising since the script executes AFTER the pipeline.
        UpdateRequest scriptedUpsert = new UpdateRequest(updateRequestIndexName, "type", "id2")
            .upsert(indexRequest3).script(mockScript("1"))
            .scriptedUpsert(true);
        bulkRequest.add(upsertRequest).add(docAsUpsertRequest).add(scriptedUpsert);

        AtomicBoolean responseCalled = new AtomicBoolean(false);
        AtomicBoolean failureCalled = new AtomicBoolean(false);
        assertNull(indexRequest1.getPipeline());
        assertNull(indexRequest2.getPipeline());
        assertNull(indexRequest3.getPipeline());
        action.execute(null, bulkRequest, ActionListener.wrap(
            response -> {
                BulkItemResponse itemResponse = response.iterator().next();
                assertThat(itemResponse.getFailure().getMessage(), containsString("fake exception"));
                responseCalled.set(true);
            },
            e -> {
                assertThat(e, sameInstance(exception));
                failureCalled.set(true);
            }));

        // check failure works, and passes through to the listener
        assertFalse(action.isExecuted); // haven't executed yet
        assertFalse(responseCalled.get());
        assertFalse(failureCalled.get());
        verify(ingestService).executeBulkRequest(eq(bulkRequest.numberOfActions()), bulkDocsItr.capture(),
            failureHandler.capture(), completionHandler.capture(), any());
        assertEquals(indexRequest1.getPipeline(), "default_pipeline");
        assertEquals(indexRequest2.getPipeline(), "default_pipeline");
        assertEquals(indexRequest3.getPipeline(), "default_pipeline");
        completionHandler.getValue().accept(null, exception);
        assertTrue(failureCalled.get());

        // now check success of the transport bulk action
        indexRequest1.setPipeline(IngestService.NOOP_PIPELINE_NAME); // this is done by the real pipeline execution service when processing
        indexRequest2.setPipeline(IngestService.NOOP_PIPELINE_NAME); // this is done by the real pipeline execution service when processing
        indexRequest3.setPipeline(IngestService.NOOP_PIPELINE_NAME); // this is done by the real pipeline execution service when processing
        completionHandler.getValue().accept(DUMMY_WRITE_THREAD, null);
        assertTrue(action.isExecuted);
        assertFalse(responseCalled.get()); // listener would only be called by real index action, not our mocked one
        verifyZeroInteractions(transportService);
    }

    public void testDoExecuteCalledTwiceCorrectly() throws Exception {
        Exception exception = new Exception("fake exception");
        IndexRequest indexRequest = new IndexRequest("missing_index", "type", "id");
        indexRequest.setPipeline("testpipeline");
        indexRequest.source(Collections.emptyMap());
        AtomicBoolean responseCalled = new AtomicBoolean(false);
        AtomicBoolean failureCalled = new AtomicBoolean(false);
        action.needToCheck = true;
        action.indexCreated = false;
        singleItemBulkWriteAction.execute(null, indexRequest, ActionListener.wrap(
            response -> responseCalled.set(true),
            e -> {
                assertThat(e, sameInstance(exception));
                failureCalled.set(true);
            }));

        // check failure works, and passes through to the listener
        assertFalse(action.isExecuted); // haven't executed yet
        assertFalse(action.indexCreated); // no index yet
        assertFalse(responseCalled.get());
        assertFalse(failureCalled.get());
        verify(ingestService).executeBulkRequest(eq(1), bulkDocsItr.capture(), failureHandler.capture(),
            completionHandler.capture(), any());
        completionHandler.getValue().accept(null, exception);
        assertFalse(action.indexCreated); // still no index yet, the ingest node failed.
        assertTrue(failureCalled.get());

        // now check success
        indexRequest.setPipeline(IngestService.NOOP_PIPELINE_NAME); // this is done by the real pipeline execution service when processing
        completionHandler.getValue().accept(DUMMY_WRITE_THREAD, null);
        assertTrue(action.isExecuted);
        assertTrue(action.indexCreated); // now the index is created since we skipped the ingest node path.
        assertFalse(responseCalled.get()); // listener would only be called by real index action, not our mocked one
        verifyZeroInteractions(transportService);
    }

    public void testNotFindDefaultPipelineFromTemplateMatches(){
        Exception exception = new Exception("fake exception");
        IndexRequest indexRequest = new IndexRequest("missing_index", "type", "id");
        indexRequest.source(Collections.emptyMap());
        AtomicBoolean responseCalled = new AtomicBoolean(false);
        AtomicBoolean failureCalled = new AtomicBoolean(false);
        singleItemBulkWriteAction.execute(null, indexRequest, ActionListener.wrap(
            response -> responseCalled.set(true),
            e -> {
                assertThat(e, sameInstance(exception));
                failureCalled.set(true);
            }));
        assertEquals(IngestService.NOOP_PIPELINE_NAME, indexRequest.getPipeline());
        verifyZeroInteractions(ingestService);

    }

    @Ignore
    public void testFindDefaultPipelineFromTemplateMatch(){
        Exception exception = new Exception("fake exception");
        ClusterState state = clusterService.state();

        ImmutableOpenMap.Builder<String, IndexTemplateMetaData> templateMetaDataBuilder = ImmutableOpenMap.builder();
        templateMetaDataBuilder.put("template1", IndexTemplateMetaData.builder("template1").patterns(Arrays.asList("missing_index"))
            .order(1).settings(Settings.builder().put(IndexSettings.DEFAULT_PIPELINE.getKey(), "pipeline1").build()).build());
        templateMetaDataBuilder.put("template2", IndexTemplateMetaData.builder("template2").patterns(Arrays.asList("missing_*"))
            .order(2).settings(Settings.builder().put(IndexSettings.DEFAULT_PIPELINE.getKey(), "pipeline2").build()).build());
        templateMetaDataBuilder.put("template3", IndexTemplateMetaData.builder("template3").patterns(Arrays.asList("missing*"))
            .order(3).build());
        templateMetaDataBuilder.put("template4", IndexTemplateMetaData.builder("template4").patterns(Arrays.asList("nope"))
            .order(4).settings(Settings.builder().put(IndexSettings.DEFAULT_PIPELINE.getKey(), "pipeline4").build()).build());

        MetaData metaData = mock(MetaData.class);
        when(state.metaData()).thenReturn(metaData);
        when(state.getMetaData()).thenReturn(metaData);
        when(metaData.templates()).thenReturn(templateMetaDataBuilder.build());
        when(metaData.getTemplates()).thenReturn(templateMetaDataBuilder.build());
        when(metaData.indices()).thenReturn(ImmutableOpenMap.of());

        IndexRequest indexRequest = new IndexRequest("missing_index", "type", "id");
        indexRequest.source(Collections.emptyMap());
        AtomicBoolean responseCalled = new AtomicBoolean(false);
        AtomicBoolean failureCalled = new AtomicBoolean(false);
        singleItemBulkWriteAction.execute(null, indexRequest, ActionListener.wrap(
            response -> responseCalled.set(true),
            e -> {
                assertThat(e, sameInstance(exception));
                failureCalled.set(true);
            }));

        assertEquals("pipeline2", indexRequest.getPipeline());
        verify(ingestService).executeBulkRequest(eq(1), bulkDocsItr.capture(), failureHandler.capture(),
            completionHandler.capture(), any());
    }

    private void validateDefaultPipeline(IndexRequest indexRequest) {
        Exception exception = new Exception("fake exception");
        indexRequest.source(Collections.emptyMap());
        AtomicBoolean responseCalled = new AtomicBoolean(false);
        AtomicBoolean failureCalled = new AtomicBoolean(false);
        assertNull(indexRequest.getPipeline());
        singleItemBulkWriteAction.execute(null, indexRequest, ActionListener.wrap(
            response -> {
                responseCalled.set(true);
            },
            e -> {
                assertThat(e, sameInstance(exception));
                failureCalled.set(true);
            }));

        // check failure works, and passes through to the listener
        assertFalse(action.isExecuted); // haven't executed yet
        assertFalse(responseCalled.get());
        assertFalse(failureCalled.get());
        verify(ingestService).executeBulkRequest(eq(1), bulkDocsItr.capture(), failureHandler.capture(),
            completionHandler.capture(), any());
        assertEquals(indexRequest.getPipeline(), "default_pipeline");
        completionHandler.getValue().accept(null, exception);
        assertTrue(failureCalled.get());

        // now check success
        indexRequest.setPipeline(IngestService.NOOP_PIPELINE_NAME); // this is done by the real pipeline execution service when processing
        completionHandler.getValue().accept(DUMMY_WRITE_THREAD, null);
        assertTrue(action.isExecuted);
        assertFalse(responseCalled.get()); // listener would only be called by real index action, not our mocked one
        verifyZeroInteractions(transportService);
    }
}
