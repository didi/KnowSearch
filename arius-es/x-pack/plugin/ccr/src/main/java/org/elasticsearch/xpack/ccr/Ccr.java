/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.ccr;

import org.apache.lucene.util.SetOnce;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.RequestValidators;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.index.IndexModule;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.engine.EngineFactory;
import org.elasticsearch.license.XPackLicenseState;
import org.elasticsearch.persistent.PersistentTaskParams;
import org.elasticsearch.persistent.PersistentTasksExecutor;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.EnginePlugin;
import org.elasticsearch.plugins.PersistentTaskPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.RepositoryPlugin;
import org.elasticsearch.repositories.Repository;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ExecutorBuilder;
import org.elasticsearch.threadpool.FixedExecutorBuilder;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;
import org.elasticsearch.xpack.ccr.action.AutoFollowCoordinator;
import org.elasticsearch.xpack.ccr.action.CcrRequests;
import org.elasticsearch.xpack.ccr.action.ShardChangesAction;
import org.elasticsearch.xpack.ccr.action.ShardFollowTask;
import org.elasticsearch.xpack.ccr.action.ShardFollowTaskCleaner;
import org.elasticsearch.xpack.ccr.action.ShardFollowTasksExecutor;
import org.elasticsearch.xpack.ccr.action.TransportCcrStatsAction;
import org.elasticsearch.xpack.ccr.action.TransportDeleteAutoFollowPatternAction;
import org.elasticsearch.xpack.ccr.action.TransportFollowInfoAction;
import org.elasticsearch.xpack.ccr.action.TransportFollowStatsAction;
import org.elasticsearch.xpack.ccr.action.TransportForgetFollowerAction;
import org.elasticsearch.xpack.ccr.action.TransportGetAutoFollowPatternAction;
import org.elasticsearch.xpack.ccr.action.TransportActivateAutoFollowPatternAction;
import org.elasticsearch.xpack.ccr.action.TransportPauseFollowAction;
import org.elasticsearch.xpack.ccr.action.TransportPutAutoFollowPatternAction;
import org.elasticsearch.xpack.ccr.action.TransportPutFollowAction;
import org.elasticsearch.xpack.ccr.action.TransportResumeFollowAction;
import org.elasticsearch.xpack.ccr.action.TransportUnfollowAction;
import org.elasticsearch.xpack.ccr.action.bulk.BulkShardOperationsAction;
import org.elasticsearch.xpack.ccr.action.bulk.TransportBulkShardOperationsAction;
import org.elasticsearch.xpack.ccr.action.repositories.ClearCcrRestoreSessionAction;
import org.elasticsearch.xpack.ccr.action.repositories.DeleteInternalCcrRepositoryAction;
import org.elasticsearch.xpack.ccr.action.repositories.GetCcrRestoreFileChunkAction;
import org.elasticsearch.xpack.ccr.action.repositories.PutCcrRestoreSessionAction;
import org.elasticsearch.xpack.ccr.action.repositories.PutInternalCcrRepositoryAction;
import org.elasticsearch.xpack.ccr.index.engine.FollowingEngineFactory;
import org.elasticsearch.xpack.ccr.repository.CcrRepository;
import org.elasticsearch.xpack.ccr.repository.CcrRestoreSourceService;
import org.elasticsearch.xpack.ccr.rest.RestCcrStatsAction;
import org.elasticsearch.xpack.ccr.rest.RestDeleteAutoFollowPatternAction;
import org.elasticsearch.xpack.ccr.rest.RestFollowInfoAction;
import org.elasticsearch.xpack.ccr.rest.RestFollowStatsAction;
import org.elasticsearch.xpack.ccr.rest.RestForgetFollowerAction;
import org.elasticsearch.xpack.ccr.rest.RestGetAutoFollowPatternAction;
import org.elasticsearch.xpack.ccr.rest.RestPauseAutoFollowPatternAction;
import org.elasticsearch.xpack.ccr.rest.RestPauseFollowAction;
import org.elasticsearch.xpack.ccr.rest.RestPutAutoFollowPatternAction;
import org.elasticsearch.xpack.ccr.rest.RestPutFollowAction;
import org.elasticsearch.xpack.ccr.rest.RestResumeAutoFollowPatternAction;
import org.elasticsearch.xpack.ccr.rest.RestResumeFollowAction;
import org.elasticsearch.xpack.ccr.rest.RestUnfollowAction;
import org.elasticsearch.xpack.core.XPackPlugin;
import org.elasticsearch.xpack.core.ccr.AutoFollowMetadata;
import org.elasticsearch.xpack.core.ccr.CCRFeatureSet;
import org.elasticsearch.xpack.core.ccr.ShardFollowNodeTaskStatus;
import org.elasticsearch.xpack.core.ccr.action.CcrStatsAction;
import org.elasticsearch.xpack.core.ccr.action.DeleteAutoFollowPatternAction;
import org.elasticsearch.xpack.core.ccr.action.FollowInfoAction;
import org.elasticsearch.xpack.core.ccr.action.FollowStatsAction;
import org.elasticsearch.xpack.core.ccr.action.ForgetFollowerAction;
import org.elasticsearch.xpack.core.ccr.action.GetAutoFollowPatternAction;
import org.elasticsearch.xpack.core.ccr.action.ActivateAutoFollowPatternAction;
import org.elasticsearch.xpack.core.ccr.action.PauseFollowAction;
import org.elasticsearch.xpack.core.ccr.action.PutAutoFollowPatternAction;
import org.elasticsearch.xpack.core.ccr.action.PutFollowAction;
import org.elasticsearch.xpack.core.ccr.action.ResumeFollowAction;
import org.elasticsearch.xpack.core.ccr.action.UnfollowAction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static org.elasticsearch.xpack.ccr.CcrSettings.CCR_FOLLOWING_INDEX_SETTING;
import static org.elasticsearch.xpack.core.XPackSettings.CCR_ENABLED_SETTING;

