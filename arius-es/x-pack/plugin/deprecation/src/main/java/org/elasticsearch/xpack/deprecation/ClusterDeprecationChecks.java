/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.deprecation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.mapper.FieldNamesFieldMapper;
import org.elasticsearch.ingest.IngestService;
import org.elasticsearch.ingest.PipelineConfiguration;
import org.elasticsearch.xpack.core.deprecation.DeprecationIssue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.elasticsearch.search.SearchModule.INDICES_MAX_CLAUSE_COUNT_SETTING;
import static org.elasticsearch.xpack.core.ilm.LifecycleSettings.LIFECYCLE_POLL_INTERVAL_SETTING;

public class ClusterDeprecationChecks {
    private static final Logger logger = LogManager.getLogger(ClusterDeprecationChecks.class);

    @SuppressWarnings("unchecked")
    static DeprecationIssue checkUserAgentPipelines(ClusterState state) {
        List<PipelineConfiguration> pipelines = IngestService.getPipelines(state);

        List<String> pipelinesWithDeprecatedEcsConfig = pipelines.stream()
            .filter(Objects::nonNull)
            .filter(pipeline -> {
                Map<String, Object> pipelineConfig = pipeline.getConfigAsMap();

                List<Map<String, Map<String, Object>>> processors =
                    (List<Map<String, Map<String, Object>>>) pipelineConfig.get("processors");
                return processors.stream()
                    .filter(Objects::nonNull)
                    .filter(processor -> processor.containsKey("user_agent"))
                    .map(processor -> processor.get("user_agent"))
                    .anyMatch(processorConfig -> processorConfig.containsKey("ecs"));
            })
            .map(PipelineConfiguration::getId)
            .sorted() // Make the warning consistent for testing purposes
            .collect(Collectors.toList());
        if (pipelinesWithDeprecatedEcsConfig.isEmpty() == false) {
            return new DeprecationIssue(DeprecationIssue.Level.WARNING,
                "User-Agent ingest plugin will always use ECS-formatted output",
                "https://www.elastic.co/guide/en/elasticsearch/reference/master/breaking-changes-8.0.html" +
                    "#ingest-user-agent-ecs-always",
                "Ingest pipelines " + pipelinesWithDeprecatedEcsConfig + " uses the [ecs] option which needs to be removed to work in 8.0");
        }
        return null;
    }

    static DeprecationIssue checkTemplatesWithTooManyFields(ClusterState state) {
        Integer maxClauseCount = INDICES_MAX_CLAUSE_COUNT_SETTING.get(state.getMetaData().settings());
        List<String> templatesOverLimit = new ArrayList<>();
        state.getMetaData().getTemplates().forEach((templateCursor) -> {
            AtomicInteger maxFields = new AtomicInteger(0);
            String templateName = templateCursor.key;
            boolean defaultFieldSet = templateCursor.value.settings().get(IndexSettings.DEFAULT_FIELD_SETTING.getKey()) != null;
            templateCursor.value.getMappings().forEach((mappingCursor) -> {
                MappingMetaData mappingMetaData = new MappingMetaData(mappingCursor.value);
                if (mappingMetaData != null && defaultFieldSet == false) {
                    maxFields.set(IndexDeprecationChecks.countFieldsRecursively(mappingMetaData.type(), mappingMetaData.sourceAsMap()));
                }
                if (maxFields.get() > maxClauseCount) {
                    templatesOverLimit.add(templateName);
                }
            });
        });

        if (templatesOverLimit.isEmpty() == false) {
            return new DeprecationIssue(DeprecationIssue.Level.WARNING,
                "Fields in index template exceed automatic field expansion limit",
                "https://www.elastic.co/guide/en/elasticsearch/reference/7.0/breaking-changes-7.0.html" +
                    "#_limiting_the_number_of_auto_expanded_fields",
                "Index templates " + templatesOverLimit + " have a number of fields which exceeds the automatic field expansion " +
                    "limit of [" + maxClauseCount + "] and does not have [" + IndexSettings.DEFAULT_FIELD_SETTING.getKey() + "] set, " +
                    "which may cause queries which use automatic field expansion, such as query_string, simple_query_string, and " +
                    "multi_match to fail if fields are not explicitly specified in the query.");
        }
        return null;
    }

