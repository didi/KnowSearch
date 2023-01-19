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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.lucene.util.SparseFixedBitSet;
import org.elasticsearch.Assertions;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.Version;
import org.elasticsearch.action.*;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.ingest.IngestActionForwarder;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.AutoCreateIndex;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.action.update.TransportUpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateObserver;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.AliasOrIndex;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.MetaDataIndexTemplateService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Randomness;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.AbstractRunnable;
import org.elasticsearch.common.util.concurrent.AtomicArray;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndexClosedException;
import org.elasticsearch.ingest.IngestService;
import org.elasticsearch.monitor.request.RequestTracker;
import org.elasticsearch.node.NodeClosedException;
import org.elasticsearch.rest.action.RestActions;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

/**
 * Groups bulk request items by shard, optionally creating non-existent indices and
 * delegates to {@link TransportShardBulkAction} for shard-level bulk execution
 */
public class TransportBulkAction extends HandledTransportAction<BulkRequest, BulkResponse> {

    private final ThreadPool threadPool;
    private final AutoCreateIndex autoCreateIndex;
    private final ClusterService clusterService;
    private final IngestService ingestService;
    private final TransportShardBulkAction shardBulkAction;
    private final LongSupplier relativeTimeProvider;
    private final IngestActionForwarder ingestForwarder;
    private final NodeClient client;
    private final IndexNameExpressionResolver indexNameExpressionResolver;
    private static final String DROPPED_ITEM_WITH_AUTO_GENERATED_ID = "auto-generated";
    private final AtomicInteger routingRandomValue;
    private static final int MAX_START_ROUTING_VALUE = 10000;
    private static final int MAX_ROUTING_VALUE = 99999990;

    @Inject
    public TransportBulkAction(ThreadPool threadPool, TransportService transportService,
                               ClusterService clusterService, IngestService ingestService,
                               TransportShardBulkAction shardBulkAction, NodeClient client,
                               ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
                               AutoCreateIndex autoCreateIndex) {
        this(threadPool, transportService, clusterService, ingestService, shardBulkAction, client, actionFilters,
            indexNameExpressionResolver, autoCreateIndex, System::nanoTime);
    }

    public TransportBulkAction(ThreadPool threadPool, TransportService transportService,
                               ClusterService clusterService, IngestService ingestService,
                               TransportShardBulkAction shardBulkAction, NodeClient client,
                               ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
                               AutoCreateIndex autoCreateIndex, LongSupplier relativeTimeProvider) {
        super(BulkAction.NAME, transportService, actionFilters, BulkRequest::new, ThreadPool.Names.WRITE);
        Objects.requireNonNull(relativeTimeProvider);
        this.threadPool = threadPool;
        this.clusterService = clusterService;
        this.ingestService = ingestService;
        this.shardBulkAction = shardBulkAction;
        this.autoCreateIndex = autoCreateIndex;
        this.relativeTimeProvider = relativeTimeProvider;
        this.ingestForwarder = new IngestActionForwarder(transportService);
        this.client = client;
        this.indexNameExpressionResolver = indexNameExpressionResolver;
        clusterService.addStateApplier(this.ingestForwarder);
        routingRandomValue = new AtomicInteger(Randomness.get().nextInt(MAX_START_ROUTING_VALUE));
    }

    /**
     * Retrieves the {@link IndexRequest} from the provided {@link DocWriteRequest} for index or upsert actions.  Upserts are
     * modeled as {@link IndexRequest} inside the {@link UpdateRequest}. Ignores {@link org.elasticsearch.action.delete.DeleteRequest}'s
     *
     * @param docWriteRequest The request to find the {@link IndexRequest}
     * @return the found {@link IndexRequest} or {@code null} if one can not be found.
     */
    public static IndexRequest getIndexWriteRequest(DocWriteRequest docWriteRequest) {
        IndexRequest indexRequest = null;
        if (docWriteRequest instanceof IndexRequest) {
            indexRequest = (IndexRequest) docWriteRequest;
        } else if (docWriteRequest instanceof UpdateRequest) {
            UpdateRequest updateRequest = (UpdateRequest) docWriteRequest;
            indexRequest = updateRequest.docAsUpsert() ? updateRequest.doc() : updateRequest.upsertRequest();

            if (indexRequest == null) {
                indexRequest = updateRequest.doc();
            }

            if (indexRequest == null) {
                indexRequest = new IndexRequest();
            }

            if (indexRequest.index() == null) {
                indexRequest.index(updateRequest.index());
                indexRequest.type(updateRequest.type());
                indexRequest.id(updateRequest.id());
                indexRequest.routing(updateRequest.routing());
                indexRequest.version(updateRequest.version());
                indexRequest.versionType(updateRequest.versionType());
                indexRequest.setPipeline(updateRequest.getPipeline());
            }
        } else if (docWriteRequest instanceof DeleteRequest) {
            DeleteRequest deleteRequest = (DeleteRequest) docWriteRequest;
            if (deleteRequest.source() != null) {
                if (deleteRequest.getIndexRequest() == null) {
                    indexRequest = new IndexRequest(deleteRequest.index(), deleteRequest.type(), deleteRequest.id());
                    indexRequest.routing(deleteRequest.routing());
                    indexRequest.version(deleteRequest.version());
                    indexRequest.versionType(deleteRequest.versionType());
                    indexRequest.source(deleteRequest.source(), deleteRequest.getContentType());
                    indexRequest.setPipeline(deleteRequest.getPipeline());
                    deleteRequest.setIndexRequest(indexRequest);
                } else {
                    indexRequest = deleteRequest.getIndexRequest();
                }
            }
        }
        return indexRequest;
    }

