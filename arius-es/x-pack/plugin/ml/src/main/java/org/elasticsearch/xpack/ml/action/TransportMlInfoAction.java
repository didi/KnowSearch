/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.xpack.core.ml.MachineLearningField;
import org.elasticsearch.xpack.core.ml.MlMetadata;
import org.elasticsearch.xpack.core.ml.action.MlInfoAction;
import org.elasticsearch.xpack.core.ml.datafeed.DatafeedConfig;
import org.elasticsearch.xpack.core.ml.job.config.AnalysisLimits;
import org.elasticsearch.xpack.core.ml.job.config.CategorizationAnalyzerConfig;
import org.elasticsearch.xpack.core.ml.job.config.Job;
import org.elasticsearch.xpack.ml.process.NativeController;
import org.elasticsearch.xpack.ml.process.NativeControllerHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class TransportMlInfoAction extends HandledTransportAction<MlInfoAction.Request, MlInfoAction.Response> {

    private static final Logger logger = LogManager.getLogger(TransportMlInfoAction.class);

    private final ClusterService clusterService;
    private final NamedXContentRegistry xContentRegistry;
    private final Map<String, Object> nativeCodeInfo;

    @Inject
    public TransportMlInfoAction(TransportService transportService, ActionFilters actionFilters, ClusterService clusterService,
                                 NamedXContentRegistry xContentRegistry, Environment env) {
        super(MlInfoAction.NAME, transportService, actionFilters, MlInfoAction.Request::new);
        this.clusterService = clusterService;
        this.xContentRegistry = xContentRegistry;

        try {
            NativeController nativeController = NativeControllerHolder.getNativeController(clusterService.getNodeName(), env);
            // TODO: this leniency is only for tests. it can be removed when NativeController is created as a component and
            // becomes a ctor arg to this action
            if (nativeController != null) {
                nativeCodeInfo = nativeController.getNativeCodeInfo();
            } else {
                nativeCodeInfo = Collections.emptyMap();
            }
        } catch (IOException e) {
            // this should not be possible since this action is only registered when ML is enabled,
            // and the MachineLearning plugin would have failed to create components
            throw new IllegalStateException("native controller failed to load", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Could not get native code info from native controller", e);
        }
    }

    @Override
    protected void doExecute(Task task, MlInfoAction.Request request, ActionListener<MlInfoAction.Response> listener) {
        Map<String, Object> info = new HashMap<>();
        info.put("defaults", defaults());
        info.put("limits", limits());
        info.put("native_code", nativeCodeInfo);
        info.put(MlMetadata.UPGRADE_MODE.getPreferredName(), upgradeMode());
        listener.onResponse(new MlInfoAction.Response(info));
    }

    private Map<String, Object> defaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("anomaly_detectors", anomalyDetectorsDefaults());
        defaults.put("datafeeds", datafeedsDefaults());
        return defaults;
    }

    private boolean upgradeMode() {
        return MlMetadata.getMlMetadata(clusterService.state()).isUpgradeMode();
    }

    private Map<String, Object> anomalyDetectorsDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put(AnalysisLimits.MODEL_MEMORY_LIMIT.getPreferredName(), defaultModelMemoryLimit());
        defaults.put(AnalysisLimits.CATEGORIZATION_EXAMPLES_LIMIT.getPreferredName(), AnalysisLimits.DEFAULT_CATEGORIZATION_EXAMPLES_LIMIT);
        defaults.put(Job.MODEL_SNAPSHOT_RETENTION_DAYS.getPreferredName(), Job.DEFAULT_MODEL_SNAPSHOT_RETENTION_DAYS);
        try {
            defaults.put(CategorizationAnalyzerConfig.CATEGORIZATION_ANALYZER.getPreferredName(),
                CategorizationAnalyzerConfig.buildDefaultCategorizationAnalyzer(Collections.emptyList())
                    .asMap(xContentRegistry).get(CategorizationAnalyzerConfig.CATEGORIZATION_ANALYZER.getPreferredName()));
        } catch (IOException e) {
            logger.error("failed to convert default categorization analyzer to map", e);
        }
        return defaults;
    }

    private ByteSizeValue defaultModelMemoryLimit() {
        ByteSizeValue defaultLimit = new ByteSizeValue(AnalysisLimits.DEFAULT_MODEL_MEMORY_LIMIT_MB, ByteSizeUnit.MB);
        ByteSizeValue maxModelMemoryLimit = clusterService.getClusterSettings().get(MachineLearningField.MAX_MODEL_MEMORY_LIMIT);
        if (maxModelMemoryLimit != null && maxModelMemoryLimit.getBytes() > 0
                && maxModelMemoryLimit.getBytes() < defaultLimit.getBytes()) {
            return maxModelMemoryLimit;
        }
        return defaultLimit;
    }

    private Map<String, Object> datafeedsDefaults() {
        Map<String, Object> anomalyDetectorsDefaults = new HashMap<>();
        anomalyDetectorsDefaults.put(DatafeedConfig.SCROLL_SIZE.getPreferredName(), DatafeedConfig.DEFAULT_SCROLL_SIZE);
        return anomalyDetectorsDefaults;
    }

    private Map<String, Object> limits() {
        Map<String, Object> limits = new HashMap<>();
        ByteSizeValue maxModelMemoryLimit = clusterService.getClusterSettings().get(MachineLearningField.MAX_MODEL_MEMORY_LIMIT);
        if (maxModelMemoryLimit != null && maxModelMemoryLimit.getBytes() > 0) {
            limits.put("max_model_memory_limit", maxModelMemoryLimit);
        }
        return limits;
    }
}
