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

package org.elasticsearch.action.admin.indices.rollover;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexClusterStateUpdateRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsAction;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.ActiveShardsObserver;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.cluster.metadata.AliasOrIndex;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.MetaDataCreateIndexService;
import org.elasticsearch.cluster.metadata.MetaDataIndexAliasesService;
import org.elasticsearch.cluster.metadata.MetaDataIndexTemplateService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.shard.DocsStats;
import org.elasticsearch.ingest.IngestService;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

/**
 * Main class to swap the index pointed to by an alias, given some conditions
 */
public class TransportRolloverAction extends TransportMasterNodeAction<RolloverRequest, RolloverResponse> {

    private static final Pattern INDEX_NAME_PATTERN = Pattern.compile("^.*_*(_v\\d+)?$");
    private static final Pattern INDEX_NAME_SUFFIX_PATTERN = Pattern.compile("^v\\d+$");
    private static final Pattern INDEX_NAME_VERSION_PATTERN = Pattern.compile("[^0-9]");
    private static final JsonFactory jsonFactory = new JsonFactory();
    private final MetaDataCreateIndexService createIndexService;
    private final MetaDataIndexAliasesService indexAliasesService;
    private final ActiveShardsObserver activeShardsObserver;
    private final IngestService ingestService;
    private final Client client;

    @Inject
    public TransportRolloverAction(TransportService transportService, ClusterService clusterService,
                                   ThreadPool threadPool, MetaDataCreateIndexService createIndexService,
                                   ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
                                   MetaDataIndexAliasesService indexAliasesService, IngestService ingestService, Client client) {
        super(RolloverAction.NAME, transportService, clusterService, threadPool, actionFilters, RolloverRequest::new,
            indexNameExpressionResolver);
        this.createIndexService = createIndexService;
        this.indexAliasesService = indexAliasesService;
        this.ingestService = ingestService;
        this.client = client;
        this.activeShardsObserver = new ActiveShardsObserver(clusterService, threadPool);
    }

    @Override
    protected String executor() {
        // we go async right away
        return ThreadPool.Names.SAME;
    }

    @Override
    protected RolloverResponse read(StreamInput in) throws IOException {
        return new RolloverResponse(in);
    }

