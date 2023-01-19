/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core;

import org.elasticsearch.Version;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.NamedDiff;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.PageCacheRecycler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.indices.breaker.CircuitBreakerService;
import org.elasticsearch.license.DeleteLicenseAction;
import org.elasticsearch.license.GetBasicStatusAction;
import org.elasticsearch.license.GetLicenseAction;
import org.elasticsearch.license.GetTrialStatusAction;
import org.elasticsearch.license.LicenseService;
import org.elasticsearch.license.LicensesMetaData;
import org.elasticsearch.license.PostStartBasicAction;
import org.elasticsearch.license.PostStartTrialAction;
import org.elasticsearch.license.PutLicenseAction;
import org.elasticsearch.persistent.PersistentTaskParams;
import org.elasticsearch.persistent.PersistentTaskState;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.NetworkPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.Transport;
import org.elasticsearch.xpack.core.action.XPackInfoAction;
import org.elasticsearch.xpack.core.action.XPackUsageAction;
import org.elasticsearch.xpack.core.analytics.AnalyticsFeatureSetUsage;
import org.elasticsearch.xpack.core.beats.BeatsFeatureSetUsage;
import org.elasticsearch.xpack.core.ccr.AutoFollowMetadata;
import org.elasticsearch.xpack.core.ccr.CCRFeatureSet;
import org.elasticsearch.xpack.core.deprecation.DeprecationInfoAction;
import org.elasticsearch.xpack.core.enrich.EnrichFeatureSet;
import org.elasticsearch.xpack.core.enrich.action.DeleteEnrichPolicyAction;
import org.elasticsearch.xpack.core.enrich.action.ExecuteEnrichPolicyAction;
import org.elasticsearch.xpack.core.enrich.action.GetEnrichPolicyAction;
import org.elasticsearch.xpack.core.enrich.action.PutEnrichPolicyAction;
import org.elasticsearch.xpack.core.flattened.FlattenedFeatureSetUsage;
import org.elasticsearch.xpack.core.frozen.FrozenIndicesFeatureSetUsage;
import org.elasticsearch.xpack.core.frozen.action.FreezeIndexAction;
import org.elasticsearch.xpack.core.graph.GraphFeatureSetUsage;
import org.elasticsearch.xpack.core.graph.action.GraphExploreAction;
import org.elasticsearch.xpack.core.ilm.AllocateAction;
import org.elasticsearch.xpack.core.ilm.DeleteAction;
import org.elasticsearch.xpack.core.ilm.ForceMergeAction;
import org.elasticsearch.xpack.core.ilm.FreezeAction;
import org.elasticsearch.xpack.core.ilm.IndexLifecycleFeatureSetUsage;
import org.elasticsearch.xpack.core.ilm.IndexLifecycleMetadata;
import org.elasticsearch.xpack.core.ilm.LifecycleAction;
import org.elasticsearch.xpack.core.ilm.LifecycleType;
import org.elasticsearch.xpack.core.ilm.ReadOnlyAction;
import org.elasticsearch.xpack.core.ilm.RolloverAction;
import org.elasticsearch.xpack.core.ilm.SetPriorityAction;
import org.elasticsearch.xpack.core.ilm.ShrinkAction;
import org.elasticsearch.xpack.core.ilm.WaitForSnapshotAction;
import org.elasticsearch.xpack.core.ilm.TimeseriesLifecycleType;
import org.elasticsearch.xpack.core.ilm.UnfollowAction;
import org.elasticsearch.xpack.core.ilm.action.DeleteLifecycleAction;
import org.elasticsearch.xpack.core.ilm.action.ExplainLifecycleAction;
import org.elasticsearch.xpack.core.ilm.action.GetLifecycleAction;
import org.elasticsearch.xpack.core.ilm.action.MoveToStepAction;
import org.elasticsearch.xpack.core.ilm.action.PutLifecycleAction;
import org.elasticsearch.xpack.core.ilm.action.RemoveIndexLifecyclePolicyAction;
import org.elasticsearch.xpack.core.ilm.action.RetryAction;
import org.elasticsearch.xpack.core.logstash.LogstashFeatureSetUsage;
import org.elasticsearch.xpack.core.ml.MachineLearningFeatureSetUsage;
import org.elasticsearch.xpack.core.ml.MlMetadata;
import org.elasticsearch.xpack.core.ml.MlTasks;
import org.elasticsearch.xpack.core.ml.action.CloseJobAction;
import org.elasticsearch.xpack.core.ml.action.DeleteCalendarAction;
import org.elasticsearch.xpack.core.ml.action.DeleteCalendarEventAction;
import org.elasticsearch.xpack.core.ml.action.DeleteDataFrameAnalyticsAction;
import org.elasticsearch.xpack.core.ml.action.DeleteDatafeedAction;
import org.elasticsearch.xpack.core.ml.action.DeleteExpiredDataAction;
import org.elasticsearch.xpack.core.ml.action.DeleteFilterAction;
import org.elasticsearch.xpack.core.ml.action.DeleteForecastAction;
import org.elasticsearch.xpack.core.ml.action.DeleteJobAction;
import org.elasticsearch.xpack.core.ml.action.DeleteModelSnapshotAction;
import org.elasticsearch.xpack.core.ml.action.DeleteTrainedModelAction;
import org.elasticsearch.xpack.core.ml.action.EvaluateDataFrameAction;
import org.elasticsearch.xpack.core.ml.action.ExplainDataFrameAnalyticsAction;
import org.elasticsearch.xpack.core.ml.action.FinalizeJobExecutionAction;
import org.elasticsearch.xpack.core.ml.action.FindFileStructureAction;
import org.elasticsearch.xpack.core.ml.action.FlushJobAction;
import org.elasticsearch.xpack.core.ml.action.ForecastJobAction;
import org.elasticsearch.xpack.core.ml.action.GetBucketsAction;
import org.elasticsearch.xpack.core.ml.action.GetCalendarEventsAction;
import org.elasticsearch.xpack.core.ml.action.GetCalendarsAction;
import org.elasticsearch.xpack.core.ml.action.GetCategoriesAction;
import org.elasticsearch.xpack.core.ml.action.GetDataFrameAnalyticsAction;
import org.elasticsearch.xpack.core.ml.action.GetDataFrameAnalyticsStatsAction;
import org.elasticsearch.xpack.core.ml.action.GetDatafeedsAction;
import org.elasticsearch.xpack.core.ml.action.GetDatafeedsStatsAction;
import org.elasticsearch.xpack.core.ml.action.GetFiltersAction;
import org.elasticsearch.xpack.core.ml.action.GetInfluencersAction;
import org.elasticsearch.xpack.core.ml.action.GetJobsAction;
import org.elasticsearch.xpack.core.ml.action.GetJobsStatsAction;
import org.elasticsearch.xpack.core.ml.action.GetModelSnapshotsAction;
import org.elasticsearch.xpack.core.ml.action.GetOverallBucketsAction;
import org.elasticsearch.xpack.core.ml.action.GetRecordsAction;
import org.elasticsearch.xpack.core.ml.action.GetTrainedModelsAction;
import org.elasticsearch.xpack.core.ml.action.GetTrainedModelsStatsAction;
import org.elasticsearch.xpack.core.ml.action.InternalInferModelAction;
import org.elasticsearch.xpack.core.ml.action.IsolateDatafeedAction;
import org.elasticsearch.xpack.core.ml.action.KillProcessAction;
import org.elasticsearch.xpack.core.ml.action.MlInfoAction;
import org.elasticsearch.xpack.core.ml.action.OpenJobAction;
import org.elasticsearch.xpack.core.ml.action.PersistJobAction;
import org.elasticsearch.xpack.core.ml.action.PostCalendarEventsAction;
import org.elasticsearch.xpack.core.ml.action.PostDataAction;
import org.elasticsearch.xpack.core.ml.action.PreviewDatafeedAction;
import org.elasticsearch.xpack.core.ml.action.PutCalendarAction;
import org.elasticsearch.xpack.core.ml.action.PutDataFrameAnalyticsAction;
import org.elasticsearch.xpack.core.ml.action.PutDatafeedAction;
import org.elasticsearch.xpack.core.ml.action.PutFilterAction;
import org.elasticsearch.xpack.core.ml.action.PutJobAction;
import org.elasticsearch.xpack.core.ml.action.PutTrainedModelAction;
import org.elasticsearch.xpack.core.ml.action.RevertModelSnapshotAction;
import org.elasticsearch.xpack.core.ml.action.SetUpgradeModeAction;
import org.elasticsearch.xpack.core.ml.action.StartDataFrameAnalyticsAction;
import org.elasticsearch.xpack.core.ml.action.StartDatafeedAction;
import org.elasticsearch.xpack.core.ml.action.StopDataFrameAnalyticsAction;
import org.elasticsearch.xpack.core.ml.action.StopDatafeedAction;
import org.elasticsearch.xpack.core.ml.action.UpdateCalendarJobAction;
import org.elasticsearch.xpack.core.ml.action.UpdateDatafeedAction;
import org.elasticsearch.xpack.core.ml.action.UpdateFilterAction;
import org.elasticsearch.xpack.core.ml.action.UpdateJobAction;
import org.elasticsearch.xpack.core.ml.action.UpdateModelSnapshotAction;
import org.elasticsearch.xpack.core.ml.action.UpdateProcessAction;
import org.elasticsearch.xpack.core.ml.action.ValidateDetectorAction;
import org.elasticsearch.xpack.core.ml.action.ValidateJobConfigAction;
import org.elasticsearch.xpack.core.ml.datafeed.DatafeedState;
import org.elasticsearch.xpack.core.ml.dataframe.DataFrameAnalyticsTaskState;
import org.elasticsearch.xpack.core.ml.dataframe.analyses.Classification;
import org.elasticsearch.xpack.core.ml.dataframe.analyses.DataFrameAnalysis;
import org.elasticsearch.xpack.core.ml.dataframe.analyses.OutlierDetection;
import org.elasticsearch.xpack.core.ml.dataframe.analyses.Regression;
import org.elasticsearch.xpack.core.ml.dataframe.evaluation.MlEvaluationNamedXContentProvider;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.CustomWordEmbedding;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.FrequencyEncoding;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.OneHotEncoding;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.PreProcessor;
import org.elasticsearch.xpack.core.ml.inference.preprocessing.TargetMeanEncoding;
import org.elasticsearch.xpack.core.ml.inference.results.ClassificationInferenceResults;
import org.elasticsearch.xpack.core.ml.inference.results.InferenceResults;
import org.elasticsearch.xpack.core.ml.inference.results.RegressionInferenceResults;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ClassificationConfig;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.InferenceConfig;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.RegressionConfig;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.TrainedModel;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.Ensemble;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.LogisticRegression;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.OutputAggregator;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.WeightedMode;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.ensemble.WeightedSum;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.langident.LangIdentNeuralNetwork;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.tree.Tree;
import org.elasticsearch.xpack.core.ml.job.config.JobTaskState;
import org.elasticsearch.xpack.core.monitoring.MonitoringFeatureSetUsage;
import org.elasticsearch.xpack.core.rollup.RollupFeatureSetUsage;
import org.elasticsearch.xpack.core.rollup.RollupField;
import org.elasticsearch.xpack.core.rollup.action.DeleteRollupJobAction;
import org.elasticsearch.xpack.core.rollup.action.GetRollupCapsAction;
import org.elasticsearch.xpack.core.rollup.action.GetRollupJobsAction;
import org.elasticsearch.xpack.core.rollup.action.PutRollupJobAction;
import org.elasticsearch.xpack.core.rollup.action.RollupSearchAction;
import org.elasticsearch.xpack.core.rollup.action.StartRollupJobAction;
import org.elasticsearch.xpack.core.rollup.action.StopRollupJobAction;
import org.elasticsearch.xpack.core.rollup.job.RollupJob;
import org.elasticsearch.xpack.core.rollup.job.RollupJobStatus;
import org.elasticsearch.xpack.core.security.SecurityFeatureSetUsage;
import org.elasticsearch.xpack.core.security.SecurityField;
import org.elasticsearch.xpack.core.security.SecuritySettings;
import org.elasticsearch.xpack.core.security.action.CreateApiKeyAction;
import org.elasticsearch.xpack.core.security.action.GetApiKeyAction;
import org.elasticsearch.xpack.core.security.action.InvalidateApiKeyAction;
import org.elasticsearch.xpack.core.security.action.realm.ClearRealmCacheAction;
import org.elasticsearch.xpack.core.security.action.role.ClearRolesCacheAction;
import org.elasticsearch.xpack.core.security.action.role.DeleteRoleAction;
import org.elasticsearch.xpack.core.security.action.role.GetRolesAction;
import org.elasticsearch.xpack.core.security.action.role.PutRoleAction;
import org.elasticsearch.xpack.core.security.action.rolemapping.DeleteRoleMappingAction;
import org.elasticsearch.xpack.core.security.action.rolemapping.GetRoleMappingsAction;
import org.elasticsearch.xpack.core.security.action.rolemapping.PutRoleMappingAction;
import org.elasticsearch.xpack.core.security.action.token.CreateTokenAction;
import org.elasticsearch.xpack.core.security.action.token.InvalidateTokenAction;
import org.elasticsearch.xpack.core.security.action.token.RefreshTokenAction;
import org.elasticsearch.xpack.core.security.action.user.AuthenticateAction;
import org.elasticsearch.xpack.core.security.action.user.ChangePasswordAction;
import org.elasticsearch.xpack.core.security.action.user.DeleteUserAction;
import org.elasticsearch.xpack.core.security.action.user.GetUsersAction;
import org.elasticsearch.xpack.core.security.action.user.HasPrivilegesAction;
import org.elasticsearch.xpack.core.security.action.user.PutUserAction;
import org.elasticsearch.xpack.core.security.action.user.SetEnabledAction;
import org.elasticsearch.xpack.core.security.authc.TokenMetaData;
import org.elasticsearch.xpack.core.security.authc.support.mapper.expressiondsl.AllExpression;
import org.elasticsearch.xpack.core.security.authc.support.mapper.expressiondsl.AnyExpression;
import org.elasticsearch.xpack.core.security.authc.support.mapper.expressiondsl.ExceptExpression;
import org.elasticsearch.xpack.core.security.authc.support.mapper.expressiondsl.FieldExpression;
import org.elasticsearch.xpack.core.security.authc.support.mapper.expressiondsl.RoleMapperExpression;
import org.elasticsearch.xpack.core.security.authz.privilege.ConfigurableClusterPrivilege;
import org.elasticsearch.xpack.core.security.authz.privilege.ConfigurableClusterPrivileges;
import org.elasticsearch.xpack.core.security.transport.netty4.SecurityNetty4Transport;
import org.elasticsearch.xpack.core.slm.SLMFeatureSetUsage;
import org.elasticsearch.xpack.core.slm.SnapshotLifecycleMetadata;
import org.elasticsearch.xpack.core.slm.action.DeleteSnapshotLifecycleAction;
import org.elasticsearch.xpack.core.slm.action.ExecuteSnapshotLifecycleAction;
import org.elasticsearch.xpack.core.slm.action.GetSnapshotLifecycleAction;
import org.elasticsearch.xpack.core.slm.action.GetSnapshotLifecycleStatsAction;
import org.elasticsearch.xpack.core.slm.action.PutSnapshotLifecycleAction;
import org.elasticsearch.xpack.core.spatial.SpatialFeatureSetUsage;
import org.elasticsearch.xpack.core.sql.SqlFeatureSetUsage;
import org.elasticsearch.xpack.core.ssl.SSLService;
import org.elasticsearch.xpack.core.ssl.action.GetCertificateInfoAction;
import org.elasticsearch.xpack.core.transform.TransformFeatureSetUsage;
import org.elasticsearch.xpack.core.transform.TransformField;
import org.elasticsearch.xpack.core.transform.action.DeleteTransformAction;
import org.elasticsearch.xpack.core.transform.action.GetTransformAction;
import org.elasticsearch.xpack.core.transform.action.GetTransformStatsAction;
import org.elasticsearch.xpack.core.transform.action.PreviewTransformAction;
import org.elasticsearch.xpack.core.transform.action.PutTransformAction;
import org.elasticsearch.xpack.core.transform.action.StartTransformAction;
import org.elasticsearch.xpack.core.transform.action.StopTransformAction;
import org.elasticsearch.xpack.core.transform.transforms.SyncConfig;
import org.elasticsearch.xpack.core.transform.transforms.TimeSyncConfig;
import org.elasticsearch.xpack.core.transform.transforms.TransformState;
import org.elasticsearch.xpack.core.transform.transforms.TransformTaskParams;
import org.elasticsearch.xpack.core.upgrade.actions.IndexUpgradeAction;
import org.elasticsearch.xpack.core.upgrade.actions.IndexUpgradeInfoAction;
import org.elasticsearch.xpack.core.vectors.VectorsFeatureSetUsage;
import org.elasticsearch.xpack.core.votingonly.VotingOnlyNodeFeatureSetUsage;
import org.elasticsearch.xpack.core.watcher.WatcherFeatureSetUsage;
import org.elasticsearch.xpack.core.watcher.WatcherMetaData;
import org.elasticsearch.xpack.core.watcher.transport.actions.ack.AckWatchAction;
import org.elasticsearch.xpack.core.watcher.transport.actions.activate.ActivateWatchAction;
import org.elasticsearch.xpack.core.watcher.transport.actions.delete.DeleteWatchAction;
import org.elasticsearch.xpack.core.watcher.transport.actions.execute.ExecuteWatchAction;
import org.elasticsearch.xpack.core.watcher.transport.actions.get.GetWatchAction;
import org.elasticsearch.xpack.core.watcher.transport.actions.put.PutWatchAction;
import org.elasticsearch.xpack.core.watcher.transport.actions.service.WatcherServiceAction;
import org.elasticsearch.xpack.core.watcher.transport.actions.stats.WatcherStatsAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class XPackClientPlugin extends Plugin implements ActionPlugin, NetworkPlugin {

    static Optional<String> X_PACK_FEATURE = Optional.of("x-pack");

    @Override
    protected Optional<String> getFeature() {
        return X_PACK_FEATURE;
    }

    private final Settings settings;

    public XPackClientPlugin(final Settings settings) {
        this.settings = settings;
    }

    @Override
    public List<Setting<?>> getSettings() {
        ArrayList<Setting<?>> settings = new ArrayList<>();
        // the only licensing one
        settings.add(Setting.groupSetting("license.", Setting.Property.NodeScope));

        //TODO split these settings up
        settings.addAll(XPackSettings.getAllSettings());

        settings.add(LicenseService.SELF_GENERATED_LICENSE_TYPE);
        settings.add(LicenseService.ALLOWED_LICENSE_TYPES_SETTING);

        // we add the `xpack.version` setting to all internal indices
        settings.add(Setting.simpleString("index.xpack.version", Setting.Property.IndexScope));

        return settings;
    }

    @Override
    public Settings additionalSettings() {
        return additionalSettings(settings, XPackSettings.SECURITY_ENABLED.get(settings), XPackPlugin.transportClientMode(settings));
    }

    static Settings additionalSettings(final Settings settings, final boolean enabled, final boolean transportClientMode) {
        if (enabled && transportClientMode) {
            return Settings.builder()
                    .put(SecuritySettings.addTransportSettings(settings))
                    .put(SecuritySettings.addUserSettings(settings))
                    .build();
        } else {
            return Settings.EMPTY;
        }
    }

    @Override
    public List<ActionType<? extends ActionResponse>> getClientActions() {
        return Arrays.asList(
                // deprecation
                DeprecationInfoAction.INSTANCE,
                // graph
                GraphExploreAction.INSTANCE,
                // ML
                GetJobsAction.INSTANCE,
                GetJobsStatsAction.INSTANCE,
                MlInfoAction.INSTANCE,
                PutJobAction.INSTANCE,
                UpdateJobAction.INSTANCE,
                DeleteJobAction.INSTANCE,
                OpenJobAction.INSTANCE,
                GetFiltersAction.INSTANCE,
                PutFilterAction.INSTANCE,
                UpdateFilterAction.INSTANCE,
                DeleteFilterAction.INSTANCE,
                KillProcessAction.INSTANCE,
                GetBucketsAction.INSTANCE,
                GetInfluencersAction.INSTANCE,
                GetOverallBucketsAction.INSTANCE,
                GetRecordsAction.INSTANCE,
                PostDataAction.INSTANCE,
                CloseJobAction.INSTANCE,
                FinalizeJobExecutionAction.INSTANCE,
                FlushJobAction.INSTANCE,
                ValidateDetectorAction.INSTANCE,
                ValidateJobConfigAction.INSTANCE,
                GetCategoriesAction.INSTANCE,
                GetModelSnapshotsAction.INSTANCE,
                RevertModelSnapshotAction.INSTANCE,
                UpdateModelSnapshotAction.INSTANCE,
                GetDatafeedsAction.INSTANCE,
                GetDatafeedsStatsAction.INSTANCE,
                PutDatafeedAction.INSTANCE,
                UpdateDatafeedAction.INSTANCE,
                DeleteDatafeedAction.INSTANCE,
                PreviewDatafeedAction.INSTANCE,
                StartDatafeedAction.INSTANCE,
                StopDatafeedAction.INSTANCE,
                IsolateDatafeedAction.INSTANCE,
                DeleteModelSnapshotAction.INSTANCE,
                UpdateProcessAction.INSTANCE,
                DeleteExpiredDataAction.INSTANCE,
                ForecastJobAction.INSTANCE,
                DeleteForecastAction.INSTANCE,
                GetCalendarsAction.INSTANCE,
                PutCalendarAction.INSTANCE,
                DeleteCalendarAction.INSTANCE,
                DeleteCalendarEventAction.INSTANCE,
                UpdateCalendarJobAction.INSTANCE,
                GetCalendarEventsAction.INSTANCE,
                PostCalendarEventsAction.INSTANCE,
                PersistJobAction.INSTANCE,
                FindFileStructureAction.INSTANCE,
                SetUpgradeModeAction.INSTANCE,
                PutDataFrameAnalyticsAction.INSTANCE,
                GetDataFrameAnalyticsAction.INSTANCE,
                GetDataFrameAnalyticsStatsAction.INSTANCE,
                DeleteDataFrameAnalyticsAction.INSTANCE,
                StartDataFrameAnalyticsAction.INSTANCE,
                StopDataFrameAnalyticsAction.INSTANCE,
                EvaluateDataFrameAction.INSTANCE,
                ExplainDataFrameAnalyticsAction.INSTANCE,
                InternalInferModelAction.INSTANCE,
                GetTrainedModelsAction.INSTANCE,
                DeleteTrainedModelAction.INSTANCE,
                GetTrainedModelsStatsAction.INSTANCE,
                PutTrainedModelAction.INSTANCE,
                // security
                ClearRealmCacheAction.INSTANCE,
                ClearRolesCacheAction.INSTANCE,
                GetUsersAction.INSTANCE,
                PutUserAction.INSTANCE,
                DeleteUserAction.INSTANCE,
                GetRolesAction.INSTANCE,
                PutRoleAction.INSTANCE,
                DeleteRoleAction.INSTANCE,
                ChangePasswordAction.INSTANCE,
                AuthenticateAction.INSTANCE,
                SetEnabledAction.INSTANCE,
                HasPrivilegesAction.INSTANCE,
                GetRoleMappingsAction.INSTANCE,
                PutRoleMappingAction.INSTANCE,
                DeleteRoleMappingAction.INSTANCE,
                CreateTokenAction.INSTANCE,
                InvalidateTokenAction.INSTANCE,
                GetCertificateInfoAction.INSTANCE,
                RefreshTokenAction.INSTANCE,
                CreateApiKeyAction.INSTANCE,
                InvalidateApiKeyAction.INSTANCE,
                GetApiKeyAction.INSTANCE,
                // upgrade
                IndexUpgradeInfoAction.INSTANCE,
                IndexUpgradeAction.INSTANCE,
                // watcher
                PutWatchAction.INSTANCE,
                DeleteWatchAction.INSTANCE,
                GetWatchAction.INSTANCE,
                WatcherStatsAction.INSTANCE,
                AckWatchAction.INSTANCE,
                ActivateWatchAction.INSTANCE,
                WatcherServiceAction.INSTANCE,
                ExecuteWatchAction.INSTANCE,
                // license
                PutLicenseAction.INSTANCE,
                GetLicenseAction.INSTANCE,
                DeleteLicenseAction.INSTANCE,
                PostStartTrialAction.INSTANCE,
                GetTrialStatusAction.INSTANCE,
                PostStartBasicAction.INSTANCE,
                GetBasicStatusAction.INSTANCE,
                // x-pack
                XPackInfoAction.INSTANCE,
                XPackUsageAction.INSTANCE,
                // rollup
                RollupSearchAction.INSTANCE,
                PutRollupJobAction.INSTANCE,
                StartRollupJobAction.INSTANCE,
                StopRollupJobAction.INSTANCE,
                DeleteRollupJobAction.INSTANCE,
                GetRollupJobsAction.INSTANCE,
                GetRollupCapsAction.INSTANCE,
                // ILM
                DeleteLifecycleAction.INSTANCE,
                GetLifecycleAction.INSTANCE,
                PutLifecycleAction.INSTANCE,
                ExplainLifecycleAction.INSTANCE,
                RemoveIndexLifecyclePolicyAction.INSTANCE,
                MoveToStepAction.INSTANCE,
                RetryAction.INSTANCE,
                PutSnapshotLifecycleAction.INSTANCE,
                GetSnapshotLifecycleAction.INSTANCE,
                DeleteSnapshotLifecycleAction.INSTANCE,
                ExecuteSnapshotLifecycleAction.INSTANCE,
                GetSnapshotLifecycleStatsAction.INSTANCE,
                // Freeze
                FreezeIndexAction.INSTANCE,
                // Data Frame
                PutTransformAction.INSTANCE,
                StartTransformAction.INSTANCE,
                StopTransformAction.INSTANCE,
                DeleteTransformAction.INSTANCE,
                GetTransformAction.INSTANCE,
                GetTransformStatsAction.INSTANCE,
                PreviewTransformAction.INSTANCE,
                // enrich
                DeleteEnrichPolicyAction.INSTANCE,
                ExecuteEnrichPolicyAction.INSTANCE,
                GetEnrichPolicyAction.INSTANCE,
                PutEnrichPolicyAction.INSTANCE
            );
    }

    @Override
    public List<NamedWriteableRegistry.Entry> getNamedWriteables() {
        return Stream.concat(
            Arrays.asList(
                // graph
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.GRAPH, GraphFeatureSetUsage::new),
                // logstash
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.LOGSTASH, LogstashFeatureSetUsage::new),
                // beats
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.BEATS, BeatsFeatureSetUsage::new),
                // ML - Custom metadata
                new NamedWriteableRegistry.Entry(MetaData.Custom.class, "ml", MlMetadata::new),
                new NamedWriteableRegistry.Entry(NamedDiff.class, "ml", MlMetadata.MlMetadataDiff::new),
                // ML - Persistent action requests
                new NamedWriteableRegistry.Entry(PersistentTaskParams.class, MlTasks.DATAFEED_TASK_NAME,
                        StartDatafeedAction.DatafeedParams::new),
                new NamedWriteableRegistry.Entry(PersistentTaskParams.class, MlTasks.JOB_TASK_NAME,
                        OpenJobAction.JobParams::new),
                new NamedWriteableRegistry.Entry(PersistentTaskParams.class, MlTasks.DATA_FRAME_ANALYTICS_TASK_NAME,
                    StartDataFrameAnalyticsAction.TaskParams::new),
                // ML - Task states
                new NamedWriteableRegistry.Entry(PersistentTaskState.class, JobTaskState.NAME, JobTaskState::new),
                new NamedWriteableRegistry.Entry(PersistentTaskState.class, DatafeedState.NAME, DatafeedState::fromStream),
                new NamedWriteableRegistry.Entry(PersistentTaskState.class, DataFrameAnalyticsTaskState.NAME,
                    DataFrameAnalyticsTaskState::new),
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.MACHINE_LEARNING,
                        MachineLearningFeatureSetUsage::new),
                // ML - Data frame analytics
                new NamedWriteableRegistry.Entry(DataFrameAnalysis.class, OutlierDetection.NAME.getPreferredName(), OutlierDetection::new),
                new NamedWriteableRegistry.Entry(DataFrameAnalysis.class, Regression.NAME.getPreferredName(), Regression::new),
                new NamedWriteableRegistry.Entry(DataFrameAnalysis.class, Classification.NAME.getPreferredName(), Classification::new),
                // ML - Inference preprocessing
                new NamedWriteableRegistry.Entry(PreProcessor.class, FrequencyEncoding.NAME.getPreferredName(), FrequencyEncoding::new),
                new NamedWriteableRegistry.Entry(PreProcessor.class, OneHotEncoding.NAME.getPreferredName(), OneHotEncoding::new),
                new NamedWriteableRegistry.Entry(PreProcessor.class, TargetMeanEncoding.NAME.getPreferredName(), TargetMeanEncoding::new),
                new NamedWriteableRegistry.Entry(PreProcessor.class, CustomWordEmbedding.NAME.getPreferredName(), CustomWordEmbedding::new),
                // ML - Inference models
                new NamedWriteableRegistry.Entry(TrainedModel.class, Tree.NAME.getPreferredName(), Tree::new),
                new NamedWriteableRegistry.Entry(TrainedModel.class, Ensemble.NAME.getPreferredName(), Ensemble::new),
                new NamedWriteableRegistry.Entry(TrainedModel.class,
                    LangIdentNeuralNetwork.NAME.getPreferredName(),
                    LangIdentNeuralNetwork::new),
                // ML - Inference aggregators
                new NamedWriteableRegistry.Entry(OutputAggregator.class, WeightedSum.NAME.getPreferredName(), WeightedSum::new),
                new NamedWriteableRegistry.Entry(OutputAggregator.class, WeightedMode.NAME.getPreferredName(), WeightedMode::new),
                new NamedWriteableRegistry.Entry(OutputAggregator.class,
                    LogisticRegression.NAME.getPreferredName(),
                    LogisticRegression::new),
                // ML - Inference Results
                new NamedWriteableRegistry.Entry(InferenceResults.class,
                    ClassificationInferenceResults.NAME,
                    ClassificationInferenceResults::new),
                new NamedWriteableRegistry.Entry(InferenceResults.class,
                    RegressionInferenceResults.NAME,
                    RegressionInferenceResults::new),
                // ML - Inference Configuration
                new NamedWriteableRegistry.Entry(InferenceConfig.class, ClassificationConfig.NAME, ClassificationConfig::new),
                new NamedWriteableRegistry.Entry(InferenceConfig.class, RegressionConfig.NAME, RegressionConfig::new),

                // monitoring
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.MONITORING, MonitoringFeatureSetUsage::new),
                // security
                new NamedWriteableRegistry.Entry(ClusterState.Custom.class, TokenMetaData.TYPE, TokenMetaData::new),
                new NamedWriteableRegistry.Entry(NamedDiff.class, TokenMetaData.TYPE, TokenMetaData::readDiffFrom),
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.SECURITY, SecurityFeatureSetUsage::new),
                // security : conditional privileges
                new NamedWriteableRegistry.Entry(ConfigurableClusterPrivilege.class,
                    ConfigurableClusterPrivileges.ManageApplicationPrivileges.WRITEABLE_NAME,
                    ConfigurableClusterPrivileges.ManageApplicationPrivileges::createFrom),
                // security : role-mappings
                new NamedWriteableRegistry.Entry(RoleMapperExpression.class, AllExpression.NAME, AllExpression::new),
                new NamedWriteableRegistry.Entry(RoleMapperExpression.class, AnyExpression.NAME, AnyExpression::new),
                new NamedWriteableRegistry.Entry(RoleMapperExpression.class, FieldExpression.NAME, FieldExpression::new),
                new NamedWriteableRegistry.Entry(RoleMapperExpression.class, ExceptExpression.NAME, ExceptExpression::new),
                // sql
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.SQL, SqlFeatureSetUsage::new),
                // watcher
                new NamedWriteableRegistry.Entry(MetaData.Custom.class, WatcherMetaData.TYPE, WatcherMetaData::new),
                new NamedWriteableRegistry.Entry(NamedDiff.class, WatcherMetaData.TYPE, WatcherMetaData::readDiffFrom),
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.WATCHER, WatcherFeatureSetUsage::new),
                // licensing
                new NamedWriteableRegistry.Entry(MetaData.Custom.class, LicensesMetaData.TYPE, LicensesMetaData::new),
                new NamedWriteableRegistry.Entry(NamedDiff.class, LicensesMetaData.TYPE, LicensesMetaData::readDiffFrom),
                // rollup
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.ROLLUP, RollupFeatureSetUsage::new),
                new NamedWriteableRegistry.Entry(PersistentTaskParams.class, RollupJob.NAME, RollupJob::new),
                new NamedWriteableRegistry.Entry(Task.Status.class, RollupJobStatus.NAME, RollupJobStatus::new),
                new NamedWriteableRegistry.Entry(PersistentTaskState.class, RollupJobStatus.NAME, RollupJobStatus::new),
                // ccr
                new NamedWriteableRegistry.Entry(AutoFollowMetadata.class, AutoFollowMetadata.TYPE, AutoFollowMetadata::new),
                new NamedWriteableRegistry.Entry(MetaData.Custom.class, AutoFollowMetadata.TYPE, AutoFollowMetadata::new),
                new NamedWriteableRegistry.Entry(NamedDiff.class, AutoFollowMetadata.TYPE,
                    in -> AutoFollowMetadata.readDiffFrom(MetaData.Custom.class, AutoFollowMetadata.TYPE, in)),
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.CCR, CCRFeatureSet.Usage::new),
                // ILM
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.INDEX_LIFECYCLE,
                    IndexLifecycleFeatureSetUsage::new),
                // SLM
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.SNAPSHOT_LIFECYCLE,
                    SLMFeatureSetUsage::new),
                // ILM - Custom Metadata
                new NamedWriteableRegistry.Entry(MetaData.Custom.class, IndexLifecycleMetadata.TYPE, IndexLifecycleMetadata::new),
                new NamedWriteableRegistry.Entry(NamedDiff.class, IndexLifecycleMetadata.TYPE,
                    IndexLifecycleMetadata.IndexLifecycleMetadataDiff::new),
                new NamedWriteableRegistry.Entry(MetaData.Custom.class, SnapshotLifecycleMetadata.TYPE, SnapshotLifecycleMetadata::new),
                new NamedWriteableRegistry.Entry(NamedDiff.class, SnapshotLifecycleMetadata.TYPE,
                    SnapshotLifecycleMetadata.SnapshotLifecycleMetadataDiff::new),
                // ILM - LifecycleTypes
                new NamedWriteableRegistry.Entry(LifecycleType.class, TimeseriesLifecycleType.TYPE,
                    (in) -> TimeseriesLifecycleType.INSTANCE),
                // ILM - Lifecycle Actions
                new NamedWriteableRegistry.Entry(LifecycleAction.class, AllocateAction.NAME, AllocateAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, ForceMergeAction.NAME, ForceMergeAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, ReadOnlyAction.NAME, ReadOnlyAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, RolloverAction.NAME, RolloverAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, ShrinkAction.NAME, ShrinkAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, DeleteAction.NAME, DeleteAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, FreezeAction.NAME, FreezeAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, SetPriorityAction.NAME, SetPriorityAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, UnfollowAction.NAME, UnfollowAction::new),
                new NamedWriteableRegistry.Entry(LifecycleAction.class, WaitForSnapshotAction.NAME, WaitForSnapshotAction::new),
                // Data Frame
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.TRANSFORM, TransformFeatureSetUsage::new),
                new NamedWriteableRegistry.Entry(PersistentTaskParams.class, TransformField.TASK_NAME, TransformTaskParams::new),
                new NamedWriteableRegistry.Entry(Task.Status.class, TransformField.TASK_NAME, TransformState::new),
                new NamedWriteableRegistry.Entry(PersistentTaskState.class, TransformField.TASK_NAME, TransformState::new),
                new NamedWriteableRegistry.Entry(SyncConfig.class, TransformField.TIME_BASED_SYNC.getPreferredName(), TimeSyncConfig::new),
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.FLATTENED, FlattenedFeatureSetUsage::new),
                // Vectors
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.VECTORS, VectorsFeatureSetUsage::new),
                // Voting Only Node
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.VOTING_ONLY, VotingOnlyNodeFeatureSetUsage::new),
                // Frozen indices
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.FROZEN_INDICES, FrozenIndicesFeatureSetUsage::new),
                // Spatial
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.SPATIAL, SpatialFeatureSetUsage::new),
                // analytics
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.ANALYTICS, AnalyticsFeatureSetUsage::new),
                // Enrich
                new NamedWriteableRegistry.Entry(XPackFeatureSet.Usage.class, XPackField.ENRICH, EnrichFeatureSet.Usage::new)
            ).stream(),
            MlEvaluationNamedXContentProvider.getNamedWriteables().stream()
        ).collect(toList());
    }

    @Override
    public List<NamedXContentRegistry.Entry> getNamedXContent() {
        return Arrays.asList(
                // ML - Custom metadata
                new NamedXContentRegistry.Entry(MetaData.Custom.class, new ParseField("ml"),
                        parser -> MlMetadata.LENIENT_PARSER.parse(parser, null).build()),
                // ML - Persistent action requests
                new NamedXContentRegistry.Entry(PersistentTaskParams.class, new ParseField(MlTasks.DATAFEED_TASK_NAME),
                        StartDatafeedAction.DatafeedParams::fromXContent),
                new NamedXContentRegistry.Entry(PersistentTaskParams.class, new ParseField(MlTasks.JOB_TASK_NAME),
                        OpenJobAction.JobParams::fromXContent),
                new NamedXContentRegistry.Entry(PersistentTaskParams.class, new ParseField(MlTasks.DATA_FRAME_ANALYTICS_TASK_NAME),
                        StartDataFrameAnalyticsAction.TaskParams::fromXContent),
                // ML - Task states
                new NamedXContentRegistry.Entry(PersistentTaskState.class, new ParseField(DatafeedState.NAME), DatafeedState::fromXContent),
                new NamedXContentRegistry.Entry(PersistentTaskState.class, new ParseField(JobTaskState.NAME), JobTaskState::fromXContent),
                new NamedXContentRegistry.Entry(PersistentTaskState.class, new ParseField(DataFrameAnalyticsTaskState.NAME),
                    DataFrameAnalyticsTaskState::fromXContent),
                // watcher
                new NamedXContentRegistry.Entry(MetaData.Custom.class, new ParseField(WatcherMetaData.TYPE),
                        WatcherMetaData::fromXContent),
                // licensing
                new NamedXContentRegistry.Entry(MetaData.Custom.class, new ParseField(LicensesMetaData.TYPE),
                        LicensesMetaData::fromXContent),
                //rollup
                new NamedXContentRegistry.Entry(PersistentTaskParams.class, new ParseField(RollupField.TASK_NAME),
                        RollupJob::fromXContent),
                new NamedXContentRegistry.Entry(Task.Status.class, new ParseField(RollupJobStatus.NAME),
                        RollupJobStatus::fromXContent),
                new NamedXContentRegistry.Entry(PersistentTaskState.class, new ParseField(RollupJobStatus.NAME),
                        RollupJobStatus::fromXContent),
                // Data Frame
                new NamedXContentRegistry.Entry(PersistentTaskParams.class, new ParseField(TransformField.TASK_NAME),
                        TransformTaskParams::fromXContent),
                new NamedXContentRegistry.Entry(Task.Status.class, new ParseField(TransformField.TASK_NAME),
                        TransformState::fromXContent),
                new NamedXContentRegistry.Entry(PersistentTaskState.class, new ParseField(TransformField.TASK_NAME),
                        TransformState::fromXContent)
        );
    }

    @Override
    public Map<String, Supplier<Transport>> getTransports(
            final Settings settings,
            final ThreadPool threadPool,
            final PageCacheRecycler pageCacheRecycler,
            final CircuitBreakerService circuitBreakerService,
            final NamedWriteableRegistry namedWriteableRegistry,
            final NetworkService networkService) {
        // this should only be used in the transport layer, so do not add it if it is not in transport mode or we are disabled
        if (XPackPlugin.transportClientMode(settings) == false || XPackSettings.SECURITY_ENABLED.get(settings) == false) {
            return Collections.emptyMap();
        }
        final SSLService sslService;
        try {
            sslService = new SSLService(settings, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Collections.singletonMap(SecurityField.NAME4, () -> new SecurityNetty4Transport(settings, Version.CURRENT, threadPool,
                networkService, pageCacheRecycler, namedWriteableRegistry, circuitBreakerService, sslService));
    }

}