    @Override
    protected void doExecute(Task task, BulkRequest bulkRequest, ActionListener<BulkResponse> listener) {
        bulkRequest.setLogPipelineId();

        IndexTimeProvider timeProvider = new IndexTimeProvider(task != null ? task.getId() : -1, relativeTime());
        final AtomicArray<BulkItemResponse> responses = new AtomicArray<>(bulkRequest.requests.size());

        boolean hasIndexRequestsWithPipelines = false;
        final MetaData metaData = clusterService.state().getMetaData();
        final Version minNodeVersion = clusterService.state().getNodes().getMinNodeVersion();
        for (DocWriteRequest<?> actionRequest : bulkRequest.requests) {
            IndexRequest indexRequest = getIndexWriteRequest(actionRequest);
            if (indexRequest != null) {
                // Each index request needs to be evaluated, because this method also modifies the IndexRequest
                boolean indexRequestHasPipeline = resolvePipelines(actionRequest, indexRequest, metaData);
                hasIndexRequestsWithPipelines |= indexRequestHasPipeline;
            }

            if (actionRequest instanceof IndexRequest) {
                IndexRequest ir = (IndexRequest) actionRequest;
                ir.checkAutoIdWithOpTypeCreateSupportedByVersion(minNodeVersion);
                if (ir.getAutoGeneratedTimestamp() != IndexRequest.UNSET_AUTO_GENERATED_TIMESTAMP) {
                    throw new IllegalArgumentException("autoGeneratedTimestamp should not be set externally");
                }
            }
        }

        if (hasIndexRequestsWithPipelines) {
            // this method (doExecute) will be called again, but with the bulk requests updated from the ingest node processing but
            // also with IngestService.NOOP_PIPELINE_NAME on each request. This ensures that this on the second time through this method,
            // this path is never taken.
            try {
                if (Assertions.ENABLED) {
                    final boolean arePipelinesResolved = bulkRequest.requests()
                        .stream()
                        .map(TransportBulkAction::getIndexWriteRequest)
                        .filter(Objects::nonNull)
                        .allMatch(IndexRequest::isPipelineResolved);
                    assert arePipelinesResolved : bulkRequest;
                }
                if (clusterService.localNode().isIngestNode()) {
                    processBulkIndexIngestRequest(task, bulkRequest, listener);
                } else {
                    ingestForwarder.forwardIngestRequest(BulkAction.INSTANCE, bulkRequest, listener);
                }
            } catch (Exception e) {
                listener.onFailure(e);
            }
            return;
        }

        if (needToCheck()) {
            // Attempt to create all the indices that we're going to need during the bulk before we start.
            // Step 1: collect all the indices in the request
            final Set<String> indices = bulkRequest.requests.stream()
                    // delete requests should not attempt to create the index (if the index does not
                    // exists), unless an external versioning is used
                .filter(request -> request.opType() != DocWriteRequest.OpType.DELETE
                        || request.versionType() == VersionType.EXTERNAL
                        || request.versionType() == VersionType.EXTERNAL_GTE)
                .map(DocWriteRequest::index)
                .collect(Collectors.toSet());
            /* Step 2: filter that to indices that don't exist and we can create. At the same time build a map of indices we can't create
             * that we'll use when we try to run the requests. */
            final Map<String, IndexNotFoundException> indicesThatCannotBeCreated = new HashMap<>();
            Set<String> autoCreateIndices = new HashSet<>();
            ClusterState state = clusterService.state();
            for (String index : indices) {
                boolean shouldAutoCreate;
                try {
                    shouldAutoCreate = shouldAutoCreate(index, state);
                } catch (IndexNotFoundException e) {
                    shouldAutoCreate = false;
                    indicesThatCannotBeCreated.put(index, e);
                }
                if (shouldAutoCreate) {
                    autoCreateIndices.add(index);
                }
            }
            // Step 3: create all the indices that are missing, if there are any missing. start the bulk after all the creates come back.
            if (autoCreateIndices.isEmpty()) {
                executeBulk(task, bulkRequest, timeProvider, listener, responses, indicesThatCannotBeCreated);
            } else {
                final AtomicInteger counter = new AtomicInteger(autoCreateIndices.size());
                for (String index : autoCreateIndices) {
                    createIndex(index, bulkRequest.timeout(), new ActionListener<CreateIndexResponse>() {
                        @Override
                        public void onResponse(CreateIndexResponse result) {
                            if (counter.decrementAndGet() == 0) {
                                threadPool.executor(ThreadPool.Names.WRITE).execute(
                                    () -> executeBulk(task, bulkRequest, timeProvider, listener, responses, indicesThatCannotBeCreated));
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (!(ExceptionsHelper.unwrapCause(e) instanceof ResourceAlreadyExistsException)) {
                                // fail all requests involving this index, if create didn't work
                                for (int i = 0; i < bulkRequest.requests.size(); i++) {
                                    DocWriteRequest<?> request = bulkRequest.requests.get(i);
                                    if (request != null && setResponseFailureIfIndexMatches(responses, i, request, index, e)) {
                                        bulkRequest.requests.set(i, null);
                                    }
                                }
                            }
                            if (counter.decrementAndGet() == 0) {
                                executeBulk(task, bulkRequest, timeProvider, ActionListener.wrap(listener::onResponse, inner -> {
                                    inner.addSuppressed(e);
                                    listener.onFailure(inner);
                                }), responses, indicesThatCannotBeCreated);
                            }
                        }
                    });
                }
            }
        } else {
            executeBulk(task, bulkRequest, timeProvider, listener, responses, emptyMap());
        }
    }

    static boolean resolvePipelines(final DocWriteRequest<?> originalRequest, final IndexRequest indexRequest, final MetaData metaData) {
        if (indexRequest.isPipelineResolved() == false) {
            final String requestPipeline = indexRequest.getPipeline();
            indexRequest.setPipeline(IngestService.NOOP_PIPELINE_NAME);
            indexRequest.setFinalPipeline(IngestService.NOOP_PIPELINE_NAME);
            String defaultPipeline = null;
            String finalPipeline = null;
            // start to look for default or final pipelines via settings found in the index meta data
            IndexMetaData indexMetaData = metaData.indices().get(originalRequest.index());
            // check the alias for the index request (this is how normal index requests are modeled)
            if (indexMetaData == null && indexRequest.index() != null) {
                AliasOrIndex indexOrAlias = metaData.getAliasAndIndexLookup().get(indexRequest.index());
                if (indexOrAlias != null && indexOrAlias.isAlias()) {
                    AliasOrIndex.Alias alias = (AliasOrIndex.Alias) indexOrAlias;
                    indexMetaData = alias.getWriteIndex();
                }
            }
            // check the alias for the action request (this is how upserts are modeled)
            if (indexMetaData == null && originalRequest.index() != null) {
                AliasOrIndex indexOrAlias = metaData.getAliasAndIndexLookup().get(originalRequest.index());
                if (indexOrAlias != null && indexOrAlias.isAlias()) {
                    AliasOrIndex.Alias alias = (AliasOrIndex.Alias) indexOrAlias;
                    indexMetaData = alias.getWriteIndex();
                }
            }
            if (indexMetaData != null) {
                final Settings indexSettings = indexMetaData.getSettings();
                if (IndexSettings.DEFAULT_PIPELINE.exists(indexSettings)) {
                    // find the default pipeline if one is defined from an existing index setting
                    defaultPipeline = IndexSettings.DEFAULT_PIPELINE.get(indexSettings);
                    indexRequest.setPipeline(defaultPipeline);
                }
                if (IndexSettings.FINAL_PIPELINE.exists(indexSettings)) {
                    // find the final pipeline if one is defined from an existing index setting
                    finalPipeline = IndexSettings.FINAL_PIPELINE.get(indexSettings);
                    indexRequest.setFinalPipeline(finalPipeline);
                }
            } else if (indexRequest.index() != null && Strings.isNullOrEmpty(requestPipeline)) {
                // the index does not exist yet (and this is a valid request), so match index templates to look for pipelines
                List<IndexTemplateMetaData> templates = MetaDataIndexTemplateService.findTemplates(metaData, indexRequest.index());
                assert (templates != null);
                // order of templates are highest order first
                for (final IndexTemplateMetaData template : templates) {
                    final Settings settings = template.settings();
                    if (defaultPipeline == null && IndexSettings.DEFAULT_PIPELINE.exists(settings)) {
                        defaultPipeline = IndexSettings.DEFAULT_PIPELINE.get(settings);
                        // we can not break in case a lower-order template has a final pipeline that we need to collect
                    }
                    if (finalPipeline == null && IndexSettings.FINAL_PIPELINE.exists(settings)) {
                        finalPipeline = IndexSettings.FINAL_PIPELINE.get(settings);
                        // we can not break in case a lower-order template has a default pipeline that we need to collect
                    }
                    if (defaultPipeline != null && finalPipeline != null) {
                        // we can break if we have already collected a default and final pipeline
                        break;
                    }
                }
                indexRequest.setPipeline(defaultPipeline != null ? defaultPipeline : IngestService.NOOP_PIPELINE_NAME);
                indexRequest.setFinalPipeline(finalPipeline != null ? finalPipeline : IngestService.NOOP_PIPELINE_NAME);
            }

            if (requestPipeline != null) {
                indexRequest.setPipeline(requestPipeline);
            }

            /*
             * We have to track whether or not the pipeline for this request has already been resolved. It can happen that the
             * pipeline for this request has already been derived yet we execute this loop again. That occurs if the bulk request
             * has been forwarded by a non-ingest coordinating node to an ingest node. In this case, the coordinating node will have
             * already resolved the pipeline for this request. It is important that we are able to distinguish this situation as we
             * can not double-resolve the pipeline because we will not be able to distinguish the case of the pipeline having been
             * set from a request pipeline parameter versus having been set by the resolution. We need to be able to distinguish
             * these cases as we need to reject the request if the pipeline was set by a required pipeline and there is a request
             * pipeline parameter too.
             */
            indexRequest.isPipelineResolved(true);
        }


        // return whether this index request has a pipeline
        return IngestService.NOOP_PIPELINE_NAME.equals(indexRequest.getPipeline()) == false
            || IngestService.NOOP_PIPELINE_NAME.equals(indexRequest.getFinalPipeline()) == false;
    }

    boolean needToCheck() {
        return autoCreateIndex.needToCheck();
    }

    boolean shouldAutoCreate(String index, ClusterState state) {
        return autoCreateIndex.shouldAutoCreate(index, state);
    }

    void createIndex(String index, TimeValue timeout, ActionListener<CreateIndexResponse> listener) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest();
        createIndexRequest.index(index);
        createIndexRequest.cause("auto(bulk api)");
        createIndexRequest.masterNodeTimeout(timeout);
        client.admin().indices().create(createIndexRequest, listener);
    }