    /**
     * Check templates that use `_field_names` explicitly, which was deprecated in https://github.com/elastic/elasticsearch/pull/42854
     * and will throw an error on new indices in 8.0
     */
    @SuppressWarnings("unchecked")
    static DeprecationIssue checkTemplatesWithFieldNamesDisabled(ClusterState state) {
        Set<String> templatesContainingFieldNames = new HashSet<>();
        state.getMetaData().getTemplates().forEach((templateCursor) -> {
            String templateName = templateCursor.key;
            templateCursor.value.getMappings().forEach((mappingCursor) -> {
                String type = mappingCursor.key;
                // there should be the type name at this level, but there was a bug where mappings could be stored without a type (#45120)
                // to make sure, we try to detect this like we try to do in MappingMetaData#sourceAsMap()
                Map<String, Object> mapping = XContentHelper.convertToMap(mappingCursor.value.compressedReference(), true).v2();
                if (mapping.size() == 1 && mapping.containsKey(type)) {
                    // the type name is the root value, reduce it
                    mapping = (Map<String, Object>) mapping.get(type);
                }
                if (mapContainsFieldNamesDisabled(mapping)) {
                    templatesContainingFieldNames.add(templateName);
                }
            });
        });

        if (templatesContainingFieldNames.isEmpty() == false) {
            return new DeprecationIssue(DeprecationIssue.Level.WARNING, "Index templates contain _field_names settings.",
                    "https://www.elastic.co/guide/en/elasticsearch/reference/master/breaking-changes-8.0.html#fieldnames-enabling",
                    "Index templates " + templatesContainingFieldNames + " use the deprecated `enable` setting for the `"
                            + FieldNamesFieldMapper.NAME + "` field. Using this setting in new index mappings will throw an error "
                                    + "in the next major version and needs to be removed from existing mappings and templates.");
        }
        return null;
    }

    /**
     * check for "_field_names" entries in the map that contain another property "enabled" in the sub-map
     */
    static boolean mapContainsFieldNamesDisabled(Map<?, ?> map) {
        Object fieldNamesMapping = map.get(FieldNamesFieldMapper.NAME);
        if (fieldNamesMapping != null) {
            if (((Map<?, ?>) fieldNamesMapping).keySet().contains("enabled")) {
                return true;
            }
        }
        return false;
    }

    static DeprecationIssue checkPollIntervalTooLow(ClusterState state) {
        String pollIntervalString = state.metaData().settings().get(LIFECYCLE_POLL_INTERVAL_SETTING.getKey());
        if (Strings.isNullOrEmpty(pollIntervalString)) {
            return null;
        }

        TimeValue pollInterval;
        try {
            pollInterval = TimeValue.parseTimeValue(pollIntervalString, LIFECYCLE_POLL_INTERVAL_SETTING.getKey());
        } catch (IllegalArgumentException e) {
            logger.error("Failed to parse [{}] value: [{}]", LIFECYCLE_POLL_INTERVAL_SETTING.getKey(), pollIntervalString);
            return null;
        }

        if (pollInterval.compareTo(TimeValue.timeValueSeconds(1)) < 0) {
            return new DeprecationIssue(DeprecationIssue.Level.CRITICAL,
                "Index Lifecycle Management poll interval is set too low",
                "https://www.elastic.co/guide/en/elasticsearch/reference/master/breaking-changes-8.0.html" +
                    "#ilm-poll-interval-limit",
                "The Index Lifecycle Management poll interval setting [" + LIFECYCLE_POLL_INTERVAL_SETTING.getKey() + "] is " +
                    "currently set to [" + pollIntervalString + "], but must be 1s or greater");
        }
        return null;
    }
}
