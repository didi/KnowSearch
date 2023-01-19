/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.history;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.xpack.watcher.test.AbstractWatcherIntegrationTestCase;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.mapper.MapperService.SINGLE_MAPPING_NAME;
import static org.elasticsearch.search.builder.SearchSourceBuilder.searchSource;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.xpack.watcher.actions.ActionBuilders.loggingAction;
import static org.elasticsearch.xpack.watcher.client.WatchSourceBuilders.watchBuilder;
import static org.elasticsearch.xpack.watcher.input.InputBuilders.simpleInput;
import static org.elasticsearch.xpack.watcher.test.WatcherTestUtils.templateRequest;
import static org.elasticsearch.xpack.watcher.transform.TransformBuilders.searchTransform;
import static org.elasticsearch.xpack.watcher.trigger.TriggerBuilders.schedule;
import static org.elasticsearch.xpack.watcher.trigger.schedule.Schedules.interval;
import static org.hamcrest.Matchers.hasItem;

public class HistoryTemplateTransformMappingsTests extends AbstractWatcherIntegrationTestCase {

    public void testTransformFields() throws Exception {
        assertAcked(client().admin().indices().prepareCreate("idx").addMapping("doc",
                jsonBuilder().startObject()
                        .startObject("properties")
                        .startObject("foo")
                        .field("type", "object")
                        .field("enabled", false)
                        .endObject()
                        .endObject()
                        .endObject()));

        client().prepareBulk().setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .add(client().prepareIndex().setIndex("idx").setId("1")
                        .setSource(jsonBuilder().startObject().field("name", "first").field("foo", "bar").endObject()))
                .add(client().prepareIndex().setIndex("idx").setId("2")
                        .setSource(jsonBuilder().startObject().field("name", "second")
                                .startObject("foo").field("what", "ever").endObject().endObject()))
                .get();

        watcherClient().preparePutWatch("_first").setSource(watchBuilder()
                .trigger(schedule(interval("5s")))
                .input(simpleInput())
                .transform(searchTransform(templateRequest(searchSource().query(QueryBuilders.termQuery("name", "first")), "idx")))
                .addAction("logger",
                        searchTransform(templateRequest(searchSource().query(QueryBuilders.termQuery("name", "first")), "idx")),
                        loggingAction("indexed")))
                .get();

        // execute another watch which with a transform that should conflict with the previous watch. Since the
        // mapping for the transform construct is disabled, there should be no problems.
        watcherClient().preparePutWatch("_second").setSource(watchBuilder()
                .trigger(schedule(interval("5s")))
                .input(simpleInput())
                .transform(searchTransform(templateRequest(searchSource().query(QueryBuilders.termQuery("name", "second")), "idx")))
                .addAction("logger",
                        searchTransform(templateRequest(searchSource().query(QueryBuilders.termQuery("name", "second")), "idx")),
                        loggingAction("indexed")))
                .get();

        watcherClient().prepareExecuteWatch("_first").setRecordExecution(true).get();
        watcherClient().prepareExecuteWatch("_second").setRecordExecution(true).get();

        assertBusy(() -> {
            GetFieldMappingsResponse response = client().admin().indices()
                                                                .prepareGetFieldMappings(".watcher-history*")
                                                                .setFields("result.actions.transform.payload")
                                                                .setTypes(SINGLE_MAPPING_NAME)
                                                                .includeDefaults(true)
                                                                .get();

            // time might have rolled over to a new day, thus we need to check that this field exists only in one of the history indices
            List<Boolean> payloadNulls = response.mappings().values().stream()
                    .map(map -> map.get(SINGLE_MAPPING_NAME))
                    .filter(Objects::nonNull)
                    .map(map -> map.get("result.actions.transform.payload"))
                    .filter(Objects::nonNull)
                    .map(GetFieldMappingsResponse.FieldMappingMetaData::isNull)
                    .collect(Collectors.toList());

            assertThat(payloadNulls, hasItem(true));
        });
    }
}