    private boolean setResponseFailureIfIndexMatches(AtomicArray<BulkItemResponse> responses, int idx, DocWriteRequest<?> request,
                                                     String index, Exception e) {
        if (index.equals(request.index())) {
            responses.set(idx, new BulkItemResponse(idx, request.opType(), new BulkItemResponse.Failure(request.index(), request.type(),
                request.id(), e)));
            return true;
        }
        return false;
    }

    private long buildTookInMillis(BulkRequest bulkRequest, IndexTimeProvider timeProvider, BulkItemResponse[] bulkItemResponses) {
        timeProvider.setEndTime();
        long cost = Math.max(1, timeProvider.getTotalCost());

        RequestTracker.getInstance().logIndexRequest(bulkRequest, timeProvider);
        RequestTracker.getInstance().addIndexErrorLog(bulkRequest.getLogPipelineId(), bulkItemResponses);

        return cost;
    }

    /**
     * retries on retryable cluster blocks, resolves item requests,
     * constructs shard bulk requests and delegates execution to shard bulk action
     * */
    private final class BulkOperation extends ActionRunnable<BulkResponse> {
        private final Task task;
        private final BulkRequest bulkRequest;
        private final AtomicArray<BulkItemResponse> responses;
        private final IndexTimeProvider timeProvider;
        private final ClusterStateObserver observer;
        private final Map<String, IndexNotFoundException> indicesThatCannotBeCreated;

