package org.elasticsearch.dcdr;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.cluster.NamedDiff;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.dcdr.action.CreateAutoReplicationAction;
import org.elasticsearch.dcdr.action.CreateReplicationAction;
import org.elasticsearch.dcdr.action.DeleteAutoReplicationAction;
import org.elasticsearch.dcdr.action.DeleteReplicationAction;
import org.elasticsearch.dcdr.action.FetchShardInfoAction;
import org.elasticsearch.dcdr.action.GetAutoReplicationAction;
import org.elasticsearch.dcdr.action.GetReplicationAction;
import org.elasticsearch.dcdr.action.ReplicationRecoverAction;
import org.elasticsearch.dcdr.action.ReplicationStatsAction;
import org.elasticsearch.dcdr.action.SwitchReplicationAction;
import org.elasticsearch.dcdr.action.TransportCreateAutoReplicationAction;
import org.elasticsearch.dcdr.action.TransportCreateReplicationAction;
import org.elasticsearch.dcdr.action.TransportDeleteAutoReplicationAction;
import org.elasticsearch.dcdr.action.TransportDeleteReplicationAction;
import org.elasticsearch.dcdr.action.TransportFetchShardInfoAction;
import org.elasticsearch.dcdr.action.TransportGetAutoReplicationAction;
import org.elasticsearch.dcdr.action.TransportGetReplicationAction;
import org.elasticsearch.dcdr.action.TransportReplicationRecoverAction;
import org.elasticsearch.dcdr.action.TransportReplicationStatsAction;
import org.elasticsearch.dcdr.action.TransportSwitchReplicationAction;
import org.elasticsearch.dcdr.indices.recovery.PeerRecoverySourceService;
import org.elasticsearch.dcdr.indices.recovery.PeerRecoveryTargetService;
import org.elasticsearch.dcdr.rest.RestCreateAutoReplicationAction;
import org.elasticsearch.dcdr.rest.RestCreateReplicationAction;
import org.elasticsearch.dcdr.rest.RestDeleteAutoReplicationAction;
import org.elasticsearch.dcdr.rest.RestDeleteReplicationAction;
import org.elasticsearch.dcdr.rest.RestFetchShardInfoAction;
import org.elasticsearch.dcdr.rest.RestGetAutoReplicationAction;
import org.elasticsearch.dcdr.rest.RestGetReplicationAction;
import org.elasticsearch.dcdr.rest.RestPauseReplicationAction;
import org.elasticsearch.dcdr.rest.RestReplicationRecoverAction;
import org.elasticsearch.dcdr.rest.RestReplicationStatsAction;
import org.elasticsearch.dcdr.rest.RestResumeReplicationAction;
import org.elasticsearch.dcdr.translog.primary.DCDRMetadata;
import org.elasticsearch.dcdr.translog.primary.ReplicationService;
import org.elasticsearch.dcdr.translog.primary.ReplicationTemplateService;
import org.elasticsearch.dcdr.translog.replica.bulk.TranslogSyncAction;
import org.elasticsearch.dcdr.translog.replica.bulk.TransportTranslogSyncAction;
import org.elasticsearch.dcdr.translog.replica.index.engine.ReplicaEngineFactory;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.engine.EngineFactory;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.EnginePlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.threadpool.ExecutorBuilder;
import org.elasticsearch.threadpool.ScalingExecutorBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * author weizijun
 * date：2019-08-05
 */
public class DCDR extends Plugin implements ActionPlugin, EnginePlugin {
    public static final String DCDR_THREAD_POOL_NAME = "dcdr";
    public static final String DCDR_RECOVER_THREAD_POOL_NAME = "dcdr_recover";

    @Override
    public Collection<Module> createGuiceModules() {
        List<Module> modules = new ArrayList<>();
        modules.add(b -> b.bind(PeerRecoveryTargetService.class).asEagerSingleton());
        modules.add(b -> b.bind(PeerRecoverySourceService.class).asEagerSingleton());
        modules.add(b -> b.bind(ReplicationService.class).asEagerSingleton());
        modules.add(b -> b.bind(ReplicationTemplateService.class).asEagerSingleton());
        return modules;
    }