/**
 * Container class for CCR functionality.
 */
public class Ccr extends Plugin implements ActionPlugin, PersistentTaskPlugin, EnginePlugin, RepositoryPlugin {

    public static final String CCR_THREAD_POOL_NAME = "ccr";
    public static final String CCR_CUSTOM_METADATA_KEY = "ccr";
    public static final String CCR_CUSTOM_METADATA_LEADER_INDEX_SHARD_HISTORY_UUIDS = "leader_index_shard_history_uuids";
    public static final String CCR_CUSTOM_METADATA_LEADER_INDEX_UUID_KEY = "leader_index_uuid";
    public static final String CCR_CUSTOM_METADATA_LEADER_INDEX_NAME_KEY = "leader_index_name";
    public static final String CCR_CUSTOM_METADATA_REMOTE_CLUSTER_NAME_KEY = "remote_cluster_name";

    public static final String REQUESTED_OPS_MISSING_METADATA_KEY = "es.requested_operations_missing";

    private final boolean enabled;
    private final Settings settings;
    private final CcrLicenseChecker ccrLicenseChecker;
    private final SetOnce<CcrRestoreSourceService> restoreSourceService = new SetOnce<>();
    private final SetOnce<CcrSettings> ccrSettings = new SetOnce<>();
    private Client client;
    private final boolean transportClientMode;

    /**
     * Construct an instance of the CCR container with the specified settings.
     *
     * @param settings the settings
     */
    @SuppressWarnings("unused") // constructed reflectively by the plugin infrastructure
    public Ccr(final Settings settings) {
        this(settings, new CcrLicenseChecker());
    }

    /**
     * Construct an instance of the CCR container with the specified settings and license checker.
     *
     * @param settings          the settings
     * @param ccrLicenseChecker the CCR license checker
     */
    Ccr(final Settings settings, final CcrLicenseChecker ccrLicenseChecker) {
        this.settings = settings;
        this.enabled = CCR_ENABLED_SETTING.get(settings);
        this.ccrLicenseChecker = Objects.requireNonNull(ccrLicenseChecker);
        this.transportClientMode = XPackPlugin.transportClientMode(settings);
    }