        BulkOperation(Task task, BulkRequest bulkRequest, ActionListener<BulkResponse> listener, AtomicArray<BulkItemResponse> responses,
                IndexTimeProvider timeProvider, Map<String, IndexNotFoundException> indicesThatCannotBeCreated) {
            super(listener);
            this.task = task;
            this.bulkRequest = bulkRequest;
            this.responses = responses;
            this.timeProvider = timeProvider;
            this.indicesThatCannotBeCreated = indicesThatCannotBeCreated;
            this.observer = new ClusterStateObserver(clusterService, bulkRequest.timeout(), logger, threadPool.getThreadContext());
        }

        @Override
        protected void doRun() {
            final ClusterState clusterState = observer.setAndGetObservedState();
            if (handleBlockExceptions(clusterState)) {
                return;
            }
            final ConcreteIndices concreteIndices = new ConcreteIndices(clusterState, indexNameExpressionResolver);
            MetaData metaData = clusterState.metaData();
            int routingValue = routingRandomValue.incrementAndGet();
            if (routingValue > MAX_ROUTING_VALUE) {
                routingRandomValue.compareAndSet(routingValue, 0);
            }
            for (int i = 0; i < bulkRequest.requests.size(); i++) {
                DocWriteRequest<?> docWriteRequest = bulkRequest.requests.get(i);
                //the request can only be null because we set it to null in the previous step, so it gets ignored
                if (docWriteRequest == null) {
                    continue;
                }
                if (addFailureIfIndexIsUnavailable(docWriteRequest, i, concreteIndices, metaData)) {
                    continue;
                }
                Index concreteIndex = concreteIndices.resolveIfAbsent(docWriteRequest);
                try {
                    switch (docWriteRequest.opType()) {
                        case CREATE:
                        case INDEX:
                            IndexRequest indexRequest = (IndexRequest) docWriteRequest;
                            final IndexMetaData indexMetaData = metaData.index(concreteIndex);
                            MappingMetaData mappingMd = indexMetaData.mappingOrDefault();
                            boolean autoId = indexRequest.id() == null;
                            Version indexCreated = indexMetaData.getCreationVersion();
                            indexRequest.resolveRouting(metaData);
                            boolean useRandomValue = false;
                            if (Strings.isEmpty(indexRequest.id())
                                && Strings.isEmpty(indexRequest.routing())
                                && indexMetaData.getSettings().getAsBoolean(IndexSettings.INDEX_ROUTING_RANDOM, false)) {
                                indexRequest.routing(String.valueOf(routingValue));
                                useRandomValue = true;
                            }
                            indexRequest.process(indexCreated, mappingMd, concreteIndex.getName());

                            if (autoId && !useRandomValue && indexMetaData.isRoutingSize()) {
                                indexRequest.id(
                                    clusterService.operationRouting()
                                        .generateRoutingSizeId(indexMetaData, indexRequest.id(), String.valueOf(routingValue))
                                );
                            }
                            break;
                        case UPDATE:
                            TransportUpdateAction.resolveAndValidateRouting(metaData, concreteIndex.getName(),
                                (UpdateRequest) docWriteRequest);
                            break;
                        case DELETE:
                            docWriteRequest.routing(metaData.resolveWriteIndexRouting(docWriteRequest.routing(), docWriteRequest.index()));
                            // check if routing is required, if so, throw error if routing wasn't specified
                            if (docWriteRequest.routing() == null && metaData.routingRequired(concreteIndex.getName())) {
                                throw new RoutingMissingException(concreteIndex.getName(), docWriteRequest.type(), docWriteRequest.id());
                            }
                            break;
                        default: throw new AssertionError("request type not supported: [" + docWriteRequest.opType() + "]");
                    }
                } catch (ElasticsearchParseException | IllegalArgumentException | RoutingMissingException e) {
                    BulkItemResponse.Failure failure = new BulkItemResponse.Failure(concreteIndex.getName(), docWriteRequest.type(),
                        docWriteRequest.id(), e);
                    BulkItemResponse bulkItemResponse = new BulkItemResponse(i, docWriteRequest.opType(), failure);
                    responses.set(i, bulkItemResponse);
                    // make sure the request gets never processed again
                    bulkRequest.requests.set(i, null);
                }
            }

            // first, go over all the requests and create a ShardId -> Operations mapping
            Map<ShardId, List<BulkItemRequest>> requestsByShard = new HashMap<>();
            for (int i = 0; i < bulkRequest.requests.size(); i++) {
                DocWriteRequest<?> request = bulkRequest.requests.get(i);
                if (request == null) {
                    continue;
                }
                String concreteIndex = concreteIndices.getConcreteIndex(request.index()).getName();
                ShardId shardId = clusterService.operationRouting().indexShards(clusterState, concreteIndex, request.id(),
                    request.routing()).shardId();
                List<BulkItemRequest> shardRequests = requestsByShard.computeIfAbsent(shardId, shard -> new ArrayList<>());
                shardRequests.add(new BulkItemRequest(i, request));
            }

            if (requestsByShard.isEmpty()) {
                BulkItemResponse[] bulkItemResponses = responses.toArray(new BulkItemResponse[responses.length()]);
                listener.onResponse(new BulkResponse(bulkItemResponses, buildTookInMillis(bulkRequest, timeProvider, bulkItemResponses)));
                return;
            }

            final AtomicInteger counter = new AtomicInteger(requestsByShard.size());
            String nodeId = clusterService.localNode().getId();
            for (Map.Entry<ShardId, List<BulkItemRequest>> entry : requestsByShard.entrySet()) {
                final ShardId shardId = entry.getKey();
                final List<BulkItemRequest> requests = entry.getValue();
                BulkShardRequest bulkShardRequest = new BulkShardRequest(shardId, bulkRequest.getRefreshPolicy(),
                        requests.toArray(new BulkItemRequest[requests.size()]));
                bulkShardRequest.waitForActiveShards(bulkRequest.waitForActiveShards());
                bulkShardRequest.timeout(bulkRequest.timeout());
                bulkShardRequest.routedBasedOnClusterVersion(clusterState.version());
                if (task != null) {
                    bulkShardRequest.setParentTask(nodeId, task.getId());
                }
                shardBulkAction.execute(bulkShardRequest, new ActionListener<BulkShardResponse>() {
                    @Override
                    public void onResponse(BulkShardResponse bulkShardResponse) {
                        timeProvider.putShardCost(shardId, relativeTime() - timeProvider.getIngestTime(), bulkShardResponse.getResponses().length);


                        for (BulkItemResponse bulkItemResponse : bulkShardResponse.getResponses()) {
                            // we may have no response if item failed
                            if (bulkItemResponse.getResponse() != null) {
                                bulkItemResponse.getResponse().setShardInfo(bulkShardResponse.getShardInfo());
                            }
                            responses.set(bulkItemResponse.getItemId(), bulkItemResponse);
                        }
                        if (counter.decrementAndGet() == 0) {
                            finishHim();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        timeProvider.incrFailedShards();;
                        timeProvider.addFailedCount(requests.size());

                        // create failures for all relevant requests
                        for (BulkItemRequest request : requests) {
                            final String indexName = concreteIndices.getConcreteIndex(request.index()).getName();
                            DocWriteRequest<?> docWriteRequest = request.request();
                            responses.set(request.id(), new BulkItemResponse(request.id(), docWriteRequest.opType(),
                                    new BulkItemResponse.Failure(indexName, docWriteRequest.type(), docWriteRequest.id(), e)));
                        }
                        if (counter.decrementAndGet() == 0) {
                            finishHim();
                        }
                    }

                    private void finishHim() {
                        BulkItemResponse[] bulkItemResponses = responses.toArray(new BulkItemResponse[responses.length()]);
                        listener.onResponse(new BulkResponse(bulkItemResponses, buildTookInMillis(bulkRequest, timeProvider, bulkItemResponses)));
                    }
                });
            }
        }

        private boolean handleBlockExceptions(ClusterState state) {
            ClusterBlockException blockException = state.blocks().globalBlockedException(ClusterBlockLevel.WRITE);
            if (blockException != null) {
                if (blockException.retryable()) {
                    logger.trace("cluster is blocked, scheduling a retry", blockException);
                    retry(blockException);
                } else {
                    onFailure(blockException);
                }
                return true;
            }
            return false;
        }

        void retry(Exception failure) {
            assert failure != null;
            if (observer.isTimedOut()) {
                // we running as a last attempt after a timeout has happened. don't retry
                onFailure(failure);
                return;
            }
            observer.waitForNextChange(new ClusterStateObserver.Listener() {
                @Override
                public void onNewClusterState(ClusterState state) {
                    run();
                }

                @Override
                public void onClusterServiceClose() {
                    onFailure(new NodeClosedException(clusterService.localNode()));
                }

                @Override
                public void onTimeout(TimeValue timeout) {
                    // Try one more time...
                    run();
                }
            });
        }

        private boolean addFailureIfIndexIsUnavailable(DocWriteRequest<?> request, int idx, final ConcreteIndices concreteIndices,
                final MetaData metaData) {
            IndexNotFoundException cannotCreate = indicesThatCannotBeCreated.get(request.index());
            if (cannotCreate != null) {
                addFailure(request, idx, cannotCreate);
                return true;
            }
            Index concreteIndex = concreteIndices.getConcreteIndex(request.index());
            if (concreteIndex == null) {
                try {
                    concreteIndex = concreteIndices.resolveIfAbsent(request);
                } catch (IndexClosedException | IndexNotFoundException ex) {
                    addFailure(request, idx, ex);
                    return true;
                }
            }
            IndexMetaData indexMetaData = metaData.getIndexSafe(concreteIndex);
            if (indexMetaData.getState() == IndexMetaData.State.CLOSE) {
                addFailure(request, idx, new IndexClosedException(concreteIndex));
                return true;
            }
            return false;
        }

        private void addFailure(DocWriteRequest<?> request, int idx, Exception unavailableException) {
            BulkItemResponse.Failure failure = new BulkItemResponse.Failure(request.index(), request.type(), request.id(),
                    unavailableException);
            BulkItemResponse bulkItemResponse = new BulkItemResponse(idx, request.opType(), failure);
            responses.set(idx, bulkItemResponse);
            // make sure the request gets never processed again
            bulkRequest.requests.set(idx, null);
        }
    }