    @Override
    public List<org.elasticsearch.common.xcontent.NamedXContentRegistry.Entry> getNamedXContent() {
        return Arrays.asList(
            // Custom Metadata
            new NamedXContentRegistry.Entry(
                MetaData.Custom.class,
                new ParseField(DCDRMetadata.TYPE),
                parser -> DCDRMetadata.PARSER.parse(parser, null)
            )
        );
    }

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Arrays.asList(
            // internal actions
            new ActionHandler<>(CreateReplicationAction.INSTANCE, TransportCreateReplicationAction.class),
            new ActionHandler<>(DeleteReplicationAction.INSTANCE, TransportDeleteReplicationAction.class),
            new ActionHandler<>(TranslogSyncAction.INSTANCE, TransportTranslogSyncAction.class),
            new ActionHandler<>(ReplicationStatsAction.INSTANCE, TransportReplicationStatsAction.class),
            new ActionHandler<>(SwitchReplicationAction.INSTANCE, TransportSwitchReplicationAction.class),
            new ActionHandler<>(CreateAutoReplicationAction.INSTANCE, TransportCreateAutoReplicationAction.class),
            new ActionHandler<>(DeleteAutoReplicationAction.INSTANCE, TransportDeleteAutoReplicationAction.class),
            new ActionHandler<>(GetAutoReplicationAction.INSTANCE, TransportGetAutoReplicationAction.class),
            new ActionHandler<>(GetReplicationAction.INSTANCE, TransportGetReplicationAction.class),
            new ActionHandler<>(ReplicationRecoverAction.INSTANCE, TransportReplicationRecoverAction.class),
            new ActionHandler<>(FetchShardInfoAction.INSTANCE, TransportFetchShardInfoAction.class)
        );
    }

    @Override
    public List<RestHandler> getRestHandlers(
        Settings settings,
        RestController restController,
        ClusterSettings clusterSettings,
        IndexScopedSettings indexScopedSettings,
        SettingsFilter settingsFilter,
        IndexNameExpressionResolver indexNameExpressionResolver,
        Supplier<DiscoveryNodes> nodesInCluster
    ) {
        return Arrays.asList(
            // stats API
            new RestCreateReplicationAction(settings, restController),
            new RestDeleteReplicationAction(settings, restController),
            new RestReplicationStatsAction(settings, restController),
            new RestPauseReplicationAction(settings, restController),
            new RestResumeReplicationAction(settings, restController),
            new RestCreateAutoReplicationAction(settings, restController),
            new RestDeleteAutoReplicationAction(settings, restController),
            new RestGetAutoReplicationAction(settings, restController),
            new RestGetReplicationAction(settings, restController),
            new RestReplicationRecoverAction(settings, restController),
            new RestFetchShardInfoAction(settings, restController)
        );
    }

    @Override
    public List<NamedWriteableRegistry.Entry> getNamedWriteables() {
        return Arrays.asList(
            new NamedWriteableRegistry.Entry(MetaData.Custom.class, DCDRMetadata.TYPE, DCDRMetadata::new),
            new NamedWriteableRegistry.Entry(
                NamedDiff.class,
                DCDRMetadata.TYPE,
                DCDRMetadata.DCDRMetadataDiff::new
            )
        );
    }

    @Override
    public Optional<EngineFactory> getEngineFactory(final IndexSettings indexSettings) {
        if (DCDRSettings.DCDR_REPLICA_INDEX_SETTING.get(indexSettings.getSettings())) {
            return Optional.of(new ReplicaEngineFactory());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(
            DCDRSettings.DCDR_REPLICA_INDEX_SETTING,
            PeerRecoverySourceService.MAX_RECOVER_SOURCE_SHARD_SETTING,
            PeerRecoveryTargetService.MAX_RECOVER_TARGET_SHARD_SETTING
        );
    }

    @Override
    public List<ExecutorBuilder<?>> getExecutorBuilders(Settings settings) {
        return Collections.unmodifiableList(Arrays.asList(
            new ScalingExecutorBuilder(DCDR_THREAD_POOL_NAME, 1, 32, TimeValue.timeValueSeconds(30), "dcdr.dcdr_thread_pool"),
            new ScalingExecutorBuilder(DCDR_RECOVER_THREAD_POOL_NAME, 1, 500, TimeValue.timeValueSeconds(30), "dcdr.dcdr_recover_thread_pool")
        ));
    }

}