    @Override
    public Collection<Object> createComponents(
            final Client client,
            final ClusterService clusterService,
            final ThreadPool threadPool,
            final ResourceWatcherService resourceWatcherService,
            final ScriptService scriptService,
            final NamedXContentRegistry xContentRegistry,
            final Environment environment,
            final NodeEnvironment nodeEnvironment,
            final NamedWriteableRegistry namedWriteableRegistry) {
        this.client = client;
        if (enabled == false) {
            return emptyList();
        }

        CcrSettings ccrSettings = new CcrSettings(settings, clusterService.getClusterSettings());
        this.ccrSettings.set(ccrSettings);
        CcrRestoreSourceService restoreSourceService = new CcrRestoreSourceService(threadPool, ccrSettings);
        this.restoreSourceService.set(restoreSourceService);
        return Arrays.asList(
            ccrLicenseChecker,
            restoreSourceService,
            new CcrRepositoryManager(settings, clusterService, client),
            new ShardFollowTaskCleaner(clusterService, threadPool, client),
            new AutoFollowCoordinator(
                settings,
                client,
                clusterService,
                ccrLicenseChecker,
                threadPool::relativeTimeInMillis,
                threadPool::absoluteTimeInMillis,
                threadPool.executor(Ccr.CCR_THREAD_POOL_NAME)));
    }

    @Override
    public List<PersistentTasksExecutor<?>> getPersistentTasksExecutor(ClusterService clusterService,
                                                                       ThreadPool threadPool,
                                                                       Client client,
                                                                       SettingsModule settingsModule) {
        return Collections.singletonList(new ShardFollowTasksExecutor(client, threadPool, clusterService, settingsModule));
    }

    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        if (enabled == false) {
            return emptyList();
        }