    void executeBulk(Task task, final BulkRequest bulkRequest, final IndexTimeProvider timeProvider, final ActionListener<BulkResponse> listener,
            final AtomicArray<BulkItemResponse> responses, Map<String, IndexNotFoundException> indicesThatCannotBeCreated) {
        timeProvider.setIngestTime();

        new BulkOperation(task, bulkRequest, listener, responses, timeProvider, indicesThatCannotBeCreated).run();
    }

    private static class ConcreteIndices  {
        private final ClusterState state;
        private final IndexNameExpressionResolver indexNameExpressionResolver;
        private final Map<String, Index> indices = new HashMap<>();

        ConcreteIndices(ClusterState state, IndexNameExpressionResolver indexNameExpressionResolver) {
            this.state = state;
            this.indexNameExpressionResolver = indexNameExpressionResolver;
        }

        Index getConcreteIndex(String indexOrAlias) {
            return indices.get(indexOrAlias);
        }

        Index resolveIfAbsent(DocWriteRequest<?> request) {
            Index concreteIndex = indices.get(request.index());
            if (concreteIndex == null) {
                concreteIndex = indexNameExpressionResolver.concreteWriteIndex(state, request);
                indices.put(request.index(), concreteIndex);
            }
            return concreteIndex;
        }
    }