    @Override
    protected ClusterBlockException checkBlock(RolloverRequest request, ClusterState state) {
        IndicesOptions indicesOptions = IndicesOptions.fromOptions(true, true,
            request.indicesOptions().expandWildcardsOpen(), request.indicesOptions().expandWildcardsClosed());
        return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA_WRITE,
            indexNameExpressionResolver.concreteIndexNames(state, indicesOptions, request.indices()));
    }

    @Override
    protected void masterOperation(RolloverRequest request, ClusterState state,
                                   ActionListener<RolloverResponse> listener) throws Exception {
        throw new UnsupportedOperationException("The task parameter is required");
    }

    @Override
    protected void masterOperation(Task task, final RolloverRequest rolloverRequest, final ClusterState state,
                                   final ActionListener<RolloverResponse> listener) {
        final MetaData metaData = state.metaData();
        validate(metaData, rolloverRequest);
        final AliasOrIndex.Alias alias = (AliasOrIndex.Alias) metaData.getAliasAndIndexLookup().get(rolloverRequest.getAlias());
        IndexMetaData indexMetaData = null;
        if (Strings.isNullOrEmpty(rolloverRequest.getSourceIndexName())) {
            indexMetaData = alias.getWriteIndex();
        } else {
            indexMetaData = metaData.index(rolloverRequest.getSourceIndexName());
        }
        final boolean explicitWriteIndex = Boolean.TRUE.equals(indexMetaData.getAliases().get(alias.getAliasName()).writeIndex());
        final String sourceProvidedName = indexMetaData.getSettings().get(IndexMetaData.SETTING_INDEX_PROVIDED_NAME,
            indexMetaData.getIndex().getName());
        final String sourceIndexName = indexMetaData.getIndex().getName();
        final String unresolvedName = (rolloverRequest.getNewIndexName() != null)
            ? rolloverRequest.getNewIndexName()
            : generateRolloverIndexName(sourceProvidedName, indexNameExpressionResolver);
        final String rolloverIndexName = indexNameExpressionResolver.resolveDateMathExpression(unresolvedName);

        if (state.routingTable().hasIndex(rolloverIndexName)) {
            // 平台已经创建了
            Map<String, Boolean> conditionStatus = new HashMap<>();
            conditionStatus.putIfAbsent(MaxAgeCondition.NAME, true);
            conditionStatus.putIfAbsent(MaxDocsCondition.NAME, true);
            conditionStatus.putIfAbsent(MaxSizeCondition.NAME, true);

            if (rolloverRequest.isDryRun()) {
                listener.onResponse(
                    new RolloverResponse(sourceIndexName, rolloverIndexName, conditionStatus, true, false, false, false));
                return;
            }

            IndicesStatsRequest statsRequest = new IndicesStatsRequest().indices(rolloverRequest.getAlias(), sourceIndexName)
                .clear()
                .indicesOptions(IndicesOptions.fromOptions(true, false, true, true))
                .docs(true);
            statsRequest.setParentTask(clusterService.localNode().getId(), task.getId());
            client.execute(IndicesStatsAction.INSTANCE, statsRequest, new ActionListener<IndicesStatsResponse>() {

                @Override
                public void onResponse(IndicesStatsResponse indicesStatsResponse) {
                    // 索引有更新或删除操作时不滚动
                    for (Map.Entry<String, IndexStats> entry : indicesStatsResponse.getIndices().entrySet()) {
                        if (entry.getValue().getTotal().docs.getDeleted() > 0) {
                            listener.onFailure(new UnsupportedOperationException("docs delete > 0"));
                            return;
                        }
                    }

                    clusterService.submitStateUpdateTask("rollover_index source [" + sourceIndexName + "] to target ["
                        + rolloverIndexName + "]", new ClusterStateUpdateTask() {

                        @Override
                        public String taskType() {
                            return "rollover_index";
                        }

                        @Override
                        public ClusterState execute(ClusterState currentState) throws Exception {
                            List<AliasAction> actions = new ArrayList<>();
                            actions.addAll(rolloverAliasToNewIndex(sourceIndexName, rolloverIndexName, rolloverRequest, explicitWriteIndex));
                            if (alias.getWriteIndex() != null) {
                                actions.addAll(rolloverAliasToNewIndex(
                                    alias.getWriteIndex().getIndex().getName(), rolloverIndexName, rolloverRequest, explicitWriteIndex));
                            }

                            ClusterState newState = indexAliasesService.applyAliasActions(currentState, actions);

                            List<Condition<?>> conditions = new ArrayList<>(rolloverRequest.getConditions().values());
                            RolloverInfo rolloverInfo = new RolloverInfo(rolloverRequest.getAlias(), conditions,
                                threadPool.absoluteTimeInMillis());
                            return ClusterState.builder(newState)
                                .metaData(MetaData.builder(newState.metaData())
                                    .put(IndexMetaData.builder(newState.metaData().index(sourceIndexName))
                                        .putRolloverInfo(rolloverInfo))).build();
                        }

                        @Override
                        public void onFailure(String source, Exception e) {
                            listener.onFailure(e);
                        }

                        @Override
                        public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
                            if (newState.equals(oldState) == false) {
                                activeShardsObserver.waitForActiveShards(new String[]{rolloverIndexName},
                                    rolloverRequest.getCreateIndexRequest().waitForActiveShards(),
                                    rolloverRequest.masterNodeTimeout(),
                                    isShardsAcknowledged -> listener.onResponse(new RolloverResponse(
                                        sourceIndexName, rolloverIndexName, conditionStatus, false, true, true,
                                        isShardsAcknowledged)),
                                    listener::onFailure);

                                // update pipeline
                                updatePipeline(rolloverRequest, rolloverIndexName);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(e);
                }
            });
        } else {
            MetaDataCreateIndexService.validateIndexName(rolloverIndexName, state); // will fail if the index already exists
            checkNoDuplicatedAliasInIndexTemplate(metaData, rolloverIndexName, rolloverRequest.getAlias());
            IndicesStatsRequest statsRequest = new IndicesStatsRequest().indices(rolloverRequest.getAlias(), sourceIndexName)
                .clear()
                .indicesOptions(IndicesOptions.fromOptions(true, false, true, true))
                .docs(true);
            statsRequest.setParentTask(clusterService.localNode().getId(), task.getId());
            client.execute(IndicesStatsAction.INSTANCE, statsRequest,
                new ActionListener<IndicesStatsResponse>() {
                    @Override
                    public void onResponse(IndicesStatsResponse statsResponse) {
                        final Map<String, Boolean> conditionResults = evaluateConditions(rolloverRequest.getConditions().values(),
                            metaData.index(sourceIndexName), statsResponse);

                        if (rolloverRequest.isDryRun()) {
                            listener.onResponse(
                                new RolloverResponse(sourceIndexName, rolloverIndexName, conditionResults, true, false, false, false));
                            return;
                        }

                        // 索引有更新或删除操作时不滚动
                        for (Map.Entry<String, IndexStats> entry : statsResponse.getIndices().entrySet()) {
                            if (entry.getValue().getTotal().docs.getDeleted() > 0) {
                                listener.onFailure(new UnsupportedOperationException("docs delete > 0"));
                                return;
                            }
                        }

                        List<Condition<?>> metConditions = rolloverRequest.getConditions().values().stream()
                            .filter(condition -> conditionResults.get(condition.toString())).collect(Collectors.toList());
                        if (conditionResults.size() == 0 || metConditions.size() > 0) {
                            CreateIndexClusterStateUpdateRequest createIndexRequest = prepareCreateIndexRequest(unresolvedName,
                                rolloverIndexName, rolloverRequest);
                            clusterService.submitStateUpdateTask("rollover_index source [" + sourceIndexName + "] to target ["
                                + rolloverIndexName + "]", new ClusterStateUpdateTask() {

                                @Override
                                public String taskType() {
                                    return "rollover_index";
                                }

                                @Override
                                public ClusterState execute(ClusterState currentState) throws Exception {
                                    ClusterState newState = createIndexService.applyCreateIndexRequest(currentState, createIndexRequest);

                                    List<AliasAction> actions = new ArrayList<>();
                                    actions.addAll(rolloverAliasToNewIndex(sourceIndexName, rolloverIndexName, rolloverRequest, explicitWriteIndex));
                                    if (alias.getWriteIndex() != null) {
                                        actions.addAll(rolloverAliasToNewIndex(
                                            alias.getWriteIndex().getIndex().getName(), rolloverIndexName, rolloverRequest, explicitWriteIndex));
                                    }
                                    newState = indexAliasesService.applyAliasActions(newState, actions);

                                    RolloverInfo rolloverInfo = new RolloverInfo(rolloverRequest.getAlias(), metConditions,
                                        threadPool.absoluteTimeInMillis());
                                    return ClusterState.builder(newState)
                                        .metaData(MetaData.builder(newState.metaData())
                                            .put(IndexMetaData.builder(newState.metaData().index(sourceIndexName))
                                                .putRolloverInfo(rolloverInfo))).build();
                                }

                                @Override
                                public void onFailure(String source, Exception e) {
                                    listener.onFailure(e);
                                }

                                @Override
                                public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
                                    if (newState.equals(oldState) == false) {
                                        activeShardsObserver.waitForActiveShards(new String[]{rolloverIndexName},
                                            rolloverRequest.getCreateIndexRequest().waitForActiveShards(),
                                            rolloverRequest.masterNodeTimeout(),
                                            isShardsAcknowledged -> listener.onResponse(new RolloverResponse(
                                                sourceIndexName, rolloverIndexName, conditionResults, false, true, true,
                                                isShardsAcknowledged)),
                                            listener::onFailure);

                                        // update pipeline
                                        updatePipeline(rolloverRequest, rolloverIndexName);
                                    }
                                }
                            });
                        } else {
                            // conditions not met
                            listener.onResponse(
                                new RolloverResponse(sourceIndexName, rolloverIndexName, conditionResults, false, false, false, false)
                            );
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        listener.onFailure(e);
                    }
                }
            );
        }
    }

    private void updatePipeline(RolloverRequest rolloverRequest, String rolloverIndexName) {
        Map<String, IngestService.PipelineHolder> pipelines = ingestService.pipelines();
        IngestService.PipelineHolder pipelineHolder = pipelines.get(rolloverRequest.getAlias());
        int indexVersion = rolloverIndexNameVersion(rolloverIndexName);
        String pipeline = null;
        try {
            if (pipelineHolder != null) {
                // pipeline exists
                JsonParser parser = jsonFactory.createParser(pipelineHolder.configuration.getConfig().streamInput());
                XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                try {
                    if (parser.nextToken() != JsonToken.START_OBJECT) {
                        throw new IOException("expected data to start with an object");
                    }
                    jsonBuilder.startObject();
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
                            if ("description".equals(parser.getCurrentName())) {
                                jsonBuilder.field(parser.getCurrentName(), parser.nextTextValue());
                                continue;
                            }
                            if ("processors".equals(parser.getCurrentName())) {
                                jsonBuilder.startArray("processors");
                                while (parser.nextToken() != JsonToken.END_ARRAY) {
                                    if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                                        jsonBuilder.startObject();
                                    } else if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
                                        if ("throttle".equals(parser.getCurrentName())
                                            || "index_template".equals(parser.getCurrentName())) {
                                            jsonBuilder.startObject(parser.getCurrentName());
                                            parser.nextToken();
                                        } else {
                                            String currentName = parser.getCurrentName();
                                            JsonToken jsonToken = parser.nextToken();
                                            if (jsonToken.isNumeric()) {
                                                int valueAsInt = parser.getValueAsInt();
                                                if (currentName.equals("index_version")) {
                                                    valueAsInt = Math.max(valueAsInt, indexVersion);
                                                }
                                                jsonBuilder.field(currentName, valueAsInt);
                                            } else {
                                                jsonBuilder.field(currentName, parser.getValueAsString());
                                            }
                                        }
                                    } else if (parser.getCurrentToken() == JsonToken.END_OBJECT) {
                                        jsonBuilder.endObject();
                                    }
                                }
                                jsonBuilder.endArray();
                            }
                        }
                    }
                    jsonBuilder.endObject();

                    jsonBuilder.flush();
                    BytesReference bytes = BytesReference.bytes(jsonBuilder);
                    pipeline = bytes.utf8ToString();
                } finally {
                    parser.close();
                    jsonBuilder.close();
                }
            } else {
                // pipeline not exists
                pipeline = String.format("{\n" +
                    "    \"description\" : \"%s\",\n" +
                    "    \"processors\" : [\n" +
                    "      {\n" +
                    "        \"index_template\" : {\n" +
                    "          \"index_version\" : %d\n" +
                    "        }\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }", rolloverRequest.getAlias(), indexVersion);
            }
            client.admin().cluster().preparePutPipeline(
                rolloverRequest.getAlias(), new BytesArray(pipeline), XContentType.JSON).execute(
                new ActionListener<AcknowledgedResponse>() {
                    @Override
                    public void onResponse(AcknowledgedResponse acknowledgedResponse) {
                        logger.info("put pipeline {} success", rolloverRequest.getAlias());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        logger.error("put pipeline {} failed", rolloverRequest.getAlias(), e);
                    }
                });
        } catch (Exception e) {
            logger.error("update pipeline {} failed", rolloverRequest.getAlias(), e);
        }
    }

    /**
     * Creates the alias actions to reflect the alias rollover from the old (source) index to the new (target/rolled over) index. An
     * alias pointing to multiple indices will have to be an explicit write index (ie. the old index alias has is_write_index set to true)
     * in which case, after the rollover, the new index will need to be the explicit write index.
     */
    static List<AliasAction> rolloverAliasToNewIndex(String oldIndex, String newIndex, RolloverRequest request,
                                                     boolean explicitWriteIndex) {
        if (explicitWriteIndex) {
            return unmodifiableList(Arrays.asList(
                new AliasAction.Add(newIndex, request.getAlias(), null, null, null, true),
                new AliasAction.Add(oldIndex, request.getAlias(), null, null, null, false)));
        } else {
            return unmodifiableList(Arrays.asList(
                new AliasAction.Add(newIndex, request.getAlias(), null, null, null, null),
                new AliasAction.Remove(oldIndex, request.getAlias())));
        }
    }

    static String generateRolloverIndexName(String sourceIndexName, IndexNameExpressionResolver indexNameExpressionResolver) {
        String resolvedName = indexNameExpressionResolver.resolveDateMathExpression(sourceIndexName);
        final boolean isDateMath = sourceIndexName.equals(resolvedName) == false;
        if (INDEX_NAME_PATTERN.matcher(resolvedName).matches()) {
            int numberIndex = sourceIndexName.lastIndexOf("_");
            if (numberIndex == -1) {
                // 不分区索引
                return sourceIndexName + "_v1";
            } else {
                // 分区索引或已经滚动的不分区索引
                String indexNameSuffix = sourceIndexName.substring(numberIndex + 1, isDateMath ? sourceIndexName.length() - 1 : sourceIndexName.length());
                if (INDEX_NAME_SUFFIX_PATTERN.matcher(indexNameSuffix).matches()) {
                    // 分区索引或已经滚动的不分区索引,版本号不为0
                    int counter = Integer.parseInt(INDEX_NAME_VERSION_PATTERN.matcher(indexNameSuffix).replaceAll("").trim());
                    return sourceIndexName.substring(0, numberIndex) + "_v" + String.format(Locale.ROOT, "%d", ++counter) + (isDateMath ? ">" : "");
                } else {
                    // 分区索引版本号为0
                    return sourceIndexName + "_v1";
                }
            }
        } else {
            throw new IllegalArgumentException("index name [" + sourceIndexName + "] does not match pattern '^.*_*(_v\\d+)?$'");
        }
    }

    static int rolloverIndexNameVersion(String rolloverIndexName) {
        if (INDEX_NAME_PATTERN.matcher(rolloverIndexName).matches()) {
            int numberIndex = rolloverIndexName.lastIndexOf("_");
            if (numberIndex == -1) {
                // 不分区索引
                return 1;
            } else {
                // 分区索引或已经滚动的不分区索引
                String indexNameSuffix = rolloverIndexName.substring(numberIndex + 1, rolloverIndexName.length());
                if (INDEX_NAME_SUFFIX_PATTERN.matcher(indexNameSuffix).matches()) {
                    // 分区索引或已经滚动的不分区索引,版本号不为0
                    return Integer.parseInt(INDEX_NAME_VERSION_PATTERN.matcher(indexNameSuffix).replaceAll("").trim());
                } else {
                    // 分区索引版本号为0
                    return 1;
                }
            }
        } else {
            throw new IllegalArgumentException("index name [" + rolloverIndexName + "] does not match pattern '^.*_*(_v\\d+)?$'");
        }
    }

    static Map<String, Boolean> evaluateConditions(final Collection<Condition<?>> conditions,
                                                   @Nullable final DocsStats docsStats,
                                                   @Nullable final IndexMetaData metaData) {
        if (metaData == null) {
            return conditions.stream().collect(Collectors.toMap(Condition::toString, cond -> false));
        }
        final long numDocs = docsStats == null ? 0 : docsStats.getCount();
        final long indexSize = docsStats == null ? 0 : docsStats.getTotalSizeInBytes();
        final int numberOfShards = metaData.getNumberOfShards();
        // 索引大小改成分片大小
        final Condition.Stats stats = new Condition.Stats(numDocs, metaData.getCreationDate(), new ByteSizeValue(indexSize / numberOfShards));
        return conditions.stream()
            .map(condition -> condition.evaluate(stats))
            .collect(Collectors.toMap(result -> result.condition.toString(), result -> result.matched));
    }

    static Map<String, Boolean> evaluateConditions(final Collection<Condition<?>> conditions,
                                                   @Nullable final IndexMetaData metaData,
                                                   @Nullable final IndicesStatsResponse statsResponse) {
        if (metaData == null) {
            return conditions.stream().collect(Collectors.toMap(Condition::toString, cond -> false));
        } else {
            final DocsStats docsStats = Optional.ofNullable(statsResponse)
                .map(stats -> stats.getIndex(metaData.getIndex().getName()))
                .map(indexStats -> indexStats.getPrimaries().getDocs())
                .orElse(null);
            return evaluateConditions(conditions, docsStats, metaData);
        }
    }

    static void validate(MetaData metaData, RolloverRequest request) {
        final AliasOrIndex aliasOrIndex = metaData.getAliasAndIndexLookup().get(request.getAlias());
        if (aliasOrIndex == null) {
            throw new IllegalArgumentException("source alias does not exist");
        }
        if (aliasOrIndex.isAlias() == false) {
            throw new IllegalArgumentException("source alias is a concrete index");
        }
        final AliasOrIndex.Alias alias = (AliasOrIndex.Alias) aliasOrIndex;
        if (alias.getWriteIndex() == null) {
            throw new IllegalArgumentException("source alias [" + alias.getAliasName() + "] does not point to a write index");
        }
    }

    static CreateIndexClusterStateUpdateRequest prepareCreateIndexRequest(final String providedIndexName, final String targetIndexName,
                                                                          final RolloverRequest rolloverRequest) {

        final CreateIndexRequest createIndexRequest = rolloverRequest.getCreateIndexRequest();
        createIndexRequest.cause("rollover_index");
        createIndexRequest.index(targetIndexName);
        return new CreateIndexClusterStateUpdateRequest(
            "rollover_index", targetIndexName, providedIndexName)
            .ackTimeout(createIndexRequest.timeout())
            .masterNodeTimeout(createIndexRequest.masterNodeTimeout())
            .settings(createIndexRequest.settings())
            .aliases(createIndexRequest.aliases())
            .waitForActiveShards(ActiveShardCount.NONE) // not waiting for shards here, will wait on the alias switch operation
            .mappings(createIndexRequest.mappings());
    }

    /**
     * If the newly created index matches with an index template whose aliases contains the rollover alias,
     * the rollover alias will point to multiple indices. This causes indexing requests to be rejected.
     * To avoid this, we make sure that there is no duplicated alias in index templates before creating a new index.
     */
    static void checkNoDuplicatedAliasInIndexTemplate(MetaData metaData, String rolloverIndexName, String rolloverRequestAlias) {
        final List<IndexTemplateMetaData> matchedTemplates = MetaDataIndexTemplateService.findTemplates(metaData, rolloverIndexName);
        for (IndexTemplateMetaData template : matchedTemplates) {
            if (template.aliases().containsKey(rolloverRequestAlias)) {
                throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "Rollover alias [%s] can point to multiple indices, found duplicated alias [%s] in index template [%s]",
                    rolloverRequestAlias, template.aliases().keys(), template.name()));
            }
        }
    }
}