        return Arrays.asList(
                // internal actions
                new ActionHandler<>(BulkShardOperationsAction.INSTANCE, TransportBulkShardOperationsAction.class),
                new ActionHandler<>(ShardChangesAction.INSTANCE, ShardChangesAction.TransportAction.class),
                new ActionHandler<>(PutInternalCcrRepositoryAction.INSTANCE,
                    PutInternalCcrRepositoryAction.TransportPutInternalRepositoryAction.class),
                new ActionHandler<>(DeleteInternalCcrRepositoryAction.INSTANCE,
                    DeleteInternalCcrRepositoryAction.TransportDeleteInternalRepositoryAction.class),
                new ActionHandler<>(PutCcrRestoreSessionAction.INSTANCE,
                    PutCcrRestoreSessionAction.TransportPutCcrRestoreSessionAction.class),
                new ActionHandler<>(ClearCcrRestoreSessionAction.INSTANCE,
                    ClearCcrRestoreSessionAction.TransportDeleteCcrRestoreSessionAction.class),
                new ActionHandler<>(GetCcrRestoreFileChunkAction.INSTANCE,
                    GetCcrRestoreFileChunkAction.TransportGetCcrRestoreFileChunkAction.class),
                // stats action
                new ActionHandler<>(FollowStatsAction.INSTANCE, TransportFollowStatsAction.class),
                new ActionHandler<>(CcrStatsAction.INSTANCE, TransportCcrStatsAction.class),
                new ActionHandler<>(FollowInfoAction.INSTANCE, TransportFollowInfoAction.class),
                // follow actions
                new ActionHandler<>(PutFollowAction.INSTANCE, TransportPutFollowAction.class),
                new ActionHandler<>(ResumeFollowAction.INSTANCE, TransportResumeFollowAction.class),
                new ActionHandler<>(PauseFollowAction.INSTANCE, TransportPauseFollowAction.class),
                new ActionHandler<>(UnfollowAction.INSTANCE, TransportUnfollowAction.class),
                // auto-follow actions
                new ActionHandler<>(DeleteAutoFollowPatternAction.INSTANCE, TransportDeleteAutoFollowPatternAction.class),
                new ActionHandler<>(PutAutoFollowPatternAction.INSTANCE, TransportPutAutoFollowPatternAction.class),
                new ActionHandler<>(GetAutoFollowPatternAction.INSTANCE, TransportGetAutoFollowPatternAction.class),
                new ActionHandler<>(ActivateAutoFollowPatternAction.INSTANCE, TransportActivateAutoFollowPatternAction.class),
                // forget follower action
                new ActionHandler<>(ForgetFollowerAction.INSTANCE, TransportForgetFollowerAction.class));
    }

    public List<RestHandler> getRestHandlers(Settings settings, RestController restController, ClusterSettings clusterSettings,
                                             IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter,
                                             IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {
        if (enabled == false) {
            return emptyList();
        }

        return Arrays.asList(
                // stats API
                new RestFollowStatsAction(restController),
                new RestCcrStatsAction(restController),
                new RestFollowInfoAction(restController),
                // follow APIs
                new RestPutFollowAction(restController),
                new RestResumeFollowAction(restController),
                new RestPauseFollowAction(restController),
                new RestUnfollowAction(restController),
                // auto-follow APIs
                new RestDeleteAutoFollowPatternAction(restController),
                new RestPutAutoFollowPatternAction(restController),
                new RestGetAutoFollowPatternAction(restController),
                new RestPauseAutoFollowPatternAction(restController),
                new RestResumeAutoFollowPatternAction(restController),
                // forget follower API
                new RestForgetFollowerAction(restController));
    }

    public List<NamedWriteableRegistry.Entry> getNamedWriteables() {
        return Arrays.asList(
                // Persistent action requests
                new NamedWriteableRegistry.Entry(PersistentTaskParams.class, ShardFollowTask.NAME,
                        ShardFollowTask::readFrom),

                // Task statuses
                new NamedWriteableRegistry.Entry(Task.Status.class, ShardFollowNodeTaskStatus.STATUS_PARSER_NAME,
                        ShardFollowNodeTaskStatus::new)
        );
    }

    public List<NamedXContentRegistry.Entry> getNamedXContent() {
        return Arrays.asList(
                // auto-follow metadata, persisted into the cluster state as XContent
                new NamedXContentRegistry.Entry(
                        MetaData.Custom.class,
                        new ParseField(AutoFollowMetadata.TYPE),
                        AutoFollowMetadata::fromXContent),
                // persistent action requests
                new NamedXContentRegistry.Entry(
                        PersistentTaskParams.class,
                        new ParseField(ShardFollowTask.NAME),
                        ShardFollowTask::fromXContent),
                // task statuses
                new NamedXContentRegistry.Entry(
                        ShardFollowNodeTaskStatus.class,
                        new ParseField(ShardFollowNodeTaskStatus.STATUS_PARSER_NAME),
                        ShardFollowNodeTaskStatus::fromXContent));
    }

    /**
     * The settings defined by CCR.
     *
     * @return the settings
     */
    public List<Setting<?>> getSettings() {
        return CcrSettings.getSettings();
    }

    /**
     * The optional engine factory for CCR. This method inspects the index settings for the {@link CcrSettings#CCR_FOLLOWING_INDEX_SETTING}
     * setting to determine whether or not the engine implementation should be a following engine.
     *
     * @return the optional engine factory
     */
    public Optional<EngineFactory> getEngineFactory(final IndexSettings indexSettings) {
        if (CCR_FOLLOWING_INDEX_SETTING.get(indexSettings.getSettings())) {
            return Optional.of(new FollowingEngineFactory());
        } else {
            return Optional.empty();
        }
    }

    public List<ExecutorBuilder<?>> getExecutorBuilders(Settings settings) {
        if (enabled == false) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new FixedExecutorBuilder(settings, CCR_THREAD_POOL_NAME, 32, 100, "xpack.ccr.ccr_thread_pool"));
    }

    @Override
    public Map<String, Repository.Factory> getInternalRepositories(Environment env, NamedXContentRegistry namedXContentRegistry,
                                                                   ClusterService clusterService) {
        Repository.Factory repositoryFactory =
            (metadata) -> new CcrRepository(metadata, client, ccrLicenseChecker, settings, ccrSettings.get(),
                clusterService.getClusterApplierService().threadPool());
        return Collections.singletonMap(CcrRepository.TYPE, repositoryFactory);
    }

    @Override
    public void onIndexModule(IndexModule indexModule) {
        if (enabled) {
            indexModule.addIndexEventListener(this.restoreSourceService.get());
        }
    }

    @Override
    public Collection<Module> createGuiceModules() {
        if (transportClientMode) {
            return Collections.emptyList();
        }

        return Collections.singleton(b -> XPackPlugin.bindFeatureSet(b, CCRFeatureSet.class));
    }

    protected XPackLicenseState getLicenseState() { return XPackPlugin.getSharedLicenseState(); }

    @Override
    public Collection<RequestValidators.RequestValidator<PutMappingRequest>> mappingRequestValidators() {
        return Collections.singletonList(CcrRequests.CCR_PUT_MAPPING_REQUEST_VALIDATOR);
    }

    @Override
    public Collection<RequestValidators.RequestValidator<IndicesAliasesRequest>> indicesAliasesRequestValidators() {
        return Collections.singletonList(CcrRequests.CCR_INDICES_ALIASES_REQUEST_VALIDATOR);
    }
}