    public long relativeTime() {
        return relativeTimeProvider.getAsLong();
    }

    void processBulkIndexIngestRequest(Task task, BulkRequest original, ActionListener<BulkResponse> listener) {
        final long ingestStartTimeInNanos = System.nanoTime();
        final BulkRequestModifier bulkRequestModifier = new BulkRequestModifier(original);
        ingestService.executeBulkRequest(
            original.numberOfActions(),
            () -> bulkRequestModifier,
            bulkRequestModifier::markItemAsFailed,
            (originalThread, exception) -> {
                if (exception != null) {
                    logger.error("failed to execute pipeline for a bulk request", exception);
                    listener.onFailure(exception);
                } else {
                    long ingestTookInMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - ingestStartTimeInNanos);
                    BulkRequest bulkRequest = bulkRequestModifier.getBulkRequest();
                    ActionListener<BulkResponse> actionListener = bulkRequestModifier.wrapActionListenerIfNeeded(ingestTookInMillis,
                        listener);
                    if (bulkRequest.requests().isEmpty()) {
                        // at this stage, the transport bulk action can't deal with a bulk request with no requests,
                        // so we stop and send an empty response back to the client.
                        // (this will happen if pre-processing all items in the bulk failed)
                        actionListener.onResponse(new BulkResponse(new BulkItemResponse[0], 0));
                    } else {
                        // If a processor went async and returned a response on a different thread then
                        // before we continue the bulk request we should fork back on a write thread:
                        if (originalThread == Thread.currentThread()) {
                            assert Thread.currentThread().getName().contains(ThreadPool.Names.WRITE);
                            doExecute(task, bulkRequest, actionListener);
                        } else {
                            threadPool.executor(ThreadPool.Names.WRITE).execute(new AbstractRunnable() {
                                @Override
                                public void onFailure(Exception e) {
                                    listener.onFailure(e);
                                }

                                @Override
                                protected void doRun() throws Exception {
                                    doExecute(task, bulkRequest, actionListener);
                                }

                                @Override
                                public boolean isForceExecution() {
                                    // If we fork back to a write thread we **not** should fail, because tp queue is full.
                                    // (Otherwise the work done during ingest will be lost)
                                    // It is okay to force execution here. Throttling of write requests happens prior to
                                    // ingest when a node receives a bulk request.
                                    return true;
                                }
                            });
                        }
                    }
                }
            },
            bulkRequestModifier::markItemAsDropped
        );
    }

    static final class BulkRequestModifier implements Iterator<DocWriteRequest<?>> {

        private static final Logger LOGGER = LogManager.getLogger(BulkRequestModifier.class);

        final BulkRequest bulkRequest;
        final SparseFixedBitSet failedSlots;
        final List<BulkItemResponse> itemResponses;
        final AtomicIntegerArray originalSlots;

        volatile int currentSlot = -1;

        BulkRequestModifier(BulkRequest bulkRequest) {
            this.bulkRequest = bulkRequest;
            this.failedSlots = new SparseFixedBitSet(bulkRequest.requests().size());
            this.itemResponses = new ArrayList<>(bulkRequest.requests().size());
            this.originalSlots = new AtomicIntegerArray(bulkRequest.requests().size()); // oversize, but that's ok
        }

        @Override
        public DocWriteRequest<?> next() {
            return bulkRequest.requests().get(++currentSlot);
        }

        @Override
        public boolean hasNext() {
            return (currentSlot + 1) < bulkRequest.requests().size();
        }

        BulkRequest getBulkRequest() {
            if (itemResponses.isEmpty()) {
                return bulkRequest;
            } else {
                BulkRequest modifiedBulkRequest = new BulkRequest();
                modifiedBulkRequest.setRefreshPolicy(bulkRequest.getRefreshPolicy());
                modifiedBulkRequest.waitForActiveShards(bulkRequest.waitForActiveShards());
                modifiedBulkRequest.timeout(bulkRequest.timeout());

                int slot = 0;
                List<DocWriteRequest<?>> requests = bulkRequest.requests();
                for (int i = 0; i < requests.size(); i++) {
                    DocWriteRequest<?> request = requests.get(i);
                    if (failedSlots.get(i) == false) {
                        modifiedBulkRequest.add(request);
                        originalSlots.set(slot++, i);
                    }
                }
                return modifiedBulkRequest;
            }
        }

        ActionListener<BulkResponse> wrapActionListenerIfNeeded(long ingestTookInMillis, ActionListener<BulkResponse> actionListener) {
            if (itemResponses.isEmpty()) {
                return ActionListener.map(actionListener,
                    response -> new BulkResponse(response.getItems(), response.getTook().getMillis(), ingestTookInMillis));
            } else {
                return ActionListener.delegateFailure(actionListener, (delegatedListener, response) -> {
                    BulkItemResponse[] items = response.getItems();
                    for (int i = 0; i < items.length; i++) {
                        itemResponses.add(originalSlots.get(i), response.getItems()[i]);
                    }
                    delegatedListener.onResponse(
                        new BulkResponse(
                            itemResponses.toArray(new BulkItemResponse[0]), response.getTook().getMillis(), ingestTookInMillis));
                });
            }
        }

        synchronized void markItemAsDropped(int slot) {
            IndexRequest indexRequest = getIndexWriteRequest(bulkRequest.requests().get(slot));
            failedSlots.set(slot);
            final String id = indexRequest.id() == null ? DROPPED_ITEM_WITH_AUTO_GENERATED_ID : indexRequest.id();
            itemResponses.add(
                new BulkItemResponse(slot, indexRequest.opType(),
                    new UpdateResponse(
                        new ShardId(indexRequest.index(), IndexMetaData.INDEX_UUID_NA_VALUE, 0),
                        indexRequest.type(), id, SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM,
                        indexRequest.version(), DocWriteResponse.Result.NOOP
                    )
                )
            );
        }

        synchronized void markItemAsFailed(int slot, Exception e) {
            IndexRequest indexRequest = getIndexWriteRequest(bulkRequest.requests().get(slot));
            LOGGER.debug(() -> new ParameterizedMessage("failed to execute pipeline [{}] for document [{}/{}/{}]",
                indexRequest.getPipeline(), indexRequest.index(), indexRequest.type(), indexRequest.id()), e);

            // We hit a error during preprocessing a request, so we:
            // 1) Remember the request item slot from the bulk, so that we're done processing all requests we know what failed
            // 2) Add a bulk item failure for this request
            // 3) Continue with the next request in the bulk.
            failedSlots.set(slot);
            BulkItemResponse.Failure failure = new BulkItemResponse.Failure(indexRequest.index(), indexRequest.type(),
                indexRequest.id(), e);
            itemResponses.add(new BulkItemResponse(slot, indexRequest.opType(), failure));
        }

    }

    public class IndexTimeProvider {
        public static final int INDEX_BULK_SLOWLOG_THRESHOLD_SIZE = 5;

        private long taskId;
        private long startTime;
        private long ingestTime;
        private long endTime;
        private Map<ShardId, Tuple<Long, Integer>> shardBulkCost = new ConcurrentHashMap<>();
        private AtomicInteger failedShards = new AtomicInteger();
        private AtomicInteger failedCount = new AtomicInteger();

        public IndexTimeProvider(long taskId, long startTime) {
            this.taskId = taskId;
            this.startTime = startTime;
        }

        public void setIngestTime() {
            this.ingestTime = relativeTime();
        }

        public void setEndTime() {
            this.endTime = relativeTime();
        }

        public void putShardCost(ShardId shardId, long cost, int itemsCount) {
            Tuple<Long, Integer> tuple = new Tuple<>(TimeUnit.NANOSECONDS.toMillis(cost), itemsCount);
            shardBulkCost.put(shardId, tuple);
        }

        public long getStartTime() {
            return startTime;
        }

        public long getIngestTime() {
            return ingestTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public long getTotalCost() {
            return TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        }

        public Map<ShardId, Tuple<Long, Integer>> getShardBulkCost() {
            return shardBulkCost;
        }

        public String recordCost(BulkRequest request, String requsetId) {
            long sum = 0;
            long max = 0;
            long avg = 0;

            int maxSize = INDEX_BULK_SLOWLOG_THRESHOLD_SIZE;
            Queue<Map.Entry<ShardId, Tuple<Long, Integer>>> queue = new PriorityQueue<>(shardBulkCost.size() > 0 ? shardBulkCost.size() : 1, (o1, o2) -> (int) (o2.getValue().v1() - o1.getValue().v1()));

            for (Map.Entry<ShardId, Tuple<Long, Integer>> entry : shardBulkCost.entrySet()) {
                long timeCost = entry.getValue().v1();

                sum += timeCost;
                if (timeCost > max) {
                    max = timeCost;
                }

                queue.add(entry);
            }

            if (shardBulkCost.size() > 0) {
                avg = sum / shardBulkCost.size();
            }

            StringBuffer buffer = new StringBuffer("bulkDetail||requestId=")
                .append(requsetId)
                .append("||taskId=")
                .append(taskId)
                .append("||size=")
                .append(request.estimatedSizeInBytes())
                .append("||items=")
                .append(request.requests.size())
                .append("||totalMills=")
                .append(getTotalCost())
                .append("||ingestCost=")
                .append(TimeUnit.NANOSECONDS.toMillis(ingestTime - startTime))
                .append("||max=")
                .append(max)
                .append("||avg=")
                .append(avg)
                .append("||shards=")
                .append(shardBulkCost.size())
                .append("||failedShards=")
                .append(failedShards.get())
                .append("||failedCount=")
                .append(failedCount.get());

            for (int i = 0; i < maxSize; ++i) {
                Map.Entry<ShardId, Tuple<Long, Integer>> entry = queue.poll();
                if (entry == null) {
                    break;
                }

                long timeCost = entry.getValue().v1();

                buffer.append("||s_");
                buffer.append(i);
                buffer.append("=");
                buffer.append(entry.getKey());
                buffer.append("||t_");
                buffer.append(i);
                buffer.append("=");
                buffer.append(timeCost);
                buffer.append("||l_");
                buffer.append(i);
                buffer.append("=");
                buffer.append(entry.getValue().v2());
            }

            return buffer.toString();
        }

        public void addFailedCount(int count) {
            this.failedCount.addAndGet(count);
        }

        public void incrFailedShards() {
            this.failedShards.incrementAndGet();
        }
    }
}
